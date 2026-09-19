package exiledsector.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;
import exiledsector.effects.SkillTreeHullMod;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.ConnectorCurve;
import exiledsector.skills.HullSizeSkillEffect;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.ShipTechLevel;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import exiledsector.skills.SkillTypeEffect;
import lunalib.lunaRefit.BaseRefitButton;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
    private static final float VIGNETTE_MARGIN_FRACTION = 0.02f;
    private static final String CIRCULAR_ICON_PATH_PREFIX = "graphics/icons/circular/";

    private static final float TOOLTIP_MAX_TEXT_WIDTH = 480f;
    private static final float TOOLTIP_MAX_TEXT_HEIGHT = 800f;
    private static final float TOOLTIP_WIDTH_SAFETY_MARGIN = 8f;
    private static final float TOOLTIP_PADDING = 10f;
    private static final float TOOLTIP_TITLE_BODY_GAP = 6f;
    private static final float TOOLTIP_CURSOR_OFFSET = 18f;
    private static final float TOOLTIP_TITLE_BOLD_OFFSET = 1f;
    private static final Color TOOLTIP_TITLE_COLOR = Color.WHITE;

    private static final Color ALLOCATED_TINT = Color.WHITE;
    private static final Color UNALLOCATED_TINT = new Color(90, 90, 90);

    private static final int RING_SEGMENTS = 32;
    private static final float RING_LINE_THICKNESS = 1.5f;
    private static final Color RING_DULL_COLOR = new Color(150, 150, 150);
    private static final float RING_DULL_ALPHA = 0.5f;

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
    private static final int CURVE_ARC_SAMPLES = 40;
    private static final int CURVE_RENDER_SEGMENTS = 20;

    private static final float PIE_START_ANGLE_DEGREES = -90f;
    private static final float PIE_WEDGE_SEGMENT_DEGREES = 6f;

    private static final String OPTIONAL_NODE_HINT = "Click to choose an option.";
    private static final float DROPDOWN_FONT_SIZE = TOOLTIP_BODY_FONT_SIZE;
    private static final float DROPDOWN_ROW_PADDING = 8f;
    private static final float DROPDOWN_ROW_GAP = 2f;
    private static final float DROPDOWN_TOP_OFFSET = 24f;
    private static final float DROPDOWN_HOVER_ALPHA = 0.35f;

    private final String symbolPath;
    private final FleetMemberAPI member;
    private final SkillTreePanelStyle style;
    private final BaseRefitButton refitButton;
    private final SkillNode activeRoot;
    private final Set<String> loadedSprites = new HashSet<>();
    private final Map<String, SkillTreePanelStyle.TooltipText> tooltipTitles = new HashMap<>();
    private final Map<String, SkillTreePanelStyle.TooltipText> tooltipBodies = new HashMap<>();
    private final Map<String, SkillTreePanelStyle.TooltipText> typeTooltipTitles = new HashMap<>();
    private final Map<String, SkillTreePanelStyle.TooltipText> typeTooltipBodies = new HashMap<>();
    private final Map<String, LazyFont.DrawableString> dropdownRowText = new HashMap<>();
    private final Map<String, Float> pulseElapsed = new HashMap<>();
    private SkillNode openDropdownNode;

    SkillTreeNodeRenderer(String symbolPath, FleetMemberAPI member, SkillTreePanelStyle style, BaseRefitButton refitButton) {
        this.symbolPath = symbolPath;
        this.member = member;
        this.style = style;
        this.refitButton = refitButton;
        this.activeRoot = findRootNode(rootTypeId(ShipTechLevel.of(member)));
    }

    private String satisfiedRootId() {
        return activeRoot == null ? null : activeRoot.getId();
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

    void render(float centerX, float centerY, float zoom, float alphaMult, float mouseX, float mouseY, boolean mouseKnown) {
        ShipSkillData data = ShipSkillDataManager.get(member.getId());

        float shipX = centerX;
        float shipY = centerY;
        if (activeRoot != null) {
            shipX = centerX + activeRoot.getOffsetX() * zoom;
            shipY = centerY - activeRoot.getOffsetY() * zoom;
        }
        drawIcon(symbolPath, shipX, shipY, SYMBOL_SIZE * zoom, alphaMult, ALLOCATED_TINT);
        drawCenterRing(shipX, shipY, SYMBOL_SIZE * zoom, alphaMult);

        for (SkillNode node : SkillTree.getAllNodes().values()) {
            if (node.getType().getTier() == SkillTier.ROOT) continue;

            float nodeX = centerX + node.getOffsetX() * zoom;
            float nodeY = centerY - node.getOffsetY() * zoom;
            boolean allocated = data.isAllocated(node.getId());
            SkillTier tier = node.getType().getTier();
            SkillType effectiveType = node.resolveEffectiveType(data);
            float footprintSize = NODE_SIZE * zoom * tier.getSizeMultiplier();
            float iconSize = footprintSize * iconSizeMultiplier(tier);

            Color tint = allocated ? ALLOCATED_TINT : UNALLOCATED_TINT;
            if (effectiveType.isOptional()) {
                drawSplitIcon(optionTypesOf(effectiveType), nodeX, nodeY, iconSize, alphaMult, tint);
            } else {
                drawIcon(effectiveType.getIconPath(), nodeX, nodeY, iconSize, alphaMult, tint);
            }
            drawRings(nodeX, nodeY, footprintSize, alphaMult, allocated, pulseElapsed.get(node.getId()), tier);
        }

        drawNodeConnectors(centerX, centerY, zoom, data, alphaMult);
        renderDropdown(centerX, centerY, zoom, mouseX, mouseY, mouseKnown, alphaMult);
    }

    private static String rootTypeId(ShipTechLevel techLevel) {
        switch (techLevel) {
            case LOW_TECH: return "root_low_tech";
            case HIGH_TECH: return "root_high_tech";
            default: return "root_midline";
        }
    }

    private static SkillNode findRootNode(String rootTypeId) {
        for (SkillNode node : SkillTree.getAllNodes().values()) {
            if (node.getType().getTier() == SkillTier.ROOT && node.getType().getId().equals(rootTypeId)) {
                return node;
            }
        }
        return null;
    }

    void renderHoverTooltip(float centerX, float centerY, float zoom, float mouseX, float mouseY, float alphaMult) {
        if (openDropdownNode != null) {
            LazyFont font = style.getFont();
            if (font == null) return;
            for (DropdownRow row : computeDropdownRows(centerX, centerY, zoom, font)) {
                if (row.contains(mouseX, mouseY)) {
                    renderTooltipForType(row.option, mouseX, mouseY, alphaMult);
                    return;
                }
            }
            return;
        }

        SkillNode hovered = findNodeAt(centerX, centerY, zoom, mouseX, mouseY);
        if (hovered != null) {
            renderTooltip(hovered, mouseX, mouseY, alphaMult);
        }
    }

    SkillNode findNodeAt(float centerX, float centerY, float zoom, float x, float y) {
        for (SkillNode node : SkillTree.getAllNodes().values()) {
            if (node.getType().getTier() == SkillTier.ROOT) continue;

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

        if (!wasAllocated && node.getType().isOptional()) {
            if (data.canAllocate(node, satisfiedRootId())) {
                openDropdownNode = node;
            }
            return;
        }

        if (wasAllocated && blockDeallocationReason(node) != null) {
            return;
        }
        data.toggle(node, SkillTree.getAllNodes().values(), satisfiedRootId());
        boolean isAllocatedNow = data.isAllocated(node.getId());
        if (isAllocatedNow != wasAllocated) {
            refreshAfterAllocationChange(node, isAllocatedNow);
        }
    }

    boolean isDropdownOpen() {
        return openDropdownNode != null;
    }

    void closeDropdown() {
        openDropdownNode = null;
    }

    SkillType findDropdownOptionAt(float centerX, float centerY, float zoom, float x, float y) {
        LazyFont font = style.getFont();
        if (font == null) return null;
        for (DropdownRow row : computeDropdownRows(centerX, centerY, zoom, font)) {
            if (row.contains(x, y)) {
                return row.option;
            }
        }
        return null;
    }

    void commitDropdownSelection(SkillType chosenOption) {
        SkillNode node = openDropdownNode;
        openDropdownNode = null;
        if (node == null) return;

        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        data.selectOption(node, chosenOption);
        refreshAfterAllocationChange(node, true);
    }

    private void refreshAfterAllocationChange(SkillNode node, boolean isAllocatedNow) {
        tooltipTitles.remove(node.getId());
        tooltipBodies.remove(node.getId());
        member.setStatUpdateNeeded(true);
        member.updateStats();
        new SkillTreeHullMod().applyEffectsBeforeShipCreation(member.getHullSpec().getHullSize(), member.getStats(), SkillTreeHullMod.ID);
        if (refitButton != null) {
            refitButton.refreshVariant();
        }
        if (isAllocatedNow) {
            pulseElapsed.put(node.getId(), 0f);
        }
    }

    private String blockDeallocationReason(SkillNode node) {
        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        SkillType type = node.resolveEffectiveType(data);
        for (SkillTypeEffect effect : type.getEffects()) {
            String reason = effect.effect().blockDeallocationReason(member, effect.magnitude());
            if (reason != null) return reason;
        }
        for (HullSizeSkillEffect effect : type.getHullSizeEffects()) {
            float magnitude = effect.valueFor(member.getHullSpec().getHullSize());
            String reason = effect.effect().blockDeallocationReason(member, magnitude);
            if (reason != null) return reason;
        }
        return null;
    }

    private List<DropdownRow> computeDropdownRows(float centerX, float centerY, float zoom, LazyFont font) {
        List<DropdownRow> rows = new ArrayList<>();
        if (openDropdownNode == null) return rows;

        List<SkillType> options = new ArrayList<>();
        for (String optionId : openDropdownNode.getType().getOptionalOptionIds()) {
            SkillType option = SkillTree.getType(optionId);
            if (option != null) options.add(option);
        }
        if (options.isEmpty()) return rows;

        float nodeX = centerX + openDropdownNode.getOffsetX() * zoom;
        float nodeY = centerY - openDropdownNode.getOffsetY() * zoom;

        float width = 0f;
        for (SkillType option : options) {
            width = Math.max(width, font.calcWidth(option.getDisplayName(), DROPDOWN_FONT_SIZE));
        }
        width += DROPDOWN_ROW_PADDING * 2f;

        float rowHeight = DROPDOWN_FONT_SIZE * FONT_LINE_HEIGHT_FACTOR + DROPDOWN_ROW_PADDING * 2f;
        float x = nodeX - width / 2f;
        float topY = nodeY - DROPDOWN_TOP_OFFSET;

        for (int i = 0; i < options.size(); i++) {
            float rowTop = topY - i * (rowHeight + DROPDOWN_ROW_GAP);
            rows.add(new DropdownRow(options.get(i), x, rowTop - rowHeight, width, rowHeight));
        }
        return rows;
    }

    private void renderDropdown(float centerX, float centerY, float zoom, float mouseX, float mouseY, boolean mouseKnown, float alphaMult) {
        if (openDropdownNode == null) return;
        LazyFont font = style.getFont();
        if (font == null) return;

        List<DropdownRow> rows = computeDropdownRows(centerX, centerY, zoom, font);
        if (rows.isEmpty()) return;

        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (DropdownRow row : rows) {
            minX = Math.min(minX, row.x);
            minY = Math.min(minY, row.y);
            maxX = Math.max(maxX, row.x + row.width);
            maxY = Math.max(maxY, row.y + row.height);
        }
        style.drawTooltipBackground(minX, minY, maxX - minX, maxY - minY, alphaMult, style.getAccentColor());

        for (DropdownRow row : rows) {
            if (mouseKnown && row.contains(mouseX, mouseY)) {
                drawDropdownRowHighlight(row, alphaMult);
            }
            LazyFont.DrawableString text = getDropdownRowText(font, row.option);
            float textY = row.y + row.height / 2f + (DROPDOWN_FONT_SIZE * FONT_LINE_HEIGHT_FACTOR) / 2f;
            text.draw(row.x + DROPDOWN_ROW_PADDING, textY);
        }
    }

    private void drawDropdownRowHighlight(DropdownRow row, float alphaMult) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Misc.setColor(GLOW_COLOR, DROPDOWN_HOVER_ALPHA * alphaMult);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(row.x, row.y);
        GL11.glVertex2f(row.x + row.width, row.y);
        GL11.glVertex2f(row.x + row.width, row.y + row.height);
        GL11.glVertex2f(row.x, row.y + row.height);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_BLEND);
    }

    private LazyFont.DrawableString getDropdownRowText(LazyFont font, SkillType option) {
        return dropdownRowText.computeIfAbsent(option.getId(), id -> {
            LazyFont.DrawableString text = font.createText(option.getDisplayName(), TOOLTIP_BODY_COLOR, DROPDOWN_FONT_SIZE);
            text.setAnchor(LazyFont.TextAnchor.TOP_LEFT);
            text.setAlignment(LazyFont.TextAlignment.LEFT);
            return text;
        });
    }

    private static final class DropdownRow {
        final SkillType option;
        final float x;
        final float y;
        final float width;
        final float height;

        DropdownRow(SkillType option, float x, float y, float width, float height) {
            this.option = option;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        boolean contains(float px, float py) {
            return px >= x && px <= x + width && py >= y && py <= y + height;
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

        if (!spritePath.startsWith(CIRCULAR_ICON_PATH_PREFIX)) {
            drawVignette(cx, cy, size, alphaMult);
        }
    }

    private List<SkillType> optionTypesOf(SkillType optionalType) {
        List<SkillType> options = new ArrayList<>();
        for (String optionId : optionalType.getOptionalOptionIds()) {
            SkillType option = SkillTree.getType(optionId);
            if (option != null) options.add(option);
        }
        return options;
    }

    private void drawSplitIcon(List<SkillType> options, float cx, float cy, float size, float alphaMult, Color tint) {
        if (options.isEmpty()) return;
        if (options.size() == 1) {
            drawIcon(options.get(0).getIconPath(), cx, cy, size, alphaMult, tint);
            return;
        }

        float radius = size / 2f;
        float sweep = 360f / options.size();

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        for (int i = 0; i < options.size(); i++) {
            float startAngle = PIE_START_ANGLE_DEGREES + sweep * i;
            float endAngle = startAngle + sweep;

            maskPieWedge(cx, cy, radius, startAngle, endAngle, 1);
            GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            drawIcon(options.get(i).getIconPath(), cx, cy, size, alphaMult, tint);
            maskPieWedge(cx, cy, radius, startAngle, endAngle, 0);
        }
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    private void maskPieWedge(float cx, float cy, float radius, float startDeg, float endDeg, int stencilValue) {
        GL11.glColorMask(false, false, false, false);
        GL11.glStencilFunc(GL11.GL_ALWAYS, stencilValue, 0xFF);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);

        int segments = Math.max(1, (int) Math.ceil((endDeg - startDeg) / PIE_WEDGE_SEGMENT_DEGREES));
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(cx, cy);
        for (int i = 0; i <= segments; i++) {
            float deg = startDeg + (endDeg - startDeg) * i / segments;
            float rad = (float) Math.toRadians(deg);
            GL11.glVertex2f(cx + (float) Math.cos(rad) * radius, cy + (float) Math.sin(rad) * radius);
        }
        GL11.glEnd();

        GL11.glColorMask(true, true, true, true);
    }

    private void drawRings(float cx, float cy, float footprintSize, float alphaMult, boolean allocated, Float pulseSeconds, SkillTier tier) {
        float half = footprintSize / 2f;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        drawNodeDonut(cx, cy, donutRadius(footprintSize, tier), tier.getSizeMultiplier(), allocated, alphaMult);

        if (pulseSeconds != null) {
            GL11.glLineWidth(RING_LINE_THICKNESS);
            float progress = pulseSeconds / PULSE_DURATION;
            float radiusFraction = PULSE_START_RADIUS_FRACTION + (PULSE_END_RADIUS_FRACTION - PULSE_START_RADIUS_FRACTION) * progress;
            drawRingOutline(cx, cy, half * radiusFraction, GLOW_COLOR, (1f - progress) * alphaMult);
        }

        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawNodeDonut(float cx, float cy, float radius, float scale, boolean allocated, float alphaMult) {
        if (allocated) {
            GL11.glLineWidth(NODE_CONNECTOR_GLOW_HALO_THICKNESS * scale);
            drawRingOutline(cx, cy, radius, GLOW_COLOR, alphaMult * NODE_CONNECTOR_GLOW_HALO_ALPHA);
            GL11.glLineWidth(NODE_CONNECTOR_GLOW_LINE_THICKNESS * scale);
            drawRingOutline(cx, cy, radius, GLOW_COLOR, alphaMult);
            return;
        }

        float gapRadius = donutGapRadius(scale);
        GL11.glLineWidth(NODE_CONNECTOR_LINE_THICKNESS * scale);
        drawRingOutline(cx, cy, radius - gapRadius, RING_DULL_COLOR, alphaMult * RING_DULL_ALPHA);
        drawRingOutline(cx, cy, radius + gapRadius, RING_DULL_COLOR, alphaMult * RING_DULL_ALPHA);
    }

    private static float donutGapRadius(float scale) {
        return (NODE_CONNECTOR_PARALLEL_GAP * scale) / 2f;
    }

    private static float donutRadius(float footprintSize, SkillTier tier) {
        return (footprintSize / 2f) * iconSizeMultiplier(tier);
    }

    private static float donutOuterRadius(float footprintSize, SkillTier tier) {
        return donutRadius(footprintSize, tier) + donutGapRadius(tier.getSizeMultiplier());
    }

    private static float iconSizeMultiplier(SkillTier tier) {
        switch (tier) {
            case KEYSTONE: return ICON_SIZE_MULTIPLIER_KEYSTONE / tier.getSizeMultiplier();
            case NOTABLE: return ICON_SIZE_MULTIPLIER_NOTABLE / tier.getSizeMultiplier();
            default: return 1f;
        }
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
        String satisfiedRootId = satisfiedRootId();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for (SkillNode node : SkillTree.getAllNodes().values()) {
            if (node.getType().getTier() == SkillTier.ROOT) continue;

            float nodeX = centerX + node.getOffsetX() * zoom;
            float nodeY = centerY - node.getOffsetY() * zoom;
            float nodeRadius = donutOuterRadius(NODE_SIZE * zoom * node.getType().getTier().getSizeMultiplier(), node.getType().getTier());

            for (String connectedId : node.getConnectedNodeIds()) {
                SkillNode other = SkillTree.get(connectedId);
                if (other == null) continue;
                if (other.getType().getTier() != SkillTier.ROOT && node.getId().compareTo(other.getId()) >= 0) continue;

                float otherX = centerX + other.getOffsetX() * zoom;
                float otherY = centerY - other.getOffsetY() * zoom;
                float otherRadius = donutOuterRadius(NODE_SIZE * zoom * other.getType().getTier().getSizeMultiplier(), other.getType().getTier());
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
            drawLine(x1, y1, x2, y2, GLOW_COLOR, alphaMult * NODE_CONNECTOR_GLOW_HALO_ALPHA, NODE_CONNECTOR_GLOW_HALO_THICKNESS);
            drawLine(x1, y1, x2, y2, GLOW_COLOR, alphaMult, NODE_CONNECTOR_GLOW_LINE_THICKNESS);
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

        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        SkillType effectiveType = node.resolveEffectiveType(data);
        boolean showOptionalHint = effectiveType == node.getType() && effectiveType.isOptional()
                && effectiveType.getDescriptionOverride() == null;

        SkillTreePanelStyle.TooltipText title = tooltipTitles.computeIfAbsent(node.getId(),
                id -> buildTooltipText(font, effectiveType.getDisplayName(), TOOLTIP_TITLE_FONT_SIZE, TOOLTIP_TITLE_COLOR));
        SkillTreePanelStyle.TooltipText body = tooltipBodies.computeIfAbsent(node.getId(),
                id -> buildTooltipText(font,
                        showOptionalHint ? OPTIONAL_NODE_HINT : SkillNode.describeType(effectiveType, member.getHullSpec().getHullSize()),
                        TOOLTIP_BODY_FONT_SIZE, TOOLTIP_BODY_COLOR));

        drawTooltipBox(title, body, mouseX, mouseY, alphaMult);
    }

    private void renderTooltipForType(SkillType type, float mouseX, float mouseY, float alphaMult) {
        LazyFont font = style.getFont();
        if (font == null) return;

        SkillTreePanelStyle.TooltipText title = typeTooltipTitles.computeIfAbsent(type.getId(),
                id -> buildTooltipText(font, type.getDisplayName(), TOOLTIP_TITLE_FONT_SIZE, TOOLTIP_TITLE_COLOR));
        SkillTreePanelStyle.TooltipText body = typeTooltipBodies.computeIfAbsent(type.getId(),
                id -> buildTooltipText(font, SkillNode.describeType(type, member.getHullSpec().getHullSize()), TOOLTIP_BODY_FONT_SIZE, TOOLTIP_BODY_COLOR));

        drawTooltipBox(title, body, mouseX, mouseY, alphaMult);
    }

    private void drawTooltipBox(SkillTreePanelStyle.TooltipText title, SkillTreePanelStyle.TooltipText body, float mouseX, float mouseY, float alphaMult) {
        float boxWidth = Math.max(title.width, body.width) + TOOLTIP_PADDING * 2f + TOOLTIP_WIDTH_SAFETY_MARGIN;
        float boxHeight = title.height + TOOLTIP_TITLE_BODY_GAP + body.height + TOOLTIP_PADDING * 2f;
        float boxX = mouseX + TOOLTIP_CURSOR_OFFSET;
        float boxY = mouseY - boxHeight - TOOLTIP_CURSOR_OFFSET;

        style.drawTooltipBackground(boxX, boxY, boxWidth, boxHeight, alphaMult, style.getAccentColor());

        float titleY = boxY + boxHeight - TOOLTIP_PADDING;
        float bodyY = titleY - title.height - TOOLTIP_TITLE_BODY_GAP;
        title.drawable.draw(boxX + TOOLTIP_PADDING, titleY);
        title.drawable.draw(boxX + TOOLTIP_PADDING + TOOLTIP_TITLE_BOLD_OFFSET, titleY);
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
