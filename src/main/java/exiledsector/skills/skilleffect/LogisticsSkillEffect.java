package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

import static exiledsector.skills.skilleffect.SkillEffectText.pctChange;
import static exiledsector.skills.skilleffect.SkillEffectText.flatChange;

public enum LogisticsSkillEffect implements SkillEffect {

    FUEL_CAPACITY_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getFuelMod().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "fuel capacity");
        }
    },
    FUEL_CAPACITY_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getFuelMod().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "fuel capacity");
        }
    },
    CARGO_CAPACITY_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getCargoMod().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "cargo capacity");
        }
    },
    CARGO_CAPACITY_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getCargoMod().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "cargo capacity");
        }
    },
    CARGO_CAPACITY_PER_FIGHTER_BAY {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            float bays = stats.getNumFighterBays().getBaseValue();
            stats.getCargoMod().modifyFlat(modId, bays * magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "cargo capacity, per fighter bay");
        }
    },
    CREW_CAPACITY_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMaxCrewMod().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "crew capacity");
        }
    },
    CREW_CAPACITY_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMaxCrewMod().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "crew capacity");
        }
    },
    BURN_LEVEL_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMaxBurnLevel().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "max burn level");
        }
    },
    SENSOR_PROFILE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getSensorProfile().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "sensor profile");
        }
    },
    SENSOR_STRENGTH_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getSensorStrength().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "sensor strength");
        }
    },
    SENSOR_RANGE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod("hrs_sensor_range_mod").modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "in-combat sensor/vision range");
        }
    },
    CR_RECOVERY_RATE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getBaseCRRecoveryRatePercentPerDay().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "combat readiness recovery rate");
        }
    },
    REPAIR_RATE_PER_DAY_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getRepairRatePercentPerDay().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "repair rate per day");
        }
    },
    CR_LOSS_PER_SECOND_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getCRLossPerSecondPercent().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "rate of combat readiness loss from extended deployment");
        }
    },
    MIN_CREW_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMinCrewMod().modifyMult(modId, 1f + magnitude / 100f);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "minimum crew required");
        }
    },
    MIN_CREW_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMinCrewMod().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "minimum crew required");
        }
    },
    MIN_CREW_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMinCrewMod().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "minimum crew required");
        }
    },
    MIN_CREW_PER_FIGHTER_BAY {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            float bays = stats.getNumFighterBays().getBaseValue();
            stats.getMinCrewMod().modifyFlat(modId, bays * magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "minimum crew required, per fighter bay");
        }
    },
    MIN_CREW_PERCENT_PER_FIGHTER_BAY {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            float bays = stats.getNumFighterBays().getBaseValue();
            float total = Math.max(bays * magnitude, -80f);
            stats.getMinCrewMod().modifyPercent(modId, total);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "minimum crew required, per fighter bay (capped at -80% total)");
        }
    },
    SUPPLIES_PER_MONTH_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getSuppliesPerMonth().modifyMult(modId, 1f + magnitude / 100f);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "supply use for maintenance");
        }
    },
    FUEL_USE_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getFuelUseMod().modifyMult(modId, 1f + magnitude / 100f);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "fuel use rate");
        }
    },
    REMOVE_CIVILIAN_HULL_PENALTY {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getSensorStrength().unmodify("civgrade");
            stats.getSensorProfile().unmodify("civgrade");
        }

        @Override
        public String describe(float magnitude) {
            return "Removes the sensor strength and sensor profile penalties of a civilian-grade hull.";
        }
    },
    CREW_LOSS_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getCrewLossMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "crew casualties");
        }
    },
    SURVEY_COST_REDUCTION_HEAVY_MACHINERY {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod("survey_cost_reduction_heavy_machinery").modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "heavy machinery required to perform surveys (fleet-wide)");
        }
    },
    SURVEY_COST_REDUCTION_SUPPLIES {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod("survey_cost_reduction_supplies").modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "supplies required to perform surveys (fleet-wide)");
        }
    };

    @Override
    public abstract void apply(MutableShipStatsAPI stats, String modId, float magnitude);

    @Override
    public abstract String describe(float magnitude);
}
