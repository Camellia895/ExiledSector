package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.impl.campaign.ids.Stats;

import static exiledsector.skills.skilleffect.SkillEffectText.pct;
import static exiledsector.skills.skilleffect.SkillEffectText.pctChange;
import static exiledsector.skills.skilleffect.SkillEffectText.pctMore;
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
    REMOVE_SHIELD {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            ship.setShield(ShieldAPI.ShieldType.NONE, 0f, 1f, 1f);
        }

        @Override
        public String describe(float magnitude) {
            return "Removes this ship's shield entirely.";
        }
    },
    CREATE_FRONT_SHIELD_IF_NONE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            if (ship.getShield() == null) {
                ship.setShield(ShieldAPI.ShieldType.FRONT, 0.5f, 1.2f, 90f);
            }
        }

        @Override
        public String describe(float magnitude) {
            return "Installs a makeshift, front-facing shield if this ship has none.";
        }
    },
    CONVERT_SHIELD_TO_FRONT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            ShieldAPI shield = ship.getShield();
            if (shield != null) {
                shield.setType(ShieldAPI.ShieldType.FRONT);
            }
        }

        @Override
        public String describe(float magnitude) {
            return "Converts this ship's shield to front-facing.";
        }
    },
    CONVERT_SHIELD_TO_OMNI {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            ShieldAPI shield = ship.getShield();
            if (shield != null) {
                shield.setType(ShieldAPI.ShieldType.OMNI);
            }
        }

        @Override
        public String describe(float magnitude) {
            return "Converts this ship's shield to omni-directional.";
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
    SHIELD_ARC_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getShieldArcBonus(), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "shield arc");
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
    SHIELD_PIERCE_CHANCE_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getDynamic().getStat(Stats.SHIELD_PIERCED_MULT), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "chance for shields to be pierced by EMP arcs");
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
    SHIELD_UPKEEP_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getShieldUpkeepMult(), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "shield flux upkeep");
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
