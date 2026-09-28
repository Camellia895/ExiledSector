package exiledsector.ui.node;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import exiledsector.compat.SecondInCommandCompat;
import exiledsector.effects.SkillTreeHullMod;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.AllocatedNode;
import exiledsector.skills.AllocatedSkillEffects;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillItemCost;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import exiledsector.skills.SkillTypeEffect;
import exiledsector.skills.progression.ShipLevelConfig;
import exiledsector.skills.progression.ShipOpBudget;
import exiledsector.skills.progression.SkillNodeOpCost;
import exiledsector.skills.skilleffect.SkillEffect;
import exiledsector.skills.unlock.SkillTypeUnlockStatus;

import java.util.List;
import java.util.function.Supplier;

final class NodeAllocator {

    static final String LOCKED_REASON = "Unidentified - explore the sector to discover this node.";

    record Snapshot(ShipSkillData data, String satisfiedRootId, int totalOpBudget, int opCostPerNode, int maxAllocatedNodes) {

        int opCostFor(SkillNode node) {
            return node.getId().equals(satisfiedRootId) ? 0 : opCostPerNode;
        }

        boolean canAllocate(SkillNode node) {
            return data.canAllocate(node, satisfiedRootId, totalOpBudget, opCostFor(node), maxAllocatedNodes);
        }
    }

    private final FleetMemberAPI member;
    private final ShipVariantAPI variant;
    private final Supplier<SkillNode> startingRoot;

    NodeAllocator(FleetMemberAPI member, ShipVariantAPI variant, Supplier<SkillNode> startingRoot) {
        this.member = member;
        this.variant = variant;
        this.startingRoot = startingRoot;
    }

    ShipSkillData data() {
        return ShipSkillDataManager.get(member.getId());
    }

    String satisfiedRootId() {
        SkillNode root = startingRoot.get();
        return root == null ? null : root.getId();
    }

    boolean isStartingRoot(SkillNode node) {
        SkillNode root = startingRoot.get();
        return root != null && node.getId().equals(root.getId());
    }

    Snapshot snapshot() {
        ShipSkillData data = data();
        ShipOpBudget budget = ShipOpBudget.of(member, variant);
        return new Snapshot(data, satisfiedRootId(), budget.total - budget.used + data.getSpentOp(),
                SkillNodeOpCost.perNode(member.getHullSpec()), ShipLevelConfig.maxAllocatedNodes());
    }

    boolean canAllocate(SkillNode node) {
        return snapshot().canAllocate(node);
    }

    boolean canDeallocate(SkillNode node) {
        return !isStartingRoot(node) && blockDeallocationReason(node) == null
                && data().canDeallocate(node, SkillTree.getAllNodes().values(), satisfiedRootId());
    }

    boolean toggle(SkillNode node) {
        Snapshot snapshot = snapshot();
        ShipSkillData data = snapshot.data();
        boolean wasAllocated = data.isAllocated(node.getId());
        data.toggle(node, SkillTree.getAllNodes().values(), snapshot.satisfiedRootId(), snapshot.totalOpBudget(),
                snapshot.opCostFor(node), snapshot.maxAllocatedNodes());
        boolean isAllocatedNow = data.isAllocated(node.getId());
        if (isAllocatedNow == wasAllocated) {
            return false;
        }
        applyItemCost(node.getType(), isAllocatedNow);
        refreshShipStats();
        return true;
    }

    void allocateOption(SkillNode node, SkillType chosenOption) {
        data().selectOption(node, chosenOption, snapshot().opCostFor(node));
        refreshShipStats();
    }

    boolean chooseStartingRoot(SkillNode root) {
        if (!data().chooseStartingRoot(root)) {
            return false;
        }
        refreshShipStats();
        return true;
    }

    String blockAllocationReason(SkillType type) {
        ShipSkillData data = data();
        if (SkillTypeUnlockStatus.isLocked(type, data)) {
            return LOCKED_REASON;
        }

        String hullModReason = hullModConflictReason(type);
        if (hullModReason != null) {
            return hullModReason;
        }

        String skillTypeReason = skillTypeConflictReason(type, data);
        if (skillTypeReason != null) {
            return skillTypeReason;
        }

        String itemCostReason = itemCostReason(type);
        if (itemCostReason != null) {
            return itemCostReason;
        }

        return effectBlockReason(type);
    }

    String blockDeallocationReason(SkillNode node) {
        SkillType type = node.resolveEffectiveType(data());
        for (SkillTypeEffect effect : type.effectsFor(member.getHullSpec().getHullSize())) {
            String blockReason = effect.effect().blockDeallocationReason(member, effect.magnitude());
            if (blockReason != null) {
                return blockReason;
            }
        }
        return null;
    }

    private void refreshShipStats() {
        new SkillTreeHullMod().applyEffectsBeforeShipCreation(member.getHullSpec().getHullSize(), member.getStats(), SkillTreeHullMod.ID);
        member.setStatUpdateNeeded(true);
        member.updateStats();
    }

    private static void applyItemCost(SkillType type, boolean allocated) {
        SkillItemCost itemCost = type.getItemCost();
        if (itemCost == null) {
            return;
        }
        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        if (allocated) {
            cargo.removeCommodity(itemCost.itemId(), itemCost.quantity());
        } else {
            cargo.addCommodity(itemCost.itemId(), itemCost.quantity());
        }
    }

    private static String itemCostReason(SkillType type) {
        SkillItemCost itemCost = type.getItemCost();
        if (itemCost == null) {
            return null;
        }
        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        float have = cargo.getCommodityQuantity(itemCost.itemId());
        if (have >= itemCost.quantity()) {
            return null;
        }
        return "Requires " + itemCost.formattedQuantity() + " " + itemCost.commodityName()
                + " (have " + SkillItemCost.formatQuantity(have) + ").";
    }

    private String hullModConflictReason(SkillType type) {
        List<String> exclusiveHullModIds = type.getExclusiveHullModIds();
        if (exclusiveHullModIds.isEmpty()) {
            return null;
        }

        member.setStatUpdateNeeded(true);
        member.updateStats();
        SkillTreeHullMod.syncOpSpentHullMod(member, variant);
        for (String hullModId : exclusiveHullModIds) {
            if (variant.hasHullMod(hullModId)) {
                return "Ship already has " + hullModName(hullModId) + " installed.";
            }
            if (SecondInCommandCompat.hasDeactivatedSMod(variant, hullModId)) {
                return "Ship has a deactivated " + hullModName(hullModId) + " S-mod that Best of the Best will restore.";
            }
        }
        return null;
    }

    private static String hullModName(String hullModId) {
        HullModSpecAPI spec = Global.getSettings().getHullModSpec(hullModId);
        return spec != null ? spec.getDisplayName() : hullModId;
    }

    private static String skillTypeConflictReason(SkillType type, ShipSkillData data) {
        List<String> exclusiveSkillTypeIds = type.getExclusiveSkillTypeIds();
        if (exclusiveSkillTypeIds.isEmpty()) {
            return null;
        }

        for (AllocatedNode allocated : AllocatedNode.of(data)) {
            SkillType allocatedType = allocated.effectiveType();
            if (exclusiveSkillTypeIds.contains(allocatedType.getId())) {
                return "Already have " + allocatedType.getDisplayName() + " allocated.";
            }
        }
        return null;
    }

    private String effectBlockReason(SkillType type) {
        List<SkillEffect> currentlyAllocatedEffects = AllocatedSkillEffects.forMember(member);
        for (SkillTypeEffect effect : type.effectsFor(member.getHullSpec().getHullSize())) {
            String blockReason = effect.effect().blockAllocationReason(member, effect.magnitude(), currentlyAllocatedEffects);
            if (blockReason != null) {
                return blockReason;
            }
        }
        return null;
    }
}
