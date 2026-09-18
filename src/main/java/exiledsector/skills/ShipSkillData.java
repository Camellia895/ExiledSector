package exiledsector.skills;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;


public class ShipSkillData {

    private final Set<String> allocatedNodeIds = new LinkedHashSet<>();
    private int spentOp = 0;
    private float xp = 0f;

    public boolean isAllocated(String nodeId) {
        return allocatedNodeIds.contains(nodeId);
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
        spentOp -= node.getOpCost();
        xp += node.getXpCost();
    }

    public boolean canAllocate(SkillNode node, String satisfiedRootId) {
        if (node.getPrerequisiteNodeIds().isEmpty()) {
            return true;
        }
        for (String prerequisiteId : node.getPrerequisiteNodeIds()) {
            if (isSatisfied(prerequisiteId, satisfiedRootId)) {
                return true;
            }
        }
        return false;
    }

    public boolean canDeallocate(SkillNode node, Collection<SkillNode> allNodes, String satisfiedRootId) {
        for (SkillNode candidate : allNodes) {
            if (!isAllocated(candidate.getId())) continue;
            if (!candidate.getPrerequisiteNodeIds().contains(node.getId())) continue;
            if (!hasAnotherSatisfiedPrerequisite(candidate, node.getId(), satisfiedRootId)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasAnotherSatisfiedPrerequisite(SkillNode node, String excludingId, String satisfiedRootId) {
        for (String prerequisiteId : node.getPrerequisiteNodeIds()) {
            if (!prerequisiteId.equals(excludingId) && isSatisfied(prerequisiteId, satisfiedRootId)) {
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
