package exiledsector.ui;

import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillType;

import java.util.List;

public class SkillTreeCanvasPlugin extends BaseCustomUIPanelPlugin {

    private static final float MIN_ZOOM = 0.4f;
    private static final float MAX_ZOOM = 2.5f;
    private static final float ZOOM_STEP = 1.1f;

    private final SkillTreePanelStyle style;
    private final SkillTreeNodeRenderer nodeRenderer;
    private final SkillTreeStatPanel statPanel;
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
    private SkillType pendingDropdownOption;

    public SkillTreeCanvasPlugin(String symbolPath, FleetMemberAPI member, float shipCardHeight) {
        this.style = new SkillTreePanelStyle(symbolPath);
        this.nodeRenderer = new SkillTreeNodeRenderer(symbolPath, member, style);
        this.statPanel = new SkillTreeStatPanel(member, style);
        this.shipCardHeight = shipCardHeight;
    }

    @Override
    public void positionChanged(PositionAPI position) {
        this.position = position;
    }

    @Override
    public void advance(float amount) {
        nodeRenderer.advance(amount);
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        if (position == null) return;

        for (InputEventAPI event : events) {
            if (event.isConsumed()) continue;

            if (event.isLMBDownEvent() && position.containsEvent(event)) {
                if (!statPanel.handleClick(position, event.getX(), event.getY())) {
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
                        } else {
                            dragging = true;
                        }
                    }
                }
                event.consume();
            } else if (event.isLMBUpEvent()) {
                dragging = false;
                if (pendingDropdownOption != null) {
                    nodeRenderer.commitDropdownSelection(pendingDropdownOption);
                    pendingDropdownOption = null;
                } else if (pendingClickNode != null) {
                    nodeRenderer.toggleAllocation(pendingClickNode);
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
                if (event.getEventValue() > 0) {
                    zoom = Math.min(MAX_ZOOM, zoom * ZOOM_STEP);
                } else {
                    zoom = Math.max(MIN_ZOOM, zoom / ZOOM_STEP);
                }
                event.consume();
            }
        }
    }

    @Override
    public void render(float alphaMult) {
        if (position == null) return;

        float centerX = centerX();
        float centerY = centerY();

        nodeRenderer.render(centerX, centerY, zoom, alphaMult, mouseX, mouseY, mouseKnown);
        statPanel.render(position, alphaMult);
        drawShipCardFrame(alphaMult);

        if (!dragging && mouseKnown) {
            nodeRenderer.renderHoverTooltip(centerX, centerY, zoom, mouseX, mouseY, alphaMult);
        }
    }

    private void drawShipCardFrame(float alphaMult) {
        float boxX = position.getX() + SkillTreeRefitButton.SHIP_CARD_MARGIN;
        float boxY = position.getY() + position.getHeight() - SkillTreeRefitButton.SHIP_CARD_MARGIN - shipCardHeight;
        style.drawTooltipBackground(boxX, boxY, SkillTreeRefitButton.SHIP_CARD_ICON_SIZE, shipCardHeight, alphaMult, style.getAccentColor());
    }

    private float centerX() {
        return position.getX() + position.getWidth() / 2f + panX;
    }

    private float centerY() {
        return position.getY() + position.getHeight() / 2f + panY;
    }
}
