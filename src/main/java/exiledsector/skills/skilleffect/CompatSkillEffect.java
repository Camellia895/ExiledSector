package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import exiledsector.compat.SecondInCommandCompat;

public enum CompatSkillEffect implements SkillEffect {

    COUNTS_AS_SHIELD_SHUNT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            String id = synergyId(modId, SecondInCommandCompat.REDISTRIBUTION_SKILL_ID);
            if (isSkillActive(stats, SecondInCommandCompat.REDISTRIBUTION_SKILL_ID)) {
                stats.getFluxDissipation().modifyPercent(id, 5f);
                stats.getArmorBonus().modifyPercent(id, 10f);
                stats.getEmpDamageTakenMult().modifyMult(id, 0.75f);
            } else {
                stats.getFluxDissipation().unmodify(id);
                stats.getArmorBonus().unmodify(id);
                stats.getEmpDamageTakenMult().unmodify(id);
            }
        }

        @Override
        public String describe(float magnitude) {
            return synergyDescription("Shield Shunt", "Redistribution");
        }
    },
    COUNTS_AS_SAFETY_OVERRIDES {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            String id = synergyId(modId, SecondInCommandCompat.ENHANCED_OVERRIDES_SKILL_ID);
            if (isSkillActive(stats, SecondInCommandCompat.ENHANCED_OVERRIDES_SKILL_ID)) {
                stats.getPeakCRDuration().modifyPercent(id, 25f);
                stats.getWeaponRangeThreshold().modifyFlat(id, 100f);
            } else {
                stats.getPeakCRDuration().unmodify(id);
                stats.getWeaponRangeThreshold().unmodify(id);
            }
        }

        @Override
        public String describe(float magnitude) {
            return synergyDescription("Safety Overrides", "Enhanced Overrides");
        }
    },
    CONVERTED_HANGAR_REFIT_TIME_MULT(FighterSkillEffect.FIGHTER_REFIT_TIME_MULT, Stats.CONVERTED_HANGAR_NO_REFIT_PENALTY),
    CONVERTED_HANGAR_REPLACEMENT_RATE_MULT(FighterSkillEffect.FIGHTER_REPLACEMENT_RATE_MULT, Stats.CONVERTED_HANGAR_NO_REFIT_PENALTY),
    CONVERTED_HANGAR_RELAUNCH_TIME_FLAT(FighterSkillEffect.FIGHTER_RELAUNCH_TIME_FLAT, Stats.CONVERTED_HANGAR_NO_REARM_INCREASE),
    CONVERTED_HANGAR_MIN_CREW_FLAT(LogisticsSkillEffect.MIN_CREW_FLAT, Stats.CONVERTED_HANGAR_NO_CREW_INCREASE);

    private final SkillEffect penalty;
    private final String waiverStatId;

    CompatSkillEffect() {
        this(null, null);
    }

    CompatSkillEffect(SkillEffect penalty, String waiverStatId) {
        this.penalty = penalty;
        this.waiverStatId = waiverStatId;
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        penalty.apply(stats, modId, isConvertedHangarPenaltyWaived(stats, waiverStatId) ? 0f : magnitude);
    }

    @Override
    public boolean supportsTemporaryGating() {
        return penalty != null;
    }

    @Override
    public String describe(float magnitude) {
        String text = penalty.describe(magnitude);
        if (!SecondInCommandCompat.isModEnabled()) {
            return text;
        }
        return text + " Waived by the Second-in-Command skill Reconfiguration.";
    }

    private static boolean isConvertedHangarPenaltyWaived(MutableShipStatsAPI stats, String waiverStatId) {
        return stats.getDynamic().getMod(waiverStatId).computeEffective(0f) > 0f
                || isSkillActive(stats, SecondInCommandCompat.RECONFIGURATION_SKILL_ID);
    }

    private static boolean isSkillActive(MutableShipStatsAPI stats, String skillId) {
        return SecondInCommandCompat.isSkillActive(stats.getFleetMember(), skillId);
    }

    private static String synergyId(String modId, String skillId) {
        return modId + "_" + skillId;
    }

    private static String synergyDescription(String hullModName, String skillName) {
        if (!SecondInCommandCompat.isModEnabled()) {
            return null;
        }
        return "Counts as the " + hullModName + " hull mod for the Second-in-Command skill " + skillName + ".";
    }
}
