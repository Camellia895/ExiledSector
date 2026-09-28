package exiledsector.ui.node;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import exiledsector.skills.unlock.SkillTypeUnlockStatus;
import exiledsector.ui.SkillTreePanelStyle;
import lunalib.lunaRefit.BaseRefitButton;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static exiledsector.ui.node.SkillTreeNodeGeometry.ICON_INSET_RATIO;
import static exiledsector.ui.node.SkillTreeNodeGeometry.NODE_SIZE;

public final class SkillTreeNodeRenderer {

    private static final Color ALLOCATED_TINT = Color.WHITE;
    private static final Color UNALLOCATED_TINT = new Color(90, 90, 90);

    private final FleetMemberAPI member;
    private final BaseRefitButton refitButton;
    private final SkillTreePanelStyle style;
    private final StartingRootChoice rootChoice;
    private final NodeAllocator allocator;

    private final SkillTreeNodeRingRenderer ringRenderer;
    private final SkillTreeNodeIconRenderer iconRenderer;
    private final SkillTreeNodeGhostRenderer ghostRenderer;
    private final SkillTreeWormholeGhostFlights wormholeGhostFlights;
    private final SkillTreeNodeConnectorRenderer connectorRenderer;
    private final SkillTreeNodeTooltipRenderer tooltipRenderer;
    private final SkillTreeNodeDropdownRenderer dropdownRenderer;
    private final NodeSearch search;

    private SkillType lastChosenOptionalOption;
    private LazyFont.DrawableString startingRootPrompt;

    public SkillTreeNodeRenderer(FleetMemberAPI member, ShipVariantAPI variant, SkillTreePanelStyle style, BaseRefitButton refitButton,
                                 NodeSearch search) {
        this.member = member;
        this.refitButton = refitButton;
        this.search = search;
        this.style = style;
        this.rootChoice = initialRootChoice(ShipSkillDataManager.get(member.getId()));
        this.allocator = new NodeAllocator(member, variant, rootChoice::chosen);
        style.setAccentIconPath(RootCrestResolver.resolve(member, rootChoice.chosen()));

        this.ringRenderer = new SkillTreeNodeRingRenderer(style);
        this.iconRenderer = new SkillTreeNodeIconRenderer();
        this.ghostRenderer = new SkillTreeNodeGhostRenderer();
        this.wormholeGhostFlights = new SkillTreeWormholeGhostFlights(ghostRenderer, new Random());
        this.connectorRenderer = new SkillTreeNodeConnectorRenderer(style, search);
        this.tooltipRenderer = new SkillTreeNodeTooltipRenderer(member, style);
        this.dropdownRenderer = new SkillTreeNodeDropdownRenderer(style);
    }

    private static StartingRootChoice initialRootChoice(ShipSkillData data) {
        String startingRootId = data.resolveStartingRootId(SkillTree.getAllNodes().values());
        SkillNode startingRoot = startingRootId == null ? null : SkillTree.get(startingRootId);
        if (startingRoot != null) {
            return StartingRootChoice.alreadyChosen(startingRoot);
        }
        List<SkillNode> roots = new ArrayList<>();
        for (SkillNode node : SkillTree.getAllNodes().values()) {
            if (node.getType().getTier() == SkillTier.ROOT) {
                roots.add(node);
            }
        }
        return StartingRootChoice.pending(roots);
    }

    public SkillNode getStartingRoot() {
        return rootChoice.phase() == StartingRootChoice.Phase.CHOSEN ? rootChoice.chosen() : null;
    }

    public boolean isChoosingStartingRoot() {
        return rootChoice.phase() == StartingRootChoice.Phase.CHOOSING;
    }

    public boolean isStartingRootFlying() {
        return rootChoice.phase() == StartingRootChoice.Phase.FLYING;
    }

    public boolean isStartingRootInputLocked() {
        return rootChoice.isInputLocked();
    }

    public float treeAlpha() {
        return rootChoice.treeAlpha();
    }

    public float startingRootOffsetX() {
        return rootChoice.offsetX(rootChoice.chosen());
    }

    public float startingRootOffsetY() {
        return rootChoice.offsetY(rootChoice.chosen());
    }

    public void chooseStartingRoot(SkillNode root) {
        if (!isChoosingStartingRoot() || !allocator.chooseStartingRoot(root)) {
            return;
        }
        rootChoice.choose(root);
        style.setAccentIconPath(RootCrestResolver.resolve(member, root));
        afterAllocationChange(root, true);
    }

    public void advance(float amount) {
        rootChoice.advance(amount);
        ringRenderer.advance(amount);
        ghostRenderer.advance(amount);
        ShipSkillData data = allocator.data();
        wormholeGhostFlights.advance(amount, data);
        connectorRenderer.advance(amount, data, allocator.satisfiedRootId());
    }

    public void render(float centerX, float centerY, float zoom, float alphaMult, float mouseX, float mouseY, boolean mouseKnown) {
        NodeAllocator.Snapshot allocation = allocator.snapshot();
        Vector2f center = new Vector2f(centerX, centerY);
        float treeAlphaMult = alphaMult * rootChoice.treeAlpha();

        for (SkillNode node : SkillTree.getAllNodes().values()) {
            if (node.getType().getTier() != SkillTier.ROOT) {
                renderNode(node, center, zoom, treeAlphaMult, allocation);
            }
        }

        connectorRenderer.draw(centerX, centerY, zoom, allocation.data(), allocation.satisfiedRootId(), treeAlphaMult);
        wormholeGhostFlights.draw(centerX, centerY, zoom, treeAlphaMult * search.backgroundAlpha());

        for (SkillNode node : SkillTree.getAllNodes().values()) {
            if (node.getType().getTier() == SkillTier.ROOT) {
                renderRootNode(node, center, zoom, alphaMult, allocation);
            }
        }

        if (isChoosingStartingRoot()) {
            renderStartingRootPrompt(centerX, centerY - rootChoice.promptOffsetY() * zoom);
        }

        dropdownRenderer.render(centerX, centerY, zoom, mouseX, mouseY, mouseKnown, alphaMult);
    }

    private void renderStartingRootPrompt(float x, float y) {
        LazyFont font = style.getFont();
        if (font == null) {
            return;
        }
        if (startingRootPrompt == null) {
            startingRootPrompt = SkillTreePanelStyle.buildSimpleText(font, StartingRootChoice.PROMPT,
                    SkillTreePanelStyle.TOOLTIP_TITLE_FONT_SIZE, SkillTreePanelStyle.TOOLTIP_TITLE_COLOR, LazyFont.TextAnchor.BOTTOM_CENTER);
        }
        startingRootPrompt.draw(x, y);
    }

    private void renderNode(SkillNode node, Vector2f center, float zoom, float alphaMult, NodeAllocator.Snapshot allocation) {
        ShipSkillData data = allocation.data();
        SkillTier tier = node.getType().getTier();
        float nodeX = center.x + node.getOffsetX() * zoom;
        float nodeY = center.y - node.getOffsetY() * zoom;
        float footprintSize = NODE_SIZE * zoom * tier.getSizeMultiplier();

        if (SkillTypeUnlockStatus.isHidden(node.getType(), data)) {
            ghostRenderer.draw(nodeX, nodeY, footprintSize, alphaMult * search.backgroundAlpha(), node.getId());
            return;
        }

        boolean allocated = data.isAllocated(node.getId());
        boolean breathing = !allocated && allocation.canAllocate(node);
        SkillType effectiveType = node.resolveEffectiveType(data);
        float iconSize = footprintSize * ICON_INSET_RATIO;

        float nodeAlpha = alphaMult * search.nodeAlpha(node, data);
        ringRenderer.draw(new Vector2f(nodeX, nodeY), footprintSize, nodeAlpha, allocated, breathing, zoom, node);

        if (tier != SkillTier.WORMHOLE) {
            Color tint = iconTint(node, data, allocated);
            if (effectiveType.isOptional()) {
                iconRenderer.drawSplitIcon(effectiveType, nodeX, nodeY, iconSize, nodeAlpha, tint);
            } else {
                iconRenderer.drawIcon(effectiveType.getIconPath(), nodeX, nodeY, iconSize, nodeAlpha, tint);
            }
        }
    }

    private void renderRootNode(SkillNode node, Vector2f center, float zoom, float alphaMult, NodeAllocator.Snapshot allocation) {
        ShipSkillData data = allocation.data();
        float nodeX = center.x + rootChoice.offsetX(node) * zoom;
        float nodeY = center.y - rootChoice.offsetY(node) * zoom;
        boolean choosing = isChoosingStartingRoot();
        boolean allocated = data.isAllocated(node.getId());
        boolean breathing = choosing || (!allocated && allocation.canAllocate(node));
        float footprintSize = NODE_SIZE * zoom * SkillTier.ROOT.getSizeMultiplier();
        float nodeAlpha = alphaMult * search.nodeAlpha(node, data);
        ringRenderer.draw(new Vector2f(nodeX, nodeY), footprintSize, nodeAlpha, allocated, breathing, zoom, node);

        Color tint = choosing ? ALLOCATED_TINT : iconTint(node, data, allocated);
        String iconPath = allocator.isStartingRoot(node) ? RootCrestResolver.resolve(member, node) : node.getType().getIconPath();
        iconRenderer.drawIcon(iconPath, nodeX, nodeY, footprintSize, nodeAlpha, tint);
    }

    private Color iconTint(SkillNode node, ShipSkillData data, boolean allocated) {
        return allocated || search.matches(node, data) ? ALLOCATED_TINT : UNALLOCATED_TINT;
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
        if (isStartingRootFlying()) {
            return null;
        }
        boolean choosing = isChoosingStartingRoot();
        for (SkillNode node : SkillTree.getAllNodes().values()) {
            if (choosing && node.getType().getTier() != SkillTier.ROOT) {
                continue;
            }
            float nodeX = centerX + rootChoice.offsetX(node) * zoom;
            float nodeY = centerY - rootChoice.offsetY(node) * zoom;
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
        if (!allocator.data().isAllocated(node.getId())) return null;
        String pairedId = node.getPairedNodeId();
        if (pairedId == null) return null;
        return SkillTree.get(pairedId);
    }

    public void launchWormholeGhosts(SkillNode from, SkillNode to) {
        wormholeGhostFlights.launchFrom(from, to);
    }

    public void toggleAllocation(SkillNode node, boolean ctrlDown) {
        if (isStartingRootInputLocked()) {
            return;
        }
        boolean wasAllocated = allocator.data().isAllocated(node.getId());
        boolean isOptional = node.getType().isOptional();

        if (!wasAllocated && isOptional) {
            toggleOptionalAllocation(node, ctrlDown);
            return;
        }

        if (!canToggle(node, wasAllocated, isOptional)) {
            return;
        }

        if (allocator.toggle(node)) {
            afterAllocationChange(node, !wasAllocated);
        }
    }

    private void toggleOptionalAllocation(SkillNode node, boolean ctrlDown) {
        if (!allocator.canAllocate(node)) {
            return;
        }
        SkillType repeated = ctrlDown ? repeatableOptionFor(node) : null;
        if (repeated != null) {
            allocateOptionalNode(node, repeated);
        } else {
            dropdownRenderer.open(node);
        }
    }

    private boolean canToggle(SkillNode node, boolean wasAllocated, boolean isOptional) {
        if (!wasAllocated) {
            return allocator.blockAllocationReason(node.getType()) == null;
        }

        boolean canDeallocate = allocator.canDeallocate(node);
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
        if (allocator.blockAllocationReason(chosenOption) != null) return;

        allocateOptionalNode(node, chosenOption);
    }

    private SkillType repeatableOptionFor(SkillNode node) {
        if (lastChosenOptionalOption == null) return null;
        if (!node.getType().getOptionalOptionIds().contains(lastChosenOptionalOption.getId())) return null;
        if (allocator.blockAllocationReason(lastChosenOptionalOption) != null) return null;
        return lastChosenOptionalOption;
    }

    private void allocateOptionalNode(SkillNode node, SkillType chosenOption) {
        allocator.allocateOption(node, chosenOption);
        lastChosenOptionalOption = chosenOption;
        afterAllocationChange(node, true);
    }

    private void afterAllocationChange(SkillNode node, boolean isAllocatedNow) {
        if (refitButton != null) {
            refitButton.refreshVariant();
        }
        if (isAllocatedNow) {
            ringRenderer.startPulse(node.getId());
        }
    }
}
