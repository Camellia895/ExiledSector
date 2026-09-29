package exiledsector.skills;

import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import exiledsector.i18n.StyledText;
import exiledsector.i18n.Translation;
import exiledsector.skills.skilleffect.SkillEffect;
import exiledsector.skills.skilleffect.WeaponEffectTooltipAggregator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SkillTreeBonusSummary {

    public record Summary(SkillType root, int level, int nodeCount, List<SkillType> notables, List<DescriptionLine> bonuses) {
    }

    private record Group(Float temporarySeconds) {
    }

    private SkillTreeBonusSummary() {
    }

    public static Summary of(ShipSkillData data, HullSize hullSize) {
        SkillType root = null;
        int nodeCount = 0;
        List<SkillType> notables = new ArrayList<>();
        Map<Group, Map<SkillEffect, Float>> totals = new LinkedHashMap<>();
        for (AllocatedNode allocated : AllocatedNode.of(data)) {
            SkillType type = allocated.effectiveType();
            SkillTier tier = allocated.node().getType().getTier();
            if (tier == SkillTier.ROOT && root == null) {
                root = type;
            } else {
                nodeCount++;
            }
            if (tier == SkillTier.NOTABLE || tier == SkillTier.KEYSTONE) {
                notables.add(type);
            }
            Map<SkillEffect, Float> group = totals.computeIfAbsent(new Group(type.getTemporaryAfterDeploymentSeconds()),
                    key -> new LinkedHashMap<>());
            for (SkillTypeEffect typeEffect : type.effectsFor(hullSize)) {
                SkillEffect effect = typeEffect.effect();
                if (!data.isNpcBuild() || effect.appliesToNpcShips()) {
                    group.merge(effect, typeEffect.magnitude(), (a, b) -> combine(effect, a, b));
                }
            }
        }
        return new Summary(root, data.getLevel(), nodeCount, notables, describe(totals));
    }

    static float combine(SkillEffect effect, float a, float b) {
        if (isMultiplicative(effect)) {
            return ((1f + a / 100f) * (1f + b / 100f) - 1f) * 100f;
        }
        return a + b;
    }

    private static boolean isMultiplicative(SkillEffect effect) {
        return effect.name().endsWith("_MULT");
    }

    private static List<DescriptionLine> describe(Map<Group, Map<SkillEffect, Float>> totals) {
        List<DescriptionLine> lines = new ArrayList<>();
        for (Map.Entry<Group, Map<SkillEffect, Float>> group : totals.entrySet()) {
            List<SkillTypeEffect> effects = new ArrayList<>();
            group.getValue().forEach((effect, total) -> effects.add(new SkillTypeEffect(effect, rounded(total))));
            for (SkillTypeEffect effect : WeaponEffectTooltipAggregator.collapse(effects)) {
                StyledText text = effect.effect().description(effect.magnitude());
                if (text != null) {
                    lines.add(new DescriptionLine(withDuration(text, group.getKey().temporarySeconds()),
                            effect.effect().lowerIsBetter()));
                }
            }
        }
        return lines;
    }

    static float rounded(float total) {
        return Math.round(total * 100f) / 100f;
    }

    private static StyledText withDuration(StyledText text, Float seconds) {
        if (seconds == null) {
            return text;
        }
        return Translation.msg("summary.forFirst").arg("seconds", seconds).arg("text", text).styled();
    }
}
