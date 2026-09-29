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
import exiledsector.i18n.StyledText;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;
import java.util.Map;

import static exiledsector.skills.skilleffect.StatMode.MULT;
import static exiledsector.skills.skilleffect.StatMode.PERCENT;
import static exiledsector.skills.skilleffect.StatTarget.bonus;
import static exiledsector.skills.skilleffect.StatTarget.dynamicMod;

public enum PhaseSkillEffect implements SkillEffect {

    PHASE_CLOAK_ACTIVATION_COST_MULT(MULT, bonus(MutableShipStatsAPI::getPhaseCloakActivationCostBonus),
            "stat.phaseCloakActivationCost", true),
    PHASE_CLOAK_FLUX_THRESHOLD_PERCENT(PERCENT, dynamicMod("phase_cloak_flux_level_for_min_speed_mod"),
            "stat.hardFluxThresholdBeforePhaseSpeedPenaltyKicksIn", false),
    COMBAT_BOOST_WHILE_PHASED {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public boolean isConditional() {
            return true;
        }

        @Override
        public boolean supportsTemporaryGating() {
            return false;
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
        public StyledText description(float magnitude) {
            return EffectText.pctMore(magnitude, "stat.fluxRateOfFireAndAmmoRegenWhilePhased");
        }
    },
    PHASE_ANCHOR_EMERGENCY_DIVE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(PHASE_ANCHOR_CR_PENALTY_KEY).modifyFlat(modId, magnitude);
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            SkillEffectSupport.ensureListener(ship, PhaseAnchorDiveListener.class, s -> new PhaseAnchorDiveListener(s, modId));
        }

        @Override
        public boolean supportsTemporaryGating() {
            return false;
        }

        @Override
        public StyledText description(float magnitude) {
            return EffectText.msg(this).arg("value", magnitude).styled();
        }
    };

    private static final String PHASE_ANCHOR_CR_PENALTY_KEY = "exiledSector_phaseAnchorCrPenaltyPercent";

    private final SimpleStatEffect simpleStat;

    PhaseSkillEffect() {
        this.simpleStat = null;
    }

    PhaseSkillEffect(StatMode mode, StatTarget target, String statKey, boolean lowerIsBetter) {
        this.simpleStat = new SimpleStatEffect(mode, target, statKey, lowerIsBetter);
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        simpleStat.apply(stats, modId, magnitude);
    }

    @Override
    public StyledText description(float magnitude) {
        return simpleStat.description(magnitude);
    }

    @Override
    public boolean lowerIsBetter() {
        return simpleStat != null && simpleStat.lowerIsBetter();
    }

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
