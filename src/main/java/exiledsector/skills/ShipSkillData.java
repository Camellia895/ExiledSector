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

    public boolean isAllocated(String nodeId) {
        return allocatedNodeIds.contains(nodeId);
    }

    public String getOptionalSelection(String nodeId) {
        if (optionalSelections == null) return null;
        return optionalSelections.get(nodeId);
    }

    public void selectOption(SkillNode node, SkillType chosenOption, int opCost) {
        if (!isAllocated(node.getId())) {
            spentOp += opCost;
        }
        allocatedNodeIds.add(node.getId());
        if (optionalSelections == null) optionalSelections = new LinkedHashMap<>();
        optionalSelections.put(node.getId(), chosenOption.getId());
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

    public void allocate(SkillNode node, int opCost) {
        allocatedNodeIds.add(node.getId());
        spentOp += opCost;
    }

    public void deallocate(SkillNode node, int opCost) {
        allocatedNodeIds.remove(node.getId());
        if (optionalSelections != null) {
            optionalSelections.remove(node.getId());
        }
        spentOp -= opCost;
    }

    public boolean canAllocate(SkillNode node, String satisfiedRootId, int totalOp, int opCost) {
        if (spentOp + opCost > totalOp) {
            return false;
        }
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

    public void toggle(SkillNode node, Collection<SkillNode> allNodes, String satisfiedRootId, int totalOp, int opCost) {
        if (isAllocated(node.getId())) {
            if (canDeallocate(node, allNodes, satisfiedRootId)) {
                deallocate(node, opCost);
            }
        } else if (canAllocate(node, satisfiedRootId, totalOp, opCost)) {
            allocate(node, opCost);
        }
    }
}
