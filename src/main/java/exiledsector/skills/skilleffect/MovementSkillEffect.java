package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

import static exiledsector.skills.skilleffect.StatMode.FLAT;
import static exiledsector.skills.skilleffect.StatMode.MULT;
import static exiledsector.skills.skilleffect.StatMode.PERCENT;
import static exiledsector.skills.skilleffect.StatTarget.stat;

public enum MovementSkillEffect implements SkillEffect {

    MANEUVERABILITY_PERCENT(PERCENT, stat(MutableShipStatsAPI::getMaxTurnRate), "maneuverability", false),
    TOP_SPEED_PERCENT(PERCENT, stat(MutableShipStatsAPI::getMaxSpeed), StatNames.TOP_SPEED, false),
    TOP_SPEED_FLAT(FLAT, stat(MutableShipStatsAPI::getMaxSpeed), StatNames.TOP_SPEED, false),
    TOP_SPEED_MULT(MULT, stat(MutableShipStatsAPI::getMaxSpeed), StatNames.TOP_SPEED, false),
    ACCELERATION_PERCENT(PERCENT, stat(MutableShipStatsAPI::getAcceleration), "acceleration", false),
    ACCELERATION_FLAT(FLAT, stat(MutableShipStatsAPI::getAcceleration), "acceleration", false),
    DECELERATION_PERCENT(PERCENT, stat(MutableShipStatsAPI::getDeceleration), "deceleration", false),
    DECELERATION_FLAT(FLAT, stat(MutableShipStatsAPI::getDeceleration), "deceleration", false),
    TURN_ACCELERATION_PERCENT(PERCENT, stat(MutableShipStatsAPI::getTurnAcceleration), "turn acceleration", false);

    private final SimpleStatEffect simpleStat;

    MovementSkillEffect(StatMode mode, StatTarget target, String statName, boolean lowerIsBetter) {
        this.simpleStat = new SimpleStatEffect(mode, target, statName, lowerIsBetter);
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        simpleStat.apply(stats, modId, magnitude);
    }

    @Override
    public String describe(float magnitude) {
        return simpleStat.describe(magnitude);
    }

    @Override
    public boolean lowerIsBetter() {
        return simpleStat.lowerIsBetter();
    }

    private static final class StatNames {
        static final String TOP_SPEED = "top speed";

        private StatNames() {
        }
    }
}
