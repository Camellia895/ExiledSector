package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import static exiledsector.skills.skilleffect.SkillEffectText.pctChange;
import static exiledsector.skills.skilleffect.SkillEffectText.pctMore;
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
            return flatChange(magnitude, "cargo capacity per fighter bay");
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
    SENSOR_PROFILE_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getSensorProfile(), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "sensor profile");
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
    SENSOR_STRENGTH_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getSensorStrength().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "sensor strength");
        }
    },
    COMBAT_VISION {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getSightRadiusMod().modifyFlat(modId, magnitude);
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
    CR_LOSS_PER_SECOND_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getCRLossPerSecondPercent(), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "rate of combat readiness loss from extended deployment");
        }
    },
    MIN_CREW_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getMinCrewMod(), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, STAT_MIN_CREW_REQUIRED);
        }
    },
    MIN_CREW_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMinCrewMod().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, STAT_MIN_CREW_REQUIRED);
        }
    },
    MIN_CREW_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMinCrewMod().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, STAT_MIN_CREW_REQUIRED);
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
            return flatChange(magnitude, "minimum crew required per fighter bay");
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
            return pctChange(magnitude, "minimum crew required per fighter bay (capped at -80% total)");
        }
    },
    SUPPLIES_PER_MONTH_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getSuppliesPerMonth(), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "supply use for maintenance");
        }
    },
    FUEL_USE_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getFuelUseMod(), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "fuel consumption rate");
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
            return "Removes the penalties of a civilian-grade hull.";
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
    CREW_LOSS_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getCrewLossMult(), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "crew casualties");
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
    },
    CORONA_RESISTANCE_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "combat readiness loss from being in a solar corona or a deep "
                    + "hyperspace storm");
        }
    },
    POST_BATTLE_SALVAGE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            // stashed on this ship's own stats (reset automatically along with everything else
            // whenever this ship's stats are rebuilt) rather than written straight to the fleet,
            // since post-battle salvage is a real fleet-level dynamic stat (Stats.BATTLE_SALVAGE_
            // MULT_FLEET, confirmed via the vanilla Salvaging skill and the Second In Command mod's
            // Piracy "Legitimate Salvage" perk) with no per-ship equivalent - recomputeFleetSalvageBonus
            // below sums every current fleet member's stashed contribution into one fleet-wide modifier
            stats.getDynamic().getMod(POST_BATTLE_SALVAGE_CONTRIBUTION_KEY).modifyFlat(modId, magnitude);
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            recomputeFleetSalvageBonus();
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "post-battle salvage recovered (fleet-wide)");
        }
    };

    private static final String STAT_MIN_CREW_REQUIRED = "minimum crew required";
    private static final String POST_BATTLE_SALVAGE_CONTRIBUTION_KEY = "exiledSector_postBattleSalvageContribution";
    private static final String POST_BATTLE_SALVAGE_FLEET_MOD_ID = "exiledSector_postBattleSalvage";

    private static void recomputeFleetSalvageBonus() {
        CampaignFleetAPI fleet = Global.getSector() != null ? Global.getSector().getPlayerFleet() : null;
        if (fleet == null) {
            return;
        }
        float totalPercent = 0f;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            totalPercent += member.getStats().getDynamic().getValue(POST_BATTLE_SALVAGE_CONTRIBUTION_KEY, 0f);
        }
        fleet.getStats().getDynamic().getStat(Stats.BATTLE_SALVAGE_MULT_FLEET)
                .modifyFlat(POST_BATTLE_SALVAGE_FLEET_MOD_ID, totalPercent / 100f);
    }
}
