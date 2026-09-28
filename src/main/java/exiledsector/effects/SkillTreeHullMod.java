package exiledsector.effects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import exiledsector.persistence.OpSpentSlotManager;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import exiledsector.skills.skilleffect.SkillEffect;
import org.magiclib.util.MagicIncompatibleHullmods;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class SkillTreeHullMod extends BaseHullMod {

    public static final String ID = "exiledSector_core";

    private static final String MOD_ID_PREFIX = "exiledSector_skill_";
    private static final String MAGICLIB_WARNING_HULLMOD_ID = "ML_incompatibleHullmodWarning";
    private static final String CONFLICT_WARNING_HULLMOD_ID = "exiledSector_conflictWarning";
    private static final String OP_SPENT_HULLMOD_ID_PREFIX = "exiledSector_opSpent_";
    private static final String INSTALLED_HULLMOD_TAG_PREFIX = "exiledSector_installed_";

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        forEachAllocatedEffect(stats.getFleetMember(), hullSize,
                (vanillaEffect, vanillaHullModId) -> vanillaEffect.applyEffectsBeforeShipCreation(hullSize, stats, vanillaHullModId),
                (effect, modId, magnitude) -> effect.apply(stats, modId, magnitude));
        syncOpSpentHullMod(stats.getFleetMember(), stats.getVariant());
        syncInstalledHullMods(stats.getFleetMember(), stats.getVariant());
        removeHullModsConflictingWithAllocatedSkills(stats.getFleetMember(), stats.getVariant());
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        forEachAllocatedEffect(ship.getMutableStats().getFleetMember(), ship.getHullSize(),
                (vanillaEffect, vanillaHullModId) -> vanillaEffect.applyEffectsAfterShipCreation(ship, vanillaHullModId),
                (effect, modId, magnitude) -> effect.applyAfterShipCreation(ship, modId, magnitude));
    }

    @Override
    public boolean affectsOPCosts() {
        return true;
    }

    @Override
    public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
        forEachAllocatedEffect(ship.getMutableStats().getFleetMember(), ship.getHullSize(),
                (vanillaEffect, vanillaHullModId) -> vanillaEffect.applyEffectsToFighterSpawnedByShip(fighter, ship, vanillaHullModId),
                (effect, modId, magnitude) -> effect.applyToFighterSpawnedByShip(fighter, ship, modId, magnitude));
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        forEachAllocatedEffect(ship.getMutableStats().getFleetMember(), ship.getHullSize(),
                null,
                (effect, modId, magnitude) -> {
                    if (effect.isConditional()) {
                        effect.advanceInCombat(ship, modId, magnitude);
                    }
                });
        reapplyTemporaryNodes(ship);
    }

    private void reapplyTemporaryNodes(ShipAPI ship) {
        FleetMemberAPI member = ship.getMutableStats().getFleetMember();
        if (member == null) return;

        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        MutableShipStatsAPI stats = ship.getMutableStats();
        HullSize hullSize = ship.getHullSize();
        for (String nodeId : data.getAllocatedNodeIds()) {
            SkillNode node = SkillTree.get(nodeId);
            SkillType type = node == null ? null : node.resolveEffectiveType(data);
            Float durationSeconds = type == null ? null : type.getTemporaryAfterDeploymentSeconds();
            if (durationSeconds == null) continue;

            boolean active = ship.getFullTimeDeployed() < durationSeconds;
            String modId = MOD_ID_PREFIX + node.getId();
            type.forEachEffect(hullSize, (effect, magnitude) -> effect.apply(stats, modId, active ? magnitude : 0f));
        }
    }

    private void forEachAllocatedEffect(FleetMemberAPI member, HullSize hullSize,
                                         VanillaDelegate vanillaDelegate, EffectAction action) {
        if (member == null) return;

        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        for (String nodeId : data.getAllocatedNodeIds()) {
            SkillNode node = SkillTree.get(nodeId);
            if (node == null) {
                continue;
            }

            SkillType type = node.resolveEffectiveType(data);
            String vanillaHullModId = type.getVanillaHullModId();
            if (vanillaHullModId != null) {
                if (vanillaDelegate != null) {
                    HullModSpecAPI spec = Global.getSettings().getHullModSpec(vanillaHullModId);
                    if (spec != null && spec.getEffect() != null) {
                        vanillaDelegate.apply(spec.getEffect(), vanillaHullModId);
                    }
                }
            } else {
                String modId = MOD_ID_PREFIX + node.getId();
                type.forEachEffect(hullSize, (effect, magnitude) -> action.apply(effect, modId, magnitude));
            }
        }
    }

    public static void syncOpSpentHullMod(FleetMemberAPI member, ShipVariantAPI variant) {
        if (member == null || variant == null) return;

        String hullModId = OP_SPENT_HULLMOD_ID_PREFIX + OpSpentSlotManager.slotFor(member.getId());
        HullModSpecAPI spec = Global.getSettings().getHullModSpec(hullModId);
        if (spec == null) return;

        int opSpent = ShipSkillDataManager.get(member.getId()).getSpentOp();
        spec.setFrigateCost(opSpent);
        spec.setDestroyerCost(opSpent);
        spec.setCruiserCost(opSpent);
        spec.setCapitalCost(opSpent);

        if (opSpent > 0) {
            if (!variant.hasHullMod(hullModId)) {
                variant.addMod(hullModId);
            }
        } else if (variant.hasHullMod(hullModId)) {
            variant.removeMod(hullModId);
        }
    }

    public static void syncInstalledHullMods(FleetMemberAPI member, ShipVariantAPI variant) {
        if (member == null || variant == null) return;

        Set<String> wanted = installedHullModIds(ShipSkillDataManager.get(member.getId()));
        for (String hullModId : wanted) {
            if (!variant.hasHullMod(hullModId)) {
                variant.addPermaMod(hullModId);
                variant.addTag(INSTALLED_HULLMOD_TAG_PREFIX + hullModId);
            }
        }
        for (String tag : new ArrayList<>(variant.getTags())) {
            String hullModId = tag.startsWith(INSTALLED_HULLMOD_TAG_PREFIX) ? tag.substring(INSTALLED_HULLMOD_TAG_PREFIX.length()) : null;
            if (hullModId != null && !wanted.contains(hullModId)) {
                variant.removePermaMod(hullModId);
                variant.removeTag(tag);
            }
        }
    }

    public static boolean isInstalledBySkillTree(ShipVariantAPI variant, String hullModId) {
        return variant.hasTag(INSTALLED_HULLMOD_TAG_PREFIX + hullModId);
    }

    private static boolean isRemovableConflict(ShipVariantAPI variant, String hullModId) {
        boolean builtIn = variant.getHullSpec() != null && variant.getHullSpec().isBuiltInMod(hullModId);
        return variant.hasHullMod(hullModId) && !builtIn && !isInstalledBySkillTree(variant, hullModId);
    }

    private static Set<String> installedHullModIds(ShipSkillData data) {
        Set<String> ids = new LinkedHashSet<>();
        for (String nodeId : data.getAllocatedNodeIds()) {
            SkillNode node = SkillTree.get(nodeId);
            if (node != null) {
                ids.addAll(node.resolveEffectiveType(data).getInstalledHullModIds());
            }
        }
        return ids;
    }

    public static void removeHullModsConflictingWithAllocatedSkills(FleetMemberAPI member, ShipVariantAPI variant) {
        if (member == null || variant == null) return;

        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        boolean conflictFound = false;
        for (String nodeId : data.getAllocatedNodeIds()) {
            SkillNode node = SkillTree.get(nodeId);
            if (node == null) continue;

            SkillType type = node.resolveEffectiveType(data);
            for (String hullModId : type.getExclusiveHullModIds()) {
                if (isRemovableConflict(variant, hullModId)) {
                    MagicIncompatibleHullmods.removeHullmodWithWarning(variant, hullModId, CONFLICT_WARNING_HULLMOD_ID);
                    variant.removeMod(MAGICLIB_WARNING_HULLMOD_ID);
                    variant.addMod(CONFLICT_WARNING_HULLMOD_ID);
                    SkillConflictWarnings.recordRemoval(variant, hullModId, type.getDisplayName());
                    conflictFound = true;
                }
            }
        }

        if (!conflictFound) {
            if (variant.hasHullMod(CONFLICT_WARNING_HULLMOD_ID)) {
                variant.removeMod(CONFLICT_WARNING_HULLMOD_ID);
            }
            SkillConflictWarnings.clear(variant);
        }
    }

    private interface VanillaDelegate {
        void apply(HullModEffect vanillaEffect, String vanillaHullModId);
    }

    private interface EffectAction {
        void apply(SkillEffect effect, String modId, float magnitude);
    }
}
