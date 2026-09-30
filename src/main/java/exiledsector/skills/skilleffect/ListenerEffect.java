package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import exiledsector.i18n.StyledText;

import java.util.function.Function;

record ListenerEffect(String magnitudeKey, Class<?> listenerType, Function<ShipAPI, ?> listenerFactory) implements EffectBacking {

    @Override
    public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        if (magnitudeKey != null) {
            stats.getDynamic().getMod(magnitudeKey).modifyFlat(modId, magnitude);
        }
    }

    @Override
    public void applyAfterShipCreation(ShipAPI ship) {
        SkillEffectSupport.ensureListener(ship, listenerType, listenerFactory);
    }

    @Override
    public boolean supportsTemporaryGating() {
        return false;
    }

    @Override
    public StyledText description(SkillEffect effect, float magnitude) {
        return EffectText.templated(effect, magnitude);
    }
}
