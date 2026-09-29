package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import exiledsector.i18n.StyledText;
import exiledsector.i18n.Translation;

import java.util.List;

import static exiledsector.skills.skilleffect.StatMode.FLAT;
import static exiledsector.skills.skilleffect.StatMode.MULT;
import static exiledsector.skills.skilleffect.StatMode.PERCENT;
import static exiledsector.skills.skilleffect.StatTarget.bonus;
import static exiledsector.skills.skilleffect.StatTarget.dynamicMod;
import static exiledsector.skills.skilleffect.StatTarget.dynamicStat;
import static exiledsector.skills.skilleffect.StatTarget.stat;

public enum LogisticsSkillEffect implements SkillEffect {

    FUEL_CAPACITY_PERCENT(PERCENT, bonus(MutableShipStatsAPI::getFuelMod), "stat.fuelCapacity", false),
    FUEL_CAPACITY_FLAT(FLAT, bonus(MutableShipStatsAPI::getFuelMod), "stat.fuelCapacity", false),
    CARGO_CAPACITY_PERCENT(PERCENT, bonus(MutableShipStatsAPI::getCargoMod), "stat.cargoCapacity", false),
    CARGO_CAPACITY_FLAT(FLAT, bonus(MutableShipStatsAPI::getCargoMod), "stat.cargoCapacity", false),
    CARGO_CAPACITY_PER_FIGHTER_BAY {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            float bays = stats.getNumFighterBays().getBaseValue();
            stats.getCargoMod().modifyFlat(modId, bays * magnitude);
        }

        @Override
        public StyledText description(float magnitude) {
            return StatMode.FLAT.describeStat(magnitude, "stat.cargoCapacityPerFighterBay");
        }
    },
    CREW_CAPACITY_PERCENT(PERCENT, bonus(MutableShipStatsAPI::getMaxCrewMod), "stat.crewCapacity", false),
    CREW_CAPACITY_FLAT(FLAT, bonus(MutableShipStatsAPI::getMaxCrewMod), "stat.crewCapacity", false),
    BURN_LEVEL_FLAT(FLAT, stat(MutableShipStatsAPI::getMaxBurnLevel), "stat.maxBurnLevel", false),
    SENSOR_PROFILE_PERCENT(PERCENT, stat(MutableShipStatsAPI::getSensorProfile), "stat.sensorProfile", true),
    SENSOR_PROFILE_MULT(MULT, stat(MutableShipStatsAPI::getSensorProfile), "stat.sensorProfile", true),
    SENSOR_STRENGTH_PERCENT(PERCENT, stat(MutableShipStatsAPI::getSensorStrength), "stat.sensorStrength", false),
    SENSOR_STRENGTH_FLAT(FLAT, stat(MutableShipStatsAPI::getSensorStrength), "stat.sensorStrength", false),
    COMBAT_VISION(FLAT, bonus(MutableShipStatsAPI::getSightRadiusMod), "stat.inCombatSensorVisionRange", false),
    CR_RECOVERY_RATE_PERCENT(PERCENT, stat(MutableShipStatsAPI::getBaseCRRecoveryRatePercentPerDay),
            "stat.combatReadinessRecoveryRate", false),
    MAX_COMBAT_READINESS_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMaxCombatReadiness().modifyFlat(modId, magnitude / 100f, Translation.gameText("modifier.shipSkillTree"));
        }

        @Override
        public StyledText description(float magnitude) {
            return StatMode.PERCENT.describeStat(magnitude, "stat.maximumCombatReadiness");
        }
    },
    REPAIR_RATE_PER_DAY_PERCENT(PERCENT, stat(MutableShipStatsAPI::getRepairRatePercentPerDay), "stat.repairRatePerDay", false),
    CR_LOSS_PER_SECOND_PERCENT(PERCENT, bonus(MutableShipStatsAPI::getCRLossPerSecondPercent),
            "stat.rateOfCombatReadinessLossFromExtendedDeployment", true),
    CR_LOSS_PER_SECOND_MULT(MULT, bonus(MutableShipStatsAPI::getCRLossPerSecondPercent),
            "stat.rateOfCombatReadinessLossFromExtendedDeployment", true),
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
        public StyledText description(float magnitude) {
            return StatMode.FLAT.describeStat(magnitude, "stat.minimumCrewRequiredPerFighterBay");
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
        public StyledText description(float magnitude) {
            return StatMode.PERCENT.describeStat(magnitude, "stat.minimumCrewRequiredPerFighterBayCappedAt80Total");
        }
    },
    SUPPLIES_PER_MONTH_MULT(MULT, stat(MutableShipStatsAPI::getSuppliesPerMonth), "stat.supplyUseForMaintenance", true),
    FUEL_USE_MULT(MULT, bonus(MutableShipStatsAPI::getFuelUseMod), "stat.fuelConsumptionRate", true),
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
    },
    REQUIRES_CIVILIAN_GRADE_HULL {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public String blockAllocationReason(FleetMemberAPI member, float magnitude, List<SkillEffect> currentlyAllocatedEffects) {
            return member.getVariant().hasHullMod(HullMods.CIVGRADE) ? null : "Requires a civilian-grade hull.";
        }
    },
    CREW_LOSS_PERCENT(PERCENT, stat(MutableShipStatsAPI::getCrewLossMult), "stat.crewCasualties", true),
    CREW_LOSS_MULT(MULT, stat(MutableShipStatsAPI::getCrewLossMult), "stat.crewCasualties", true),
    SURVEY_COST_REDUCTION_HEAVY_MACHINERY(FLAT, dynamicMod("survey_cost_reduction_heavy_machinery"),
            "stat.heavyMachineryRequiredToPerformSurveysFleetWide", false),
    SURVEY_COST_REDUCTION_SUPPLIES(FLAT, dynamicMod("survey_cost_reduction_supplies"),
            "stat.suppliesRequiredToPerformSurveysFleetWide", false),
    GROUND_SUPPORT_FLAT(FLAT, dynamicMod(Stats.FLEET_GROUND_SUPPORT),
            "stat.effectiveStrengthOfPlanetaryRaidsUpToTheTotalNumberOfMarinesInTheFleet", false),
    CORONA_RESISTANCE_MULT(MULT, dynamicStat(Stats.CORONA_EFFECT_MULT),
            "stat.combatReadinessLossFromBeingInASolarCoronaOrADeepHyperspaceStorm", true),
    POST_BATTLE_SALVAGE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(FleetWideEffects.POST_BATTLE_SALVAGE_CONTRIBUTION_KEY).modifyFlat(modId, magnitude);
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
        public StyledText description(float magnitude) {
            return StatMode.PERCENT.describeStat(magnitude, "stat.postBattleSalvageRecoveredFleetWide");
        }
    },
    PHASE_FIELD_CONTRIBUTION_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(FleetWideEffects.PHASE_FIELD_CONTRIBUTION_KEY).modifyFlat(modId, magnitude);
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            FleetWideEffects.recomputeExtendedPhaseField();
        }

        @Override
        public boolean appliesToNpcShips() {
            return false;
        }

        @Override
        public boolean supportsTemporaryGating() {
            return false;
        }
    };

    private final SimpleStatEffect simpleStat;

    LogisticsSkillEffect() {
        this.simpleStat = null;
    }

    LogisticsSkillEffect(StatMode mode, StatTarget target, String statKey, boolean lowerIsBetter) {
        this.simpleStat = new SimpleStatEffect(mode, target, statKey, lowerIsBetter);
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        simpleStat.apply(stats, modId, magnitude);
    }

    @Override
    public StyledText description(float magnitude) {
        return simpleStat != null ? simpleStat.description(magnitude) : EffectText.templated(this, magnitude);
    }

    @Override
    public boolean lowerIsBetter() {
        return simpleStat != null && simpleStat.lowerIsBetter();
    }

    private static final class StatNames {
        static final String MIN_CREW_REQUIRED = "stat.minimumCrewRequired";

        private StatNames() {
        }
    }
}
