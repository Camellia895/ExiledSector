package exiledsector.skills;

import java.util.LinkedHashSet;
import java.util.Set;


public class ShipSkillData {

    private final Set<String> unlockedNodeIds = new LinkedHashSet<>();
    private int spentOp = 0;
    private float xp = 0f;

    public boolean isUnlocked(String nodeId) {
        return unlockedNodeIds.contains(nodeId);
    }

    public Set<String> getUnlockedNodeIds() {
        return unlockedNodeIds;
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

    public void unlock(SkillNode node) {
        unlockedNodeIds.add(node.getId());
        spentOp += node.getOpCost();
        xp -= node.getXpCost();
    }
}
