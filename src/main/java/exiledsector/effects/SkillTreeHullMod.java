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
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.HullSizeSkillEffect;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import exiledsector.skills.SkillTypeEffect;
import exiledsector.skills.skilleffect.SkillEffect;
import org.magiclib.util.MagicIncompatibleHullmods;

public class SkillTreeHullMod extends BaseHullMod {

    public static final String ID = "exiledSector_core";

    private static final String MOD_ID_PREFIX = "exiledSector_skill_";
    private static final String MAGICLIB_WARNING_HULLMOD_ID = "ML_incompatibleHullmodWarning";
    private static final String CONFLICT_WARNING_HULLMOD_ID = "exiledSector_conflictWarning";
    private static final String OP_SINK_HULLMOD_ID = "exiledSector_opSink";

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        forEachAllocatedEffect(stats.getFleetMember(), hullSize,
                (vanillaEffect, vanillaHullModId) -> vanillaEffect.applyEffectsBeforeShipCreation(hullSize, stats, vanillaHullModId),
                (effect, modId, magnitude) -> effect.apply(stats, modId, magnitude));
        syncOpSinkHullMod(stats.getFleetMember(), stats.getVariant());
        removeHullModsConflictingWithAllocatedSkills(stats.getFleetMember(), stats.getVariant());
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        forEachAllocatedEffect(ship.getMutableStats().getFleetMember(), ship.getHullSize(),
                (vanillaEffect, vanillaHullModId) -> vanillaEffect.applyEffectsAfterShipCreation(ship, vanillaHullModId),
                (effect, modId, magnitude) -> effect.applyAfterShipCreation(ship, modId, magnitude));
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
    }

    private void forEachAllocatedEffect(FleetMemberAPI member, HullSize hullSize,
                                         VanillaDelegate vanillaDelegate, EffectAction action) {
        if (member == null) return;

        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        for (String nodeId : data.getAllocatedNodeIds()) {
            SkillNode node = SkillTree.get(nodeId);
            if (node == null) continue;

            SkillType type = node.resolveEffectiveType(data);

            String vanillaHullModId = type.getVanillaHullModId();
            if (vanillaHullModId != null) {
                if (vanillaDelegate != null) {
                    HullModSpecAPI spec = Global.getSettings().getHullModSpec(vanillaHullModId);
                    if (spec != null && spec.getEffect() != null) {
                        vanillaDelegate.apply(spec.getEffect(), vanillaHullModId);
                    }
                }
                continue;
            }

            String modId = MOD_ID_PREFIX + node.getId();
            for (SkillTypeEffect effect : type.getEffects()) {
                action.apply(effect.effect(), modId, effect.magnitude());
            }
            for (HullSizeSkillEffect effect : type.getHullSizeEffects()) {
                action.apply(effect.effect(), modId, effect.valueFor(hullSize));
            }
        }
    }

    private void syncOpSinkHullMod(FleetMemberAPI member, ShipVariantAPI variant) {
        if (member == null || variant == null) return;

        HullModSpecAPI spec = Global.getSettings().getHullModSpec(OP_SINK_HULLMOD_ID);
        if (spec == null) return;

        int opSpent = ShipSkillDataManager.get(member.getId()).getOpSpentOnPassivePoints();
        spec.setFrigateCost(opSpent);
        spec.setDestroyerCost(opSpent);
        spec.setCruiserCost(opSpent);
        spec.setCapitalCost(opSpent);

        if (opSpent > 0) {
            if (!variant.hasHullMod(OP_SINK_HULLMOD_ID)) {
                variant.addPermaMod(OP_SINK_HULLMOD_ID, false);
            }
        } else if (variant.hasHullMod(OP_SINK_HULLMOD_ID)) {
            variant.removePermaMod(OP_SINK_HULLMOD_ID);
        }
    }

    private void removeHullModsConflictingWithAllocatedSkills(FleetMemberAPI member, ShipVariantAPI variant) {
        if (member == null || variant == null) return;

        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        for (String nodeId : data.getAllocatedNodeIds()) {
            SkillNode node = SkillTree.get(nodeId);
            if (node == null) continue;

            SkillType type = node.resolveEffectiveType(data);
            for (String hullModId : type.getExclusiveHullModIds()) {
                if (variant.hasHullMod(hullModId)) {
                    MagicIncompatibleHullmods.removeHullmodWithWarning(variant, hullModId, CONFLICT_WARNING_HULLMOD_ID);
                    variant.removeMod(MAGICLIB_WARNING_HULLMOD_ID);
                    variant.addMod(CONFLICT_WARNING_HULLMOD_ID);
                    SkillConflictWarnings.record(variant, hullModId, type.getDisplayName());
                }
            }
        }
    }

    private interface VanillaDelegate {
        void apply(HullModEffect vanillaEffect, String vanillaHullModId);
    }

    private interface EffectAction {
        void apply(SkillEffect effect, String modId, float magnitude);
    }
}
