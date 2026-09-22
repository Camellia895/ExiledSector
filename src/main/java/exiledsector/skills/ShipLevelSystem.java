package exiledsector.skills;

import java.util.Collection;

public final class ShipLevelSystem {

    private ShipLevelSystem() {
    }

    public static float xpToReachNextLevel(int currentLevel, float xpBase, float xpGrowth) {
        return xpBase * (float) Math.pow(xpGrowth, currentLevel);
    }

    public static void awardXp(ShipSkillData data, float xpAmount, float xpBase, float xpGrowth,
                                int maxLevel, Collection<SkillNode> allNodes, int opCostPerNode) {
        if (data.getLevel() >= maxLevel) return;

        data.addXp(xpAmount);
        while (data.getLevel() < maxLevel) {
            float required = xpToReachNextLevel(data.getLevel(), xpBase, xpGrowth);
            if (data.getXp() < required) break;

            data.subtractXp(required);
            data.incrementLevel();
            if (!data.convertMostRecentAllocationToFree(allNodes, opCostPerNode)) {
                data.addFreeAllocationCredit();
            }
        }
    }
}
