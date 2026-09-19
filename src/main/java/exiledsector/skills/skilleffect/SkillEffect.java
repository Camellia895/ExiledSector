package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

public interface SkillEffect {

    void apply(MutableShipStatsAPI stats, String modId, float magnitude);

    String describe(float magnitude);

    default boolean isConditional() {
        return false;
    }

    default void advanceInCombat(ShipAPI ship, String modId, float magnitude) {
    }

    default void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
    }

    default void applyToFighterSpawnedByShip(ShipAPI fighter, ShipAPI parentShip, String modId, float magnitude) {
    }

    default String blockDeallocationReason(FleetMemberAPI member, float magnitude) {
        return null;
    }

    default String deallocationWarning(float magnitude) {
        return null;
    }

    String name();

    static SkillEffect byName(String name) {
        return SkillEffectRegistry.byName(name);
    }
}
