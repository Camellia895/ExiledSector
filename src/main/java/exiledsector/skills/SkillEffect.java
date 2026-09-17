package exiledsector.skills;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * A percent stat bonus a SkillType can apply to a ship. Each unlocked node
 * is applied by SkillTreeHullMod on every stat recompute using modifyPercent
 * keyed by the node's own id, which replaces rather than stacks a prior
 * entry for that id - so re-applying every time is safely idempotent and no
 * explicit "remove" step is needed.
 */
public enum SkillEffect {

    HULL {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getHullBonus().modifyPercent(modId, magnitude);
        }
    },
    ARMOR {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getArmorBonus().modifyPercent(modId, magnitude);
        }
    },
    FLUX_DISSIPATION {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getFluxDissipation().modifyPercent(modId, magnitude);
        }
    },
    BALLISTIC_DAMAGE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getBallisticWeaponDamageMult().modifyPercent(modId, magnitude);
        }
    },
    MISSILE_DAMAGE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMissileWeaponDamageMult().modifyPercent(modId, magnitude);
        }
    },
    // Non-beam energy weapons only (e.g. Heavy Blaster, Ion Cannon) - named
    // to match vanilla's own "non-beam energy weapons" terminology (see the
    // Energy Bolt Coherer hullmod tooltip), since "projectile energy weapon"
    // has no precedent anywhere in the game's data or text.
    NON_BEAM_ENERGY_DAMAGE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getEnergyWeaponDamageMult().modifyPercent(modId, magnitude);
        }
    },
    // Beam weapons only (e.g. Tachyon Lance, Phase Lance) - the game tracks
    // these as a separate stat from other energy weapons.
    BEAM_DAMAGE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getBeamWeaponDamageMult().modifyPercent(modId, magnitude);
        }
    },
    // Hybrid skill covering both of the above, for a player who doesn't
    // want to specialize into one or the other.
    ENERGY_DAMAGE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getEnergyWeaponDamageMult().modifyPercent(modId, magnitude);
            stats.getBeamWeaponDamageMult().modifyPercent(modId, magnitude);
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
    };

    public abstract void apply(MutableShipStatsAPI stats, String modId, float magnitude);
}
