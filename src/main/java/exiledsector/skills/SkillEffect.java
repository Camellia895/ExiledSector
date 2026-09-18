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
