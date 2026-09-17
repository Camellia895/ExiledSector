package exiledsector.ui;

import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import exiledsector.skills.SkillNode;

import java.util.List;

public class SkillTreeCanvasPlugin extends BaseCustomUIPanelPlugin {

    private static final float MIN_ZOOM = 0.4f;
    private static final float MAX_ZOOM = 2.5f;
    private static final float ZOOM_STEP = 1.1f;

    private final SkillTreeNodeRenderer nodeRenderer;
    private final SkillTreeStatPanel statPanel;

    private PositionAPI position;
    private boolean dragging = false;
    private float panX = 0f;
    private float panY = 0f;
    private float zoom = 1f;
    private float mouseX = 0f;
    private float mouseY = 0f;
    private boolean mouseKnown = false;
    private SkillNode pendingClickNode;

    public SkillTreeCanvasPlugin(String symbolPath, FleetMemberAPI member) {
        SkillTreePanelStyle style = new SkillTreePanelStyle();
        this.nodeRenderer = new SkillTreeNodeRenderer(symbolPath, member, style);
        this.statPanel = new SkillTreeStatPanel(member, style);
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
                    SkillNode clicked = nodeRenderer.findNodeAt(centerX(), centerY(), zoom, event.getX(), event.getY());
                    if (clicked != null) {
                        pendingClickNode = clicked;
                    } else {
                        dragging = true;
                    }
                }
                event.consume();
            } else if (event.isLMBUpEvent()) {
                dragging = false;
                if (pendingClickNode != null) {
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

        nodeRenderer.render(centerX, centerY, zoom, alphaMult);
        statPanel.render(position, alphaMult);

        if (!dragging && mouseKnown) {
            nodeRenderer.renderHoverTooltip(centerX, centerY, zoom, mouseX, mouseY, alphaMult);
        }
    }

    private float centerX() {
        return position.getX() + position.getWidth() / 2f + panX;
    }

    private float centerY() {
        return position.getY() + position.getHeight() / 2f + panY;
    }
}
