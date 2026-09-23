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
    private int level = 0;
    private float xp = 0f;
    private int bankedFreeAllocations = 0;
    private Set<String> freeNodeIds = new LinkedHashSet<>();
    private Set<String> pairedFreeNodeIds = new LinkedHashSet<>();

    // Ships saved before this field existed deserialize with freeNodeIds left null (XStream does not
    // run field initializers), so every access must go through this lazy accessor instead of the field.
    private Set<String> freeNodeIds() {
        if (freeNodeIds == null) freeNodeIds = new LinkedHashSet<>();
        return freeNodeIds;
    }

    private Set<String> pairedFreeNodeIds() {
        if (pairedFreeNodeIds == null) pairedFreeNodeIds = new LinkedHashSet<>();
        return pairedFreeNodeIds;
    }

    public boolean isAllocated(String nodeId) {
        return allocatedNodeIds.contains(nodeId);
    }

    public String getOptionalSelection(String nodeId) {
        if (optionalSelections == null) return null;
        return optionalSelections.get(nodeId);
    }

    public void selectOption(SkillNode node, SkillType chosenOption, int opCost) {
        if (!isAllocated(node.getId())) {
            if (opCost > 0 && bankedFreeAllocations > 0) {
                bankedFreeAllocations--;
                freeNodeIds().add(node.getId());
            } else {
                spentOp += opCost;
            }
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

    public int getLevel() {
        return level;
    }

    public float getXp() {
        return xp;
    }

    public int getBankedFreeAllocations() {
        return bankedFreeAllocations;
    }

    public boolean isFreeNode(String nodeId) {
        return freeNodeIds().contains(nodeId) || pairedFreeNodeIds().contains(nodeId);
    }

    public void addXp(float amount) {
        xp += amount;
    }

    public void subtractXp(float amount) {
        xp -= amount;
    }

    public void incrementLevel() {
        level++;
    }

    public void addFreeAllocationCredit() {
        bankedFreeAllocations++;
    }

    public boolean convertMostRecentAllocationToFree(Collection<SkillNode> allNodes, int opCostPerNode) {
        Map<String, SkillNode> byId = new HashMap<>();
        for (SkillNode candidate : allNodes) {
            byId.put(candidate.getId(), candidate);
        }
        List<String> order = new ArrayList<>(allocatedNodeIds);
        for (int i = order.size() - 1; i >= 0; i--) {
            String nodeId = order.get(i);
            if (freeNodeIds().contains(nodeId)) continue;
            SkillNode node = byId.get(nodeId);
            if (node != null && node.getType().getTier() == SkillTier.ROOT) continue;
            freeNodeIds().add(nodeId);
            spentOp -= opCostPerNode;
            return true;
        }
        return false;
    }

    public void allocate(SkillNode node, int opCost) {
        allocatedNodeIds.add(node.getId());
        if (opCost > 0 && bankedFreeAllocations > 0) {
            bankedFreeAllocations--;
            freeNodeIds().add(node.getId());
        } else {
            spentOp += opCost;
        }

        String pairedId = node.getPairedNodeId();
        if (pairedId != null && !isAllocated(pairedId)) {
            allocatedNodeIds.add(pairedId);
            pairedFreeNodeIds().add(pairedId);
        }
    }

    public void deallocate(SkillNode node, int opCost) {
        allocatedNodeIds.remove(node.getId());
        if (optionalSelections != null) {
            optionalSelections.remove(node.getId());
        }
        if (freeNodeIds().remove(node.getId())) {
            bankedFreeAllocations++;
        } else if (!pairedFreeNodeIds().remove(node.getId())) {
            spentOp -= opCost;
        }

        String pairedId = node.getPairedNodeId();
        if (pairedId != null && allocatedNodeIds.remove(pairedId)) {
            if (optionalSelections != null) {
                optionalSelections.remove(pairedId);
            }
            if (freeNodeIds().remove(pairedId)) {
                bankedFreeAllocations++;
            } else if (!pairedFreeNodeIds().remove(pairedId)) {
                spentOp -= opCost;
            }
        }
    }

    public boolean canAllocate(SkillNode node, String satisfiedRootId, int totalOp, int opCost, int maxAllocatedNodes) {
        int slotsNeeded = 1;
        String pairedId = node.getPairedNodeId();
        if (pairedId != null && !isAllocated(pairedId)) {
            slotsNeeded = 2;
        }
        if (allocatedNodeIds.size() + slotsNeeded > maxAllocatedNodes) {
            return false;
        }
        if (opCost > 0 && bankedFreeAllocations <= 0 && spentOp + opCost > totalOp) {
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

        Set<String> excluded = new HashSet<>();
        excluded.add(node.getId());
        String pairedId = node.getPairedNodeId();
        if (pairedId != null) excluded.add(pairedId);

        Set<String> reachableBefore = reachableAllocatedNodeIds(byId, childrenOf, satisfiedRootId, Set.of());
        Set<String> reachableAfter = reachableAllocatedNodeIds(byId, childrenOf, satisfiedRootId, excluded);

        for (String allocatedId : reachableBefore) {
            if (excluded.contains(allocatedId)) continue;
            if (!reachableAfter.contains(allocatedId)) {
                return false;
            }
        }
        return true;
    }

    private Set<String> reachableAllocatedNodeIds(Map<String, SkillNode> byId, Map<String, List<String>> childrenOf,
                                                    String satisfiedRootId, Set<String> excludedNodeIds) {
        Set<String> reachable = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        if (satisfiedRootId != null && !excludedNodeIds.contains(satisfiedRootId) && reachable.add(satisfiedRootId)) {
            queue.add(satisfiedRootId);
        }
        for (String allocatedId : allocatedNodeIds) {
            if (excludedNodeIds.contains(allocatedId)) continue;
            SkillNode allocatedNode = byId.get(allocatedId);
            if (allocatedNode == null) continue;
            if (allocatedNode.getConnectedNodeIds().isEmpty() && reachable.add(allocatedId)) {
                queue.add(allocatedId);
            }
        }

        while (!queue.isEmpty()) {
            String currentId = queue.poll();
            for (String childId : childrenOf.getOrDefault(currentId, List.of())) {
                if (excludedNodeIds.contains(childId)) continue;
                if (!isAllocated(childId)) continue;
                if (reachable.add(childId)) {
                    queue.add(childId);
                }
            }
        }
        return reachable;
    }

    public void toggle(SkillNode node, Collection<SkillNode> allNodes, String satisfiedRootId, int totalOp, int opCost, int maxAllocatedNodes) {
        if (isAllocated(node.getId())) {
            if (canDeallocate(node, allNodes, satisfiedRootId)) {
                deallocate(node, opCost);
            }
        } else if (canAllocate(node, satisfiedRootId, totalOp, opCost, maxAllocatedNodes)) {
            allocate(node, opCost);
        }
    }
}
