package exiledsector.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import exiledsector.ui.util.GLDraw;
import exiledsector.ui.util.SpriteCache;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.ui.LazyFont;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static exiledsector.ui.SkillTreePanelStyle.FONT_LINE_HEIGHT_FACTOR;
import static exiledsector.ui.SkillTreePanelStyle.TOOLTIP_BODY_FONT_SIZE;
import static exiledsector.ui.SkillTreePanelStyle.TOOLTIP_TITLE_FONT_SIZE;

final class SkillTreeStatPanel {

    private static final String FONT_PATH = "graphics/fonts/orbitron20aabold.fnt";
    private static final String EDGE_W_TOP = "graphics/ui/bgs/ui_border1b_w_top.png";
    private static final String EDGE_W_MID = "graphics/ui/bgs/ui_border1b_w.png";
    private static final String EDGE_W_BOT = "graphics/ui/bgs/ui_border1b_w_bot.png";
    private static final String EDGE_E_TOP = "graphics/ui/bgs/ui_border1b_e_top.png";
    private static final String EDGE_E_MID = "graphics/ui/bgs/ui_border1b_e.png";
    private static final String EDGE_E_BOT = "graphics/ui/bgs/ui_border1b_e_bot.png";
    private static final float BORDER_EDGE_WIDTH = 16f;
    private static final float BORDER_CAP_HEIGHT = 8f;
    private static final Color BORDER_TINT = Color.WHITE;
    private static final Color BACKGROUND_COLOR = new Color(0, 0, 0, 230);

    private static final float STAT_PANEL_FONT_SIZE = TOOLTIP_BODY_FONT_SIZE;
    // Kept clear of the side-edge accent's width/caps so text never renders under it.
    private static final float STAT_PANEL_PADDING = 26f;
    private static final float STAT_PANEL_MARGIN = 16f;
    private static final float STAT_PANEL_GROUP_GAP = 8f;
    private static final float STAT_PANEL_COLUMN_GAP = 20f;
    private static final float STAT_PANEL_HEADER_GAP = 10f;
    private static final float STAT_PANEL_HEADER_FONT_SIZE = TOOLTIP_TITLE_FONT_SIZE;

    private static final Color STAT_PANEL_LABEL_COLOR = new Color(0xCB, 0xF5, 0xFF);
    private static final Color STAT_PANEL_VALUE_COLOR = new Color(0xFF, 0xD2, 0x00);
    private static final Color STAT_PANEL_HEADER_TEXT_COLOR = new Color(0xCB, 0xF5, 0xFF);

    private final FleetMemberAPI member;
    private final SpriteCache spriteCache = new SpriteCache(SkillTreeStatPanel.class);
    private final Map<String, List<StatLine>> lastStatGroupLines = new HashMap<>();
    private final Map<String, SkillTreePanelStyle.TooltipText> statGroupLabelText = new HashMap<>();
    private final Map<String, SkillTreePanelStyle.TooltipText> statGroupValueText = new HashMap<>();
    private final Map<String, LazyFont.DrawableString> statGroupHeaderText = new HashMap<>();
    private LazyFont statFont;
    private boolean fontLoadFailed = false;

    SkillTreeStatPanel(FleetMemberAPI member) {
        this.member = member;
    }

    void render(PositionAPI position, float alphaMult) {
        LazyFont font = getFont();
        if (font == null) return;

        for (StatGroupLayout layout : layoutStatGroups(position, font)) {
            drawGroupBoxBackground(layout.x, layout.y, layout.width, layout.height, alphaMult);

            LazyFont.DrawableString headerText = getStatGroupHeaderText(font, layout.name);
            headerText.draw(layout.x + layout.width / 2f, layout.headerTextY);

            layout.labelText.drawable.draw(layout.x + STAT_PANEL_PADDING, layout.bodyTextY);
            layout.valueText.drawable.draw(layout.x + layout.width - STAT_PANEL_PADDING, layout.bodyTextY);
        }
    }

    private void drawGroupBoxBackground(float x, float y, float width, float height, float alphaMult) {
        GLDraw.fillQuad(x, y, width, height, BACKGROUND_COLOR, alphaMult);

        float edgeWidth = Math.min(BORDER_EDGE_WIDTH, width / 2f);
        float capHeight = Math.min(BORDER_CAP_HEIGHT, height / 2f);
        float midHeight = Math.max(0f, height - capHeight * 2f);

        drawBorderPiece(EDGE_W_TOP, x, y + height - capHeight, edgeWidth, capHeight, alphaMult);
        drawBorderPiece(EDGE_W_MID, x, y + capHeight, edgeWidth, midHeight, alphaMult);
        drawBorderPiece(EDGE_W_BOT, x, y, edgeWidth, capHeight, alphaMult);

        drawBorderPiece(EDGE_E_TOP, x + width - edgeWidth, y + height - capHeight, edgeWidth, capHeight, alphaMult);
        drawBorderPiece(EDGE_E_MID, x + width - edgeWidth, y + capHeight, edgeWidth, midHeight, alphaMult);
        drawBorderPiece(EDGE_E_BOT, x + width - edgeWidth, y, edgeWidth, capHeight, alphaMult);
    }

    private void drawBorderPiece(String path, float x, float y, float width, float height, float alphaMult) {
        if (width <= 0f || height <= 0f) return;
        if (!spriteCache.ensureLoaded(path)) return;

        SpriteAPI sprite = Global.getSettings().getSprite(path);
        sprite.setSize(width, height);
        sprite.setAlphaMult(alphaMult);
        sprite.setColor(BORDER_TINT);
        sprite.renderAtCenter(x + width / 2f, y + height / 2f);
    }

    private LazyFont getFont() {
        if (statFont == null && !fontLoadFailed) {
            statFont = SkillTreePanelStyle.loadFontOrNull(FONT_PATH);
            fontLoadFailed = statFont == null;
        }
        return statFont;
    }

    private List<StatGroupLayout> layoutStatGroups(PositionAPI position, LazyFont font) {
        List<StatGroup> groups = buildStatGroups(member);
        List<SkillTreePanelStyle.TooltipText> labelTexts = new ArrayList<>();
        List<SkillTreePanelStyle.TooltipText> valueTexts = new ArrayList<>();
        float headerHeight = STAT_PANEL_HEADER_FONT_SIZE * FONT_LINE_HEIGHT_FACTOR;
        float boxWidth = 0f;

        for (StatGroup group : groups) {
            SkillTreePanelStyle.TooltipText labelText = getOrBuildLabelText(font, group);
            SkillTreePanelStyle.TooltipText valueText = getOrBuildValueText(font, group);
            labelTexts.add(labelText);
            valueTexts.add(valueText);
            float headerMinWidth = font.calcWidth(group.name, STAT_PANEL_HEADER_FONT_SIZE) + STAT_PANEL_PADDING * 2f;
            float bodyWidth = labelText.width + STAT_PANEL_COLUMN_GAP + valueText.width + STAT_PANEL_PADDING * 2f;
            boxWidth = Math.max(boxWidth, Math.max(headerMinWidth, bodyWidth));
        }

        List<StatGroupLayout> layouts = new ArrayList<>();
        float currentTop = position.getY() + position.getHeight() - STAT_PANEL_MARGIN;

        for (int i = 0; i < groups.size(); i++) {
            StatGroup group = groups.get(i);
            SkillTreePanelStyle.TooltipText labelText = labelTexts.get(i);
            SkillTreePanelStyle.TooltipText valueText = valueTexts.get(i);
            float contentHeight = Math.max(labelText.height, valueText.height);
            float boxHeight = STAT_PANEL_PADDING * 2f + headerHeight + STAT_PANEL_HEADER_GAP + contentHeight;
            float boxX = position.getX() + position.getWidth() - boxWidth - STAT_PANEL_MARGIN;
            float boxY = currentTop - boxHeight;
            float boxTop = boxY + boxHeight;
            float headerTextY = boxTop - STAT_PANEL_PADDING;
            float bodyTextY = headerTextY - headerHeight - STAT_PANEL_HEADER_GAP;

            layouts.add(new StatGroupLayout(group.name, boxX, boxY, boxWidth, boxHeight,
                    labelText, valueText, headerTextY, bodyTextY));

            currentTop = boxY - STAT_PANEL_GROUP_GAP;
        }

        return layouts;
    }

    private LazyFont.DrawableString getStatGroupHeaderText(LazyFont font, String name) {
        return statGroupHeaderText.computeIfAbsent(name,
                n -> SkillTreePanelStyle.buildSimpleText(font, n, STAT_PANEL_HEADER_FONT_SIZE, STAT_PANEL_HEADER_TEXT_COLOR, LazyFont.TextAnchor.TOP_CENTER));
    }

    private SkillTreePanelStyle.TooltipText getOrBuildLabelText(LazyFont font, StatGroup group) {
        refreshStatGroupTextIfChanged(font, group);
        return statGroupLabelText.get(group.name);
    }

    private SkillTreePanelStyle.TooltipText getOrBuildValueText(LazyFont font, StatGroup group) {
        refreshStatGroupTextIfChanged(font, group);
        return statGroupValueText.get(group.name);
    }

    private void refreshStatGroupTextIfChanged(LazyFont font, StatGroup group) {
        List<StatLine> cached = lastStatGroupLines.get(group.name);
        if (group.statLines.equals(cached)) return;

        lastStatGroupLines.put(group.name, group.statLines);
        List<String> labels = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (StatLine line : group.statLines) {
            labels.add(line.label);
            values.add(line.value);
        }
        statGroupLabelText.put(group.name, SkillTreePanelStyle.buildJoinedText(font, labels, STAT_PANEL_FONT_SIZE, STAT_PANEL_LABEL_COLOR));
        statGroupValueText.put(group.name, SkillTreePanelStyle.buildJoinedTextRightAligned(font, values, STAT_PANEL_FONT_SIZE, STAT_PANEL_VALUE_COLOR));
    }

    private List<StatGroup> buildStatGroups(FleetMemberAPI member) {
        List<StatGroup> groups = new ArrayList<>();
        MutableShipStatsAPI stats = member.getStats();
        ShipHullSpecAPI hullSpec = member.getHullSpec();

        List<StatLine> general = new ArrayList<>();
        addStat(general, "Hull Points", stats.getHullBonus().computeEffective(hullSpec.getHitpoints()));
        addStat(general, "Armor Rating", stats.getArmorBonus().computeEffective(hullSpec.getArmorRating()));
        addStat(general, "Max Flux", stats.getFluxCapacity().getModifiedValue());
        addStat(general, "Flux Dissipation", stats.getFluxDissipation().getModifiedValue());
        groups.add(new StatGroup("General", general));

        List<StatLine> mobility = new ArrayList<>();
        addStat(mobility, "Top Speed", stats.getMaxSpeed().getModifiedValue());
        addStat(mobility, "Max Turn Rate", stats.getMaxTurnRate().getModifiedValue());
        addStat(mobility, "Acceleration", stats.getAcceleration().getModifiedValue());
        groups.add(new StatGroup("Mobility", mobility));

        ShieldAPI.ShieldType shieldType = hullSpec.getShieldType();
        if (shieldType != ShieldAPI.ShieldType.NONE) {
            List<StatLine> defense = new ArrayList<>();
            defense.add(new StatLine("Shield Type", shieldType.name()));
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

        List<StatLine> logistics = new ArrayList<>();
        logistics.add(new StatLine("Crew", Math.round(member.getMinCrew()) + "-" + Math.round(member.getMaxCrew())));
        addStat(logistics, "Cargo Capacity", member.getCargoCapacity());
        addStat(logistics, "Fuel Capacity", member.getFuelCapacity());
        addStat(logistics, "Fuel Use", member.getFuelUse());
        addStat(logistics, "Burn Level", stats.getMaxBurnLevel().getModifiedValue());
        addStat(logistics, "Sensor Profile", stats.getSensorProfile().getModifiedValue());
        try {
            MutableCharacterStatsAPI captainStats = member.getCaptain() != null ? member.getCaptain().getStats() : null;
            int totalOp = hullSpec.getOrdnancePoints(captainStats);
            int usedOp = member.getVariant().computeOPCost(captainStats);
            logistics.add(new StatLine("Ordnance Points", usedOp + "/" + totalOp));
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
        final List<StatLine> statLines;

        StatGroup(String name, List<StatLine> statLines) {
            this.name = name;
            this.statLines = statLines;
        }
    }

    private static final class StatLine {
        final String label;
        final String value;

        StatLine(String label, String value) {
            this.label = label;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StatLine)) return false;
            StatLine other = (StatLine) o;
            return label.equals(other.label) && value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(label, value);
        }
    }

    private static final class StatGroupLayout {
        final String name;
        final float x;
        final float y;
        final float width;
        final float height;
        final SkillTreePanelStyle.TooltipText labelText;
        final SkillTreePanelStyle.TooltipText valueText;
        final float headerTextY;
        final float bodyTextY;

        StatGroupLayout(String name, float x, float y, float width, float height,
                        SkillTreePanelStyle.TooltipText labelText, SkillTreePanelStyle.TooltipText valueText,
                        float headerTextY, float bodyTextY) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.labelText = labelText;
            this.valueText = valueText;
            this.headerTextY = headerTextY;
            this.bodyTextY = bodyTextY;
        }
    }

    private static void addStat(List<StatLine> lines, String label, float value) {
        addStat(lines, label, value, "");
    }

    private static void addStat(List<StatLine> lines, String label, float value, String suffix) {
        lines.add(new StatLine(label, formatStat(value) + suffix));
    }

    private static String formatStat(float value) {
        if (value == Math.round(value)) {
            return String.valueOf(Math.round(value));
        }
        return String.format("%.1f", value);
    }
}
