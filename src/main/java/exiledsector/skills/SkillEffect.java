package exiledsector.skills;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

public enum SkillEffect {

    HULL {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getHullBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return "Increases hull points by " + pct(magnitude) + "%.";
        }
    },
    ARMOR {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getArmorBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return "Increases armor rating by " + pct(magnitude) + "%.";
        }
    },
    FLUX_CAPACITY {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getFluxCapacity().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return "Increases flux capacity by " + pct(magnitude) + "%.";
        }
    },
    FLUX_CAPACITY_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getFluxCapacity().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "flux capacity");
        }
    },
    FLUX_DISSIPATION {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getFluxDissipation().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return "Increases flux dissipation by " + pct(magnitude) + "%.";
        }
    },
    FLUX_DISSIPATION_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getFluxDissipation().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "flux dissipation");
        }
    },
    HYBRID_FLUX {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getFluxCapacity().modifyPercent(modId, magnitude);
            stats.getFluxDissipation().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return "Increases flux capacity and dissipation by " + pct(magnitude) + "% each.";
        }
    },
    BALLISTIC_DAMAGE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getBallisticWeaponDamageMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return "Increases ballistic weapon damage by " + pct(magnitude) + "%.";
        }
    },
    MISSILE_DAMAGE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMissileWeaponDamageMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return "Increases missile weapon damage by " + pct(magnitude) + "%.";
        }
    },
    NON_BEAM_ENERGY_DAMAGE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getEnergyWeaponDamageMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return "Increases non-beam energy weapon damage by " + pct(magnitude) + "%.";
        }
    },
    BEAM_DAMAGE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getBeamWeaponDamageMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return "Increases beam weapon damage by " + pct(magnitude) + "%.";
        }
    },
    ENERGY_DAMAGE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getEnergyWeaponDamageMult().modifyPercent(modId, magnitude);
            stats.getBeamWeaponDamageMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return "Increases beam and non-beam energy weapon damage by " + pct(magnitude) + "%.";
        }
    },
    ALL_WEAPON_DAMAGE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getBallisticWeaponDamageMult().modifyPercent(modId, magnitude);
            stats.getMissileWeaponDamageMult().modifyPercent(modId, magnitude);
            stats.getEnergyWeaponDamageMult().modifyPercent(modId, magnitude);
            stats.getBeamWeaponDamageMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return "Increases damage of all weapon types by " + pct(magnitude) + "%.";
        }
    },
    MANEUVERABILITY {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMaxTurnRate().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "maneuverability");
        }
    },
    FUEL_CAPACITY {
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
    CARGO_CAPACITY {
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
    CREW_CAPACITY {
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
    BURN_LEVEL {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMaxBurnLevel().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "max burn level");
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
    SENSOR_PROFILE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getSensorProfile().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "sensor profile");
        }
    },
    SENSOR_STRENGTH {
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
    ELECTRONIC_WARFARE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod("electronic_warfare_flat").modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "ECM rating");
        }
    },
    NAV_RATING {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod("coord_maneuvers_flat").modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "fleet nav rating");
        }
    },
    BALLISTIC_WEAPON_RANGE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getBallisticWeaponRangeBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "ballistic weapon range");
        }
    },
    ENERGY_WEAPON_RANGE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getEnergyWeaponRangeBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "energy weapon range");
        }
    },
    BEAM_WEAPON_RANGE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getBeamWeaponRangeBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "beam weapon range");
        }
    },
    BALLISTIC_AMMO {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getBallisticAmmoBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "ballistic weapon ammo capacity");
        }
    },
    ENERGY_AMMO {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getEnergyAmmoBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "energy weapon ammo/charge capacity");
        }
    },
    MISSILE_AMMO {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMissileAmmoBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "missile weapon ammo capacity");
        }
    },
    WEAPON_TURN_RATE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getWeaponTurnRateBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "turret turn rate");
        }
    },
    SHIELD_ARC {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getShieldArcBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "shield arc");
        }
    },
    SHIELD_UPKEEP {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getShieldUpkeepMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "shield flux upkeep");
        }
    },
    SHIELD_ABSORPTION {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getShieldAbsorptionMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "damage taken by shields");
        }
    },
    SHIELD_TURN_RATE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getShieldTurnRateMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "shield turn rate");
        }
    },
    SHIELD_RAISE_RATE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getShieldUnfoldRateMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "shield raise rate");
        }
    },
    WEAPON_DURABILITY {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getWeaponHealthBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "weapon durability");
        }
    },
    ENGINE_DURABILITY {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getEngineHealthBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "engine durability");
        }
    },
    TOP_SPEED {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMaxSpeed().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "top speed");
        }
    },
    PEAK_CR_DURATION {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getPeakCRDuration().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "peak combat readiness duration");
        }
    },
    WEAPON_RANGE_FALLOFF {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getWeaponRangeMultPastThreshold().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "weapon effectiveness past normal range");
        }
    },
    REPAIR_TIME {
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
    MISSILE_GUIDANCE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMissileGuidance().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "missile guidance");
        }
    },
    CR_RECOVERY_RATE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getBaseCRRecoveryRatePercentPerDay().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "combat readiness recovery rate");
        }
    },
    REPAIR_RATE_PER_DAY {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getRepairRatePercentPerDay().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "repair rate per day");
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
    PHASE_CLOAK_ACTIVATION_COST_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getPhaseCloakActivationCostBonus().modifyMult(modId, 1f + magnitude / 100f);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "phase cloak activation cost");
        }
    },
    PHASE_CLOAK_FLUX_THRESHOLD {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod("phase_cloak_flux_level_for_min_speed_mod").modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "hard flux threshold before phase speed penalty kicks in");
        }
    },
    REMOVE_CIVILIAN_HULL_PENALTY {
        // Magnitude is unused - this removes a specific vanilla-applied penalty (id "civgrade") rather than
        // adding a tunable bonus. It's a no-op on ships that never had that penalty in the first place.
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
    CREW_LOSS {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getCrewLossMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "crew casualties");
        }
    },
    EMP_DAMAGE_TAKEN {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getEmpDamageTakenMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "EMP damage taken");
        }
    },
    FLUX_DISSIPATION_WHILE_VENTING {
        // Real vanilla effect only applies this while venting - we don't have a condition system yet,
        // so for now this is a permanent bonus, same math as FLUX_DISSIPATION. Kept as its own named
        // effect so it can be switched to a real "only while venting" check later without touching
        // any node data - just this apply() method.
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getFluxDissipation().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "flux dissipation rate");
        }
    },
    ENERGY_DAMAGE_TAKEN {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getEnergyDamageTakenMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "energy damage taken");
        }
    };

    public abstract void apply(MutableShipStatsAPI stats, String modId, float magnitude);

    public abstract String describe(float magnitude);

    static String pct(float magnitude) {
        if (magnitude == Math.rint(magnitude)) {
            return String.valueOf((int) magnitude);
        }
        return String.valueOf(magnitude);
    }

    static String pctChange(float magnitude, String stat) {
        String verb = magnitude >= 0 ? "Increases " : "Decreases ";
        return verb + stat + " by " + pct(Math.abs(magnitude)) + "%.";
    }

    static String flatChange(float magnitude, String stat) {
        String verb = magnitude >= 0 ? "Increases " : "Decreases ";
        return verb + stat + " by " + pct(Math.abs(magnitude)) + ".";
    }
}
