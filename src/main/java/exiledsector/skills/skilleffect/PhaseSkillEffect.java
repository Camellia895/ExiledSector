package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;

import static exiledsector.skills.skilleffect.SkillEffectText.pctChange;

public enum PhaseSkillEffect implements SkillEffect {

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
    COMBAT_BOOST_WHILE_PHASED {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public boolean isConditional() {
            return true;
        }

        @Override
        public void advanceInCombat(ShipAPI ship, String modId, float magnitude) {
            boolean active = ship.isPhased();
            ShipSystemAPI phaseCloak = ship.getPhaseCloak();
            if (active && phaseCloak != null && phaseCloak.isChargedown()) {
                active = false;
            }

            MutableShipStatsAPI stats = ship.getMutableStats();
            float mult = 1f + magnitude / 100f;
            MutableStat[] boosted = {
                    stats.getFluxDissipation(),
                    stats.getBallisticRoFMult(),
                    stats.getEnergyRoFMult(),
                    stats.getMissileRoFMult(),
                    stats.getBallisticAmmoRegenMult(),
                    stats.getEnergyAmmoRegenMult(),
                    stats.getMissileAmmoRegenMult()
            };
            for (MutableStat stat : boosted) {
                if (active) {
                    stat.modifyMult(modId, mult);
                } else {
                    stat.unmodifyMult(modId);
                }
            }
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "flux dissipation, weapon rate of fire, and ammo regen while phased");
        }
    };

    @Override
    public abstract void apply(MutableShipStatsAPI stats, String modId, float magnitude);

    @Override
    public abstract String describe(float magnitude);
}
