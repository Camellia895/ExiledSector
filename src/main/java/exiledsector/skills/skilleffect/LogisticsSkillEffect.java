package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.PhaseField;

import java.util.ArrayList;
import java.util.List;

import static exiledsector.skills.skilleffect.SkillEffectText.flatChange;
import static exiledsector.skills.skilleffect.SkillEffectText.pct;
import static exiledsector.skills.skilleffect.SkillEffectText.pctChange;
import static exiledsector.skills.skilleffect.StatMode.FLAT;
import static exiledsector.skills.skilleffect.StatMode.MULT;
import static exiledsector.skills.skilleffect.StatMode.PERCENT;
import static exiledsector.skills.skilleffect.StatTarget.bonus;
import static exiledsector.skills.skilleffect.StatTarget.dynamicMod;
import static exiledsector.skills.skilleffect.StatTarget.dynamicStat;
import static exiledsector.skills.skilleffect.StatTarget.stat;

public enum LogisticsSkillEffect implements SkillEffect {

    FUEL_CAPACITY_PERCENT(PERCENT, bonus(MutableShipStatsAPI::getFuelMod), "fuel capacity", false),
    FUEL_CAPACITY_FLAT(FLAT, bonus(MutableShipStatsAPI::getFuelMod), "fuel capacity", false),
    CARGO_CAPACITY_PERCENT(PERCENT, bonus(MutableShipStatsAPI::getCargoMod), "cargo capacity", false),
    CARGO_CAPACITY_FLAT(FLAT, bonus(MutableShipStatsAPI::getCargoMod), "cargo capacity", false),
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
    CREW_CAPACITY_PERCENT(PERCENT, bonus(MutableShipStatsAPI::getMaxCrewMod), "crew capacity", false),
    CREW_CAPACITY_FLAT(FLAT, bonus(MutableShipStatsAPI::getMaxCrewMod), "crew capacity", false),
    BURN_LEVEL_FLAT(FLAT, stat(MutableShipStatsAPI::getMaxBurnLevel), "max burn level", false),
    SENSOR_PROFILE_PERCENT(PERCENT, stat(MutableShipStatsAPI::getSensorProfile), "sensor profile", true),
    SENSOR_PROFILE_MULT(MULT, stat(MutableShipStatsAPI::getSensorProfile), "sensor profile", true),
    SENSOR_STRENGTH_PERCENT(PERCENT, stat(MutableShipStatsAPI::getSensorStrength), "sensor strength", false),
    SENSOR_STRENGTH_FLAT(FLAT, stat(MutableShipStatsAPI::getSensorStrength), "sensor strength", false),
    COMBAT_VISION(FLAT, bonus(MutableShipStatsAPI::getSightRadiusMod), "in-combat sensor/vision range", false),
    CR_RECOVERY_RATE_PERCENT(PERCENT, stat(MutableShipStatsAPI::getBaseCRRecoveryRatePercentPerDay),
            "combat readiness recovery rate", false),
    MAX_COMBAT_READINESS_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMaxCombatReadiness().modifyFlat(modId, magnitude / 100f, "Ship skill tree");
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "maximum combat readiness");
        }
    },
    REPAIR_RATE_PER_DAY_PERCENT(PERCENT, stat(MutableShipStatsAPI::getRepairRatePercentPerDay), "repair rate per day", false),
    CR_LOSS_PER_SECOND_PERCENT(PERCENT, bonus(MutableShipStatsAPI::getCRLossPerSecondPercent),
            "rate of combat readiness loss from extended deployment", true),
    CR_LOSS_PER_SECOND_MULT(MULT, bonus(MutableShipStatsAPI::getCRLossPerSecondPercent),
            "rate of combat readiness loss from extended deployment", true),
    MIN_CREW_MULT(MULT, bonus(MutableShipStatsAPI::getMinCrewMod), StatNames.MIN_CREW_REQUIRED, true),
    MIN_CREW_PERCENT(PERCENT, bonus(MutableShipStatsAPI::getMinCrewMod), StatNames.MIN_CREW_REQUIRED, true),
    MIN_CREW_FLAT(FLAT, bonus(MutableShipStatsAPI::getMinCrewMod), StatNames.MIN_CREW_REQUIRED, true),
    MIN_CREW_PER_FIGHTER_BAY {
        @Override
        public boolean lowerIsBetter() {
            return true;
        }

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
        public boolean lowerIsBetter() {
            return true;
        }

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
    SUPPLIES_PER_MONTH_MULT(MULT, stat(MutableShipStatsAPI::getSuppliesPerMonth), "supply use for maintenance", true),
    FUEL_USE_MULT(MULT, bonus(MutableShipStatsAPI::getFuelUseMod), "fuel consumption rate", true),
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
    REQUIRES_CIVILIAN_GRADE_HULL {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public String blockAllocationReason(FleetMemberAPI member, float magnitude, List<SkillEffect> currentlyAllocatedEffects) {
            return member.getVariant().hasHullMod(HullMods.CIVGRADE) ? null : "Requires a civilian-grade hull.";
        }

        @Override
        public String describe(float magnitude) {
            return "Can only be allocated on civilian-grade hulls.";
        }
    },
    CREW_LOSS_PERCENT(PERCENT, stat(MutableShipStatsAPI::getCrewLossMult), "crew casualties", true),
    CREW_LOSS_MULT(MULT, stat(MutableShipStatsAPI::getCrewLossMult), "crew casualties", true),
    SURVEY_COST_REDUCTION_HEAVY_MACHINERY(FLAT, dynamicMod("survey_cost_reduction_heavy_machinery"),
            "heavy machinery required to perform surveys (fleet-wide)", false),
    SURVEY_COST_REDUCTION_SUPPLIES(FLAT, dynamicMod("survey_cost_reduction_supplies"),
            "supplies required to perform surveys (fleet-wide)", false),
    GROUND_SUPPORT_FLAT(FLAT, dynamicMod(Stats.FLEET_GROUND_SUPPORT),
            "effective strength of planetary raids, up to the total number of marines in the fleet", false),
    CORONA_RESISTANCE_MULT(MULT, dynamicStat(Stats.CORONA_EFFECT_MULT),
            "combat readiness loss from being in a solar corona or a deep hyperspace storm", true),
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
        public boolean appliesToNpcShips() {
            return false;
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
        public boolean appliesToNpcShips() {
            return false;
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

    private static final String POST_BATTLE_SALVAGE_CONTRIBUTION_KEY = "exiledSector_postBattleSalvageContribution";
    private static final String POST_BATTLE_SALVAGE_FLEET_MOD_ID = "exiledSector_postBattleSalvage";
    private static final String PHASE_FIELD_CONTRIBUTION_KEY = "exiledSector_phaseFieldContributionPercent";
    private static final String EXTENDED_PHASE_FIELD_MOD_ID = "exiledSector_extendedPhaseField";

    private final SimpleStatEffect simpleStat;

    LogisticsSkillEffect() {
        this.simpleStat = null;
    }

    LogisticsSkillEffect(StatMode mode, StatTarget target, String statName, boolean lowerIsBetter) {
        this.simpleStat = new SimpleStatEffect(mode, target, statName, lowerIsBetter);
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        simpleStat.apply(stats, modId, magnitude);
    }

    @Override
    public String describe(float magnitude) {
        return simpleStat.describe(magnitude);
    }

    @Override
    public boolean lowerIsBetter() {
        return simpleStat != null && simpleStat.lowerIsBetter();
    }

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

    private static final class StatNames {
        static final String MIN_CREW_REQUIRED = "minimum crew required";

        private StatNames() {
        }
    }
}
