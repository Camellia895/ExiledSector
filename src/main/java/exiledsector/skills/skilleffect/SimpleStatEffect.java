package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import exiledsector.i18n.StyledText;

record SimpleStatEffect(StatMode mode, StatTarget target, String statKey, boolean lowerIsBetter) {

    void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        target.apply(stats, modId, mode, magnitude);
    }

    StyledText description(float magnitude) {
        return mode.describeStat(magnitude, statKey);
    }

    boolean supportsTemporaryGating() {
        return target.supportsTemporaryGating();
    }
}
