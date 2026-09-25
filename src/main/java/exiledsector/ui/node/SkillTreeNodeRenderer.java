package exiledsector.ui.node;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import exiledsector.effects.SkillTreeHullMod;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.AllocatedSkillEffects;
import exiledsector.skills.ShipLevelConfig;
import exiledsector.skills.ShipOpBudget;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.ShipTechLevel;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillNodeOpCost;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import exiledsector.skills.SkillTypeUnlockStatus;
import exiledsector.skills.skilleffect.SkillEffect;
import exiledsector.ui.SkillTreePanelStyle;
import lunalib.lunaRefit.BaseRefitButton;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;
import java.util.List;

import static exiledsector.ui.node.SkillTreeNodeGeometry.ICON_INSET_RATIO;
import static exiledsector.ui.node.SkillTreeNodeGeometry.NODE_SIZE;

public final class SkillTreeNodeRenderer {

    private static final Color ALLOCATED_TINT = Color.WHITE;
    private static final Color UNALLOCATED_TINT = new Color(90, 90, 90);

    private final FleetMemberAPI member;
    private final ShipVariantAPI variant;
    private final BaseRefitButton refitButton;
    private final SkillNode activeRoot;

    private final SkillTreeNodeRingRenderer ringRenderer;
    private final SkillTreeNodeIconRenderer iconRenderer;
    private final SkillTreeNodeGhostRenderer ghostRenderer;
    private final SkillTreeNodeConnectorRenderer connectorRenderer;
    private final SkillTreeNodeTooltipRenderer tooltipRenderer;
    private final SkillTreeNodeDropdownRenderer dropdownRenderer;

    private SkillType lastChosenOptionalOption;

    public SkillTreeNodeRenderer(FleetMemberAPI member, ShipVariantAPI variant, SkillTreePanelStyle style, BaseRefitButton refitButton) {
        this.member = member;
        this.variant = variant;
        this.refitButton = refitButton;
        this.activeRoot = findRootNode(ShipTechLevel.of(member).rootTypeId());

        this.ringRenderer = new SkillTreeNodeRingRenderer(style);
        this.iconRenderer = new SkillTreeNodeIconRenderer();
        this.ghostRenderer = new SkillTreeNodeGhostRenderer();
        this.connectorRenderer = new SkillTreeNodeConnectorRenderer(style);
        this.tooltipRenderer = new SkillTreeNodeTooltipRenderer(member, style);
        this.dropdownRenderer = new SkillTreeNodeDropdownRenderer(style);

        if (activeRoot != null) {
            ShipSkillData data = ShipSkillDataManager.get(member.getId());
            if (!data.isAllocated(activeRoot.getId())) {
                data.allocate(activeRoot, 0);
            }
        }
    }

    private int opCostFor(SkillNode node) {
        if (node.getType().getTier() == SkillTier.ROOT) return 0;
        return SkillNodeOpCost.perNode(member.getHullSpec());
    }

    private int totalOpBudgetForNodes() {
        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        ShipOpBudget budget = ShipOpBudget.of(member, variant);
        return budget.total - budget.used + data.getSpentOp();
    }

    private String satisfiedRootId() {
        return activeRoot == null ? null : activeRoot.getId();
    }

    private boolean isStartingRoot(SkillNode node) {
        return activeRoot != null && node.getType().getTier() == SkillTier.ROOT && node.getId().equals(activeRoot.getId());
    }

    public void advance(float amount) {
        ringRenderer.advance(amount);
        ghostRenderer.advance(amount);
        connectorRenderer.advance(amount, ShipSkillDataManager.get(member.getId()), satisfiedRootId());
    }

    public void render(float centerX, float centerY, float zoom, float alphaMult, float mouseX, float mouseY, boolean mouseKnown) {
        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        String satisfiedRootId = satisfiedRootId();
        int totalOpBudget = totalOpBudgetForNodes();

        for (SkillNode node : SkillTree.getAllNodes().values()) {
            if (node.getType().getTier() != SkillTier.ROOT) {
                renderNode(node, centerX, centerY, zoom, alphaMult, data, satisfiedRootId, totalOpBudget);
            }
        }

        connectorRenderer.draw(centerX, centerY, zoom, data, satisfiedRootId, alphaMult);

        for (SkillNode node : SkillTree.getAllNodes().values()) {
            if (node.getType().getTier() == SkillTier.ROOT) {
                renderRootNode(node, centerX, centerY, zoom, alphaMult, data, satisfiedRootId, totalOpBudget);
            }
        }

        dropdownRenderer.render(centerX, centerY, zoom, mouseX, mouseY, mouseKnown, alphaMult);
    }

    private void renderNode(SkillNode node, float centerX, float centerY, float zoom, float alphaMult,
                             ShipSkillData data, String satisfiedRootId, int totalOpBudget) {
        SkillTier tier = node.getType().getTier();
        float nodeX = centerX + node.getOffsetX() * zoom;
        float nodeY = centerY - node.getOffsetY() * zoom;
        float footprintSize = NODE_SIZE * zoom * tier.getSizeMultiplier();

        if (SkillTypeUnlockStatus.isHidden(node.getType(), data)) {
            ghostRenderer.draw(nodeX, nodeY, footprintSize, alphaMult, node.getId());
            return;
        }

        boolean allocated = data.isAllocated(node.getId());
        boolean breathing = !allocated && data.canAllocate(node, satisfiedRootId, totalOpBudget, opCostFor(node), ShipLevelConfig.maxAllocatedNodes());
        SkillType effectiveType = node.resolveEffectiveType(data);
        float iconSize = footprintSize * ICON_INSET_RATIO;

        ringRenderer.draw(new Vector2f(nodeX, nodeY), footprintSize, alphaMult, allocated, breathing, zoom, node);

        if (tier != SkillTier.WORMHOLE) {
            Color tint = allocated ? ALLOCATED_TINT : UNALLOCATED_TINT;
            if (effectiveType.isOptional()) {
                iconRenderer.drawSplitIcon(effectiveType, nodeX, nodeY, iconSize, alphaMult, tint);
            } else {
                iconRenderer.drawIcon(effectiveType.getIconPath(), nodeX, nodeY, iconSize, alphaMult, tint);
            }
        }
    }

    private void renderRootNode(SkillNode node, float centerX, float centerY, float zoom, float alphaMult,
                                 ShipSkillData data, String satisfiedRootId, int totalOpBudget) {
        float nodeX = centerX + node.getOffsetX() * zoom;
        float nodeY = centerY - node.getOffsetY() * zoom;
        boolean isActiveRoot = activeRoot != null && node.getId().equals(activeRoot.getId());
        boolean allocated = data.isAllocated(node.getId());
        boolean breathing = !allocated && data.canAllocate(node, satisfiedRootId, totalOpBudget, opCostFor(node), ShipLevelConfig.maxAllocatedNodes());
        float footprintSize = NODE_SIZE * zoom * SkillTier.ROOT.getSizeMultiplier();
        ringRenderer.draw(new Vector2f(nodeX, nodeY), footprintSize, alphaMult, allocated, breathing, zoom, node);

        Color tint = allocated ? ALLOCATED_TINT : UNALLOCATED_TINT;
        String iconPath = isActiveRoot ? RootCrestResolver.resolve(member) : node.getType().getIconPath();
        iconRenderer.drawIcon(iconPath, nodeX, nodeY, footprintSize, alphaMult, tint);
    }

    public SkillNode getActiveRoot() {
        return activeRoot;
    }

    private static SkillNode findRootNode(String rootTypeId) {
        for (SkillNode node : SkillTree.getAllNodes().values()) {
            if (node.getType().getTier() == SkillTier.ROOT && node.getType().getId().equals(rootTypeId)) {
                return node;
            }
        }
        return null;
    }

    public void renderHoverTooltip(float centerX, float centerY, float zoom, float mouseX, float mouseY, float alphaMult) {
        if (dropdownRenderer.isOpen()) {
            SkillType hovered = dropdownRenderer.findOptionAt(centerX, centerY, zoom, mouseX, mouseY);
            if (hovered != null) {
                tooltipRenderer.renderTooltipForType(hovered, mouseX, mouseY, alphaMult);
            }
            return;
        }

        SkillNode hovered = findNodeAt(centerX, centerY, zoom, mouseX, mouseY);
        if (hovered != null) {
            tooltipRenderer.renderTooltip(hovered, mouseX, mouseY, alphaMult);
        }
    }

    public SkillNode findNodeAt(float centerX, float centerY, float zoom, float x, float y) {
        for (SkillNode node : SkillTree.getAllNodes().values()) {
            float nodeX = centerX + node.getOffsetX() * zoom;
            float nodeY = centerY - node.getOffsetY() * zoom;
            float halfSize = NODE_SIZE * zoom * node.getType().getTier().getSizeMultiplier() / 2f;
            if (Math.abs(x - nodeX) <= halfSize && Math.abs(y - nodeY) <= halfSize) {
                return node;
            }
        }
        return null;
    }

    public SkillNode wormholeJumpTarget(SkillNode node, boolean ctrlDown) {
        if (!ctrlDown) return null;
        if (node.getType().getTier() != SkillTier.WORMHOLE) return null;
        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        if (!data.isAllocated(node.getId())) return null;
        String pairedId = node.getPairedNodeId();
        if (pairedId == null) return null;
        return SkillTree.get(pairedId);
    }

    public void toggleAllocation(SkillNode node, boolean ctrlDown) {
        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        boolean wasAllocated = data.isAllocated(node.getId());
        boolean isOptional = node.getType().isOptional();
        int opCost = opCostFor(node);

        if (!wasAllocated && isOptional) {
            toggleOptionalAllocation(node, ctrlDown, data, opCost);
            return;
        }

        if (!canToggle(node, data, wasAllocated, isOptional)) {
            return;
        }

        data.toggle(node, SkillTree.getAllNodes().values(), satisfiedRootId(), totalOpBudgetForNodes(), opCost, ShipLevelConfig.maxAllocatedNodes());
        boolean isAllocatedNow = data.isAllocated(node.getId());
        if (isAllocatedNow != wasAllocated) {
            refreshAfterAllocationChange(node, isAllocatedNow);
        }
    }

    private void toggleOptionalAllocation(SkillNode node, boolean ctrlDown, ShipSkillData data, int opCost) {
        if (!data.canAllocate(node, satisfiedRootId(), totalOpBudgetForNodes(), opCost, ShipLevelConfig.maxAllocatedNodes())) {
            return;
        }
        SkillType repeated = ctrlDown ? repeatableOptionFor(node) : null;
        if (repeated != null) {
            allocateOptionalNode(node, repeated);
        } else {
            dropdownRenderer.open(node);
        }
    }

    private boolean canToggle(SkillNode node, ShipSkillData data, boolean wasAllocated, boolean isOptional) {
        if (wasAllocated && isStartingRoot(node)) {
            return false;
        }

        if (!wasAllocated) {
            return blockAllocationReason(node.getType()) == null;
        }

        boolean canDeallocate = blockDeallocationReason(node) == null
                && data.canDeallocate(node, SkillTree.getAllNodes().values(), satisfiedRootId());
        if (!canDeallocate && isOptional) {
            dropdownRenderer.open(node);
        }
        return canDeallocate;
    }

    public boolean isDropdownOpen() {
        return dropdownRenderer.isOpen();
    }

    public void closeDropdown() {
        dropdownRenderer.close();
    }

    public SkillType findDropdownOptionAt(float centerX, float centerY, float zoom, float x, float y) {
        return dropdownRenderer.findOptionAt(centerX, centerY, zoom, x, y);
    }

    public void commitDropdownSelection(SkillType chosenOption) {
        SkillNode node = dropdownRenderer.getOpenNode();
        dropdownRenderer.close();
        if (node == null) return;
        if (blockAllocationReason(chosenOption) != null) return;

        allocateOptionalNode(node, chosenOption);
    }

    private SkillType repeatableOptionFor(SkillNode node) {
        if (lastChosenOptionalOption == null) return null;
        if (!node.getType().getOptionalOptionIds().contains(lastChosenOptionalOption.getId())) return null;
        if (blockAllocationReason(lastChosenOptionalOption) != null) return null;
        return lastChosenOptionalOption;
    }

    private void allocateOptionalNode(SkillNode node, SkillType chosenOption) {
        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        data.selectOption(node, chosenOption, opCostFor(node));
        lastChosenOptionalOption = chosenOption;
        refreshAfterAllocationChange(node, true);
    }

    private void refreshAfterAllocationChange(SkillNode node, boolean isAllocatedNow) {
        new SkillTreeHullMod().applyEffectsBeforeShipCreation(member.getHullSpec().getHullSize(), member.getStats(), SkillTreeHullMod.ID);
        member.setStatUpdateNeeded(true);
        member.updateStats();
        if (refitButton != null) {
            refitButton.refreshVariant();
        }
        if (isAllocatedNow) {
            ringRenderer.startPulse(node.getId());
        }
    }

    private String blockAllocationReason(SkillType type) {
        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        if (SkillTypeUnlockStatus.isLocked(type, data)) {
            return "Unidentified - explore the sector to discover this node.";
        }

        String hullModReason = hullModConflictReason(type);
        if (hullModReason != null) {
            return hullModReason;
        }

        String skillTypeReason = skillTypeConflictReason(type, data);
        if (skillTypeReason != null) {
            return skillTypeReason;
        }

        return effectBlockReason(type);
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
                HullModSpecAPI spec = Global.getSettings().getHullModSpec(hullModId);
                String name = spec != null ? spec.getDisplayName() : hullModId;
                return "Ship already has " + name + " installed.";
            }
        }
        return null;
    }

    private String skillTypeConflictReason(SkillType type, ShipSkillData data) {
        List<String> exclusiveSkillTypeIds = type.getExclusiveSkillTypeIds();
        if (exclusiveSkillTypeIds.isEmpty()) {
            return null;
        }

        for (String nodeId : data.getAllocatedNodeIds()) {
            SkillNode allocatedNode = SkillTree.get(nodeId);
            if (allocatedNode == null) {
                continue;
            }
            SkillType allocatedType = allocatedNode.resolveEffectiveType(data);
            if (exclusiveSkillTypeIds.contains(allocatedType.getId())) {
                return "Already have " + allocatedType.getDisplayName() + " allocated.";
            }
        }
        return null;
    }

    private String effectBlockReason(SkillType type) {
        List<SkillEffect> currentlyAllocatedEffects = AllocatedSkillEffects.forMember(member);
        String[] blockReason = new String[1];
        type.forEachEffect(member.getHullSpec().getHullSize(), (effect, magnitude) -> {
            if (blockReason[0] != null) return;
            blockReason[0] = effect.blockAllocationReason(member, magnitude, currentlyAllocatedEffects);
        });
        return blockReason[0];
    }

    private String blockDeallocationReason(SkillNode node) {
        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        SkillType type = node.resolveEffectiveType(data);
        String[] blockReason = new String[1];
        type.forEachEffect(member.getHullSpec().getHullSize(), (effect, magnitude) -> {
            if (blockReason[0] != null) return;
            blockReason[0] = effect.blockDeallocationReason(member, magnitude);
        });
        return blockReason[0];
    }
}
