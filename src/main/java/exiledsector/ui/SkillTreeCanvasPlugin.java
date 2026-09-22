package exiledsector.ui;

import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import exiledsector.effects.SkillTreeHullMod;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillType;
import exiledsector.ui.decoration.SkillTreeRingBeltRenderer;
import exiledsector.ui.decoration.SkillTreeStarRenderer;
import exiledsector.ui.decoration.SkillTreeStarfieldRenderer;
import exiledsector.ui.decoration.SkillTreeStaticImageRenderer;
import exiledsector.ui.node.RootCrestResolver;
import exiledsector.ui.node.SkillTreeNodeRenderer;
import exiledsector.ui.util.BorderedPanel;
import lunalib.lunaRefit.BaseRefitButton;

import java.util.List;

public class SkillTreeCanvasPlugin extends BaseCustomUIPanelPlugin {

    private static final float MIN_ZOOM = 0.4f;
    private static final float MAX_ZOOM = 2.5f;
    private static final float ZOOM_STEP = 1.1f;
    private static final float SHIP_CARD_FRAME_OUTSET = 8f;

    private final SkillTreePanelStyle style;
    private final SkillTreeStarfieldRenderer starfieldRenderer;
    private final SkillTreeStaticImageRenderer staticImageRenderer;
    private final SkillTreeRingBeltRenderer ringBeltRenderer;
    private final SkillTreeStarRenderer starRenderer;
    private final SkillTreeNodeRenderer nodeRenderer;
    private final SkillTreeStatPanel statPanel;
    private final SkillTreeOrdnancePointsBar ordnancePointsBar;
    private final SkillTreeLevelBar levelBar;
    private final BorderedPanel shipCardPanel = new BorderedPanel(SkillTreeCanvasPlugin.class);
    private final float shipCardHeight;
    private final FleetMemberAPI member;
    private final ShipVariantAPI variant;

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

    public SkillTreeCanvasPlugin(FleetMemberAPI member, ShipVariantAPI variant, float shipCardHeight, BaseRefitButton refitButton) {
        this.member = member;
        this.variant = variant;
        this.style = new SkillTreePanelStyle(RootCrestResolver.resolve(member));
        this.starfieldRenderer = new SkillTreeStarfieldRenderer(style);
        this.staticImageRenderer = new SkillTreeStaticImageRenderer();
        this.ringBeltRenderer = new SkillTreeRingBeltRenderer();
        this.starRenderer = new SkillTreeStarRenderer();
        this.nodeRenderer = new SkillTreeNodeRenderer(member, variant, style, refitButton);
        this.statPanel = new SkillTreeStatPanel(member, variant);
        this.ordnancePointsBar = new SkillTreeOrdnancePointsBar(member, variant);
        this.levelBar = new SkillTreeLevelBar(member);
        this.shipCardHeight = shipCardHeight;
    }

    @Override
    public void positionChanged(PositionAPI position) {
        this.position = position;
    }

    @Override
    public void advance(float amount) {
        starfieldRenderer.advance(amount);
        staticImageRenderer.advance(amount);
        ringBeltRenderer.advance(amount);
        starRenderer.advance(amount);
        nodeRenderer.advance(amount);
        ordnancePointsBar.advance(amount, position, mouseX, mouseY, mouseKnown);
        levelBar.advance(amount, position, mouseX, mouseY, mouseKnown);
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        if (position == null) return;

        for (InputEventAPI event : events) {
            if (event.isConsumed()) continue;

            if (event.isLMBDownEvent() && position.containsEvent(event)) {
                if (nodeRenderer.isDropdownOpen()) {
                    SkillType option = nodeRenderer.findDropdownOptionAt(centerX(), centerY(), zoom, event.getX(), event.getY());
                    if (option != null) {
                        pendingDropdownOption = option;
                    } else {
                        nodeRenderer.closeDropdown();
                    }
                } else {
                    SkillNode clicked = nodeRenderer.findNodeAt(centerX(), centerY(), zoom, event.getX(), event.getY());
                    if (clicked != null) {
                        pendingClickNode = clicked;
                        pendingClickCtrlDown = event.isCtrlDown();
                    } else {
                        dragging = true;
                    }
                }
                event.consume();
            } else if (event.isLMBUpEvent()) {
                dragging = false;
                if (pendingDropdownOption != null) {
                    nodeRenderer.commitDropdownSelection(pendingDropdownOption);
                    pendingDropdownOption = null;
                } else if (pendingClickNode != null) {
                    nodeRenderer.toggleAllocation(pendingClickNode, pendingClickCtrlDown);
                    pendingClickNode = null;
                }
            } else if (event.isMouseMoveEvent()) {
                mouseX = event.getX();
                mouseY = event.getY();
                mouseKnown = true;
                if (dragging) {
                    panX += event.getDX();
                    panY += event.getDY();
                    event.consume();
                }
            } else if (event.isMouseScrollEvent() && position.containsEvent(event)) {
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
        }
    }

    @Override
    public void render(float alphaMult) {
        if (position == null) return;

        SkillTreeHullMod.syncOpSpentHullMod(member, variant);

        float centerX = centerX();
        float centerY = centerY();

        starfieldRenderer.render(position, panX, panY, alphaMult);
        starRenderer.renderDisc(centerX, centerY, zoom, alphaMult, position);
        ringBeltRenderer.render(centerX, centerY, zoom, alphaMult, position);
        staticImageRenderer.render(centerX, centerY, zoom, alphaMult, position);
        nodeRenderer.render(centerX, centerY, zoom, alphaMult, mouseX, mouseY, mouseKnown);
        starRenderer.renderGlow(centerX, centerY, zoom, alphaMult, position);
        statPanel.render(position, alphaMult);
        ordnancePointsBar.render(position, alphaMult);
        levelBar.render(position, alphaMult);
        drawShipCardFrame(alphaMult);

        if (!dragging && mouseKnown) {
            nodeRenderer.renderHoverTooltip(centerX, centerY, zoom, mouseX, mouseY, alphaMult);
        }
    }

    private void drawShipCardFrame(float alphaMult) {
        float boxX = position.getX() + SkillTreeRefitButton.SHIP_CARD_MARGIN - SHIP_CARD_FRAME_OUTSET;
        float boxY = position.getY() + SkillTreeRefitButton.SHIP_CARD_MARGIN - SHIP_CARD_FRAME_OUTSET;
        float boxWidth = SkillTreeRefitButton.SHIP_CARD_ICON_SIZE + SHIP_CARD_FRAME_OUTSET * 2f;
        float boxHeight = shipCardHeight + SHIP_CARD_FRAME_OUTSET * 2f;
        shipCardPanel.draw(boxX, boxY, boxWidth, boxHeight, alphaMult);
    }

    private float centerX() {
        return position.getX() + position.getWidth() / 2f + panX;
    }

    private float centerY() {
        return position.getY() + position.getHeight() / 2f + panY;
    }
}
