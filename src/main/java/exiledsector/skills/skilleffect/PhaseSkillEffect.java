package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import org.lwjgl.util.vector.Vector2f;

import java.util.Map;

import static exiledsector.skills.skilleffect.SkillEffectText.pct;
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
    PHASE_CLOAK_FLUX_THRESHOLD_PERCENT {
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
            return pctChange(magnitude, "flux dissipation, weapon rate of fire, and ammo regeneration while phased");
        }
    },
    PHASE_ANCHOR_EMERGENCY_DIVE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            if (!ship.hasListenerOfClass(PhaseAnchorDiveListener.class)) {
                ship.addListener(new PhaseAnchorDiveListener(ship, magnitude));
            }
        }

        @Override
        public String describe(float magnitude) {
            return "While at risk of being destroyed or disabled, this ship can instead perform an emergency dive "
                    + "into phase space, retreating from battle and suffering an additional combat readiness "
                    + "penalty equal to " + pct(magnitude) + "% of its deployment cost. The ship must have enough "
                    + "combat readiness to cover this cost. Only one ship per battle, regardless of side, can "
                    + "perform this dive.";
        }
    };

    @Override
    public abstract void apply(MutableShipStatsAPI stats, String modId, float magnitude);

    @Override
    public abstract String describe(float magnitude);

    private static final class PhaseAnchorDiveListener implements HullDamageAboutToBeTakenListener, AdvanceableListener {

        private static final String DIVE_FLAG_KEY = "phaseAnchor_canDive";

        private final ShipAPI ship;
        private final float crPenaltyMult;
        private boolean diving;

        private PhaseAnchorDiveListener(ShipAPI ship, float magnitude) {
            this.ship = ship;
            this.crPenaltyMult = magnitude / 100f;
        }

        @Override
        public boolean notifyAboutToTakeHullDamage(Object param, ShipAPI ship, Vector2f point, float damageAmount) {
            if (diving) {
                return true;
            }
            if (damageAmount < ship.getHitpoints()) {
                return false;
            }

            Map<String, Object> customData = Global.getCombatEngine().getCustomData();
            if (customData.containsKey(DIVE_FLAG_KEY)) {
                return false;
            }

            FleetMemberAPI member = ship.getFleetMember();
            float deployCost = member != null ? member.getDeployCost() : 0f;
            float crCost = crPenaltyMult * deployCost;
            if (ship.getCurrentCR() < crCost) {
                return false;
            }

            ship.setHitpoints(1f);
            if (member != null) {
                member.getRepairTracker().applyCREvent(-crCost, "Emergency phase dive");
            }
            diving = true;
            customData.put(DIVE_FLAG_KEY, Boolean.TRUE);
            return true;
        }

        @Override
        public void advance(float amount) {
            if (!diving) {
                return;
            }
            ShipSystemAPI phaseCloak = ship.getPhaseCloak();
            if (phaseCloak != null) {
                phaseCloak.forceState(ShipSystemAPI.SystemState.IN, 1f);
            }
            ship.setRetreating(true, false);
            ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
            ship.getMutableStats().getHullDamageTakenMult().modifyMult(DIVE_FLAG_KEY, 0f);
        }
    }
}
