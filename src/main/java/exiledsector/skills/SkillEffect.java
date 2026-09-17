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
    };

    public abstract void apply(MutableShipStatsAPI stats, String modId, float magnitude);

    public abstract String describe(float magnitude);

    static String pct(float magnitude) {
        if (magnitude == Math.rint(magnitude)) {
            return String.valueOf((int) magnitude);
        }
        return String.valueOf(magnitude);
    }
}
