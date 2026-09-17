package exiledsector.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;
import exiledsector.effects.SkillTreeHullMod;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillTree;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static exiledsector.ui.SkillTreePanelStyle.FONT_LINE_HEIGHT_FACTOR;
import static exiledsector.ui.SkillTreePanelStyle.GLOW_COLOR;
import static exiledsector.ui.SkillTreePanelStyle.TOOLTIP_BODY_COLOR;
import static exiledsector.ui.SkillTreePanelStyle.TOOLTIP_BODY_FONT_SIZE;
import static exiledsector.ui.SkillTreePanelStyle.TOOLTIP_TITLE_FONT_SIZE;

final class SkillTreeNodeRenderer {

    private static final float SYMBOL_SIZE = 128f;
    private static final float NODE_SIZE = 64f;

    private static final int VIGNETTE_SEGMENTS = 48;
    private static final float VIGNETTE_INNER_FRACTION = 0.88f;
    private static final float VIGNETTE_OUTER_FRACTION = 1f;
    private static final float VIGNETTE_MARGIN_FRACTION = 0.375f;

    private static final float TOOLTIP_MAX_TEXT_WIDTH = 240f;
    private static final float TOOLTIP_MAX_TEXT_HEIGHT = 200f;
    private static final float TOOLTIP_WIDTH_SAFETY_MARGIN = 8f;
    private static final float TOOLTIP_PADDING = 10f;
    private static final float TOOLTIP_TITLE_BODY_GAP = 6f;
    private static final float TOOLTIP_CURSOR_OFFSET = 18f;
    private static final Color TOOLTIP_TITLE_COLOR = Color.WHITE;

    private static final Color ALLOCATED_TINT = Color.WHITE;
    private static final Color UNALLOCATED_TINT = new Color(90, 90, 90);

    private static final float[] RING_OFFSETS_SMALL = {0f, 0.25f};
    private static final float[] RING_OFFSETS_NOTABLE = {0f, 0.13f, 0.26f, 0.39f};
    private static final int RING_SEGMENTS = 32;
    private static final float RING_LINE_THICKNESS = 1.5f;
    private static final Color RING_DULL_COLOR = new Color(150, 150, 150);
    private static final float RING_DULL_ALPHA = 0.5f;
    private static final float RING_ALLOCATED_ALPHA = 0.9f;

    private static final float KEYSTONE_RING_GAP = 0.3f;
    private static final float KEYSTONE_RING_THICKNESS = 4f;
    private static final int KEYSTONE_CIRCUIT_BRANCH_COUNT = 8;
    private static final float KEYSTONE_CIRCUIT_JOG_FRACTION = 1.55f;
    private static final float KEYSTONE_CIRCUIT_JOG_LENGTH_FRACTION = 0.3f;
    private static final float KEYSTONE_CIRCUIT_LINE_THICKNESS = 1.5f;
    private static final float KEYSTONE_CIRCUIT_VIA_RADIUS = 3f;

    private static final float ICON_SIZE_MULTIPLIER_NOTABLE = 1.2f;
    private static final float ICON_SIZE_MULTIPLIER_KEYSTONE = 1.4f;

    private static final float PULSE_DURATION = 0.5f;
    private static final float PULSE_START_RADIUS_FRACTION = 1f;
    private static final float PULSE_END_RADIUS_FRACTION = 2.2f;

    private static final float CENTER_RING_RADIUS_FRACTION = 1f;

    private static final float NODE_CONNECTOR_PARALLEL_GAP = 4f;
    private static final float NODE_CONNECTOR_LINE_THICKNESS = 1.5f;
    private static final float NODE_CONNECTOR_GLOW_LINE_THICKNESS = 2.5f;
    private static final float NODE_CONNECTOR_GLOW_HALO_THICKNESS = 7f;
    private static final float NODE_CONNECTOR_GLOW_HALO_ALPHA = 0.35f;

    private final String symbolPath;
    private final FleetMemberAPI member;
    private final SkillTreePanelStyle style;
    private final Set<String> loadedSprites = new HashSet<>();
    private final Map<String, SkillTreePanelStyle.TooltipText> tooltipTitles = new HashMap<>();
    private final Map<String, SkillTreePanelStyle.TooltipText> tooltipBodies = new HashMap<>();
    private final Map<String, Float> pulseElapsed = new HashMap<>();

    SkillTreeNodeRenderer(String symbolPath, FleetMemberAPI member, SkillTreePanelStyle style) {
        this.symbolPath = symbolPath;
        this.member = member;
        this.style = style;
    }

    void advance(float amount) {
        if (pulseElapsed.isEmpty()) return;

        Iterator<Map.Entry<String, Float>> it = pulseElapsed.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Float> entry = it.next();
            float elapsed = entry.getValue() + amount;
            if (elapsed >= PULSE_DURATION) {
                it.remove();
            } else {
                entry.setValue(elapsed);
            }
        }
    }

    void render(float centerX, float centerY, float zoom, float alphaMult) {
        ShipSkillData data = ShipSkillDataManager.get(member.getId());

        drawIcon(symbolPath, centerX, centerY, SYMBOL_SIZE * zoom, alphaMult, ALLOCATED_TINT);
        drawCenterRing(centerX, centerY, SYMBOL_SIZE * zoom, alphaMult);

        for (SkillNode node : SkillTree.getAllNodes().values()) {
            float nodeX = centerX + node.getOffsetX() * zoom;
            float nodeY = centerY - node.getOffsetY() * zoom;
            boolean allocated = data.isAllocated(node.getId());
            SkillTier tier = node.getType().getTier();
            float footprintSize = NODE_SIZE * zoom * tier.getSizeMultiplier();
            float iconSize = footprintSize * iconSizeMultiplier(tier);

            drawIcon(node.getIconPath(), nodeX, nodeY, iconSize, alphaMult, allocated ? ALLOCATED_TINT : UNALLOCATED_TINT);
            drawRings(nodeX, nodeY, footprintSize, alphaMult, allocated, pulseElapsed.get(node.getId()), tier);
        }

        drawNodeConnectors(centerX, centerY, zoom, data, alphaMult);
    }

    void renderHoverTooltip(float centerX, float centerY, float zoom, float mouseX, float mouseY, float alphaMult) {
        SkillNode hovered = findNodeAt(centerX, centerY, zoom, mouseX, mouseY);
        if (hovered != null) {
            renderTooltip(hovered, mouseX, mouseY, alphaMult);
        }
    }

    SkillNode findNodeAt(float centerX, float centerY, float zoom, float x, float y) {
        for (SkillNode node : SkillTree.getAllNodes().values()) {
            float nodeX = centerX + node.getOffsetX() * zoom;
            float nodeY = centerY - node.getOffsetY() * zoom;
            float halfSize = NODE_SIZE * zoom * node.getType().getTier().getSizeMultiplier() / 2f;
            if (Math.abs(x - nodeX) <= halfSize && Math.abs(y - nodeY) <= halfSize) {
                return node;
            }
        }
        return null;
    }

    void toggleAllocation(SkillNode node) {
        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        boolean wasAllocated = data.isAllocated(node.getId());
        data.toggle(node, SkillTree.getAllNodes().values());
        boolean isAllocatedNow = data.isAllocated(node.getId());
        if (isAllocatedNow != wasAllocated) {
            member.setStatUpdateNeeded(true);
            member.updateStats();
            new SkillTreeHullMod().applyEffectsBeforeShipCreation(member.getHullSpec().getHullSize(), member.getStats(), SkillTreeHullMod.ID);
            if (isAllocatedNow) {
                pulseElapsed.put(node.getId(), 0f);
            }
        }
    }

    private boolean ensureTextureLoaded(String spritePath) {
        if (loadedSprites.add(spritePath)) {
            try {
                Global.getSettings().loadTexture(spritePath);
            } catch (IOException e) {
                Logger.getLogger(SkillTreeNodeRenderer.class).error("Failed to load texture " + spritePath, e);
                return false;
            }
        }
        return true;
    }

    private void drawIcon(String spritePath, float cx, float cy, float size, float alphaMult, Color tint) {
        if (!ensureTextureLoaded(spritePath)) return;

        SpriteAPI sprite = Global.getSettings().getSprite(spritePath);
        sprite.setSize(size, size);
        sprite.setAlphaMult(alphaMult);
        sprite.setColor(tint);
        sprite.renderAtCenter(cx, cy);

        drawVignette(cx, cy, size, alphaMult);
    }

    private void drawRings(float cx, float cy, float footprintSize, float alphaMult, boolean allocated, Float pulseSeconds, SkillTier tier) {
        float half = footprintSize / 2f;
        float iconEdgeFraction = iconSizeMultiplier(tier);
        Color ringColor = allocated ? GLOW_COLOR : RING_DULL_COLOR;
        float ringAlpha = allocated ? RING_ALLOCATED_ALPHA : RING_DULL_ALPHA;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (tier == SkillTier.KEYSTONE) {
            float ringFraction = iconEdgeFraction + KEYSTONE_RING_GAP;
            GL11.glLineWidth(KEYSTONE_RING_THICKNESS);
            drawRingOutline(cx, cy, half * ringFraction, ringColor, ringAlpha * alphaMult);
            drawKeystoneCircuit(cx, cy, half, ringFraction, ringColor, ringAlpha * alphaMult);
        } else {
            GL11.glLineWidth(RING_LINE_THICKNESS);
            float[] offsets = tier == SkillTier.NOTABLE ? RING_OFFSETS_NOTABLE : RING_OFFSETS_SMALL;
            for (float offset : offsets) {
                drawRingOutline(cx, cy, half * (iconEdgeFraction + offset), ringColor, ringAlpha * alphaMult);
            }
        }

        if (pulseSeconds != null) {
            GL11.glLineWidth(RING_LINE_THICKNESS);
            float progress = pulseSeconds / PULSE_DURATION;
            float radiusFraction = PULSE_START_RADIUS_FRACTION + (PULSE_END_RADIUS_FRACTION - PULSE_START_RADIUS_FRACTION) * progress;
            drawRingOutline(cx, cy, half * radiusFraction, GLOW_COLOR, (1f - progress) * alphaMult);
        }

        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawKeystoneCircuit(float cx, float cy, float half, float ringFraction, Color color, float alpha) {
        GL11.glLineWidth(KEYSTONE_CIRCUIT_LINE_THICKNESS);
        float jogLength = half * KEYSTONE_CIRCUIT_JOG_LENGTH_FRACTION;

        for (int i = 0; i < KEYSTONE_CIRCUIT_BRANCH_COUNT; i++) {
            float angle = (float) (2 * Math.PI * i / KEYSTONE_CIRCUIT_BRANCH_COUNT);
            float dirX = (float) Math.cos(angle);
            float dirY = (float) Math.sin(angle);
            float perpX = -dirY * (i % 2 == 0 ? 1 : -1);
            float perpY = dirX * (i % 2 == 0 ? 1 : -1);

            float startX = cx + dirX * half * ringFraction;
            float startY = cy + dirY * half * ringFraction;
            float jogX = cx + dirX * half * KEYSTONE_CIRCUIT_JOG_FRACTION;
            float jogY = cy + dirY * half * KEYSTONE_CIRCUIT_JOG_FRACTION;
            float endX = jogX + perpX * jogLength;
            float endY = jogY + perpY * jogLength;

            drawLine(startX, startY, jogX, jogY, color, alpha, KEYSTONE_CIRCUIT_LINE_THICKNESS);
            drawLine(jogX, jogY, endX, endY, color, alpha, KEYSTONE_CIRCUIT_LINE_THICKNESS);
            drawRingOutline(jogX, jogY, KEYSTONE_CIRCUIT_VIA_RADIUS, color, alpha);
            drawRingOutline(endX, endY, KEYSTONE_CIRCUIT_VIA_RADIUS, color, alpha);
        }
    }

    private static float iconSizeMultiplier(SkillTier tier) {
        switch (tier) {
            case KEYSTONE: return ICON_SIZE_MULTIPLIER_KEYSTONE / tier.getSizeMultiplier();
            case NOTABLE: return ICON_SIZE_MULTIPLIER_NOTABLE / tier.getSizeMultiplier();
            default: return 1f;
        }
    }

    private static float outerNodeRadius(float footprintSize, SkillTier tier) {
        float half = footprintSize / 2f;
        float iconEdgeFraction = iconSizeMultiplier(tier);
        if (tier == SkillTier.KEYSTONE) return half * (iconEdgeFraction + KEYSTONE_RING_GAP);
        float[] offsets = tier == SkillTier.NOTABLE ? RING_OFFSETS_NOTABLE : RING_OFFSETS_SMALL;
        return half * (iconEdgeFraction + offsets[offsets.length - 1]);
    }

    private void drawRingOutline(float cx, float cy, float radius, Color color, float alpha) {
        Misc.setColor(color, alpha);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int i = 0; i < RING_SEGMENTS; i++) {
            float angle = (float) (2 * Math.PI * i / RING_SEGMENTS);
            GL11.glVertex2f(cx + (float) Math.cos(angle) * radius, cy + (float) Math.sin(angle) * radius);
        }
        GL11.glEnd();
    }

    private void drawCenterRing(float cx, float cy, float symbolSize, float alphaMult) {
        float radius = (symbolSize / 2f) * CENTER_RING_RADIUS_FRACTION;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(RING_LINE_THICKNESS);

        drawRingOutline(cx, cy, radius, GLOW_COLOR, alphaMult);

        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawNodeConnectors(float centerX, float centerY, float zoom, ShipSkillData data, float alphaMult) {
        float centerRadius = (SYMBOL_SIZE * zoom / 2f) * CENTER_RING_RADIUS_FRACTION;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for (SkillNode node : SkillTree.getAllNodes().values()) {
            float nodeX = centerX + node.getOffsetX() * zoom;
            float nodeY = centerY - node.getOffsetY() * zoom;
            float nodeRadius = outerNodeRadius(NODE_SIZE * zoom * node.getType().getTier().getSizeMultiplier(), node.getType().getTier());

            if (node.getPrerequisiteNodeIds().isEmpty()) {
                drawNodeConnectorLine(centerX, centerY, centerRadius, nodeX, nodeY, nodeRadius, data.isAllocated(node.getId()), alphaMult);
            } else {
                for (String prerequisiteId : node.getPrerequisiteNodeIds()) {
                    SkillNode parent = SkillTree.get(prerequisiteId);
                    if (parent == null) continue;

                    float parentX = centerX + parent.getOffsetX() * zoom;
                    float parentY = centerY - parent.getOffsetY() * zoom;
                    float parentRadius = outerNodeRadius(NODE_SIZE * zoom * parent.getType().getTier().getSizeMultiplier(), parent.getType().getTier());
                    boolean bothAllocated = data.isAllocated(node.getId()) && data.isAllocated(parent.getId());
                    drawNodeConnectorLine(parentX, parentY, parentRadius, nodeX, nodeY, nodeRadius, bothAllocated, alphaMult);
                }
            }
        }

        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawNodeConnectorLine(float x1, float y1, float r1, float x2, float y2, float r2, boolean glowing, float alphaMult) {
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

        if (glowing) {
            drawLine(startX, startY, endX, endY, GLOW_COLOR, alphaMult * NODE_CONNECTOR_GLOW_HALO_ALPHA, NODE_CONNECTOR_GLOW_HALO_THICKNESS);
            drawLine(startX, startY, endX, endY, GLOW_COLOR, alphaMult, NODE_CONNECTOR_GLOW_LINE_THICKNESS);
            return;
        }

        float perpX = -dirY * (NODE_CONNECTOR_PARALLEL_GAP / 2f);
        float perpY = dirX * (NODE_CONNECTOR_PARALLEL_GAP / 2f);

        drawLine(startX + perpX, startY + perpY, endX + perpX, endY + perpY, RING_DULL_COLOR, alphaMult * RING_DULL_ALPHA, NODE_CONNECTOR_LINE_THICKNESS);
        drawLine(startX - perpX, startY - perpY, endX - perpX, endY - perpY, RING_DULL_COLOR, alphaMult * RING_DULL_ALPHA, NODE_CONNECTOR_LINE_THICKNESS);
    }

    private void drawLine(float x1, float y1, float x2, float y2, Color color, float alpha, float thickness) {
        Misc.setColor(color, alpha);
        GL11.glLineWidth(thickness);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x2, y2);
        GL11.glEnd();
    }

    private void drawVignette(float cx, float cy, float iconSize, float alphaMult) {
        float half = iconSize / 2f;
        float innerRadius = half * VIGNETTE_INNER_FRACTION;
        float outerRadius = half * VIGNETTE_OUTER_FRACTION;
        float margin = iconSize * VIGNETTE_MARGIN_FRACTION;
        float boxHalfWidth = half + margin;
        float boxHalfHeight = half + margin;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glBegin(GL11.GL_QUAD_STRIP);
        for (int i = 0; i <= VIGNETTE_SEGMENTS; i++) {
            float angle = (float) (2 * Math.PI * i / VIGNETTE_SEGMENTS);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            Misc.setColor(Color.BLACK, 0f);
            GL11.glVertex2f(cx + cos * innerRadius, cy + sin * innerRadius);

            Misc.setColor(Color.BLACK, alphaMult);
            GL11.glVertex2f(cx + cos * outerRadius, cy + sin * outerRadius);
        }
        GL11.glEnd();

        Misc.setColor(Color.BLACK, alphaMult);
        GL11.glBegin(GL11.GL_QUAD_STRIP);
        for (int i = 0; i <= VIGNETTE_SEGMENTS; i++) {
            float angle = (float) (2 * Math.PI * i / VIGNETTE_SEGMENTS);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            float boundary = boundaryRadius(cos, sin, boxHalfWidth, boxHalfHeight);

            GL11.glVertex2f(cx + cos * outerRadius, cy + sin * outerRadius);
            GL11.glVertex2f(cx + cos * boundary, cy + sin * boundary);
        }
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
    }

    private void renderTooltip(SkillNode node, float mouseX, float mouseY, float alphaMult) {
        LazyFont font = style.getFont();
        if (font == null) return;

        SkillTreePanelStyle.TooltipText title = tooltipTitles.computeIfAbsent(node.getId(),
                id -> buildTooltipText(font, node.getDisplayName(), TOOLTIP_TITLE_FONT_SIZE, TOOLTIP_TITLE_COLOR));
        SkillTreePanelStyle.TooltipText body = tooltipBodies.computeIfAbsent(node.getId(),
                id -> buildTooltipText(font, node.getDescription(member.getHullSpec().getHullSize()), TOOLTIP_BODY_FONT_SIZE, TOOLTIP_BODY_COLOR));

        float boxWidth = Math.max(title.width, body.width) + TOOLTIP_PADDING * 2f + TOOLTIP_WIDTH_SAFETY_MARGIN;
        float boxHeight = title.height + TOOLTIP_TITLE_BODY_GAP + body.height + TOOLTIP_PADDING * 2f;
        float boxX = mouseX + TOOLTIP_CURSOR_OFFSET;
        float boxY = mouseY - boxHeight - TOOLTIP_CURSOR_OFFSET;

        style.drawTooltipBackground(boxX, boxY, boxWidth, boxHeight, alphaMult, style.getAccentColor());

        float titleY = boxY + boxHeight - TOOLTIP_PADDING;
        float bodyY = titleY - title.height - TOOLTIP_TITLE_BODY_GAP;
        title.drawable.draw(boxX + TOOLTIP_PADDING, titleY);
        body.drawable.draw(boxX + TOOLTIP_PADDING, bodyY);
    }

    private SkillTreePanelStyle.TooltipText buildTooltipText(LazyFont font, String rawText, float fontSize, Color color) {
        String wrapped = font.wrapString(rawText, fontSize, TOOLTIP_MAX_TEXT_WIDTH, TOOLTIP_MAX_TEXT_HEIGHT);
        String[] lines = wrapped.split("\n", -1);

        float width = 0f;
        for (String line : lines) {
            width = Math.max(width, font.calcWidth(line, fontSize));
        }
        float height = lines.length * fontSize * FONT_LINE_HEIGHT_FACTOR;

        LazyFont.DrawableString drawable = font.createText(wrapped, color, fontSize);
        drawable.setAlignment(LazyFont.TextAlignment.LEFT);
        drawable.setAnchor(LazyFont.TextAnchor.TOP_LEFT);
        return new SkillTreePanelStyle.TooltipText(drawable, width, height);
    }

    private static float boundaryRadius(float cos, float sin, float halfWidth, float halfHeight) {
        float rx = cos != 0f ? halfWidth / Math.abs(cos) : Float.MAX_VALUE;
        float ry = sin != 0f ? halfHeight / Math.abs(sin) : Float.MAX_VALUE;
        return Math.min(rx, ry);
    }
}
