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
    ENERGY_DAMAGE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            // The game tracks beams as a separate stat from other energy
            // weapons, but a player picking "energy weapon damage" expects
            // it to cover beams too.
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
