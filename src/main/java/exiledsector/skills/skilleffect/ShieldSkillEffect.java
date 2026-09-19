package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.impl.campaign.ids.Stats;

import static exiledsector.skills.skilleffect.SkillEffectText.pct;
import static exiledsector.skills.skilleffect.SkillEffectText.pctChange;
import static exiledsector.skills.skilleffect.SkillEffectText.flatChange;

public enum ShieldSkillEffect implements SkillEffect {

    BEAM_DAMAGE_HARD_FLUX_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            if (!ship.hasListenerOfClass(BeamHardFluxListener.class)) {
                ship.addListener(new BeamHardFluxListener(magnitude));
            }
        }

        @Override
        public String describe(float magnitude) {
            return "Causes " + pct(magnitude) + "% of beam weapon damage dealt to shields to be hard flux.";
        }
    },
    SHIELD_ARC_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getShieldArcBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "shield arc");
        }
    },
    SHIELD_ARC_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getShieldArcBonus().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "shield arc, in degrees");
        }
    },
    SHIELD_PIERCE_CHANCE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getStat(Stats.SHIELD_PIERCED_MULT).modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "chance for shields to be pierced by EMP arcs");
        }
    },
    SHIELD_UPKEEP_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getShieldUpkeepMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "shield flux upkeep");
        }
    },
    SHIELD_TURN_RATE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getShieldTurnRateMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "shield turn rate");
        }
    },
    SHIELD_RAISE_RATE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getShieldUnfoldRateMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "shield raise rate");
        }
    };

    @Override
    public abstract void apply(MutableShipStatsAPI stats, String modId, float magnitude);

    @Override
    public abstract String describe(float magnitude);

    private static final class BeamHardFluxListener implements DamageDealtModifier {

        private final float percent;

        private BeamHardFluxListener(float percent) {
            this.percent = percent;
        }

        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (!shieldHit) return null;
            if (!(param instanceof BeamAPI)) return null;
            if (!(target instanceof ShipAPI)) return null;

            float hardPortion = damage.getDamage() * (percent / 100f);
            if (hardPortion <= 0f) return null;

            damage.setDamage(damage.getDamage() - hardPortion);
            float hardFlux = damage.computeFluxDealt(hardPortion);
            ((ShipAPI) target).getFluxTracker().increaseFlux(hardFlux, true);
            return null;
        }
    }
}
