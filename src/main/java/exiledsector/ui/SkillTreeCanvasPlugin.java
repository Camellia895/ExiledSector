package exiledsector.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.util.Misc;
import exiledsector.effects.SkillTreeHullMod;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTree;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SkillTreeCanvasPlugin extends BaseCustomUIPanelPlugin {

    private static final float MIN_ZOOM = 0.4f;
    private static final float MAX_ZOOM = 2.5f;
    private static final float ZOOM_STEP = 1.1f;

    private static final float SYMBOL_SIZE = 128f;
    private static final float NODE_SIZE = 64f;

    private static final int VIGNETTE_SEGMENTS = 48;
    private static final float VIGNETTE_INNER_FRACTION = 0.88f;
    private static final float VIGNETTE_OUTER_FRACTION = 1f;
    private static final float VIGNETTE_MARGIN_FRACTION = 0.375f;

    private static final String TOOLTIP_FONT_PATH = "graphics/fonts/insignia15LTaa.fnt";
    private static final float TOOLTIP_TITLE_FONT_SIZE = 20f;
    private static final float TOOLTIP_BODY_FONT_SIZE = 20f;
    private static final float TOOLTIP_MAX_TEXT_WIDTH = 240f;
    private static final float TOOLTIP_MAX_TEXT_HEIGHT = 200f;
    private static final float TOOLTIP_WIDTH_SAFETY_MARGIN = 8f;
    private static final float FONT_LINE_HEIGHT_FACTOR = 1.25f;
    private static final float TOOLTIP_PADDING = 10f;
    private static final float TOOLTIP_TITLE_BODY_GAP = 6f;
    private static final float TOOLTIP_CURSOR_OFFSET = 18f;
    private static final Color TOOLTIP_TITLE_COLOR = Color.WHITE;
    private static final Color TOOLTIP_BODY_COLOR = new Color(230, 230, 230);
    private static final Color TOOLTIP_BACKGROUND_COLOR = Color.BLACK;
    private static final float TOOLTIP_BORDER_THICKNESS = 2f;
    private static final Color DEFAULT_TOOLTIP_BORDER_COLOR = Color.LIGHT_GRAY;
    private static final int COLOR_QUANTIZE_STEP = 24;
    private static final int MIN_ALPHA_TO_SAMPLE = 128;

    private static final Color ALLOCATED_TINT = Color.WHITE;
    private static final Color UNALLOCATED_TINT = new Color(90, 90, 90);

    private static final float[] RING_RADIUS_FRACTIONS = {1f, 1.25f};
    private static final float NODE_CONNECTOR_RADIUS_FRACTION = RING_RADIUS_FRACTIONS[RING_RADIUS_FRACTIONS.length - 1];
    private static final int RING_SEGMENTS = 32;
    private static final float RING_LINE_THICKNESS = 1.5f;
    private static final Color RING_DULL_COLOR = new Color(150, 150, 150);
    private static final float RING_DULL_ALPHA = 0.5f;
    private static final float RING_ALLOCATED_ALPHA = 0.9f;

    private static final Color GLOW_COLOR = new Color(120, 200, 255);

    private static final float PULSE_DURATION = 0.5f;
    private static final float PULSE_START_RADIUS_FRACTION = 1f;
    private static final float PULSE_END_RADIUS_FRACTION = 2.2f;

    private static final float CENTER_RING_RADIUS_FRACTION = 1f;

    private static final float NODE_CONNECTOR_PARALLEL_GAP = 4f;
    private static final float NODE_CONNECTOR_LINE_THICKNESS = 1.5f;
    private static final float NODE_CONNECTOR_GLOW_LINE_THICKNESS = 2.5f;
    private static final float NODE_CONNECTOR_GLOW_HALO_THICKNESS = 7f;
    private static final float NODE_CONNECTOR_GLOW_HALO_ALPHA = 0.35f;

    private static final float STAT_PANEL_FONT_SIZE = TOOLTIP_BODY_FONT_SIZE;
    private static final float STAT_PANEL_PADDING = 10f;
    private static final float STAT_PANEL_MARGIN = 16f;
    private static final float STAT_PANEL_GROUP_GAP = 8f;
    private static final Color STAT_PANEL_TEXT_COLOR = TOOLTIP_BODY_COLOR;
    private static final float STAT_PANEL_HEADER_FONT_SIZE = TOOLTIP_TITLE_FONT_SIZE;
    private static final float STAT_PANEL_HEADER_PADDING = 10f;
    private static final Color STAT_PANEL_HEADER_COLOR = new Color(15, 40, 50);
    private static final Color STAT_PANEL_HEADER_TEXT_COLOR = Color.WHITE;

    private final String symbolPath;
    private final FleetMemberAPI member;
    private final Set<String> loadedSprites = new HashSet<>();

    private final Map<String, TooltipText> tooltipTitles = new HashMap<>();
    private final Map<String, TooltipText> tooltipBodies = new HashMap<>();
    private LazyFont tooltipFont;
    private boolean tooltipFontLoadFailed = false;
    private Color symbolDominantColor;
    private final Map<String, List<String>> lastStatGroupLines = new HashMap<>();
    private final Map<String, TooltipText> statGroupText = new HashMap<>();
    private final Map<String, LazyFont.DrawableString> statGroupHeaderText = new HashMap<>();
    private final Map<String, Boolean> statGroupCollapsed = new HashMap<>();
    private LazyFont.DrawableString expandedIcon;
    private LazyFont.DrawableString collapsedIcon;

    private PositionAPI position;
    private boolean dragging = false;
    private float panX = 0f;
    private float panY = 0f;
    private float zoom = 1f;
    private float mouseX = 0f;
    private float mouseY = 0f;
    private boolean mouseKnown = false;
    private SkillNode pendingClickNode;
    private final Map<String, Float> pulseElapsed = new HashMap<>();

    public SkillTreeCanvasPlugin(String symbolPath, FleetMemberAPI member) {
        this.symbolPath = symbolPath;
        this.member = member;
    }

    @Override
    public void positionChanged(PositionAPI position) {
        this.position = position;
    }

    @Override
    public void advance(float amount) {
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

    @Override
    public void processInput(List<InputEventAPI> events) {
        if (position == null) return;

        for (InputEventAPI event : events) {
            if (event.isConsumed()) continue;

            if (event.isLMBDownEvent() && position.containsEvent(event)) {
                String clickedHeader = findStatGroupHeaderAt(event.getX(), event.getY());
                if (clickedHeader != null) {
                    boolean collapsed = Boolean.TRUE.equals(statGroupCollapsed.get(clickedHeader));
                    statGroupCollapsed.put(clickedHeader, !collapsed);
                } else {
                    SkillNode clicked = findNodeAt(event.getX(), event.getY());
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
                    toggleAllocation(pendingClickNode);
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
        ShipSkillData data = ShipSkillDataManager.get(member.getId());

        drawIcon(symbolPath, centerX, centerY, SYMBOL_SIZE * zoom, alphaMult, ALLOCATED_TINT);
        drawCenterRing(centerX, centerY, SYMBOL_SIZE * zoom, alphaMult);

        for (SkillNode node : SkillTree.getAllNodes().values()) {
            float nodeX = centerX + node.getOffsetX() * zoom;
            float nodeY = centerY - node.getOffsetY() * zoom;
            boolean allocated = data.isAllocated(node.getId());
            float nodeSize = NODE_SIZE * zoom;

            drawIcon(node.getIconPath(), nodeX, nodeY, nodeSize, alphaMult, allocated ? ALLOCATED_TINT : UNALLOCATED_TINT);
            drawRings(nodeX, nodeY, nodeSize, alphaMult, allocated, pulseElapsed.get(node.getId()));
        }

        drawNodeConnectors(centerX, centerY, data, alphaMult);
        drawStatPanel(alphaMult);

        if (!dragging && mouseKnown) {
            SkillNode hovered = findNodeAt(mouseX, mouseY);
            if (hovered != null) {
                renderTooltip(hovered, alphaMult);
            }
        }
    }

    private float centerX() {
        return position.getX() + position.getWidth() / 2f + panX;
    }

    private float centerY() {
        return position.getY() + position.getHeight() / 2f + panY;
    }

    private SkillNode findNodeAt(float x, float y) {
        if (position == null) return null;

        float centerX = centerX();
        float centerY = centerY();
        float halfSize = NODE_SIZE * zoom / 2f;
        for (SkillNode node : SkillTree.getAllNodes().values()) {
            float nodeX = centerX + node.getOffsetX() * zoom;
            float nodeY = centerY - node.getOffsetY() * zoom;
            if (Math.abs(x - nodeX) <= halfSize && Math.abs(y - nodeY) <= halfSize) {
                return node;
            }
        }
        return null;
    }

    private void toggleAllocation(SkillNode node) {
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

    private void drawIcon(String spritePath, float cx, float cy, float size, float alphaMult, Color tint) {
        if (loadedSprites.add(spritePath)) {
            try {
                Global.getSettings().loadTexture(spritePath);
            } catch (IOException e) {
                Logger.getLogger(SkillTreeCanvasPlugin.class).error("Failed to load texture " + spritePath, e);
                return;
            }
        }
        SpriteAPI sprite = Global.getSettings().getSprite(spritePath);
        sprite.setSize(size, size);
        sprite.setAlphaMult(alphaMult);
        sprite.setColor(tint);
        sprite.renderAtCenter(cx, cy);

        drawVignette(cx, cy, size, alphaMult);
    }

    private void drawRings(float cx, float cy, float iconSize, float alphaMult, boolean allocated, Float pulseSeconds) {
        float half = iconSize / 2f;
        Color ringColor = allocated ? GLOW_COLOR : RING_DULL_COLOR;
        float ringAlpha = allocated ? RING_ALLOCATED_ALPHA : RING_DULL_ALPHA;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(RING_LINE_THICKNESS);

        for (float fraction : RING_RADIUS_FRACTIONS) {
            drawRingOutline(cx, cy, half * fraction, ringColor, ringAlpha * alphaMult);
        }

        if (pulseSeconds != null) {
            float progress = pulseSeconds / PULSE_DURATION;
            float radiusFraction = PULSE_START_RADIUS_FRACTION + (PULSE_END_RADIUS_FRACTION - PULSE_START_RADIUS_FRACTION) * progress;
            drawRingOutline(cx, cy, half * radiusFraction, GLOW_COLOR, (1f - progress) * alphaMult);
        }

        GL11.glDisable(GL11.GL_BLEND);
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

    private void drawNodeConnectors(float centerX, float centerY, ShipSkillData data, float alphaMult) {
        float centerRadius = (SYMBOL_SIZE * zoom / 2f) * CENTER_RING_RADIUS_FRACTION;
        float nodeRadius = (NODE_SIZE * zoom / 2f) * NODE_CONNECTOR_RADIUS_FRACTION;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for (SkillNode node : SkillTree.getAllNodes().values()) {
            float nodeX = centerX + node.getOffsetX() * zoom;
            float nodeY = centerY - node.getOffsetY() * zoom;

            if (node.getPrerequisiteNodeIds().isEmpty()) {
                drawNodeConnectorLine(centerX, centerY, centerRadius, nodeX, nodeY, nodeRadius, data.isAllocated(node.getId()), alphaMult);
            } else {
                for (String prerequisiteId : node.getPrerequisiteNodeIds()) {
                    SkillNode parent = SkillTree.get(prerequisiteId);
                    if (parent == null) continue;

                    float parentX = centerX + parent.getOffsetX() * zoom;
                    float parentY = centerY - parent.getOffsetY() * zoom;
                    boolean bothAllocated = data.isAllocated(node.getId()) && data.isAllocated(parent.getId());
                    drawNodeConnectorLine(parentX, parentY, nodeRadius, nodeX, nodeY, nodeRadius, bothAllocated, alphaMult);
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

    private void renderTooltip(SkillNode node, float alphaMult) {
        LazyFont font = getTooltipFont();
        if (font == null) return;

        TooltipText title = tooltipTitles.computeIfAbsent(node.getId(),
                id -> buildTooltipText(font, node.getDisplayName(), TOOLTIP_TITLE_FONT_SIZE, TOOLTIP_TITLE_COLOR));
        TooltipText body = tooltipBodies.computeIfAbsent(node.getId(),
                id -> buildTooltipText(font, node.getDescription(), TOOLTIP_BODY_FONT_SIZE, TOOLTIP_BODY_COLOR));

        float boxWidth = Math.max(title.width, body.width) + TOOLTIP_PADDING * 2f + TOOLTIP_WIDTH_SAFETY_MARGIN;
        float boxHeight = title.height + TOOLTIP_TITLE_BODY_GAP + body.height + TOOLTIP_PADDING * 2f;
        float boxX = mouseX + TOOLTIP_CURSOR_OFFSET;
        float boxY = mouseY - boxHeight - TOOLTIP_CURSOR_OFFSET;

        drawTooltipBackground(boxX, boxY, boxWidth, boxHeight, alphaMult, getSymbolDominantColor());

        float titleY = boxY + boxHeight - TOOLTIP_PADDING;
        float bodyY = titleY - title.height - TOOLTIP_TITLE_BODY_GAP;
        title.drawable.draw(boxX + TOOLTIP_PADDING, titleY);
        body.drawable.draw(boxX + TOOLTIP_PADDING, bodyY);
    }

    private TooltipText buildTooltipText(LazyFont font, String rawText, float fontSize, Color color) {
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
        return new TooltipText(drawable, width, height);
    }

    private void drawStatPanel(float alphaMult) {
        LazyFont font = getTooltipFont();
        if (font == null) return;

        for (StatGroupLayout layout : layoutStatGroups(font)) {
            drawTooltipBackground(layout.x, layout.y, layout.width, layout.height, alphaMult, GLOW_COLOR);
            drawStatGroupHeaderBar(font, layout, alphaMult);
            if (!layout.collapsed) {
                layout.bodyText.drawable.draw(layout.x + STAT_PANEL_PADDING, layout.headerY - STAT_PANEL_PADDING);
            }
        }
    }

    private String findStatGroupHeaderAt(float x, float y) {
        if (position == null) return null;

        LazyFont font = getTooltipFont();
        if (font == null) return null;

        for (StatGroupLayout layout : layoutStatGroups(font)) {
            if (x >= layout.x && x <= layout.x + layout.width && y >= layout.headerY && y <= layout.headerY + layout.headerHeight) {
                return layout.name;
            }
        }
        return null;
    }

    private List<StatGroupLayout> layoutStatGroups(LazyFont font) {
        List<StatGroup> groups = buildStatGroups(member);
        List<TooltipText> bodyTexts = new ArrayList<>();
        float headerHeight = STAT_PANEL_HEADER_FONT_SIZE * FONT_LINE_HEIGHT_FACTOR + STAT_PANEL_HEADER_PADDING * 2f;
        float iconWidth = font.calcWidth("+", STAT_PANEL_HEADER_FONT_SIZE);
        float boxWidth = 0f;

        for (StatGroup group : groups) {
            TooltipText bodyText = getOrBuildStatGroupText(font, group);
            bodyTexts.add(bodyText);
            float headerTextWidth = font.calcWidth(group.name, STAT_PANEL_HEADER_FONT_SIZE);
            float headerMinWidth = headerTextWidth + iconWidth + STAT_PANEL_HEADER_PADDING * 3f;
            float bodyWidth = bodyText.width + STAT_PANEL_PADDING * 2f;
            boxWidth = Math.max(boxWidth, Math.max(headerMinWidth, bodyWidth));
        }

        List<StatGroupLayout> layouts = new ArrayList<>();
        float currentTop = position.getY() + position.getHeight() - STAT_PANEL_MARGIN;

        for (int i = 0; i < groups.size(); i++) {
            StatGroup group = groups.get(i);
            TooltipText bodyText = bodyTexts.get(i);
            boolean collapsed = Boolean.TRUE.equals(statGroupCollapsed.get(group.name));
            float bodyHeight = collapsed ? 0f : bodyText.height + STAT_PANEL_PADDING * 2f;
            float boxHeight = headerHeight + bodyHeight;
            float boxX = position.getX() + position.getWidth() - boxWidth - STAT_PANEL_MARGIN;
            float boxY = currentTop - boxHeight;

            layouts.add(new StatGroupLayout(group.name, collapsed, boxX, boxY, boxWidth, boxHeight, headerHeight, boxY + bodyHeight, bodyText));

            currentTop = boxY - STAT_PANEL_GROUP_GAP;
        }

        return layouts;
    }

    private void drawStatGroupHeaderBar(LazyFont font, StatGroupLayout layout, float alphaMult) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Misc.setColor(STAT_PANEL_HEADER_COLOR, alphaMult);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(layout.x, layout.headerY);
        GL11.glVertex2f(layout.x + layout.width, layout.headerY);
        GL11.glVertex2f(layout.x + layout.width, layout.headerY + layout.headerHeight);
        GL11.glVertex2f(layout.x, layout.headerY + layout.headerHeight);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);

        float textHeight = STAT_PANEL_HEADER_FONT_SIZE * FONT_LINE_HEIGHT_FACTOR;
        float textY = layout.headerY + layout.headerHeight / 2f + textHeight / 2f;

        LazyFont.DrawableString headerText = getStatGroupHeaderText(font, layout.name);
        headerText.draw(layout.x + STAT_PANEL_HEADER_PADDING, textY);

        String iconChar = layout.collapsed ? "+" : "-";
        LazyFont.DrawableString icon = getToggleIcon(font, layout.collapsed);
        float iconWidth = font.calcWidth(iconChar, STAT_PANEL_HEADER_FONT_SIZE);
        icon.draw(layout.x + layout.width - STAT_PANEL_HEADER_PADDING - iconWidth, textY);
    }

    private LazyFont.DrawableString getStatGroupHeaderText(LazyFont font, String name) {
        return statGroupHeaderText.computeIfAbsent(name, n -> {
            LazyFont.DrawableString text = font.createText(n, STAT_PANEL_HEADER_TEXT_COLOR, STAT_PANEL_HEADER_FONT_SIZE);
            text.setAnchor(LazyFont.TextAnchor.TOP_LEFT);
            text.setAlignment(LazyFont.TextAlignment.LEFT);
            return text;
        });
    }

    private LazyFont.DrawableString getToggleIcon(LazyFont font, boolean collapsed) {
        if (collapsed) {
            if (collapsedIcon == null) {
                collapsedIcon = font.createText("+", STAT_PANEL_HEADER_TEXT_COLOR, STAT_PANEL_HEADER_FONT_SIZE);
                collapsedIcon.setAnchor(LazyFont.TextAnchor.TOP_LEFT);
            }
            return collapsedIcon;
        }
        if (expandedIcon == null) {
            expandedIcon = font.createText("-", STAT_PANEL_HEADER_TEXT_COLOR, STAT_PANEL_HEADER_FONT_SIZE);
            expandedIcon.setAnchor(LazyFont.TextAnchor.TOP_LEFT);
        }
        return expandedIcon;
    }

    private TooltipText getOrBuildStatGroupText(LazyFont font, StatGroup group) {
        List<String> cached = lastStatGroupLines.get(group.name);
        if (!group.statLines.equals(cached)) {
            lastStatGroupLines.put(group.name, group.statLines);
            statGroupText.put(group.name, buildMultiLineText(font, group.statLines, STAT_PANEL_FONT_SIZE, STAT_PANEL_TEXT_COLOR));
        }
        return statGroupText.get(group.name);
    }

    private List<StatGroup> buildStatGroups(FleetMemberAPI member) {
        List<StatGroup> groups = new ArrayList<>();
        MutableShipStatsAPI stats = member.getStats();
        ShipHullSpecAPI hullSpec = member.getHullSpec();

        List<String> general = new ArrayList<>();
        addStat(general, "Hull Points", stats.getHullBonus().computeEffective(hullSpec.getHitpoints()));
        addStat(general, "Armor Rating", stats.getArmorBonus().computeEffective(hullSpec.getArmorRating()));
        addStat(general, "Max Flux", stats.getFluxCapacity().getModifiedValue());
        addStat(general, "Flux Dissipation", stats.getFluxDissipation().getModifiedValue());
        groups.add(new StatGroup("General", general));

        List<String> mobility = new ArrayList<>();
        addStat(mobility, "Top Speed", stats.getMaxSpeed().getModifiedValue());
        addStat(mobility, "Max Turn Rate", stats.getMaxTurnRate().getModifiedValue());
        addStat(mobility, "Acceleration", stats.getAcceleration().getModifiedValue());
        groups.add(new StatGroup("Mobility", mobility));

        ShieldAPI.ShieldType shieldType = hullSpec.getShieldType();
        if (shieldType != ShieldAPI.ShieldType.NONE) {
            List<String> defense = new ArrayList<>();
            defense.add("Shield Type " + shieldType.name());
            try {
                addStat(defense, "Shield Arc", stats.getShieldArcBonus().computeEffective(hullSpec.getShieldSpec().getArc()));
            } catch (RuntimeException e) {
                Logger.getLogger(SkillTreeCanvasPlugin.class).error("Failed to compute shield arc stat", e);
            }
            addStat(defense, "Shield Efficiency", hullSpec.getBaseShieldFluxPerDamageAbsorbed() * stats.getShieldAbsorptionMult().getModifiedValue());
            try {
                addStat(defense, "Shield Upkeep", hullSpec.getShieldSpec().getUpkeepCost() * stats.getShieldUpkeepMult().getModifiedValue());
            } catch (RuntimeException e) {
                Logger.getLogger(SkillTreeCanvasPlugin.class).error("Failed to compute shield upkeep stat", e);
            }
            groups.add(new StatGroup("Defense", defense));
        }

        List<String> logistics = new ArrayList<>();
        logistics.add("Crew " + Math.round(member.getMinCrew()) + "-" + Math.round(member.getMaxCrew()));
        addStat(logistics, "Cargo Capacity", member.getCargoCapacity());
        addStat(logistics, "Fuel Capacity", member.getFuelCapacity());
        addStat(logistics, "Fuel Use", member.getFuelUse());
        addStat(logistics, "Burn Level", stats.getMaxBurnLevel().getModifiedValue());
        addStat(logistics, "Sensor Profile", stats.getSensorProfile().getModifiedValue());
        try {
            MutableCharacterStatsAPI captainStats = member.getCaptain() != null ? member.getCaptain().getStats() : null;
            int totalOp = hullSpec.getOrdnancePoints(captainStats);
            int usedOp = member.getVariant().computeOPCost(captainStats);
            logistics.add("Ordnance Points " + usedOp + "/" + totalOp);
        } catch (RuntimeException e) {
            Logger.getLogger(SkillTreeCanvasPlugin.class).error("Failed to compute ordnance point stats", e);
        }
        addStat(logistics, "Max Combat Readiness", stats.getMaxCombatReadiness().getModifiedValue() * 100f, "%");
        addStat(logistics, "Supplies/mo", stats.getSuppliesPerMonth().getModifiedValue());
        groups.add(new StatGroup("Logistics", logistics));

        return groups;
    }

    private static final class StatGroup {
        final String name;
        final List<String> statLines;

        StatGroup(String name, List<String> statLines) {
            this.name = name;
            this.statLines = statLines;
        }
    }

    private static final class StatGroupLayout {
        final String name;
        final boolean collapsed;
        final float x;
        final float y;
        final float width;
        final float height;
        final float headerHeight;
        final float headerY;
        final TooltipText bodyText;

        StatGroupLayout(String name, boolean collapsed, float x, float y, float width, float height,
                        float headerHeight, float headerY, TooltipText bodyText) {
            this.name = name;
            this.collapsed = collapsed;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.headerHeight = headerHeight;
            this.headerY = headerY;
            this.bodyText = bodyText;
        }
    }

    private static void addStat(List<String> lines, String label, float value) {
        addStat(lines, label, value, "");
    }

    private static void addStat(List<String> lines, String label, float value, String suffix) {
        lines.add(label + " " + formatStat(value) + suffix);
    }

    private static String formatStat(float value) {
        if (value == Math.round(value)) {
            return String.valueOf(Math.round(value));
        }
        return String.format("%.1f", value);
    }

    private TooltipText buildMultiLineText(LazyFont font, List<String> lines, float fontSize, Color color) {
        String joined = String.join("\n", lines);

        float width = 0f;
        for (String line : lines) {
            width = Math.max(width, font.calcWidth(line, fontSize));
        }
        float height = lines.size() * fontSize * FONT_LINE_HEIGHT_FACTOR;

        LazyFont.DrawableString drawable = font.createText(joined, color, fontSize);
        drawable.setAlignment(LazyFont.TextAlignment.LEFT);
        drawable.setAnchor(LazyFont.TextAnchor.TOP_LEFT);
        return new TooltipText(drawable, width, height);
    }

    private static final class TooltipText {
        final LazyFont.DrawableString drawable;
        final float width;
        final float height;

        TooltipText(LazyFont.DrawableString drawable, float width, float height) {
            this.drawable = drawable;
            this.width = width;
            this.height = height;
        }
    }

    private LazyFont getTooltipFont() {
        if (tooltipFont == null && !tooltipFontLoadFailed) {
            try {
                tooltipFont = LazyFont.loadFont(TOOLTIP_FONT_PATH);
            } catch (FontException e) {
                Logger.getLogger(SkillTreeCanvasPlugin.class).error("Failed to load tooltip font " + TOOLTIP_FONT_PATH, e);
                tooltipFontLoadFailed = true;
            }
        }
        return tooltipFont;
    }

    private void drawTooltipBackground(float x, float y, float width, float height, float alphaMult, Color borderColor) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Misc.setColor(TOOLTIP_BACKGROUND_COLOR, alphaMult);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();

        Misc.setColor(borderColor, alphaMult);
        GL11.glLineWidth(TOOLTIP_BORDER_THICKNESS);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
    }

    private Color getSymbolDominantColor() {
        if (symbolDominantColor == null) {
            symbolDominantColor = computeDominantColor(symbolPath);
        }
        return symbolDominantColor;
    }

    private Color computeDominantColor(String path) {
        try (InputStream in = Global.getSettings().openStream(path)) {
            BufferedImage image = ImageIO.read(in);
            if (image == null) return DEFAULT_TOOLTIP_BORDER_COLOR;

            Map<Integer, Integer> bucketCounts = new HashMap<>();
            Map<Integer, int[]> bucketSums = new HashMap<>();
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    int alpha = (argb >>> 24) & 0xFF;
                    if (alpha < MIN_ALPHA_TO_SAMPLE) continue;

                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int b = argb & 0xFF;
                    int bucket = (quantize(r) << 16) | (quantize(g) << 8) | quantize(b);

                    bucketCounts.merge(bucket, 1, Integer::sum);
                    int[] sum = bucketSums.computeIfAbsent(bucket, key -> new int[3]);
                    sum[0] += r;
                    sum[1] += g;
                    sum[2] += b;
                }
            }

            if (bucketCounts.isEmpty()) return DEFAULT_TOOLTIP_BORDER_COLOR;

            Map.Entry<Integer, Integer> mostCommon = null;
            for (Map.Entry<Integer, Integer> entry : bucketCounts.entrySet()) {
                if (mostCommon == null || entry.getValue() > mostCommon.getValue()) {
                    mostCommon = entry;
                }
            }

            int[] sum = bucketSums.get(mostCommon.getKey());
            int pixelCount = mostCommon.getValue();
            return new Color(sum[0] / pixelCount, sum[1] / pixelCount, sum[2] / pixelCount);
        } catch (IOException e) {
            Logger.getLogger(SkillTreeCanvasPlugin.class).error("Failed to read " + path + " for tooltip border colour", e);
            return DEFAULT_TOOLTIP_BORDER_COLOR;
        }
    }

    private static int quantize(int channel) {
        return (channel / COLOR_QUANTIZE_STEP) * COLOR_QUANTIZE_STEP;
    }

    private static float boundaryRadius(float cos, float sin, float halfWidth, float halfHeight) {
        float rx = cos != 0f ? halfWidth / Math.abs(cos) : Float.MAX_VALUE;
        float ry = sin != 0f ? halfHeight / Math.abs(sin) : Float.MAX_VALUE;
        return Math.min(rx, ry);
    }
}
