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
import java.util.Random;
import java.util.Set;

import static exiledsector.ui.SkillTreePanelStyle.FONT_LINE_HEIGHT_FACTOR;
import static exiledsector.ui.SkillTreePanelStyle.GLOW_COLOR;
import static exiledsector.ui.SkillTreePanelStyle.TOOLTIP_BODY_COLOR;
import static exiledsector.ui.SkillTreePanelStyle.TOOLTIP_BODY_FONT_SIZE;
import static exiledsector.ui.SkillTreePanelStyle.TOOLTIP_TITLE_FONT_SIZE;

final class SkillTreeNodeRenderer {

    private static final float NODE_SIZE = 64f;

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

    private static final String[] RING_STACK_TEXTURES = {
            "graphics/fx/wormhole_ring_bright2.png",
            "graphics/fx/wormhole_ring_bright3.png"
    };

    private static final float RING_INSTANCE_MIN_ROTATION_SPEED_DEG = 20f;
    private static final float RING_INSTANCE_MAX_ROTATION_SPEED_DEG = 60f;
    private static final float RING_INSTANCE_JITTER_RATIO = 0.05f;
    private static final float RING_INSTANCE_BASE_ALPHA = 0.5f;
    private static final float RING_MIN_RADIUS_FRACTION = 0.62f;
    private static final float UNALLOCATED_ALPHA_MULT = 0.45f;

    private static final Color RING_PINK_COLOR = new Color(255, 60, 220);
    private static final float RING_PINK_SCALE_RATIO = 0.85f;

    private static final String GLOW_TEXTURE_PATH = "graphics/fx/star_halo.png";
    private static final Color AMBIENT_GLOW_COLOR = new Color(255, 170, 255);
    private static final float AMBIENT_GLOW_ALPHA = 1f;
    private static final float AMBIENT_GLOW_SIZE_RATIO = 3.2f;

    private static final float NOTABLE_RING_OUTER_RADIUS_RATIO = 1.05f;
    private static final float NOTABLE_RING_RADIUS_DECAY = 0.88f;
    private static final int NOTABLE_RING_COUNT = 10;

    private static final String DEFAULT_KEYSTONE_RING_BELT_PATH = "graphics/planets/ring_band_asteroids.png";
    private static final float KEYSTONE_BELT_WIDTH_RATIO = 1.1f;

    private static final String AURORA_TEXTURE_PATH = "graphics/planets/aurorae.png";
    private static final Color DEFAULT_AURORA_COLOR = new Color(140, 120, 255);

    private static final float ICON_INSET_RATIO = 0.9f;
    private static final float ROOT_CONNECTOR_OVERLAP_RATIO = 0.7f;

    private static final float PULSE_DURATION = 0.5f;
    private static final float PULSE_START_RADIUS_FRACTION = 1f;
    private static final float PULSE_END_RADIUS_FRACTION = 2.2f;

    private static final float BREATHING_PERIOD_SECONDS = 2.2f;
    private static final float BREATHING_MIN_ALPHA = 0.35f;
    private static final float BREATHING_MAX_ALPHA = 1f;

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
    private final Map<String, List<RingInstance>> ringStacks = new HashMap<>();
    private final Map<String, List<RingInstance>> pinkRingStacks = new HashMap<>();
    private SkillNode openDropdownNode;
    private float breathingPhase = 0f;
    private float ringElapsedSeconds = 0f;

    SkillTreeNodeRenderer(FleetMemberAPI member, SkillTreePanelStyle style, BaseRefitButton refitButton) {
        this.member = member;
        this.style = style;
        this.refitButton = refitButton;
        this.activeRoot = findRootNode(ShipTechLevel.of(member).rootTypeId());

        if (activeRoot != null) {
            ShipSkillData data = ShipSkillDataManager.get(member.getId());
            if (!data.isAllocated(activeRoot.getId())) {
                data.allocate(activeRoot);
            }
        }
    }

    private String satisfiedRootId() {
        return activeRoot == null ? null : activeRoot.getId();
    }

    private boolean isStartingRoot(SkillNode node) {
        return activeRoot != null && node.getType().getTier() == SkillTier.ROOT && node.getId().equals(activeRoot.getId());
    }

    void advance(float amount) {
        breathingPhase = (breathingPhase + amount) % BREATHING_PERIOD_SECONDS;
        ringElapsedSeconds += amount;
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
        String satisfiedRootId = satisfiedRootId();

        for (SkillNode node : SkillTree.getAllNodes().values()) {
            SkillTier tier = node.getType().getTier();
            if (tier == SkillTier.ROOT) continue;

            float nodeX = centerX + node.getOffsetX() * zoom;
            float nodeY = centerY - node.getOffsetY() * zoom;
            boolean allocated = data.isAllocated(node.getId());
            boolean breathing = !allocated && data.canAllocate(node, satisfiedRootId);
            SkillType effectiveType = node.resolveEffectiveType(data);
            float footprintSize = NODE_SIZE * zoom * tier.getSizeMultiplier();
            float iconSize = footprintSize * ICON_INSET_RATIO;

            String ringBeltPath = tier == SkillTier.KEYSTONE ? resolveRingBeltPath(node) : null;
            Color ringBeltColor = tier == SkillTier.KEYSTONE ? resolveRingBeltColor(node) : null;
            float ringBeltWidth = tier == SkillTier.KEYSTONE ? resolveRingBeltWidth(node) : 0f;
            drawRings(nodeX, nodeY, footprintSize, alphaMult, allocated, breathing, pulseElapsed.get(node.getId()), tier, zoom, node.getId(), ringBeltPath, ringBeltColor, ringBeltWidth);

            Color tint = allocated ? ALLOCATED_TINT : UNALLOCATED_TINT;
            if (effectiveType.isOptional()) {
                drawSplitIcon(optionTypesOf(effectiveType), nodeX, nodeY, iconSize, alphaMult, tint);
            } else {
                drawIcon(effectiveType.getIconPath(), nodeX, nodeY, iconSize, alphaMult, tint);
            }
        }

        drawNodeConnectors(centerX, centerY, zoom, data, alphaMult);

        for (SkillNode node : SkillTree.getAllNodes().values()) {
            if (node.getType().getTier() != SkillTier.ROOT) continue;

            float nodeX = centerX + node.getOffsetX() * zoom;
            float nodeY = centerY - node.getOffsetY() * zoom;
            boolean isActiveRoot = activeRoot != null && node.getId().equals(activeRoot.getId());
            boolean allocated = data.isAllocated(node.getId());
            boolean breathing = !allocated && data.canAllocate(node, satisfiedRootId);
            float footprintSize = NODE_SIZE * zoom * SkillTier.ROOT.getSizeMultiplier();
            drawRings(nodeX, nodeY, footprintSize, alphaMult, allocated, breathing, pulseElapsed.get(node.getId()), SkillTier.ROOT, zoom, node.getId(), null, null, 0f);

            Color tint = allocated ? ALLOCATED_TINT : UNALLOCATED_TINT;
            String iconPath = isActiveRoot ? RootCrestResolver.resolve(member) : node.getType().getIconPath();
            drawIcon(iconPath, nodeX, nodeY, footprintSize, alphaMult, tint);
        }

        renderDropdown(centerX, centerY, zoom, mouseX, mouseY, mouseKnown, alphaMult);
    }

    private static String resolveRingBeltPath(SkillNode node) {
        String path = node.getRingBeltPath();
        return path != null && !path.isEmpty() ? path : DEFAULT_KEYSTONE_RING_BELT_PATH;
    }

    private static Color resolveRingBeltColor(SkillNode node) {
        String hex = node.getRingBeltColor();
        if (hex == null || hex.isEmpty()) return DEFAULT_AURORA_COLOR;
        try {
            String cleaned = hex.startsWith("#") ? hex.substring(1) : hex;
            if (cleaned.length() == 6) cleaned = "FF" + cleaned;
            long argb = Long.parseLong(cleaned, 16);
            return new Color((int) argb, true);
        } catch (NumberFormatException e) {
            Logger.getLogger(SkillTreeNodeRenderer.class).warn("Invalid ringBeltColor \"" + hex + "\" on node \"" + node.getId() + "\", using default");
            return DEFAULT_AURORA_COLOR;
        }
    }

    private static float resolveRingBeltWidth(SkillNode node) {
        Float width = node.getRingBeltWidth();
        return width != null && width > 0f ? width : KEYSTONE_BELT_WIDTH_RATIO;
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

        if (wasAllocated && isStartingRoot(node)) {
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

    private void drawRings(float cx, float cy, float footprintSize, float alphaMult, boolean allocated, boolean breathing, Float pulseSeconds, SkillTier tier, float zoom, String nodeId, String ringBeltPath, Color ringBeltColor, float ringBeltWidth) {
        float half = footprintSize / 2f;
        float scale = tier.getSizeMultiplier();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        float ringRadius = donutRadius(footprintSize);
        if (tier == SkillTier.NOTABLE) {
            float stateAlpha = allocated ? 1f : UNALLOCATED_ALPHA_MULT;
            drawRingStack(cx, cy, footprintSize * NOTABLE_RING_OUTER_RADIUS_RATIO, ringRadius, nodeId,
                    NOTABLE_RING_COUNT, NOTABLE_RING_RADIUS_DECAY, stateAlpha, alphaMult);
            drawAmbientGlow(cx, cy, footprintSize, stateAlpha, alphaMult);
        } else if (tier == SkillTier.KEYSTONE) {
            float stateAlpha = allocated ? 1f : UNALLOCATED_ALPHA_MULT;
            if (AURORA_TEXTURE_PATH.equals(ringBeltPath)) {
                drawKeystoneAuroraBelt(cx, cy, footprintSize, ringBeltWidth, ringBeltColor, stateAlpha, alphaMult);
            } else {
                drawKeystoneRingBelt(cx, cy, footprintSize, ringBeltWidth, ringBeltPath, stateAlpha, alphaMult);
            }
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (tier == SkillTier.NOTABLE || tier == SkillTier.KEYSTONE) {
            float iconRadius = footprintSize * ICON_INSET_RATIO / 2f;
            drawSingleDonut(cx, cy, iconRadius, allocated, breathing, zoom, alphaMult);
        } else {
            drawNodeDonut(cx, cy, ringRadius, scale, zoom, allocated, alphaMult);

            if (breathing) {
                float breathingT = (float) (0.5 + 0.5 * Math.sin(2 * Math.PI * breathingPhase / BREATHING_PERIOD_SECONDS));
                float breathingAlpha = (BREATHING_MIN_ALPHA + (BREATHING_MAX_ALPHA - BREATHING_MIN_ALPHA) * breathingT) * alphaMult;
                GL11.glLineWidth(NODE_CONNECTOR_GLOW_LINE_THICKNESS * scale * zoom);
                drawRingOutline(cx, cy, ringRadius, style.getAccentColor(), breathingAlpha);
            }
        }

        if (pulseSeconds != null) {
            GL11.glLineWidth(RING_LINE_THICKNESS * zoom);
            float progress = pulseSeconds / PULSE_DURATION;
            float radiusFraction = PULSE_START_RADIUS_FRACTION + (PULSE_END_RADIUS_FRACTION - PULSE_START_RADIUS_FRACTION) * progress;
            drawRingOutline(cx, cy, half * radiusFraction, style.getAccentColor(), (1f - progress) * alphaMult);
        }

        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawNodeDonut(float cx, float cy, float radius, float scale, float zoom, boolean allocated, float alphaMult) {
        if (allocated) {
            GL11.glLineWidth(NODE_CONNECTOR_GLOW_HALO_THICKNESS * scale * zoom);
            drawRingOutline(cx, cy, radius, style.getAccentColor(), alphaMult * NODE_CONNECTOR_GLOW_HALO_ALPHA);
            GL11.glLineWidth(NODE_CONNECTOR_GLOW_LINE_THICKNESS * scale * zoom);
            drawRingOutline(cx, cy, radius, style.getAccentColor(), alphaMult);
            return;
        }

        float gapRadius = donutGapRadius(scale, zoom);
        GL11.glLineWidth(NODE_CONNECTOR_LINE_THICKNESS * scale * zoom);
        drawRingOutline(cx, cy, radius - gapRadius, RING_DULL_COLOR, alphaMult * RING_DULL_ALPHA);
        drawRingOutline(cx, cy, radius + gapRadius, RING_DULL_COLOR, alphaMult * RING_DULL_ALPHA);
    }

    private void drawSingleDonut(float cx, float cy, float radius, boolean allocated, boolean breathing, float zoom, float alphaMult) {
        Color color;
        float alpha;
        if (allocated) {
            color = style.getAccentColor();
            alpha = alphaMult;
        } else if (breathing) {
            float breathingT = (float) (0.5 + 0.5 * Math.sin(2 * Math.PI * breathingPhase / BREATHING_PERIOD_SECONDS));
            color = style.getAccentColor();
            alpha = (BREATHING_MIN_ALPHA + (BREATHING_MAX_ALPHA - BREATHING_MIN_ALPHA) * breathingT) * alphaMult;
        } else {
            color = RING_DULL_COLOR;
            alpha = alphaMult * RING_DULL_ALPHA;
        }

        GL11.glLineWidth(NODE_CONNECTOR_GLOW_LINE_THICKNESS * zoom);
        drawRingOutline(cx, cy, radius, color, alpha);
    }

    private void drawRingStack(float cx, float cy, float outerRadius, float donutRadiusValue, String nodeId,
                                int count, float radiusDecay, float stateAlpha, float alphaMult) {
        drawRingStackPass(cx, cy, outerRadius, ringStacks.computeIfAbsent(nodeId, id -> generateRingInstances(id, count, radiusDecay)),
                Color.WHITE, 1f, stateAlpha, alphaMult);
        drawRingStackPass(cx, cy, outerRadius, pinkRingStacks.computeIfAbsent(nodeId + "_pink", id -> generateRingInstances(id, count, radiusDecay)),
                RING_PINK_COLOR, RING_PINK_SCALE_RATIO, stateAlpha, alphaMult);
    }

    private void drawRingStackPass(float cx, float cy, float outerRadius, List<RingInstance> instances,
                                    Color color, float scaleRatio, float stateAlpha, float alphaMult) {
        float alpha = RING_INSTANCE_BASE_ALPHA * stateAlpha * alphaMult;

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        for (RingInstance instance : instances) {
            String path = RING_STACK_TEXTURES[instance.textureIndex];
            if (!ensureTextureLoaded(path)) continue;

            float radius = outerRadius * scaleRatio * instance.radiusFraction;
            float jitterMag = radius * RING_INSTANCE_JITTER_RATIO;

            float angle = instance.baseAngleDeg + ringElapsedSeconds * instance.rotationSpeedDeg;
            float wanderRad = instance.jitterPhase + ringElapsedSeconds * instance.jitterSpeed;
            float jx = (float) Math.cos(wanderRad) * jitterMag;
            float jy = (float) Math.sin(wanderRad) * jitterMag;
            float size = radius * 2f * instance.sizeJitter;

            SpriteAPI sprite = Global.getSettings().getSprite(path);
            sprite.setSize(size, size);
            sprite.setAngle(angle);
            sprite.setColor(color);
            sprite.setAlphaMult(alpha);
            sprite.renderAtCenter(cx + jx, cy + jy);
        }
    }

    private void drawAmbientGlow(float cx, float cy, float footprintSize, float stateAlpha, float alphaMult) {
        if (!ensureTextureLoaded(GLOW_TEXTURE_PATH)) return;

        float size = footprintSize * AMBIENT_GLOW_SIZE_RATIO;

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        SpriteAPI sprite = Global.getSettings().getSprite(GLOW_TEXTURE_PATH);
        sprite.setSize(size, size);
        sprite.setColor(AMBIENT_GLOW_COLOR);
        sprite.setAlphaMult(AMBIENT_GLOW_ALPHA * stateAlpha * alphaMult);
        sprite.renderAtCenter(cx, cy);
    }

    private void drawKeystoneRingBelt(float cx, float cy, float footprintSize, float widthRatio, String ringArtPath, float stateAlpha, float alphaMult) {
        if (!ensureTextureLoaded(ringArtPath)) return;
        SpriteAPI sprite = Global.getSettings().getSprite(ringArtPath);
        RingBeltRenderer.render(sprite, cx, cy, beltInnerRadius(footprintSize), beltOuterRadius(footprintSize, widthRatio),
                Color.WHITE, stateAlpha * alphaMult);
    }

    private void drawKeystoneAuroraBelt(float cx, float cy, float footprintSize, float widthRatio, Color tint, float stateAlpha, float alphaMult) {
        if (!ensureTextureLoaded(AURORA_TEXTURE_PATH)) return;
        SpriteAPI sprite = Global.getSettings().getSprite(AURORA_TEXTURE_PATH);
        AuroraBeltRenderer.render(sprite, cx, cy, beltInnerRadius(footprintSize), beltOuterRadius(footprintSize, widthRatio),
                tint, stateAlpha * alphaMult, ringElapsedSeconds);
    }

    private static float beltInnerRadius(float footprintSize) {
        return footprintSize * ICON_INSET_RATIO / 2f;
    }

    private static float beltOuterRadius(float footprintSize, float widthRatio) {
        return beltInnerRadius(footprintSize) + footprintSize * widthRatio;
    }

    private static List<RingInstance> generateRingInstances(String seedKey, int count, float radiusDecay) {
        Random random = new Random(seedKey.hashCode());
        List<RingInstance> instances = new ArrayList<>(count);
        float rawMin = (float) Math.pow(radiusDecay, count - 1);
        float rawRange = 1f - rawMin;
        for (int i = 0; i < count; i++) {
            RingInstance instance = new RingInstance();
            instance.baseAngleDeg = random.nextFloat() * 360f;
            float speed = RING_INSTANCE_MIN_ROTATION_SPEED_DEG
                    + random.nextFloat() * (RING_INSTANCE_MAX_ROTATION_SPEED_DEG - RING_INSTANCE_MIN_ROTATION_SPEED_DEG);
            instance.rotationSpeedDeg = random.nextBoolean() ? speed : -speed;
            float raw = (float) Math.pow(radiusDecay, i);
            float t = rawRange > 0.0001f ? (raw - rawMin) / rawRange : 1f;
            instance.radiusFraction = RING_MIN_RADIUS_FRACTION + (1f - RING_MIN_RADIUS_FRACTION) * t;
            instance.jitterPhase = random.nextFloat() * (float) (Math.PI * 2);
            instance.jitterSpeed = 0.5f + random.nextFloat();
            instance.textureIndex = i % RING_STACK_TEXTURES.length;
            instance.sizeJitter = 0.9f + random.nextFloat() * 0.2f;
            instances.add(instance);
        }
        return instances;
    }

    private static final class RingInstance {
        float baseAngleDeg;
        float rotationSpeedDeg;
        float radiusFraction;
        float jitterPhase;
        float jitterSpeed;
        int textureIndex;
        float sizeJitter;
    }

    private static float donutGapRadius(float scale, float zoom) {
        return (NODE_CONNECTOR_PARALLEL_GAP * scale * zoom) / 2f;
    }

    private static float donutRadius(float footprintSize) {
        return footprintSize / 2f;
    }

    private static float donutOuterRadius(float footprintSize, SkillTier tier, float zoom) {
        return donutRadius(footprintSize) + donutGapRadius(tier.getSizeMultiplier(), zoom);
    }

    private static float connectorEndpointRadius(SkillTier tier, float footprintSize, float zoom) {
        if (tier == SkillTier.ROOT) {
            return donutRadius(footprintSize) * ROOT_CONNECTOR_OVERLAP_RATIO;
        }
        return donutOuterRadius(footprintSize, tier, zoom);
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

    private void drawNodeConnectors(float centerX, float centerY, float zoom, ShipSkillData data, float alphaMult) {
        String satisfiedRootId = satisfiedRootId();

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
}
