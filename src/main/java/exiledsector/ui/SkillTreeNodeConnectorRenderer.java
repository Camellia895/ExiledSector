package exiledsector.ui;

import com.fs.starfarer.api.util.Misc;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillTree;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

import static exiledsector.ui.SkillTreeNodeGeometry.NODE_CONNECTOR_GLOW_HALO_ALPHA;
import static exiledsector.ui.SkillTreeNodeGeometry.NODE_CONNECTOR_GLOW_HALO_THICKNESS;
import static exiledsector.ui.SkillTreeNodeGeometry.NODE_CONNECTOR_GLOW_LINE_THICKNESS;
import static exiledsector.ui.SkillTreeNodeGeometry.NODE_CONNECTOR_LINE_THICKNESS;
import static exiledsector.ui.SkillTreeNodeGeometry.NODE_CONNECTOR_PARALLEL_GAP;
import static exiledsector.ui.SkillTreeNodeGeometry.NODE_SIZE;
import static exiledsector.ui.SkillTreeNodeGeometry.RING_DULL_ALPHA;
import static exiledsector.ui.SkillTreeNodeGeometry.RING_DULL_COLOR;
import static exiledsector.ui.SkillTreeNodeGeometry.connectorEndpointRadius;

final class SkillTreeNodeConnectorRenderer {

    private static final int CURVE_ARC_SAMPLES = 40;
    private static final int CURVE_RENDER_SEGMENTS = 20;

    private final SkillTreePanelStyle style;

    SkillTreeNodeConnectorRenderer(SkillTreePanelStyle style) {
        this.style = style;
    }

    void draw(float centerX, float centerY, float zoom, ShipSkillData data, String satisfiedRootId, float alphaMult) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for (SkillNode node : SkillTree.getAllNodes().values()) {
            if (node.getType().getTier() == SkillTier.ROOT) continue;

            float nodeX = centerX + node.getOffsetX() * zoom;
            float nodeY = centerY - node.getOffsetY() * zoom;
            float nodeRadius = connectorEndpointRadius(node.getType().getTier(), NODE_SIZE * zoom * node.getType().getTier().getSizeMultiplier(), zoom);

            for (String connectedId : node.getConnectedNodeIds()) {
                SkillNode other = SkillTree.get(connectedId);
                if (other == null) continue;
                if (other.getType().getTier() != SkillTier.ROOT && node.getId().compareTo(other.getId()) >= 0) continue;

                float otherX = centerX + other.getOffsetX() * zoom;
                float otherY = centerY - other.getOffsetY() * zoom;
                float otherRadius = connectorEndpointRadius(other.getType().getTier(), NODE_SIZE * zoom * other.getType().getTier().getSizeMultiplier(), zoom);
                boolean bothSatisfied = data.isSatisfied(node.getId(), satisfiedRootId) && data.isSatisfied(other.getId(), satisfiedRootId);

                ConnectorCurve curve = SkillTree.getCurve(node.getId(), other.getId());
                if (curve == null) {
                    drawStraightNodeConnectorLine(otherX, otherY, otherRadius, nodeX, nodeY, nodeRadius, bothSatisfied, alphaMult);
                } else {
                    float throughX = centerX + curve.getControlOffsetX() * zoom;
                    float throughY = centerY - curve.getControlOffsetY() * zoom;
                    drawCurvedNodeConnectorLine(otherX, otherY, otherRadius, throughX, throughY, nodeX, nodeY, nodeRadius, bothSatisfied, alphaMult);
                }
            }
        }

        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawStraightNodeConnectorLine(float x1, float y1, float r1, float x2, float y2, float r2, boolean glowing, float alphaMult) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length <= r1 + r2) return;

        float dirX = dx / length;
        float dirY = dy / length;
        float startX = x1 + dirX * r1;
        float startY = y1 + dirY * r1;
        float endX = x2 - dirX * r2;
        float endY = y2 - dirY * r2;

        drawConnectorSegment(startX, startY, endX, endY, glowing, alphaMult);
    }

    private void drawCurvedNodeConnectorLine(float x0, float y0, float r1, float throughX, float throughY, float x2, float y2, float r2, boolean glowing, float alphaMult) {
        float cx = 2f * throughX - (x0 + x2) / 2f;
        float cy = 2f * throughY - (y0 + y2) / 2f;
        float[] xs = new float[CURVE_ARC_SAMPLES + 1];
        float[] ys = new float[CURVE_ARC_SAMPLES + 1];
        float[] cumLen = new float[CURVE_ARC_SAMPLES + 1];
        for (int i = 0; i <= CURVE_ARC_SAMPLES; i++) {
            float t = (float) i / CURVE_ARC_SAMPLES;
            float omt = 1f - t;
            xs[i] = omt * omt * x0 + 2f * omt * t * cx + t * t * x2;
            ys[i] = omt * omt * y0 + 2f * omt * t * cy + t * t * y2;
            if (i > 0) {
                float dx = xs[i] - xs[i - 1];
                float dy = ys[i] - ys[i - 1];
                cumLen[i] = cumLen[i - 1] + (float) Math.sqrt(dx * dx + dy * dy);
            }
        }
        float totalLength = cumLen[CURVE_ARC_SAMPLES];
        if (totalLength <= r1 + r2) return;

        float tStart = curveParamAtArcLength(cumLen, r1);
        float tEnd = curveParamAtArcLength(cumLen, totalLength - r2);
        if (tEnd <= tStart) return;

        float prevX = 0, prevY = 0;
        for (int i = 0; i <= CURVE_RENDER_SEGMENTS; i++) {
            float t = tStart + (tEnd - tStart) * i / CURVE_RENDER_SEGMENTS;
            float omt = 1f - t;
            float x = omt * omt * x0 + 2f * omt * t * cx + t * t * x2;
            float y = omt * omt * y0 + 2f * omt * t * cy + t * t * y2;
            if (i > 0) {
                drawConnectorSegment(prevX, prevY, x, y, glowing, alphaMult);
            }
            prevX = x;
            prevY = y;
        }
    }

    private float curveParamAtArcLength(float[] cumLen, float targetLength) {
        if (targetLength <= 0f) return 0f;
        if (targetLength >= cumLen[CURVE_ARC_SAMPLES]) return 1f;
        for (int i = 1; i <= CURVE_ARC_SAMPLES; i++) {
            if (cumLen[i] >= targetLength) {
                float segLength = cumLen[i] - cumLen[i - 1];
                float frac = segLength <= 0f ? 0f : (targetLength - cumLen[i - 1]) / segLength;
                return ((i - 1) + frac) / CURVE_ARC_SAMPLES;
            }
        }
        return 1f;
    }

    private void drawConnectorSegment(float x1, float y1, float x2, float y2, boolean glowing, float alphaMult) {
        if (glowing) {
            drawLine(x1, y1, x2, y2, style.getAccentColor(), alphaMult * NODE_CONNECTOR_GLOW_HALO_ALPHA, NODE_CONNECTOR_GLOW_HALO_THICKNESS);
            drawLine(x1, y1, x2, y2, style.getAccentColor(), alphaMult, NODE_CONNECTOR_GLOW_LINE_THICKNESS);
            return;
        }

        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length <= 0.0001f) return;
        float dirX = dx / length;
        float dirY = dy / length;
        float perpX = -dirY * (NODE_CONNECTOR_PARALLEL_GAP / 2f);
        float perpY = dirX * (NODE_CONNECTOR_PARALLEL_GAP / 2f);

        drawLine(x1 + perpX, y1 + perpY, x2 + perpX, y2 + perpY, RING_DULL_COLOR, alphaMult * RING_DULL_ALPHA, NODE_CONNECTOR_LINE_THICKNESS);
        drawLine(x1 - perpX, y1 - perpY, x2 - perpX, y2 - perpY, RING_DULL_COLOR, alphaMult * RING_DULL_ALPHA, NODE_CONNECTOR_LINE_THICKNESS);
    }

    private void drawLine(float x1, float y1, float x2, float y2, Color color, float alpha, float thickness) {
        Misc.setColor(color, alpha);
        GL11.glLineWidth(thickness);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x2, y2);
        GL11.glEnd();
    }
}
