package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.i18n.LegacyHighlight;
import exiledsector.i18n.StyledText;

import java.util.List;

public interface SkillEffect {

    void apply(MutableShipStatsAPI stats, String modId, float magnitude);

    default String describe(float magnitude) {
        StyledText description = description(magnitude);
        return description == null ? null : description.plain();
    }

    // TODO: make this abstract and drop the describe fallback once every effect builds its text from catalogue templates
    default StyledText description(float magnitude) {
        return LegacyHighlight.of(describe(magnitude));
    }

    default boolean isConditional() {
        return false;
    }

    default boolean supportsTemporaryGating() {
        return true;
    }

    default boolean lowerIsBetter() {
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

    default String blockAllocationReason(FleetMemberAPI member, float magnitude, List<SkillEffect> currentlyAllocatedEffects) {
        return null;
    }

    default String shieldTypeBlockReason(ShieldAPI.ShieldType resolvedShieldType) {
        return null;
    }

    default boolean appliesToNpcShips() {
        return true;
    }

    default String deallocationWarning(float magnitude) {
        return null;
    }

    String name();

    static SkillEffect byName(String name) {
        return SkillEffectRegistry.byName(name);
    }
}
