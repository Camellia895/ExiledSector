package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.PhaseField;

import java.util.ArrayList;
import java.util.List;

import static exiledsector.skills.skilleffect.SkillEffectText.pct;
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
        public boolean supportsTemporaryGating() {
            return false;
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
    GROUND_SUPPORT_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(Stats.FLEET_GROUND_SUPPORT).modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "effective strength of planetary raids, up to the total number of marines in the fleet");
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
            stats.getDynamic().getMod(POST_BATTLE_SALVAGE_CONTRIBUTION_KEY).modifyFlat(modId, magnitude);
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            recomputeFleetSalvageBonus();
        }

        @Override
        public boolean supportsTemporaryGating() {
            return false;
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "post-battle salvage recovered (fleet-wide)");
        }
    },
    PHASE_FIELD_CONTRIBUTION_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(PHASE_FIELD_CONTRIBUTION_KEY).modifyFlat(modId, magnitude);
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            recomputeExtendedPhaseField();
        }

        @Override
        public boolean supportsTemporaryGating() {
            return false;
        }

        @Override
        public String describe(float magnitude) {
            return "Even if this ship isn't a phase ship, " + pct(magnitude) + "% of its sensor strength "
                    + "counts toward the fleet-wide detected-at range reduction that phase ships normally "
                    + "provide (see the Phase Field hull mod) - the more (and stronger-sensor) ships "
                    + "contribute, the less visible the fleet becomes, with diminishing returns. Only "
                    + "applies while the fleet's transponder is off.";
        }
    };

    private static final String STAT_MIN_CREW_REQUIRED = "minimum crew required";
    private static final String POST_BATTLE_SALVAGE_CONTRIBUTION_KEY = "exiledSector_postBattleSalvageContribution";
    private static final String POST_BATTLE_SALVAGE_FLEET_MOD_ID = "exiledSector_postBattleSalvage";
    private static final String PHASE_FIELD_CONTRIBUTION_KEY = "exiledSector_phaseFieldContributionPercent";
    private static final String EXTENDED_PHASE_FIELD_MOD_ID = "exiledSector_extendedPhaseField";

    private static CampaignFleetAPI getPlayerFleet() {
        SectorAPI sector = Global.getSector();
        return sector == null ? null : sector.getPlayerFleet();
    }

    private static void recomputeFleetSalvageBonus() {
        CampaignFleetAPI fleet = getPlayerFleet();
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

    public static void recomputeExtendedPhaseField() {
        CampaignFleetAPI fleet = getPlayerFleet();
        if (fleet == null) {
            return;
        }

        // This replaces vanilla's own Phase Field fleet-wide calculation (which only ever counts
        // real phase ships) with one that also folds in ships with this node, so both share a
        // single pool and a single floor instead of stacking two independently-clamped
        // multipliers (which could otherwise compound below vanilla's intended floor).
        fleet.getStats().getDetectedRangeMod().unmodifyMult(PhaseField.MOD_KEY);

        if (fleet.isTransponderOn()) {
            fleet.getStats().getDetectedRangeMod().unmodifyMult(EXTENDED_PHASE_FIELD_MOD_ID);
            return;
        }

        List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
        float[] profiles = new float[members.size()];
        List<Float> phaseSensorValues = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            FleetMemberAPI member = members.get(i);
            profiles[i] = member.getStats().getSensorProfile().getModifiedValue();
            Float contribution = phaseSensorContribution(member);
            if (contribution != null) {
                phaseSensorValues.add(contribution);
            }
        }

        if (phaseSensorValues.isEmpty()) {
            fleet.getStats().getDetectedRangeMod().unmodifyMult(EXTENDED_PHASE_FIELD_MOD_ID);
            return;
        }

        float[] phaseSensors = new float[phaseSensorValues.size()];
        for (int i = 0; i < phaseSensorValues.size(); i++) {
            phaseSensors[i] = phaseSensorValues.get(i);
        }

        int topShips = Global.getSettings().getInt("maxSensorShips");
        float totalProfile = PhaseField.getTopKValuesSum(profiles, topShips);
        float totalPhaseSensors = PhaseField.getTopKValuesSum(phaseSensors, topShips);
        float total = Math.max(totalProfile + totalPhaseSensors, 1f);
        float mult = Math.max(PhaseField.MIN_FIELD_MULT, Math.min(1f, totalProfile / total));

        fleet.getStats().getDetectedRangeMod()
                .modifyMult(EXTENDED_PHASE_FIELD_MOD_ID, mult, "Phase ships and phase sensor networks in fleet");
    }

    private static Float phaseSensorContribution(FleetMemberAPI member) {
        if (member.isMothballed() || member.getRepairTracker().getCR() < PhaseField.MIN_CR) {
            return null;
        }
        if (member.getVariant().hasHullMod("phasefield")) {
            return member.getStats().getSensorStrength().getModifiedValue();
        }
        float contributionPercent = member.getStats().getDynamic().getValue(PHASE_FIELD_CONTRIBUTION_KEY, 0f);
        if (contributionPercent <= 0f) {
            return null;
        }
        return member.getStats().getSensorStrength().getModifiedValue() * contributionPercent / 100f;
    }
}
