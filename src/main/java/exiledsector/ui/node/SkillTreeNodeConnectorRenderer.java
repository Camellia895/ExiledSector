package exiledsector.ui.node;

import com.fs.starfarer.api.util.Misc;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillTypeUnlockStatus;
import exiledsector.ui.SkillTreePanelStyle;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static exiledsector.ui.node.SkillTreeNodeGeometry.NODE_CONNECTOR_GLOW_HALO_ALPHA;
import static exiledsector.ui.node.SkillTreeNodeGeometry.NODE_CONNECTOR_GLOW_HALO_THICKNESS;
import static exiledsector.ui.node.SkillTreeNodeGeometry.NODE_CONNECTOR_GLOW_LINE_THICKNESS;
import static exiledsector.ui.node.SkillTreeNodeGeometry.NODE_CONNECTOR_LINE_THICKNESS;
import static exiledsector.ui.node.SkillTreeNodeGeometry.NODE_CONNECTOR_PARALLEL_GAP;
import static exiledsector.ui.node.SkillTreeNodeGeometry.NODE_SIZE;
import static exiledsector.ui.node.SkillTreeNodeGeometry.RING_DULL_ALPHA;
import static exiledsector.ui.node.SkillTreeNodeGeometry.RING_DULL_COLOR;
import static exiledsector.ui.node.SkillTreeNodeGeometry.connectorEndpointRadius;

final class SkillTreeNodeConnectorRenderer {

    private static final int CURVE_ARC_SAMPLES = 40;
    private static final int CURVE_RENDER_SEGMENTS = 20;
    private static final float WORMHOLE_TIP_FADE_LENGTH = 14f;
    private static final float WORMHOLE_TIP_FADE_MAX_FRACTION = 0.4f;

    private final SkillTreePanelStyle style;

    private final List<LineVertex> dullLineVertices = new ArrayList<>();
    private final List<LineVertex> glowLineVertices = new ArrayList<>();
    private final List<LineVertex> glowHaloVertices = new ArrayList<>();

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
            float nodeRadius = endpointRadius(node, data, satisfiedRootId, zoom);

            for (String connectedId : node.getConnectedNodeIds()) {
                SkillNode other = SkillTree.get(connectedId);
                if (other == null) continue;
                if (other.getType().getTier() != SkillTier.ROOT && node.getId().compareTo(other.getId()) >= 0) continue;
                if (!SkillTree.isConnectorVisible(node.getId(), other.getId())) continue;

                float otherX = centerX + other.getOffsetX() * zoom;
                float otherY = centerY - other.getOffsetY() * zoom;
                float otherRadius = endpointRadius(other, data, satisfiedRootId, zoom);
                boolean bothSatisfied = data.isSatisfied(node.getId(), satisfiedRootId) && data.isSatisfied(other.getId(), satisfiedRootId);

                boolean fadeOtherToBlack = isClosedWormhole(other, data, satisfiedRootId) || isHiddenNode(other, data);
                boolean fadeNodeToBlack = isClosedWormhole(node, data, satisfiedRootId) || isHiddenNode(node, data);
                boolean tipFadeOtherToBlack = isOpenWormhole(other, data, satisfiedRootId);
                boolean tipFadeNodeToBlack = isOpenWormhole(node, data, satisfiedRootId);

                ConnectorCurve curve = SkillTree.getCurve(node.getId(), other.getId());
                if (curve == null) {
                    drawStraightNodeConnectorLine(otherX, otherY, otherRadius, nodeX, nodeY, nodeRadius,
                            bothSatisfied, fadeOtherToBlack, fadeNodeToBlack, tipFadeOtherToBlack, tipFadeNodeToBlack, zoom, alphaMult);
                } else {
                    float throughX = centerX + curve.getControlOffsetX() * zoom;
                    float throughY = centerY - curve.getControlOffsetY() * zoom;
                    drawCurvedNodeConnectorLine(otherX, otherY, otherRadius, throughX, throughY, nodeX, nodeY, nodeRadius,
                            bothSatisfied, fadeOtherToBlack, fadeNodeToBlack, tipFadeOtherToBlack, tipFadeNodeToBlack, zoom, alphaMult);
                }
            }
        }

        GL11.glDisable(GL11.GL_BLEND);
    }

    private static float endpointRadius(SkillNode node, ShipSkillData data, String satisfiedRootId, float zoom) {
        if (isOpenWormhole(node, data, satisfiedRootId)) return 0f;
        return connectorEndpointRadius(node.getType().getTier(), NODE_SIZE * zoom * node.getType().getTier().getSizeMultiplier(), zoom);
    }

    private static boolean isOpenWormhole(SkillNode node, ShipSkillData data, String satisfiedRootId) {
        return node.getType().getTier() == SkillTier.WORMHOLE && data.isSatisfied(node.getId(), satisfiedRootId);
    }

    private static boolean isClosedWormhole(SkillNode node, ShipSkillData data, String satisfiedRootId) {
        return node.getType().getTier() == SkillTier.WORMHOLE && !data.isSatisfied(node.getId(), satisfiedRootId);
    }

    private static boolean isHiddenNode(SkillNode node, ShipSkillData data) {
        return SkillTypeUnlockStatus.isHidden(node.getType(), data);
    }

    private void drawStraightNodeConnectorLine(float x1, float y1, float r1, float x2, float y2, float r2,
                                                boolean glowing, boolean fadeR1ToBlack, boolean fadeR2ToBlack,
                                                boolean tipFadeR1ToBlack, boolean tipFadeR2ToBlack, float zoom, float alphaMult) {
        clearBatch();
        drawStraightNodeConnectorLineImpl(x1, y1, r1, x2, y2, r2, glowing, fadeR1ToBlack, fadeR2ToBlack,
                tipFadeR1ToBlack, tipFadeR2ToBlack, zoom, alphaMult);
        flushBatch();
    }

    private void drawStraightNodeConnectorLineImpl(float x1, float y1, float r1, float x2, float y2, float r2,
                                                     boolean glowing, boolean fadeR1ToBlack, boolean fadeR2ToBlack,
                                                     boolean tipFadeR1ToBlack, boolean tipFadeR2ToBlack, float zoom, float alphaMult) {
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
        float visibleLength = length - r1 - r2;

        if (glowing) {
            if (tipFadeR1ToBlack || tipFadeR2ToBlack) {
                drawGlowingLineWithTipFade(startX, startY, endX, endY, visibleLength, tipFadeR1ToBlack, tipFadeR2ToBlack, zoom, alphaMult);
            } else {
                drawConnectorSegment(startX, startY, endX, endY, true, alphaMult);
            }
        } else if (fadeR1ToBlack || fadeR2ToBlack) {
            drawFadedDullLine(startX, startY, endX, endY, fadeR1ToBlack, fadeR2ToBlack, alphaMult);
        } else {
            drawConnectorSegment(startX, startY, endX, endY, false, alphaMult);
        }
    }

    private void drawGlowingLineWithTipFade(float x1, float y1, float x2, float y2, float visibleLength,
                                             boolean tipFadeR1ToBlack, boolean tipFadeR2ToBlack, float zoom, float alphaMult) {
        if (visibleLength <= 0.0001f) {
            drawConnectorSegment(x1, y1, x2, y2, true, alphaMult);
            return;
        }

        float maxTip = visibleLength * WORMHOLE_TIP_FADE_MAX_FRACTION;
        float tipLen1 = tipFadeR1ToBlack ? Math.min(WORMHOLE_TIP_FADE_LENGTH * zoom, maxTip) : 0f;
        float tipLen2 = tipFadeR2ToBlack ? Math.min(WORMHOLE_TIP_FADE_LENGTH * zoom, maxTip) : 0f;

        float dirX = (x2 - x1) / visibleLength;
        float dirY = (y2 - y1) / visibleLength;
        Color accent = style.getAccentColor();

        float innerX1 = x1 + dirX * tipLen1;
        float innerY1 = y1 + dirY * tipLen1;
        float innerX2 = x2 - dirX * tipLen2;
        float innerY2 = y2 - dirY * tipLen2;

        if (tipLen1 > 0f) {
            drawGradientLine(x1, y1, Color.BLACK, innerX1, innerY1, accent, alphaMult * NODE_CONNECTOR_GLOW_HALO_ALPHA, NODE_CONNECTOR_GLOW_HALO_THICKNESS);
            drawGradientLine(x1, y1, Color.BLACK, innerX1, innerY1, accent, alphaMult, NODE_CONNECTOR_GLOW_LINE_THICKNESS);
        }
        if (tipLen2 > 0f) {
            drawGradientLine(innerX2, innerY2, accent, x2, y2, Color.BLACK, alphaMult * NODE_CONNECTOR_GLOW_HALO_ALPHA, NODE_CONNECTOR_GLOW_HALO_THICKNESS);
            drawGradientLine(innerX2, innerY2, accent, x2, y2, Color.BLACK, alphaMult, NODE_CONNECTOR_GLOW_LINE_THICKNESS);
        }

        drawConnectorSegment(innerX1, innerY1, innerX2, innerY2, true, alphaMult);
    }

    private void drawCurvedNodeConnectorLine(float x0, float y0, float r1, float throughX, float throughY, float x2, float y2, float r2,
                                              boolean glowing, boolean fadeR1ToBlack, boolean fadeR2ToBlack,
                                              boolean tipFadeR1ToBlack, boolean tipFadeR2ToBlack, float zoom, float alphaMult) {
        clearBatch();
        drawCurvedNodeConnectorLineImpl(x0, y0, r1, throughX, throughY, x2, y2, r2, glowing, fadeR1ToBlack, fadeR2ToBlack,
                tipFadeR1ToBlack, tipFadeR2ToBlack, zoom, alphaMult);
        flushBatch();
    }

    private void drawCurvedNodeConnectorLineImpl(float x0, float y0, float r1, float throughX, float throughY, float x2, float y2, float r2,
                                                  boolean glowing, boolean fadeR1ToBlack, boolean fadeR2ToBlack,
                                                  boolean tipFadeR1ToBlack, boolean tipFadeR2ToBlack, float zoom, float alphaMult) {
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

        boolean dullFading = !glowing && (fadeR1ToBlack || fadeR2ToBlack);
        boolean glowTipFading = glowing && (tipFadeR1ToBlack || tipFadeR2ToBlack);
        float visibleArcLength = totalLength - r1 - r2;
        float maxTip = visibleArcLength * WORMHOLE_TIP_FADE_MAX_FRACTION;
        float tipFraction1 = glowTipFading && tipFadeR1ToBlack ? Math.min(WORMHOLE_TIP_FADE_LENGTH * zoom, maxTip) / visibleArcLength : 0f;
        float tipFraction2 = glowTipFading && tipFadeR2ToBlack ? Math.min(WORMHOLE_TIP_FADE_LENGTH * zoom, maxTip) / visibleArcLength : 0f;

        float prevX = 0, prevY = 0;
        for (int i = 0; i <= CURVE_RENDER_SEGMENTS; i++) {
            float t = tStart + (tEnd - tStart) * i / CURVE_RENDER_SEGMENTS;
            float omt = 1f - t;
            float x = omt * omt * x0 + 2f * omt * t * cx + t * t * x2;
            float y = omt * omt * y0 + 2f * omt * t * cy + t * t * y2;
            if (i > 0) {
                float progressPrev = (float) (i - 1) / CURVE_RENDER_SEGMENTS;
                float progressCur = (float) i / CURVE_RENDER_SEGMENTS;
                if (dullFading) {
                    drawFadedDullSegment(prevX, prevY, x, y, progressPrev, progressCur, fadeR1ToBlack, fadeR2ToBlack, alphaMult);
                } else if (glowTipFading) {
                    drawGlowingSegmentWithTipFade(prevX, prevY, x, y, progressPrev, progressCur, tipFraction1, tipFraction2, alphaMult);
                } else {
                    drawConnectorSegment(prevX, prevY, x, y, glowing, alphaMult);
                }
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

    private void drawFadedDullLine(float x1, float y1, float x2, float y2, boolean fadeR1ToBlack, boolean fadeR2ToBlack, float alphaMult) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length <= 0.0001f) return;
        float dirX = dx / length;
        float dirY = dy / length;
        float perpX = -dirY * (NODE_CONNECTOR_PARALLEL_GAP / 2f);
        float perpY = dirX * (NODE_CONNECTOR_PARALLEL_GAP / 2f);
        float midX = (x1 + x2) / 2f;
        float midY = (y1 + y2) / 2f;

        Color startColor = colorForFadeProgress(0f, fadeR1ToBlack, fadeR2ToBlack);
        Color midColor = RING_DULL_COLOR;
        Color endColor = colorForFadeProgress(1f, fadeR1ToBlack, fadeR2ToBlack);
        float alpha = alphaMult * RING_DULL_ALPHA;

        drawGradientLine(x1 + perpX, y1 + perpY, startColor, midX + perpX, midY + perpY, midColor, alpha, NODE_CONNECTOR_LINE_THICKNESS);
        drawGradientLine(midX + perpX, midY + perpY, midColor, x2 + perpX, y2 + perpY, endColor, alpha, NODE_CONNECTOR_LINE_THICKNESS);
        drawGradientLine(x1 - perpX, y1 - perpY, startColor, midX - perpX, midY - perpY, midColor, alpha, NODE_CONNECTOR_LINE_THICKNESS);
        drawGradientLine(midX - perpX, midY - perpY, midColor, x2 - perpX, y2 - perpY, endColor, alpha, NODE_CONNECTOR_LINE_THICKNESS);
    }

    private void drawFadedDullSegment(float x1, float y1, float x2, float y2, float progress0, float progress1,
                                       boolean fadeR1ToBlack, boolean fadeR2ToBlack, float alphaMult) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length <= 0.0001f) return;
        float dirX = dx / length;
        float dirY = dy / length;
        float perpX = -dirY * (NODE_CONNECTOR_PARALLEL_GAP / 2f);
        float perpY = dirX * (NODE_CONNECTOR_PARALLEL_GAP / 2f);

        Color color0 = colorForFadeProgress(progress0, fadeR1ToBlack, fadeR2ToBlack);
        Color color1 = colorForFadeProgress(progress1, fadeR1ToBlack, fadeR2ToBlack);
        float alpha = alphaMult * RING_DULL_ALPHA;

        drawGradientLine(x1 + perpX, y1 + perpY, color0, x2 + perpX, y2 + perpY, color1, alpha, NODE_CONNECTOR_LINE_THICKNESS);
        drawGradientLine(x1 - perpX, y1 - perpY, color0, x2 - perpX, y2 - perpY, color1, alpha, NODE_CONNECTOR_LINE_THICKNESS);
    }

    private void drawGlowingSegmentWithTipFade(float x1, float y1, float x2, float y2, float progress0, float progress1,
                                                float tipFraction1, float tipFraction2, float alphaMult) {
        Color color0 = colorForTipFade(progress0, tipFraction1, tipFraction2);
        Color color1 = colorForTipFade(progress1, tipFraction1, tipFraction2);
        drawGradientLine(x1, y1, color0, x2, y2, color1, alphaMult * NODE_CONNECTOR_GLOW_HALO_ALPHA, NODE_CONNECTOR_GLOW_HALO_THICKNESS);
        drawGradientLine(x1, y1, color0, x2, y2, color1, alphaMult, NODE_CONNECTOR_GLOW_LINE_THICKNESS);
    }

    private Color colorForTipFade(float progress, float tipFraction1, float tipFraction2) {
        float t = 0f;
        if (tipFraction1 > 0f && progress < tipFraction1) {
            t = 1f - progress / tipFraction1;
        } else if (tipFraction2 > 0f && progress > 1f - tipFraction2) {
            t = 1f - (1f - progress) / tipFraction2;
        }
        return lerpColor(style.getAccentColor(), Color.BLACK, t);
    }

    private static Color colorForFadeProgress(float progress, boolean fadeR1ToBlack, boolean fadeR2ToBlack) {
        float t;
        if (progress <= 0.5f) {
            t = fadeR1ToBlack ? (1f - progress / 0.5f) : 0f;
        } else {
            t = fadeR2ToBlack ? ((progress - 0.5f) / 0.5f) : 0f;
        }
        return lerpColor(RING_DULL_COLOR, Color.BLACK, t);
    }

    private static Color lerpColor(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = Math.round(a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return new Color(r, g, bl);
    }

    private void drawLine(float x1, float y1, float x2, float y2, Color color, float alpha, float thickness) {
        drawGradientLine(x1, y1, color, x2, y2, color, alpha, thickness);
    }

    private void drawGradientLine(float x1, float y1, Color color1, float x2, float y2, Color color2, float alpha, float thickness) {
        List<LineVertex> bucket = bucketFor(thickness);
        bucket.add(new LineVertex(x1, y1, color1, alpha));
        bucket.add(new LineVertex(x2, y2, color2, alpha));
    }

    private List<LineVertex> bucketFor(float thickness) {
        if (thickness == NODE_CONNECTOR_LINE_THICKNESS) return dullLineVertices;
        if (thickness == NODE_CONNECTOR_GLOW_LINE_THICKNESS) return glowLineVertices;
        return glowHaloVertices;
    }

    private void clearBatch() {
        dullLineVertices.clear();
        glowLineVertices.clear();
        glowHaloVertices.clear();
    }

    private void flushBatch() {
        flushBucket(dullLineVertices, NODE_CONNECTOR_LINE_THICKNESS);
        flushBucket(glowHaloVertices, NODE_CONNECTOR_GLOW_HALO_THICKNESS);
        flushBucket(glowLineVertices, NODE_CONNECTOR_GLOW_LINE_THICKNESS);
    }

    private void flushBucket(List<LineVertex> vertices, float thickness) {
        if (vertices.isEmpty()) return;

        GL11.glLineWidth(thickness);
        GL11.glBegin(GL11.GL_LINES);
        for (LineVertex vertex : vertices) {
            Misc.setColor(vertex.color, vertex.alpha);
            GL11.glVertex2f(vertex.x, vertex.y);
        }
        GL11.glEnd();
    }

    private static final class LineVertex {
        final float x;
        final float y;
        final Color color;
        final float alpha;

        LineVertex(float x, float y, Color color, float alpha) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.alpha = alpha;
        }
    }
}
