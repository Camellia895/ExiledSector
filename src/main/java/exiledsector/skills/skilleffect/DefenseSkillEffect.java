package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import static exiledsector.skills.skilleffect.SkillEffectText.pct;
import static exiledsector.skills.skilleffect.SkillEffectText.pctChange;
import static exiledsector.skills.skilleffect.SkillEffectText.flatChange;

public enum DefenseSkillEffect implements SkillEffect {

    HULL_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getHullBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return "Increases hull points by " + pct(magnitude) + "%.";
        }
    },
    ARMOR_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getArmorBonus().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "armor rating");
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
            return "Increases armor rating by " + pct(magnitude) + "%.";
        }
    },
    SHIELD_ABSORPTION_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getShieldAbsorptionMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "damage taken by shields");
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
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getCombatWeaponRepairTimeMult().modifyPercent(modId, magnitude);
            stats.getCombatEngineRepairTimeMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "weapon and engine repair time");
        }
    },
    EMP_DAMAGE_TAKEN_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getEmpDamageTakenMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "EMP damage taken");
        }
    },
    ENERGY_DAMAGE_TAKEN_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getEnergyDamageTakenMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "energy damage taken");
        }
    };

    @Override
    public abstract void apply(MutableShipStatsAPI stats, String modId, float magnitude);

    @Override
    public abstract String describe(float magnitude);
}
