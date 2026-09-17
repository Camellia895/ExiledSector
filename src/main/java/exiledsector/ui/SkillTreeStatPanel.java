package exiledsector.ui;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static exiledsector.ui.SkillTreePanelStyle.FONT_LINE_HEIGHT_FACTOR;
import static exiledsector.ui.SkillTreePanelStyle.TOOLTIP_BODY_COLOR;
import static exiledsector.ui.SkillTreePanelStyle.TOOLTIP_BODY_FONT_SIZE;
import static exiledsector.ui.SkillTreePanelStyle.TOOLTIP_TITLE_FONT_SIZE;

final class SkillTreeStatPanel {

    private static final float STAT_PANEL_FONT_SIZE = TOOLTIP_BODY_FONT_SIZE;
    private static final float STAT_PANEL_PADDING = 10f;
    private static final float STAT_PANEL_MARGIN = 16f;
    private static final float STAT_PANEL_GROUP_GAP = 8f;
    private static final Color STAT_PANEL_TEXT_COLOR = TOOLTIP_BODY_COLOR;
    private static final float STAT_PANEL_HEADER_FONT_SIZE = TOOLTIP_TITLE_FONT_SIZE;
    private static final float STAT_PANEL_HEADER_PADDING = 10f;
    private static final Color STAT_PANEL_HEADER_TEXT_COLOR = Color.WHITE;

    private final FleetMemberAPI member;
    private final SkillTreePanelStyle style;
    private final Map<String, List<String>> lastStatGroupLines = new HashMap<>();
    private final Map<String, SkillTreePanelStyle.TooltipText> statGroupText = new HashMap<>();
    private final Map<String, LazyFont.DrawableString> statGroupHeaderText = new HashMap<>();
    private final Map<String, Boolean> statGroupCollapsed = new HashMap<>();
    private LazyFont.DrawableString expandedIcon;
    private LazyFont.DrawableString collapsedIcon;

    SkillTreeStatPanel(FleetMemberAPI member, SkillTreePanelStyle style) {
        this.member = member;
        this.style = style;
    }

    void render(PositionAPI position, float alphaMult) {
        LazyFont font = style.getFont();
        if (font == null) return;

        for (StatGroupLayout layout : layoutStatGroups(position, font)) {
            style.drawTooltipBackground(layout.x, layout.y, layout.width, layout.height, alphaMult, style.getAccentColor());
            drawStatGroupHeaderBar(font, layout, alphaMult);
            if (!layout.collapsed) {
                layout.bodyText.drawable.draw(layout.x + STAT_PANEL_PADDING, layout.headerY - STAT_PANEL_PADDING);
            }
        }
    }

    boolean handleClick(PositionAPI position, float x, float y) {
        LazyFont font = style.getFont();
        if (font == null) return false;

        for (StatGroupLayout layout : layoutStatGroups(position, font)) {
            if (x >= layout.x && x <= layout.x + layout.width && y >= layout.headerY && y <= layout.headerY + layout.headerHeight) {
                boolean collapsed = Boolean.TRUE.equals(statGroupCollapsed.get(layout.name));
                statGroupCollapsed.put(layout.name, !collapsed);
                return true;
            }
        }
        return false;
    }

    private List<StatGroupLayout> layoutStatGroups(PositionAPI position, LazyFont font) {
        List<StatGroup> groups = buildStatGroups(member);
        List<SkillTreePanelStyle.TooltipText> bodyTexts = new ArrayList<>();
        float headerHeight = STAT_PANEL_HEADER_FONT_SIZE * FONT_LINE_HEIGHT_FACTOR + STAT_PANEL_HEADER_PADDING * 2f;
        float iconWidth = font.calcWidth("+", STAT_PANEL_HEADER_FONT_SIZE);
        float boxWidth = 0f;

        for (StatGroup group : groups) {
            SkillTreePanelStyle.TooltipText bodyText = getOrBuildStatGroupText(font, group);
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
            SkillTreePanelStyle.TooltipText bodyText = bodyTexts.get(i);
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

    private void drawHeaderBarBackground(float x, float y, float width, float height, float alphaMult) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Misc.setColor(style.getHeaderBackgroundColor(), alphaMult);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawStatGroupHeaderBar(LazyFont font, StatGroupLayout layout, float alphaMult) {
        drawHeaderBarBackground(layout.x, layout.headerY, layout.width, layout.headerHeight, alphaMult);

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

    private SkillTreePanelStyle.TooltipText getOrBuildStatGroupText(LazyFont font, StatGroup group) {
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
                Logger.getLogger(SkillTreeStatPanel.class).error("Failed to compute shield arc stat", e);
            }
            addStat(defense, "Shield Efficiency", hullSpec.getBaseShieldFluxPerDamageAbsorbed() * stats.getShieldAbsorptionMult().getModifiedValue());
            try {
                addStat(defense, "Shield Upkeep", hullSpec.getShieldSpec().getUpkeepCost() * stats.getShieldUpkeepMult().getModifiedValue());
            } catch (RuntimeException e) {
                Logger.getLogger(SkillTreeStatPanel.class).error("Failed to compute shield upkeep stat", e);
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
            Logger.getLogger(SkillTreeStatPanel.class).error("Failed to compute ordnance point stats", e);
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
        final SkillTreePanelStyle.TooltipText bodyText;

        StatGroupLayout(String name, boolean collapsed, float x, float y, float width, float height,
                        float headerHeight, float headerY, SkillTreePanelStyle.TooltipText bodyText) {
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

    private SkillTreePanelStyle.TooltipText buildMultiLineText(LazyFont font, List<String> lines, float fontSize, Color color) {
        String joined = String.join("\n", lines);

        float width = 0f;
        for (String line : lines) {
            width = Math.max(width, font.calcWidth(line, fontSize));
        }
        float height = lines.size() * fontSize * FONT_LINE_HEIGHT_FACTOR;

        LazyFont.DrawableString drawable = font.createText(joined, color, fontSize);
        drawable.setAlignment(LazyFont.TextAlignment.LEFT);
        drawable.setAnchor(LazyFont.TextAnchor.TOP_LEFT);
        return new SkillTreePanelStyle.TooltipText(drawable, width, height);
    }
}
