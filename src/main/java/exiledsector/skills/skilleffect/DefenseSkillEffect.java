package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import static exiledsector.skills.skilleffect.SkillEffectText.pct;
import static exiledsector.skills.skilleffect.SkillEffectText.pctChange;
import static exiledsector.skills.skilleffect.SkillEffectText.pctMore;
import static exiledsector.skills.skilleffect.SkillEffectText.flatChange;

public enum DefenseSkillEffect implements SkillEffect {

    HULL_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getHullBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, STAT_HULL_POINTS);
        }
    },
    HULL_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getHullBonus().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, STAT_HULL_POINTS);
        }
    },
    HULL_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getHullBonus(), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, STAT_HULL_POINTS);
        }
    },
    ARMOR_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getArmorBonus().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "armor");
        }
    },
    SHIP_RECOVERY_CHANCE_BONUS {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return "If disabled, this ship is almost always recoverable after the battle.";
        }
    },
    BREAK_PROBABILITY_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getBreakProb().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "chance of this ship breaking apart when destroyed");
        }
    },
    ARMOR_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getArmorBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "armor");
        }
    },
    ARMOR_DAMAGE_TAKEN_MULT_PER_DMOD {
        @Override
        public boolean lowerIsBetter() {
            return true;
        }

        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getArmorDamageTakenMult().modifyMult(modId, SkillEffectSupport.compoundMultPerDMod(stats, magnitude));
        }

        @Override
        public String describe(float magnitude) {
            String verb = magnitude >= 0 ? "more" : "less";
            return pct(Math.abs(magnitude)) + "% " + verb + " armor damage taken per D-mod ";
        }
    },
    DMOD_EFFECT_MULT {
        @Override
        public boolean lowerIsBetter() {
            return true;
        }

        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyDModEffectMult(stats, modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "negative effects from D-mods");
        }
    },
    SHIELD_ABSORPTION_PERCENT {
        @Override
        public boolean lowerIsBetter() {
            return true;
        }

        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getShieldAbsorptionMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "damage taken by shields");
        }
    },
    SHIELD_DAMAGE_TAKEN_MULT {
        @Override
        public boolean lowerIsBetter() {
            return true;
        }

        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getShieldDamageTakenMult(), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "damage taken by shields");
        }
    },
    ENGINE_DURABILITY_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getEngineHealthBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "engine durability");
        }
    },
    REPAIR_TIME_PERCENT {
        @Override
        public boolean lowerIsBetter() {
            return true;
        }

        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getCombatWeaponRepairTimeMult().modifyPercent(modId, magnitude);
            stats.getCombatEngineRepairTimeMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "weapon and engine repair time");
        }
    },
    REPAIR_TIME_MULT {
        @Override
        public boolean lowerIsBetter() {
            return true;
        }

        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            float mult = SkillEffectSupport.multFrom(magnitude);
            stats.getCombatWeaponRepairTimeMult().modifyMult(modId, mult);
            stats.getCombatEngineRepairTimeMult().modifyMult(modId, mult);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "weapon and engine repair time");
        }
    },
    EMP_DAMAGE_TAKEN_PERCENT {
        @Override
        public boolean lowerIsBetter() {
            return true;
        }

        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getEmpDamageTakenMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "EMP damage taken");
        }
    },
    EMP_DAMAGE_TAKEN_MULT {
        @Override
        public boolean lowerIsBetter() {
            return true;
        }

        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getEmpDamageTakenMult(), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "EMP damage taken");
        }
    },
    ENERGY_DAMAGE_TAKEN_PERCENT {
        @Override
        public boolean lowerIsBetter() {
            return true;
        }

        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getEnergyDamageTakenMult().modifyPercent(modId, magnitude);
            stats.getEnergyShieldDamageTakenMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "energy damage taken, including hits on shields, armor, and hull");
        }
    };

    private static final String STAT_HULL_POINTS = "hull points";
}
