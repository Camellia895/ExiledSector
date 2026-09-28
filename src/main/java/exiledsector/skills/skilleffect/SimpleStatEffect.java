package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

record SimpleStatEffect(StatMode mode, StatTarget target, String statName, boolean lowerIsBetter) {

    void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        target.apply(stats, modId, mode, magnitude);
    }

    String describe(float magnitude) {
        return mode.describe(magnitude, statName);
    }

    boolean supportsTemporaryGating() {
        return target.supportsTemporaryGating();
    }
}
