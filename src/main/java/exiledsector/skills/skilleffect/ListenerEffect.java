package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import java.util.function.Function;

record ListenerEffect(String magnitudeKey, Class<?> listenerType, Function<ShipAPI, ?> listenerFactory) {

    void storeMagnitude(MutableShipStatsAPI stats, String modId, float magnitude) {
        if (magnitudeKey != null) {
            stats.getDynamic().getMod(magnitudeKey).modifyFlat(modId, magnitude);
        }
    }

    void attach(ShipAPI ship) {
        SkillEffectSupport.ensureListener(ship, listenerType, listenerFactory);
    }
}
