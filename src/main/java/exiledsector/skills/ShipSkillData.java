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

    public boolean canAllocate(SkillNode node) {
        for (String prerequisiteId : node.getPrerequisiteNodeIds()) {
            if (!isAllocated(prerequisiteId)) {
                return false;
            }
        }
        return true;
    }

    public boolean canDeallocate(SkillNode node, Collection<SkillNode> allNodes) {
        for (SkillNode candidate : allNodes) {
            if (candidate.getPrerequisiteNodeIds().contains(node.getId()) && isAllocated(candidate.getId())) {
                return false;
            }
        }
        return true;
    }

    public void toggle(SkillNode node, Collection<SkillNode> allNodes) {
        if (isAllocated(node.getId())) {
            if (canDeallocate(node, allNodes)) {
                deallocate(node);
            }
        } else if (canAllocate(node)) {
            allocate(node);
        }
    }
}
