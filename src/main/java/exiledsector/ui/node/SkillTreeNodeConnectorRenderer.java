package exiledsector.ui.node;

import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillTree;
import exiledsector.skills.layout.ConnectorCurve;
import exiledsector.ui.SkillTreePanelStyle;
import exiledsector.ui.TreeViewport;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final float WORMHOLE_OPEN_FADE_SECONDS = 2f;

    private final SkillTreePanelStyle style;
    private final NodeSearch search;

    private final List<LineVertex> dullLineVertices = new ArrayList<>();
    private final List<LineVertex> glowLineVertices = new ArrayList<>();
    private final List<LineVertex> glowHaloVertices = new ArrayList<>();
    private final Map<String, FaderUtil> wormholeOpenFaders = new HashMap<>();

    SkillTreeNodeConnectorRenderer(SkillTreePanelStyle style, NodeSearch search) {
        this.style = style;
        this.search = search;
    }

    void advance(float amount, ShipSkillData data, String satisfiedRootId) {
        for (SkillNode node : SkillTree.getAllNodes().values()) {
            if (node.getType().getTier() != SkillTier.WORMHOLE) continue;

            FaderUtil fader = wormholeOpenFaders.computeIfAbsent(node.getId(),
                    key -> new FaderUtil(0f, WORMHOLE_OPEN_FADE_SECONDS, WORMHOLE_OPEN_FADE_SECONDS));

            if (data.isSatisfied(node.getId(), satisfiedRootId)) {
                fader.fadeIn();
            } else {
                fader.fadeOut();
            }
            fader.advance(amount);
        }
    }

    private float wormholeOpenFraction(String nodeId) {
        FaderUtil fader = wormholeOpenFaders.get(nodeId);
        return fader == null ? 0f : fader.getBrightness();
    }

    void draw(TreeViewport viewport, NodeAllocator.Snapshot tree, float alphaMult) {
        float zoom = viewport.zoom();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        clearBatch();

        for (SkillNode node : SkillTree.getAllNodes().values()) {
            if (node.getType().getTier() == SkillTier.ROOT) continue;

            float nodeX = viewport.screenX(node.getOffsetX());
            float nodeY = viewport.screenY(node.getOffsetY());
            ConnectorEndpoint nodeEndpoint = new ConnectorEndpoint(new Vector2f(nodeX, nodeY), endpointRadius(node, zoom));

            drawConnectorsFrom(node, nodeEndpoint, viewport, tree, alphaMult);
        }

        flushBatch();
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawConnectorsFrom(SkillNode node, ConnectorEndpoint nodeEndpoint, TreeViewport viewport,
                                     NodeAllocator.Snapshot tree, float alphaMult) {
        float zoom = viewport.zoom();
        ShipSkillData data = tree.data();
        String satisfiedRootId = tree.satisfiedRootId();
        for (String connectedId : node.getConnectedNodeIds()) {
            SkillNode other = SkillTree.get(connectedId);
            boolean skip = other == null
                    || (other.getType().getTier() != SkillTier.ROOT && node.getId().compareTo(other.getId()) >= 0)
                    || !SkillTree.isConnectorVisible(node.getId(), other.getId());
            if (skip) {
                continue;
            }

            float otherX = viewport.screenX(other.getOffsetX());
            float otherY = viewport.screenY(other.getOffsetY());
            ConnectorEndpoint otherEndpoint = new ConnectorEndpoint(new Vector2f(otherX, otherY), endpointRadius(other, zoom));
            boolean bothSatisfied = data.isSatisfied(node.getId(), satisfiedRootId) && data.isSatisfied(other.getId(), satisfiedRootId);

            ConnectorFade fade = new ConnectorFade(
                    bothSatisfied,
                    isWormhole(other) || tree.isHidden(other),
                    isWormhole(node) || tree.isHidden(node),
                    isOpenWormhole(other, data, satisfiedRootId),
                    isOpenWormhole(node, data, satisfiedRootId));

            float edgeAlpha = alphaMult * search.connectorAlpha(node, other, tree);
            ConnectorCurve curve = SkillTree.getCurve(node.getId(), other.getId());
            if (curve == null) {
                drawStraightNodeConnectorLine(otherEndpoint, nodeEndpoint, fade, zoom, edgeAlpha);
            } else {
                float throughX = viewport.screenX(curve.getControlOffsetX());
                float throughY = viewport.screenY(curve.getControlOffsetY());
                drawCurvedNodeConnectorLine(otherEndpoint, new Vector2f(throughX, throughY), nodeEndpoint, fade, zoom, edgeAlpha);
            }
        }
    }

    private float endpointRadius(SkillNode node, float zoom) {
        float fullRadius = connectorEndpointRadius(node.getType().getTier(), NODE_SIZE * zoom * node.getType().getTier().getSizeMultiplier(), zoom);
        if (node.getType().getTier() != SkillTier.WORMHOLE) return fullRadius;
        return fullRadius * (1f - wormholeOpenFraction(node.getId()));
    }

    private static boolean isOpenWormhole(SkillNode node, ShipSkillData data, String satisfiedRootId) {
        return node.getType().getTier() == SkillTier.WORMHOLE && data.isSatisfied(node.getId(), satisfiedRootId);
    }

    private static boolean isWormhole(SkillNode node) {
        return node.getType().getTier() == SkillTier.WORMHOLE;
    }

    private void drawStraightNodeConnectorLine(ConnectorEndpoint a, ConnectorEndpoint b, ConnectorFade fade, float zoom, float alphaMult) {
        float x1 = a.point().x;
        float y1 = a.point().y;
        float r1 = a.radius();
        float x2 = b.point().x;
        float y2 = b.point().y;
        float r2 = b.radius();
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
        SegmentFade segmentFade = segmentFade(fade, length - r1 - r2, zoom, alphaMult);

        float[] breakpoints = straightBreakpoints(segmentFade);
        for (int i = 1; i < breakpoints.length; i++) {
            float from = breakpoints[i - 1];
            float to = breakpoints[i];
            if (to > from) {
                drawFadedSegment(startX + (endX - startX) * from, startY + (endY - startY) * from,
                        startX + (endX - startX) * to, startY + (endY - startY) * to, from, to, segmentFade);
            }
        }
    }

    private static float[] straightBreakpoints(SegmentFade segmentFade) {
        if (segmentFade.fade().dullFading()) {
            return new float[]{0f, 0.5f, 1f};
        }
        if (segmentFade.fade().glowTipFading()) {
            return new float[]{0f, segmentFade.tipFraction1(), 1f - segmentFade.tipFraction2(), 1f};
        }
        return new float[]{0f, 1f};
    }

    private static SegmentFade segmentFade(ConnectorFade fade, float visibleLength, float zoom, float alphaMult) {
        return new SegmentFade(fade,
                tipFraction(fade.glowing() && fade.tipFadeR1ToBlack(), visibleLength, zoom),
                tipFraction(fade.glowing() && fade.tipFadeR2ToBlack(), visibleLength, zoom),
                alphaMult);
    }

    private static float tipFraction(boolean fades, float visibleLength, float zoom) {
        if (!fades || visibleLength <= 0f) {
            return 0f;
        }
        return Math.min(WORMHOLE_TIP_FADE_LENGTH * zoom, visibleLength * WORMHOLE_TIP_FADE_MAX_FRACTION) / visibleLength;
    }

    private void drawFadedSegment(float x1, float y1, float x2, float y2, float progress1, float progress2,
                                  SegmentFade segmentFade) {
        ConnectorFade fade = segmentFade.fade();
        float alphaMult = segmentFade.alphaMult();
        if (fade.dullFading()) {
            drawFadedDullSegment(new Vector2f(x1, y1), new Vector2f(x2, y2), progress1, progress2,
                    fade.fadeR1ToBlack(), fade.fadeR2ToBlack(), alphaMult);
        } else if (fade.glowTipFading()) {
            drawGlowingSegmentWithTipFade(new Vector2f(x1, y1), new Vector2f(x2, y2), progress1, progress2,
                    segmentFade.tipFraction1(), segmentFade.tipFraction2(), alphaMult);
        } else {
            drawConnectorSegment(x1, y1, x2, y2, fade.glowing(), alphaMult);
        }
    }

    private void drawCurvedNodeConnectorLine(ConnectorEndpoint a, Vector2f through, ConnectorEndpoint b, ConnectorFade fade, float zoom, float alphaMult) {
        float r1 = a.radius();
        float r2 = b.radius();
        float cx = 2f * through.x - (a.point().x + b.point().x) / 2f;
        float cy = 2f * through.y - (a.point().y + b.point().y) / 2f;
        QuadraticCurve curve = new QuadraticCurve(a.point().x, a.point().y, cx, cy, b.point().x, b.point().y);

        float[] cumLen = computeCumulativeArcLength(curve);
        float totalLength = cumLen[CURVE_ARC_SAMPLES];
        if (totalLength <= r1 + r2) return;

        float tStart = curveParamAtArcLength(cumLen, r1);
        float tEnd = curveParamAtArcLength(cumLen, totalLength - r2);
        if (tEnd <= tStart) return;

        renderCurveSegments(curve, tStart, tEnd, totalLength - r1 - r2, fade, zoom, alphaMult);
    }

    private float[] computeCumulativeArcLength(QuadraticCurve curve) {
        float[] cumLen = new float[CURVE_ARC_SAMPLES + 1];
        float prevX = curve.xAt(0f);
        float prevY = curve.yAt(0f);
        for (int i = 1; i <= CURVE_ARC_SAMPLES; i++) {
            float t = (float) i / CURVE_ARC_SAMPLES;
            float x = curve.xAt(t);
            float y = curve.yAt(t);
            float dx = x - prevX;
            float dy = y - prevY;
            cumLen[i] = cumLen[i - 1] + (float) Math.sqrt(dx * dx + dy * dy);
            prevX = x;
            prevY = y;
        }
        return cumLen;
    }

    private void renderCurveSegments(QuadraticCurve curve, float tStart, float tEnd, float visibleArcLength,
                                      ConnectorFade fade, float zoom, float alphaMult) {
        SegmentFade segmentFade = segmentFade(fade, visibleArcLength, zoom, alphaMult);

        float prevX = curve.xAt(tStart);
        float prevY = curve.yAt(tStart);
        for (int i = 1; i <= CURVE_RENDER_SEGMENTS; i++) {
            float t = tStart + (tEnd - tStart) * i / CURVE_RENDER_SEGMENTS;
            float x = curve.xAt(t);
            float y = curve.yAt(t);
            float progressPrev = (float) (i - 1) / CURVE_RENDER_SEGMENTS;
            float progressCur = (float) i / CURVE_RENDER_SEGMENTS;
            drawFadedSegment(prevX, prevY, x, y, progressPrev, progressCur, segmentFade);
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

    private void drawFadedDullSegment(Vector2f p1, Vector2f p2, float progress0, float progress1,
                                       boolean fadeR1ToBlack, boolean fadeR2ToBlack, float alphaMult) {
        float dx = p2.x - p1.x;
        float dy = p2.y - p1.y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length <= 0.0001f) return;
        float dirX = dx / length;
        float dirY = dy / length;
        float perpX = -dirY * (NODE_CONNECTOR_PARALLEL_GAP / 2f);
        float perpY = dirX * (NODE_CONNECTOR_PARALLEL_GAP / 2f);

        Color color0 = colorForFadeProgress(progress0, fadeR1ToBlack, fadeR2ToBlack);
        Color color1 = colorForFadeProgress(progress1, fadeR1ToBlack, fadeR2ToBlack);
        float alpha = alphaMult * RING_DULL_ALPHA;

        drawGradientLine(new Vector2f(p1.x + perpX, p1.y + perpY), color0, new Vector2f(p2.x + perpX, p2.y + perpY), color1, alpha, NODE_CONNECTOR_LINE_THICKNESS);
        drawGradientLine(new Vector2f(p1.x - perpX, p1.y - perpY), color0, new Vector2f(p2.x - perpX, p2.y - perpY), color1, alpha, NODE_CONNECTOR_LINE_THICKNESS);
    }

    private void drawGlowingSegmentWithTipFade(Vector2f p1, Vector2f p2, float progress0, float progress1,
                                                float tipFraction1, float tipFraction2, float alphaMult) {
        Color color0 = colorForTipFade(progress0, tipFraction1, tipFraction2);
        Color color1 = colorForTipFade(progress1, tipFraction1, tipFraction2);
        drawGradientLine(p1, color0, p2, color1, alphaMult * NODE_CONNECTOR_GLOW_HALO_ALPHA, NODE_CONNECTOR_GLOW_HALO_THICKNESS);
        drawGradientLine(p1, color0, p2, color1, alphaMult, NODE_CONNECTOR_GLOW_LINE_THICKNESS);
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
        drawGradientLine(new Vector2f(x1, y1), color, new Vector2f(x2, y2), color, alpha, thickness);
    }

    private void drawGradientLine(Vector2f p1, Color color1, Vector2f p2, Color color2, float alpha, float thickness) {
        List<LineVertex> bucket = bucketFor(thickness);
        bucket.add(new LineVertex(p1.x, p1.y, color1, alpha));
        bucket.add(new LineVertex(p2.x, p2.y, color2, alpha));
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

    private record ConnectorEndpoint(Vector2f point, float radius) {
    }

    private record ConnectorFade(boolean glowing, boolean fadeR1ToBlack, boolean fadeR2ToBlack,
                                  boolean tipFadeR1ToBlack, boolean tipFadeR2ToBlack) {

        boolean dullFading() {
            return !glowing && (fadeR1ToBlack || fadeR2ToBlack);
        }

        boolean glowTipFading() {
            return glowing && (tipFadeR1ToBlack || tipFadeR2ToBlack);
        }
    }

    private record SegmentFade(ConnectorFade fade, float tipFraction1, float tipFraction2, float alphaMult) {
    }

    private record QuadraticCurve(float x0, float y0, float cx, float cy, float x2, float y2) {
        float xAt(float t) {
            float omt = 1f - t;
            return omt * omt * x0 + 2f * omt * t * cx + t * t * x2;
        }

        float yAt(float t) {
            float omt = 1f - t;
            return omt * omt * y0 + 2f * omt * t * cy + t * t * y2;
        }
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
