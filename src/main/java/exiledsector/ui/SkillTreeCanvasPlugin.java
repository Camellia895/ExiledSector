package exiledsector.ui;

import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import exiledsector.effects.SkillTreeHullMod;
import exiledsector.i18n.Translation;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillType;
import exiledsector.skills.progression.ShipLevelConfig;
import exiledsector.skills.progression.SkillNodeOpCost;
import exiledsector.ui.decoration.SkillTreeRingBeltRenderer;
import exiledsector.ui.decoration.SkillTreeStarRenderer;
import exiledsector.ui.decoration.SkillTreeStarfieldRenderer;
import exiledsector.ui.decoration.SkillTreeStaticImageRenderer;
import exiledsector.ui.node.NodeSearch;
import exiledsector.ui.node.SkillTreeNodeRenderer;
import exiledsector.ui.util.BorderedPanel;
import lunalib.lunaRefit.BaseRefitButton;

import java.util.List;

public class SkillTreeCanvasPlugin extends BaseCustomUIPanelPlugin {

    private static final float MIN_ZOOM = 0.2f;
    private static final float MAX_ZOOM = 2.5f;
    private static final float ZOOM_STEP = 1.1f;
    private static final float SHIP_CARD_FRAME_OUTSET = 8f;


    private final String readoutTooltipBody;

    private final SkillTreeStarfieldRenderer starfieldRenderer;
    private final SkillTreeStaticImageRenderer staticImageRenderer;
    private final SkillTreeRingBeltRenderer ringBeltRenderer;
    private final SkillTreeStarRenderer starRenderer;
    private final SkillTreeNodeRenderer nodeRenderer;
    private final SkillTreeStatPanel statPanel;
    private final SkillTreeOrdnancePointsBar ordnancePointsBar;
    private final SkillTreeLevelBar levelBar;
    private final SkillTreeInfoTooltipRenderer readoutTooltipRenderer;
    private final NodeSearch search = new NodeSearch();
    private final SkillTreeSearchBar searchBar;
    private final BorderedPanel shipCardPanel = new BorderedPanel(SkillTreeCanvasPlugin.class);
    private final float shipCardHeight;

    private PositionAPI position;
    private boolean dragging = false;
    private float panX = 0f;
    private float panY = 0f;
    private float zoom = 1f;
    private float mouseX = 0f;
    private float mouseY = 0f;
    private boolean mouseKnown = false;
    private SkillNode pendingClickNode;
    private boolean pendingClickCtrlDown;
    private SkillType pendingDropdownOption;
    private CameraPanAnimation cameraPan;

    public SkillTreeCanvasPlugin(FleetMemberAPI member, ShipVariantAPI variant, float shipCardHeight, BaseRefitButton refitButton) {
        SkillTreeHullMod.syncOpSpentHullMod(member, variant);
        SkillTreePanelStyle style = new SkillTreePanelStyle();
        this.starfieldRenderer = new SkillTreeStarfieldRenderer(style);
        this.staticImageRenderer = new SkillTreeStaticImageRenderer();
        this.ringBeltRenderer = new SkillTreeRingBeltRenderer();
        this.starRenderer = new SkillTreeStarRenderer();
        this.nodeRenderer = new SkillTreeNodeRenderer(member, variant, style, refitButton, search);
        this.searchBar = new SkillTreeSearchBar(search);
        this.statPanel = new SkillTreeStatPanel(member, variant);
        this.ordnancePointsBar = new SkillTreeOrdnancePointsBar(member, variant);
        this.levelBar = new SkillTreeLevelBar(member);
        this.readoutTooltipRenderer = new SkillTreeInfoTooltipRenderer(style);
        this.readoutTooltipBody = buildReadoutTooltipBody(member);
        this.shipCardHeight = shipCardHeight;

        SkillNode startingRoot = nodeRenderer.getStartingRoot();
        if (startingRoot != null) {
            this.panX = -startingRoot.getOffsetX();
            this.panY = startingRoot.getOffsetY();
        }
    }

    @Override
    public void positionChanged(PositionAPI position) {
        this.position = position;
    }

    @Override
    public void advance(float amount) {
        advanceCameraPan(amount);
        searchBar.advance(amount);
        starfieldRenderer.advance(amount);
        staticImageRenderer.advance(amount);
        ringBeltRenderer.advance(amount);
        starRenderer.advance(amount);
        boolean followingStartingRoot = nodeRenderer.isStartingRootFlying();
        nodeRenderer.advance(amount);
        if (followingStartingRoot) {
            panX = -nodeRenderer.startingRootOffsetX() * zoom;
            panY = nodeRenderer.startingRootOffsetY() * zoom;
        }
        ordnancePointsBar.advance(amount, position, mouseX, mouseY, mouseKnown);
        levelBar.advance(amount, position, mouseX, mouseY, mouseKnown);
    }

    private void advanceCameraPan(float amount) {
        if (cameraPan == null) {
            return;
        }
        cameraPan.advance(amount);
        panX = cameraPan.x() * zoom;
        panY = cameraPan.y() * zoom;
        if (cameraPan.isFinished()) {
            cameraPan = null;
        }
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        if (position == null) return;

        for (InputEventAPI event : events) {
            if (!event.isConsumed()) {
                handleEvent(event);
            }
        }
    }

    private void handleEvent(InputEventAPI event) {
        if (nodeRenderer.isStartingRootInputLocked()) {
            handleStartingRootEvent(event);
        } else if (event.isLMBDownEvent() && position.containsEvent(event)) {
            handleLmbDown(event);
        } else if (event.isLMBUpEvent()) {
            handleLmbUp(event);
        } else if (event.isMouseMoveEvent()) {
            handleMouseMove(event);
        } else if (event.isMouseScrollEvent() && position.containsEvent(event)) {
            handleMouseScroll(event);
        } else if (event.isKeyboardEvent() && searchBar.handleKey(event)) {
            event.consume();
        }
    }

    private void handleStartingRootEvent(InputEventAPI event) {
        if (event.isMouseMoveEvent()) {
            handleMouseMove(event);
        } else if (event.isLMBDownEvent() && position.containsEvent(event)) {
            pendingClickNode = isOverOverlay(event.getX(), event.getY())
                    ? null : nodeRenderer.findNodeAt(viewport(), event.getX(), event.getY());
            event.consume();
        } else if (event.isLMBUpEvent() && pendingClickNode != null) {
            nodeRenderer.chooseStartingRoot(pendingClickNode);
            pendingClickNode = null;
            event.consume();
        } else if (event.isMouseScrollEvent() && position.containsEvent(event)) {
            event.consume();
        }
    }

    private void handleLmbDown(InputEventAPI event) {
        if (searchBar.handleClick(position, event.getX(), event.getY())) {
            event.consume();
            return;
        }
        if (statPanel.isCollapseButtonHit(position, event.getX(), event.getY())) {
            statPanel.toggleCollapsed();
            event.consume();
            return;
        }
        if (isOverOverlay(event.getX(), event.getY())) {
            event.consume();
            return;
        }
        if (nodeRenderer.isDropdownOpen()) {
            SkillType option = nodeRenderer.findDropdownOptionAt(viewport(), event.getX(), event.getY());
            if (option != null) {
                pendingDropdownOption = option;
            } else {
                nodeRenderer.closeDropdown();
            }
        } else {
            SkillNode clicked = nodeRenderer.findNodeAt(viewport(), event.getX(), event.getY());
            if (clicked != null) {
                pendingClickNode = clicked;
                pendingClickCtrlDown = event.isCtrlDown();
            } else {
                dragging = true;
                cameraPan = null;
            }
        }
        event.consume();
    }

    private void handleLmbUp(InputEventAPI event) {
        boolean wasOurGesture = dragging || pendingDropdownOption != null || pendingClickNode != null;
        dragging = false;
        if (pendingDropdownOption != null) {
            nodeRenderer.commitDropdownSelection(pendingDropdownOption);
            pendingDropdownOption = null;
        } else if (pendingClickNode != null) {
            SkillNode jumpTarget = nodeRenderer.wormholeJumpTarget(pendingClickNode, pendingClickCtrlDown);
            if (jumpTarget != null) {
                cameraPan = new CameraPanAnimation(panX / zoom, panY / zoom,
                        -jumpTarget.getOffsetX(), jumpTarget.getOffsetY());
                nodeRenderer.launchWormholeGhosts(pendingClickNode, jumpTarget);
            } else {
                nodeRenderer.toggleAllocation(pendingClickNode, pendingClickCtrlDown);
            }
            pendingClickNode = null;
        }
        if (wasOurGesture) {
            event.consume();
        }
    }

    private void handleMouseMove(InputEventAPI event) {
        mouseX = event.getX();
        mouseY = event.getY();
        mouseKnown = true;
        if (dragging) {
            panX += event.getDX();
            panY += event.getDY();
            event.consume();
        }
    }

    private void handleMouseScroll(InputEventAPI event) {
        float oldZoom = zoom;
        if (event.getEventValue() > 0) {
            zoom = Math.min(MAX_ZOOM, zoom * ZOOM_STEP);
        } else {
            zoom = Math.max(MIN_ZOOM, zoom / ZOOM_STEP);
        }
        float zoomRatio = zoom / oldZoom;
        panX *= zoomRatio;
        panY *= zoomRatio;
        event.consume();
    }

    @Override
    public void render(float alphaMult) {
        if (position == null) return;

        TreeViewport viewport = viewport();

        float backgroundAlpha = alphaMult * search.backgroundAlpha() * nodeRenderer.treeAlpha();
        starfieldRenderer.render(position, panX, panY, backgroundAlpha);
        starRenderer.renderDisc(viewport, backgroundAlpha);
        starRenderer.renderAtmosphere(viewport, backgroundAlpha);
        starRenderer.renderAurora(viewport, backgroundAlpha);
        ringBeltRenderer.render(viewport, backgroundAlpha);
        staticImageRenderer.render(viewport, backgroundAlpha);
        boolean treeHovered = mouseKnown && !isOverOverlay(mouseX, mouseY);
        nodeRenderer.render(viewport, alphaMult, mouseX, mouseY, treeHovered);
        starRenderer.renderGlow(viewport, backgroundAlpha);
        statPanel.render(position, alphaMult);
        ordnancePointsBar.render(position, alphaMult);
        levelBar.render(position, alphaMult);
        if (!nodeRenderer.isStartingRootInputLocked()) {
            searchBar.render(position, alphaMult);
        }
        drawShipCardFrame(alphaMult);

        if (!dragging && mouseKnown) {
            if (treeHovered) {
                nodeRenderer.renderHoverTooltip(viewport, mouseX, mouseY, alphaMult);
            }
            if (ordnancePointsBar.isHovered(position, mouseX, mouseY) || levelBar.isHovered(position, mouseX, mouseY)) {
                readoutTooltipRenderer.render(Translation.text("ui.readout.title"), readoutTooltipBody, mouseX, mouseY, alphaMult);
            }
        }
    }

    private static String buildReadoutTooltipBody(FleetMemberAPI member) {
        int opCost = SkillNodeOpCost.perNode(member.getHullSpec());
        int maxNodes = ShipLevelConfig.maxAllocatedNodes();
        return Translation.msg("ui.readout.body").arg("opCost", opCost).arg("maxNodes", maxNodes).text();
    }

    private void drawShipCardFrame(float alphaMult) {
        ScreenRect frame = shipCardFrame();
        shipCardPanel.draw(frame.left(), frame.bottom(), frame.width(), frame.height(), alphaMult);
    }

    private ScreenRect shipCardFrame() {
        return new ScreenRect(position.getX() + SkillTreeRefitButton.SHIP_CARD_MARGIN - SHIP_CARD_FRAME_OUTSET,
                position.getY() + SkillTreeRefitButton.SHIP_CARD_MARGIN - SHIP_CARD_FRAME_OUTSET,
                SkillTreeRefitButton.SHIP_CARD_ICON_SIZE + SHIP_CARD_FRAME_OUTSET * 2f,
                shipCardHeight + SHIP_CARD_FRAME_OUTSET * 2f);
    }

    private boolean isOverOverlay(float x, float y) {
        return statPanel.contains(x, y)
                || ordnancePointsBar.isHovered(position, x, y)
                || levelBar.isHovered(position, x, y)
                || (!nodeRenderer.isStartingRootInputLocked() && SkillTreeSearchBar.contains(position, x, y))
                || shipCardFrame().contains(x, y);
    }

    private TreeViewport viewport() {
        return new TreeViewport(position.getX() + position.getWidth() / 2f + panX,
                position.getY() + position.getHeight() / 2f + panY, zoom);
    }
}
