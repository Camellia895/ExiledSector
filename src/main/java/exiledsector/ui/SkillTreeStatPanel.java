package exiledsector.ui;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.AllocatedSkillEffects;
import exiledsector.skills.ShipOpBudget;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.skilleffect.ShieldSkillEffect;
import exiledsector.ui.util.BorderedPanel;
import exiledsector.ui.util.FallbackSupport;
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

    private static final float STAT_PANEL_FONT_SIZE = TOOLTIP_BODY_FONT_SIZE;
    private static final float STAT_PANEL_PADDING = 26f;
    private static final float STAT_PANEL_MARGIN = 16f;
    private static final float STAT_PANEL_GROUP_GAP = 8f;
    private static final float STAT_PANEL_COLUMN_GAP = 20f;
    private static final float STAT_PANEL_HEADER_GAP = 10f;
    private static final float STAT_PANEL_HEADER_FONT_SIZE = TOOLTIP_TITLE_FONT_SIZE;

    private static final Color STAT_PANEL_LABEL_COLOR = new Color(0xCB, 0xF5, 0xFF);
    private static final Color STAT_PANEL_VALUE_COLOR = new Color(0xFF, 0xD2, 0x00);
    private static final Color STAT_PANEL_HEADER_TEXT_COLOR = new Color(0xCB, 0xF5, 0xFF);
    private static final Color STAT_DECREASED_COLOR = new Color(0xFC, 0x63, 0x00);
    private static final Color STAT_INCREASED_COLOR = new Color(0x98, 0xFB, 0x00);
    private static final float STAT_COMPARISON_EPSILON = 0.001f;

    private final FleetMemberAPI member;
    private final ShipVariantAPI variant;
    private final BorderedPanel borderedPanel = new BorderedPanel(SkillTreeStatPanel.class);
    private final Map<String, List<StatLine>> lastStatGroupLines = new HashMap<>();
    private final Map<String, SkillTreePanelStyle.TooltipText> statGroupLabelText = new HashMap<>();
    private final Map<String, List<LazyFont.DrawableString>> statGroupValueLines = new HashMap<>();
    private final Map<String, Float> statGroupValueWidth = new HashMap<>();
    private final Map<String, LazyFont.DrawableString> statGroupHeaderText = new HashMap<>();
    private LazyFont statFont;
    private boolean fontLoadFailed = false;

    SkillTreeStatPanel(FleetMemberAPI member, ShipVariantAPI variant) {
        this.member = member;
        this.variant = variant;
    }

    void render(PositionAPI position, float alphaMult) {
        LazyFont font = getFont();
        if (font == null) return;

        float rowStep = STAT_PANEL_FONT_SIZE * FONT_LINE_HEIGHT_FACTOR;

        for (StatGroupLayout layout : layoutStatGroups(position, font)) {
            borderedPanel.draw(layout.x, layout.y, layout.width, layout.height, alphaMult);

            LazyFont.DrawableString headerText = getStatGroupHeaderText(font, layout.name);
            headerText.draw(layout.x + layout.width / 2f, layout.headerTextY);

            layout.labelText.drawable.draw(layout.x + STAT_PANEL_PADDING, layout.bodyTextY);

            float rowY = layout.bodyTextY;
            for (LazyFont.DrawableString valueLine : layout.valueLines) {
                valueLine.draw(layout.x + layout.width - STAT_PANEL_PADDING, rowY);
                rowY -= rowStep;
            }
        }
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
        List<List<LazyFont.DrawableString>> valueLinesList = new ArrayList<>();
        float headerHeight = STAT_PANEL_HEADER_FONT_SIZE * FONT_LINE_HEIGHT_FACTOR;
        float boxWidth = 0f;

        for (StatGroup group : groups) {
            SkillTreePanelStyle.TooltipText labelText = getOrBuildLabelText(font, group);
            List<LazyFont.DrawableString> valueLines = getOrBuildValueLines(font, group);
            float valueWidth = getValueWidth(font, group);
            labelTexts.add(labelText);
            valueLinesList.add(valueLines);
            float headerMinWidth = font.calcWidth(group.name, STAT_PANEL_HEADER_FONT_SIZE) + STAT_PANEL_PADDING * 2f;
            float bodyWidth = labelText.width + STAT_PANEL_COLUMN_GAP + valueWidth + STAT_PANEL_PADDING * 2f;
            boxWidth = Math.max(boxWidth, Math.max(headerMinWidth, bodyWidth));
        }

        List<StatGroupLayout> layouts = new ArrayList<>();
        float currentTop = position.getY() + position.getHeight() - STAT_PANEL_MARGIN;

        for (int i = 0; i < groups.size(); i++) {
            StatGroup group = groups.get(i);
            SkillTreePanelStyle.TooltipText labelText = labelTexts.get(i);
            List<LazyFont.DrawableString> valueLines = valueLinesList.get(i);
            float contentHeight = labelText.height;
            float boxHeight = STAT_PANEL_PADDING * 2f + headerHeight + STAT_PANEL_HEADER_GAP + contentHeight;
            float boxX = position.getX() + position.getWidth() - boxWidth - STAT_PANEL_MARGIN;
            float boxY = currentTop - boxHeight;
            float boxTop = boxY + boxHeight;
            float headerTextY = boxTop - STAT_PANEL_PADDING;
            float bodyTextY = headerTextY - headerHeight - STAT_PANEL_HEADER_GAP;

            layouts.add(new StatGroupLayout(group.name, boxX, boxY, boxWidth, boxHeight,
                    labelText, valueLines, headerTextY, bodyTextY));

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

    private List<LazyFont.DrawableString> getOrBuildValueLines(LazyFont font, StatGroup group) {
        refreshStatGroupTextIfChanged(font, group);
        return statGroupValueLines.get(group.name);
    }

    private float getValueWidth(LazyFont font, StatGroup group) {
        refreshStatGroupTextIfChanged(font, group);
        return statGroupValueWidth.get(group.name);
    }

    private void refreshStatGroupTextIfChanged(LazyFont font, StatGroup group) {
        List<StatLine> cached = lastStatGroupLines.get(group.name);
        if (group.statLines.equals(cached)) return;

        lastStatGroupLines.put(group.name, group.statLines);
        List<String> labels = new ArrayList<>();
        List<LazyFont.DrawableString> valueLines = new ArrayList<>();
        float valueWidth = 0f;
        for (StatLine line : group.statLines) {
            labels.add(line.label);
            valueLines.add(SkillTreePanelStyle.buildSimpleText(font, line.value, STAT_PANEL_FONT_SIZE, line.valueColor, LazyFont.TextAnchor.TOP_RIGHT));
            valueWidth = Math.max(valueWidth, font.calcWidth(line.value, STAT_PANEL_FONT_SIZE));
        }
        statGroupLabelText.put(group.name, SkillTreePanelStyle.buildJoinedText(font, labels, STAT_PANEL_FONT_SIZE, STAT_PANEL_LABEL_COLOR));
        statGroupValueLines.put(group.name, valueLines);
        statGroupValueWidth.put(group.name, valueWidth);
    }

    private List<StatGroup> buildStatGroups(FleetMemberAPI member) {
        List<StatGroup> groups = new ArrayList<>();
        MutableShipStatsAPI stats = member.getStats();
        ShipHullSpecAPI hullSpec = member.getHullSpec();

        List<StatLine> general = new ArrayList<>();
        addComparedStat(general, "Hull Points", stats.getHullBonus().computeEffective(hullSpec.getHitpoints()), hullSpec.getHitpoints());
        addComparedStat(general, "Armor Rating", stats.getArmorBonus().computeEffective(hullSpec.getArmorRating()), hullSpec.getArmorRating());
        addComparedStat(general, "Max Flux", stats.getFluxCapacity().getModifiedValue(), stats.getFluxCapacity().getBaseValue());
        addComparedStat(general, "Flux Dissipation", stats.getFluxDissipation().getModifiedValue(), stats.getFluxDissipation().getBaseValue());
        groups.add(new StatGroup("General", general));

        List<StatLine> mobility = new ArrayList<>();
        addComparedStat(mobility, "Top Speed", stats.getMaxSpeed().getModifiedValue(), stats.getMaxSpeed().getBaseValue());
        addComparedStat(mobility, "Max Turn Rate", stats.getMaxTurnRate().getModifiedValue(), stats.getMaxTurnRate().getBaseValue());
        addComparedStat(mobility, "Acceleration", stats.getAcceleration().getModifiedValue(), stats.getAcceleration().getBaseValue());
        groups.add(new StatGroup("Mobility", mobility));

        ShieldAPI.ShieldType shieldType = ShieldSkillEffect.resolveDisplayShieldType(hullSpec.getShieldType(), AllocatedSkillEffects.forMember(member));
        if (shieldType != ShieldAPI.ShieldType.NONE) {
            List<StatLine> defense = new ArrayList<>();
            defense.add(new StatLine("Shield Type", shieldType.name()));
            ShipHullSpecAPI.ShieldSpecAPI shieldSpec = getShieldSpecOrNull(hullSpec);
            if (shieldSpec != null) {
                addComparedStat(defense, "Shield Arc", stats.getShieldArcBonus().computeEffective(shieldSpec.getArc()), shieldSpec.getArc());
                addComparedStat(defense, "Shield Efficiency",
                        hullSpec.getBaseShieldFluxPerDamageAbsorbed() * stats.getShieldAbsorptionMult().getModifiedValue(),
                        hullSpec.getBaseShieldFluxPerDamageAbsorbed() * stats.getShieldAbsorptionMult().getBaseValue());
                addComparedStatLowerIsBetter(defense, "Shield Upkeep",
                        shieldSpec.getUpkeepCost() * stats.getShieldUpkeepMult().getModifiedValue(),
                        shieldSpec.getUpkeepCost() * stats.getShieldUpkeepMult().getBaseValue());
            } else {
                addStat(defense, "Shield Arc", stats.getShieldArcBonus().computeEffective(ShieldSkillEffect.MAKESHIFT_SHIELD_ARC));
                addStat(defense, "Shield Efficiency", ShieldSkillEffect.MAKESHIFT_SHIELD_EFFICIENCY * stats.getShieldAbsorptionMult().getModifiedValue());
            }
            groups.add(new StatGroup("Defense", defense));
        }

        List<StatLine> logistics = new ArrayList<>();
        logistics.add(new StatLine("Crew", Math.round(member.getMinCrew()) + "-" + Math.round(member.getMaxCrew())));
        addComparedStat(logistics, "Cargo Capacity", member.getCargoCapacity(), hullSpec.getCargo());
        addComparedStat(logistics, "Fuel Capacity", member.getFuelCapacity(), hullSpec.getFuel());
        addStat(logistics, "Fuel Use", member.getFuelUse());
        addComparedStat(logistics, "Burn Level", stats.getMaxBurnLevel().getModifiedValue(), stats.getMaxBurnLevel().getBaseValue());
        addComparedStatLowerIsBetter(logistics, "Sensor Profile", stats.getSensorProfile().getModifiedValue(), stats.getSensorProfile().getBaseValue());
        try {
            ShipOpBudget budget = ShipOpBudget.of(member, variant);
            logistics.add(new StatLine("Ordnance Points", budget.used + "/" + budget.total));
        } catch (RuntimeException e) {
            Logger.getLogger(SkillTreeStatPanel.class).error("Failed to compute ordnance point stats", e);
        }
        ShipSkillData skillData = ShipSkillDataManager.get(member.getId());
        logistics.add(new StatLine("Level", skillData.getLevel() + " (" + Math.round(skillData.getXp()) + " XP)"));
        if (skillData.getBankedFreeAllocations() > 0) {
            logistics.add(new StatLine("Free Allocations Banked", String.valueOf(skillData.getBankedFreeAllocations())));
        }
        addStat(logistics, "Max Combat Readiness", stats.getMaxCombatReadiness().getModifiedValue() * 100f, "%");
        addComparedStatLowerIsBetter(logistics, "Supplies/mo", stats.getSuppliesPerMonth().getModifiedValue(), stats.getSuppliesPerMonth().getBaseValue());
        groups.add(new StatGroup("Logistics", logistics));

        return groups;
    }

    private static ShipHullSpecAPI.ShieldSpecAPI getShieldSpecOrNull(ShipHullSpecAPI hullSpec) {
        return FallbackSupport.getOrFallback(hullSpec::getShieldSpec, null,
                Logger.getLogger(SkillTreeStatPanel.class), "Failed to read shield spec");
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
        final Color valueColor;

        StatLine(String label, String value) {
            this(label, value, STAT_PANEL_VALUE_COLOR);
        }

        StatLine(String label, String value, Color valueColor) {
            this.label = label;
            this.value = value;
            this.valueColor = valueColor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StatLine)) return false;
            StatLine other = (StatLine) o;
            return label.equals(other.label) && value.equals(other.value) && valueColor.equals(other.valueColor);
        }

        @Override
        public int hashCode() {
            return Objects.hash(label, value, valueColor);
        }
    }

    private static final class StatGroupLayout {
        final String name;
        final float x;
        final float y;
        final float width;
        final float height;
        final SkillTreePanelStyle.TooltipText labelText;
        final List<LazyFont.DrawableString> valueLines;
        final float headerTextY;
        final float bodyTextY;

        StatGroupLayout(String name, float x, float y, float width, float height,
                        SkillTreePanelStyle.TooltipText labelText, List<LazyFont.DrawableString> valueLines,
                        float headerTextY, float bodyTextY) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.labelText = labelText;
            this.valueLines = valueLines;
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

    private static void addComparedStat(List<StatLine> lines, String label, float current, float base) {
        addComparedStat(lines, label, current, base, "");
    }

    private static void addComparedStat(List<StatLine> lines, String label, float current, float base, String suffix) {
        lines.add(new StatLine(label, formatStat(current) + suffix, colorForComparison(current, base, false)));
    }

    private static void addComparedStatLowerIsBetter(List<StatLine> lines, String label, float current, float base) {
        addComparedStatLowerIsBetter(lines, label, current, base, "");
    }

    private static void addComparedStatLowerIsBetter(List<StatLine> lines, String label, float current, float base, String suffix) {
        lines.add(new StatLine(label, formatStat(current) + suffix, colorForComparison(current, base, true)));
    }

    private static Color colorForComparison(float current, float base, boolean lowerIsBetter) {
        if (current > base + STAT_COMPARISON_EPSILON) return lowerIsBetter ? STAT_DECREASED_COLOR : STAT_INCREASED_COLOR;
        if (current < base - STAT_COMPARISON_EPSILON) return lowerIsBetter ? STAT_INCREASED_COLOR : STAT_DECREASED_COLOR;
        return STAT_PANEL_VALUE_COLOR;
    }

    private static String formatStat(float value) {
        if (value == Math.round(value)) {
            return String.valueOf(Math.round(value));
        }
        return String.format("%.1f", value);
    }
}
