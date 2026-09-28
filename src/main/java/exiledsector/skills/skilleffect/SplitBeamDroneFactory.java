package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.loading.BeamWeaponSpecAPI;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponGroupType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class SplitBeamDroneFactory {

    static final String HULL_ID = "exiledSector_split_beam_drone";

    private static final String INVULNERABLE_MOD_ID = "exiledSector_splitBeamDrone";
    private static final float MOTHERSHIP_FLAG_DURATION = 100000f;
    private static final Map<String, Boolean> SUPPORT_BY_WEAPON_ID = new ConcurrentHashMap<>();
    private static final Map<WeaponSize, String> SLOT_IDS = Map.of(
            WeaponSize.SMALL, "WS SMALL",
            WeaponSize.MEDIUM, "WS MEDIUM",
            WeaponSize.LARGE, "WS LARGE");

    private SplitBeamDroneFactory() {
    }

    static boolean supports(WeaponAPI weapon) {
        if (!(weapon.getSpec() instanceof BeamWeaponSpecAPI spec) || spec.getWeaponId() == null) {
            return false;
        }
        return SUPPORT_BY_WEAPON_ID.computeIfAbsent(spec.getWeaponId(), id -> canMountOnDrone(weapon, spec));
    }

    private static boolean canMountOnDrone(WeaponAPI weapon, BeamWeaponSpecAPI spec) {
        boolean blocklisted = spec.getBeamEffect() != null
                && CsvIdBlocklist.SPLIT_BEAM_EFFECTS.contains(spec.getBeamEffect().getClass().getName());
        return !blocklisted && SLOT_IDS.containsKey(weapon.getSize()) && Global.getSettings().getHullSpec(HULL_ID) != null;
    }

    static ShipAPI create(ShipAPI firingShip, WeaponAPI weapon) {
        ShipHullSpecAPI hull = Global.getSettings().getHullSpec(HULL_ID);
        ShipVariantAPI variant = Global.getSettings().createEmptyVariant(HULL_ID, hull);
        String slotId = SLOT_IDS.get(weapon.getSize());
        variant.addWeapon(slotId, weapon.getSpec().getWeaponId());
        WeaponGroupSpec group = new WeaponGroupSpec(WeaponGroupType.LINKED);
        group.addSlot(slotId);
        variant.addWeaponGroup(group);

        CombatEngineAPI engine = Global.getCombatEngine();
        ShipAPI drone = engine.createFXDrone(variant);
        drone.setLayer(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
        drone.setOwner(firingShip.getOwner());
        drone.setDrone(true);
        drone.getAIFlags().setFlag(AIFlags.DRONE_MOTHERSHIP, MOTHERSHIP_FLAG_DURATION, firingShip);
        drone.setCollisionClass(CollisionClass.NONE);
        drone.giveCommand(ShipCommand.SELECT_GROUP, null, 0);
        drone.getMutableStats().getHullDamageTakenMult().modifyMult(INVULNERABLE_MOD_ID, 0f);
        drone.setCaptain(officerCopy(firingShip.getCaptain()));
        SplitBeamDroneStats.mirror(firingShip.getMutableStats(), drone.getMutableStats());
        shareDamageListeners(firingShip, drone);
        engine.addEntity(drone);
        return drone;
    }

    static PersonAPI officerCopy(PersonAPI captain) {
        PersonAPI copy = Global.getFactory().createPerson();
        if (captain == null) {
            return copy;
        }
        copy.setName(captain.getName());
        copy.setPortraitSprite(captain.getPortraitSprite());
        copy.setPersonality(captain.getPersonalityAPI().getId());
        copy.setAICoreId(captain.getAICoreId());
        copy.getStats().setLevel(captain.getStats().getLevel());
        for (MutableCharacterStatsAPI.SkillLevelAPI skill : captain.getStats().getSkillsCopy()) {
            copy.getStats().setSkillLevel(skill.getSkill().getId(), skill.getLevel());
        }
        return copy;
    }

    static void shareDamageListeners(ShipAPI firingShip, ShipAPI drone) {
        for (DamageDealtModifier listener : firingShip.getListeners(DamageDealtModifier.class)) {
            if (isShareable(listener)) {
                drone.removeListenerOfClass(listener.getClass());
                drone.addListener(listener);
            }
        }
    }

    private static boolean isShareable(DamageDealtModifier listener) {
        boolean splitsBeams = listener instanceof SplitBeamSource;
        boolean wouldTickTwicePerFrame = listener instanceof AdvanceableListener;
        return !splitsBeams && !wouldTickTwicePerFrame;
    }
}
