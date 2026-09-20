package exiledsector.skills;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ShipSkillData {

    private final Set<String> allocatedNodeIds = new LinkedHashSet<>();
    private Map<String, String> optionalSelections = new LinkedHashMap<>();
    private int spentOp = 0;
    private float xp = 0f;

    public boolean isAllocated(String nodeId) {
        return allocatedNodeIds.contains(nodeId);
    }

    public String getOptionalSelection(String nodeId) {
        if (optionalSelections == null) return null;
        return optionalSelections.get(nodeId);
    }

    public void selectOption(SkillNode node, SkillType chosenOption) {
        allocatedNodeIds.add(node.getId());
        if (optionalSelections == null) optionalSelections = new LinkedHashMap<>();
        optionalSelections.put(node.getId(), chosenOption.getId());
        spentOp += chosenOption.getOpCost();
        xp -= chosenOption.getXpCost();
    }

    public boolean isSatisfied(String nodeId, String satisfiedRootId) {
        return isAllocated(nodeId) || nodeId.equals(satisfiedRootId);
    }

    public Set<String> getAllocatedNodeIds() {
        return allocatedNodeIds;
    }

    public int getSpentOp() {
        return spentOp;
    }

    public float getXp() {
        return xp;
    }

    public void addXp(float amount) {
        xp += amount;
    }

    public void allocate(SkillNode node) {
        allocatedNodeIds.add(node.getId());
        spentOp += node.getOpCost();
        xp -= node.getXpCost();
    }

    public void deallocate(SkillNode node) {
        allocatedNodeIds.remove(node.getId());
        String selectedOptionId = optionalSelections == null ? null : optionalSelections.remove(node.getId());
        if (selectedOptionId != null) {
            SkillType chosenOption = SkillTree.getType(selectedOptionId);
            if (chosenOption != null) {
                spentOp -= chosenOption.getOpCost();
                xp += chosenOption.getXpCost();
                return;
            }
        }
        spentOp -= node.getOpCost();
        xp += node.getXpCost();
    }

    public boolean canAllocate(SkillNode node, String satisfiedRootId) {
        if (node.getConnectedNodeIds().isEmpty()) {
            return true;
        }
        for (String connectedId : node.getConnectedNodeIds()) {
            if (isSatisfied(connectedId, satisfiedRootId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A node can be deallocated only if it doesn't cause any currently-reachable allocated node to
     * become unreachable - i.e. deallocating it must not sever the link between the root and any
     * other allocated node that's presently connected. This is done by computing the full set of
     * root-reachable allocated nodes twice (BFS seeded from the satisfied root plus any allocated
     * node with no prerequisites at all, walking the "child lists its prerequisites" edges reversed
     * so we can walk root-outward) - once with everything as-is, once with the candidate node
     * excluded - and comparing the two sets, rather than just checking the node's immediate
     * neighbors (a local-only check can't detect a break further down the chain).
     *
     * Comparing against the "before" set (rather than requiring every allocated node to be
     * reachable "after", full stop) matters: it means a node that's already stranded for some
     * unrelated reason - e.g. a leftover node from an earlier version of this logic, or manual
     * editor surgery - doesn't block deallocation everywhere else in the tree. Only removing this
     * specific node is checked; pre-existing disconnection elsewhere is not this action's problem.
     *
     * Only the ship's own starting root (satisfiedRootId) is an unconditional anchor. Any other
     * allocated ROOT-tier node (from the multi-root-per-ship feature) is NOT automatically treated
     * as reachable just because it's a root - it has to trace its own path back to the starting
     * root like any other node, otherwise a loop built entirely off a second root could pass this
     * check without ever actually connecting back to the ship's real root.
     */
    public boolean canDeallocate(SkillNode node, Collection<SkillNode> allNodes, String satisfiedRootId) {
        Map<String, SkillNode> byId = new HashMap<>();
        Map<String, List<String>> childrenOf = new HashMap<>();
        for (SkillNode candidate : allNodes) {
            byId.put(candidate.getId(), candidate);
            for (String prerequisiteId : candidate.getConnectedNodeIds()) {
                childrenOf.computeIfAbsent(prerequisiteId, key -> new ArrayList<>()).add(candidate.getId());
            }
        }

        Set<String> reachableBefore = reachableAllocatedNodeIds(byId, childrenOf, satisfiedRootId, null);
        Set<String> reachableAfter = reachableAllocatedNodeIds(byId, childrenOf, satisfiedRootId, node.getId());

        for (String allocatedId : reachableBefore) {
            if (allocatedId.equals(node.getId())) continue;
            if (!reachableAfter.contains(allocatedId)) {
                return false;
            }
        }
        return true;
    }

    private Set<String> reachableAllocatedNodeIds(Map<String, SkillNode> byId, Map<String, List<String>> childrenOf,
                                                    String satisfiedRootId, String excludedNodeId) {
        Set<String> reachable = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        if (satisfiedRootId != null && !satisfiedRootId.equals(excludedNodeId) && reachable.add(satisfiedRootId)) {
            queue.add(satisfiedRootId);
        }
        for (String allocatedId : allocatedNodeIds) {
            if (allocatedId.equals(excludedNodeId)) continue;
            SkillNode allocatedNode = byId.get(allocatedId);
            if (allocatedNode == null) continue;
            if (allocatedNode.getConnectedNodeIds().isEmpty() && reachable.add(allocatedId)) {
                queue.add(allocatedId);
            }
        }

        while (!queue.isEmpty()) {
            String currentId = queue.poll();
            for (String childId : childrenOf.getOrDefault(currentId, List.of())) {
                if (childId.equals(excludedNodeId)) continue;
                if (!isAllocated(childId)) continue;
                if (reachable.add(childId)) {
                    queue.add(childId);
                }
            }
        }
        return reachable;
    }

    public void toggle(SkillNode node, Collection<SkillNode> allNodes, String satisfiedRootId) {
        if (isAllocated(node.getId())) {
            if (canDeallocate(node, allNodes, satisfiedRootId)) {
                deallocate(node);
            }
        } else if (canAllocate(node, satisfiedRootId)) {
            allocate(node);
        }
    }
}
