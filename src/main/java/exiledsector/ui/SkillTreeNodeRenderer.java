package exiledsector.ui;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.effects.SkillTreeHullMod;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.HullSizeSkillEffect;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.ShipTechLevel;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import exiledsector.skills.SkillTypeEffect;
import lunalib.lunaRefit.BaseRefitButton;

import java.awt.Color;

import static exiledsector.ui.SkillTreeNodeGeometry.ICON_INSET_RATIO;
import static exiledsector.ui.SkillTreeNodeGeometry.NODE_SIZE;

final class SkillTreeNodeRenderer {

    private static final Color ALLOCATED_TINT = Color.WHITE;
    private static final Color UNALLOCATED_TINT = new Color(90, 90, 90);

    private final FleetMemberAPI member;
    private final BaseRefitButton refitButton;
    private final SkillNode activeRoot;

    private final SkillTreeNodeRingRenderer ringRenderer;
    private final SkillTreeNodeIconRenderer iconRenderer;
    private final SkillTreeNodeConnectorRenderer connectorRenderer;
    private final SkillTreeNodeTooltipRenderer tooltipRenderer;
    private final SkillTreeNodeDropdownRenderer dropdownRenderer;

    SkillTreeNodeRenderer(FleetMemberAPI member, SkillTreePanelStyle style, BaseRefitButton refitButton) {
        this.member = member;
        this.refitButton = refitButton;
        this.activeRoot = findRootNode(ShipTechLevel.of(member).rootTypeId());

        this.ringRenderer = new SkillTreeNodeRingRenderer(style);
        this.iconRenderer = new SkillTreeNodeIconRenderer();
        this.connectorRenderer = new SkillTreeNodeConnectorRenderer(style);
        this.tooltipRenderer = new SkillTreeNodeTooltipRenderer(member, style);
        this.dropdownRenderer = new SkillTreeNodeDropdownRenderer(style);

        if (activeRoot != null) {
            ShipSkillData data = ShipSkillDataManager.get(member.getId());
            if (!data.isAllocated(activeRoot.getId())) {
                data.allocate(activeRoot);
            }
        }
    }

    private String satisfiedRootId() {
        return activeRoot == null ? null : activeRoot.getId();
    }

    private boolean isStartingRoot(SkillNode node) {
        return activeRoot != null && node.getType().getTier() == SkillTier.ROOT && node.getId().equals(activeRoot.getId());
    }

    void advance(float amount) {
        ringRenderer.advance(amount);
    }

    void render(float centerX, float centerY, float zoom, float alphaMult, float mouseX, float mouseY, boolean mouseKnown) {
        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        String satisfiedRootId = satisfiedRootId();

        for (SkillNode node : SkillTree.getAllNodes().values()) {
            SkillTier tier = node.getType().getTier();
            if (tier == SkillTier.ROOT) continue;

            float nodeX = centerX + node.getOffsetX() * zoom;
            float nodeY = centerY - node.getOffsetY() * zoom;
            boolean allocated = data.isAllocated(node.getId());
            boolean breathing = !allocated && data.canAllocate(node, satisfiedRootId);
            SkillType effectiveType = node.resolveEffectiveType(data);
            float footprintSize = NODE_SIZE * zoom * tier.getSizeMultiplier();
            float iconSize = footprintSize * ICON_INSET_RATIO;

            ringRenderer.draw(nodeX, nodeY, footprintSize, alphaMult, allocated, breathing, zoom, node);

            Color tint = allocated ? ALLOCATED_TINT : UNALLOCATED_TINT;
            if (effectiveType.isOptional()) {
                iconRenderer.drawSplitIcon(effectiveType, nodeX, nodeY, iconSize, alphaMult, tint);
            } else {
                iconRenderer.drawIcon(effectiveType.getIconPath(), nodeX, nodeY, iconSize, alphaMult, tint);
            }
        }

        connectorRenderer.draw(centerX, centerY, zoom, data, satisfiedRootId, alphaMult);

        for (SkillNode node : SkillTree.getAllNodes().values()) {
            if (node.getType().getTier() != SkillTier.ROOT) continue;

            float nodeX = centerX + node.getOffsetX() * zoom;
            float nodeY = centerY - node.getOffsetY() * zoom;
            boolean isActiveRoot = activeRoot != null && node.getId().equals(activeRoot.getId());
            boolean allocated = data.isAllocated(node.getId());
            boolean breathing = !allocated && data.canAllocate(node, satisfiedRootId);
            float footprintSize = NODE_SIZE * zoom * SkillTier.ROOT.getSizeMultiplier();
            ringRenderer.draw(nodeX, nodeY, footprintSize, alphaMult, allocated, breathing, zoom, node);

            Color tint = allocated ? ALLOCATED_TINT : UNALLOCATED_TINT;
            String iconPath = isActiveRoot ? RootCrestResolver.resolve(member) : node.getType().getIconPath();
            iconRenderer.drawIcon(iconPath, nodeX, nodeY, footprintSize, alphaMult, tint);
        }

        dropdownRenderer.render(centerX, centerY, zoom, mouseX, mouseY, mouseKnown, alphaMult);
    }

    private static SkillNode findRootNode(String rootTypeId) {
        for (SkillNode node : SkillTree.getAllNodes().values()) {
            if (node.getType().getTier() == SkillTier.ROOT && node.getType().getId().equals(rootTypeId)) {
                return node;
            }
        }
        return null;
    }

    void renderHoverTooltip(float centerX, float centerY, float zoom, float mouseX, float mouseY, float alphaMult) {
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

    SkillNode findNodeAt(float centerX, float centerY, float zoom, float x, float y) {
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

    void toggleAllocation(SkillNode node) {
        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        boolean wasAllocated = data.isAllocated(node.getId());

        if (!wasAllocated && node.getType().isOptional()) {
            if (data.canAllocate(node, satisfiedRootId())) {
                dropdownRenderer.open(node);
            }
            return;
        }

        if (wasAllocated && isStartingRoot(node)) {
            return;
        }
        if (wasAllocated && blockDeallocationReason(node) != null) {
            return;
        }
        data.toggle(node, SkillTree.getAllNodes().values(), satisfiedRootId());
        boolean isAllocatedNow = data.isAllocated(node.getId());
        if (isAllocatedNow != wasAllocated) {
            refreshAfterAllocationChange(node, isAllocatedNow);
        }
    }

    boolean isDropdownOpen() {
        return dropdownRenderer.isOpen();
    }

    void closeDropdown() {
        dropdownRenderer.close();
    }

    SkillType findDropdownOptionAt(float centerX, float centerY, float zoom, float x, float y) {
        return dropdownRenderer.findOptionAt(centerX, centerY, zoom, x, y);
    }

    void commitDropdownSelection(SkillType chosenOption) {
        SkillNode node = dropdownRenderer.getOpenNode();
        dropdownRenderer.close();
        if (node == null) return;

        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        data.selectOption(node, chosenOption);
        refreshAfterAllocationChange(node, true);
    }

    private void refreshAfterAllocationChange(SkillNode node, boolean isAllocatedNow) {
        tooltipRenderer.invalidate(node.getId());
        member.setStatUpdateNeeded(true);
        member.updateStats();
        new SkillTreeHullMod().applyEffectsBeforeShipCreation(member.getHullSpec().getHullSize(), member.getStats(), SkillTreeHullMod.ID);
        if (refitButton != null) {
            refitButton.refreshVariant();
        }
        if (isAllocatedNow) {
            ringRenderer.startPulse(node.getId());
        }
    }

    private String blockDeallocationReason(SkillNode node) {
        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        SkillType type = node.resolveEffectiveType(data);
        for (SkillTypeEffect effect : type.getEffects()) {
            String reason = effect.effect().blockDeallocationReason(member, effect.magnitude());
            if (reason != null) return reason;
        }
        for (HullSizeSkillEffect effect : type.getHullSizeEffects()) {
            float magnitude = effect.valueFor(member.getHullSpec().getHullSize());
            String reason = effect.effect().blockDeallocationReason(member, magnitude);
            if (reason != null) return reason;
        }
        return null;
    }
}
