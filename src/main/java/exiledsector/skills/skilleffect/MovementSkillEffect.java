package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

import static exiledsector.skills.skilleffect.SkillEffectText.pctChange;
import static exiledsector.skills.skilleffect.SkillEffectText.flatChange;

public enum MovementSkillEffect implements SkillEffect {

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
    TOP_SPEED_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMaxSpeed().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "top speed");
        }
    },
    ACCELERATION {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getAcceleration().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "acceleration");
        }
    },
    ACCELERATION_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getAcceleration().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "acceleration");
        }
    },
    DECELERATION {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDeceleration().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "deceleration");
        }
    },
    DECELERATION_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDeceleration().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "deceleration");
        }
    },
    TURN_ACCELERATION {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getTurnAcceleration().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "turn acceleration");
        }
    };

    @Override
    public abstract void apply(MutableShipStatsAPI stats, String modId, float magnitude);

    @Override
    public abstract String describe(float magnitude);
}
