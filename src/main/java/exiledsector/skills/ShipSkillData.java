package exiledsector.skills;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

    public boolean canDeallocate(SkillNode node, Collection<SkillNode> allNodes, String satisfiedRootId) {
        for (SkillNode candidate : allNodes) {
            if (!isAllocated(candidate.getId())) continue;
            if (!candidate.getConnectedNodeIds().contains(node.getId())) continue;
            if (!hasAnotherSatisfiedConnection(candidate, node.getId(), satisfiedRootId)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasAnotherSatisfiedConnection(SkillNode node, String excludingId, String satisfiedRootId) {
        for (String connectedId : node.getConnectedNodeIds()) {
            if (!connectedId.equals(excludingId) && isSatisfied(connectedId, satisfiedRootId)) {
                return true;
            }
        }
        return false;
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
