package exiledsector.skills;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.persistence.ShipSkillDataManager;

import java.util.Collection;

public final class ShipLevelSystem {

    private ShipLevelSystem() {
    }

    public static void awardXpToFleet(CampaignFleetAPI fleet, float xpAmount) {
        if (fleet == null) return;

        Collection<SkillNode> allNodes = SkillTree.getAllNodes().values();
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            ShipSkillData data = ShipSkillDataManager.get(member.getId());
            int opCostPerNode = SkillNodeOpCost.perNode(member.getHullSpec());
            awardXp(data, xpAmount, ShipLevelConfig.xpBase(), ShipLevelConfig.xpGrowth(),
                    ShipLevelConfig.xpGrowthCutoffLevel(), ShipLevelConfig.maxLevel(), allNodes, opCostPerNode);
        }
    }

    public static float xpToReachNextLevel(int currentLevel, float xpBase, float xpGrowth, int growthCutoffLevel) {
        int cappedLevel = Math.min(currentLevel, Math.max(growthCutoffLevel - 1, 0));
        return xpBase * (float) Math.pow(xpGrowth, cappedLevel);
    }

    public static void awardXp(ShipSkillData data, float xpAmount, float xpBase, float xpGrowth, int growthCutoffLevel,
                                int maxLevel, Collection<SkillNode> allNodes, int opCostPerNode) {
        if (data.getLevel() >= maxLevel) return;

        data.addXp(xpAmount);
        while (data.getLevel() < maxLevel) {
            float required = xpToReachNextLevel(data.getLevel(), xpBase, xpGrowth, growthCutoffLevel);
            if (data.getXp() < required) break;

            data.subtractXp(required);
            data.incrementLevel();
            if (!data.convertMostRecentAllocationToFree(allNodes, opCostPerNode)) {
                data.addFreeAllocationCredit();
            }
        }
    }
}
