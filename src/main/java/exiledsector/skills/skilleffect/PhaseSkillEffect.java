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
import com.fs.starfarer.api.impl.campaign.skills.NeuralLinkScript;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;
import java.util.Map;

import static exiledsector.skills.skilleffect.SkillEffectText.pct;
import static exiledsector.skills.skilleffect.SkillEffectText.pctChange;
import static exiledsector.skills.skilleffect.SkillEffectText.pctMore;

public enum PhaseSkillEffect implements SkillEffect {

    PHASE_CLOAK_ACTIVATION_COST_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getPhaseCloakActivationCostBonus(), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "phase cloak activation cost");
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
            float mult = SkillEffectSupport.multFrom(magnitude);
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
            return pctMore(magnitude, "flux dissipation, weapon rate of fire, and ammo regeneration while phased");
        }
    },
    PHASE_ANCHOR_EMERGENCY_DIVE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(PHASE_ANCHOR_CR_PENALTY_KEY).modifyFlat(modId, magnitude);
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            if (!ship.hasListenerOfClass(PhaseAnchorDiveListener.class)) {
                ship.addListener(new PhaseAnchorDiveListener(ship, modId));
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

    private static final String PHASE_ANCHOR_CR_PENALTY_KEY = "exiledSector_phaseAnchorCrPenaltyPercent";

    private static final class PhaseAnchorDiveListener implements HullDamageAboutToBeTakenListener, AdvanceableListener {

        private static final String DIVE_FLAG_KEY = "phaseAnchor_canDive";

        private final ShipAPI ship;
        private final String modId;
        private final FaderUtil diveFader = new FaderUtil(1f, 1f);
        private boolean diving;
        private float diveProgress;

        private PhaseAnchorDiveListener(ShipAPI ship, String modId) {
            this.ship = ship;
            this.modId = modId;
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
            float crPenaltyMult = ship.getMutableStats().getDynamic().getValue(PHASE_ANCHOR_CR_PENALTY_KEY, 0f) / 100f;
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
            if (phaseCloak == null) {
                return;
            }

            Color effectColor = Misc.setAlpha(phaseCloak.getSpecAPI().getEffectColor2(), 255);
            effectColor = Misc.interpolateColor(effectColor, Color.white, 0.5f);

            if (diveProgress == 0f && ship.getFluxTracker().showFloaty()) {
                float timeMult = ship.getMutableStats().getTimeMult().getModifiedValue();
                Global.getCombatEngine().addFloatingTextAlways(ship.getLocation(), "Emergency dive!",
                        NeuralLinkScript.getFloatySize(ship), effectColor, ship,
                        16f * timeMult, 3.2f / timeMult, 1f / timeMult, 0f, 0f, 1f);
            }

            diveFader.advance(amount);
            ship.setRetreating(true, false);
            ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM);

            // multiplying (not dividing) by chargeUpDur matches vanilla's own phase-anchor dive timing exactly
            diveProgress += amount * phaseCloak.getChargeUpDur();
            float extraAlphaMult = ship.getExtraAlphaMult();
            phaseCloak.forceState(ShipSystemAPI.SystemState.IN, Math.min(1f, Math.max(extraAlphaMult, diveProgress)));

            ship.getMutableStats().getHullDamageTakenMult().modifyMult(modId, 0f);

            if (diveProgress < 1f) {
                return;
            }

            if (diveFader.isIdle()) {
                Global.getSoundPlayer().playSound("phase_anchor_vanish", 1f, 1f, ship.getLocation(), ship.getVelocity());
            }
            diveFader.fadeOut();
            diveFader.advance(amount);
            float brightness = diveFader.getBrightness();
            ship.setExtraAlphaMult2(brightness);

            float jitterAmount = ship.getCollisionRadius() * 5f;
            ship.setJitter(this, effectColor, brightness, 20, jitterAmount * (1f - brightness));

            if (diveFader.isFadedOut()) {
                ship.getLocation().set(0f, -1000000f);
            }
        }
    }
}
