package exiledsector.effects;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.ShipLevelConfig;
import exiledsector.skills.ShipLevelSystem;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillNodeOpCost;
import exiledsector.skills.SkillTree;

import java.util.Collection;

public class ShipLevelScript implements EveryFrameScript {

    private boolean xpAwarded = false;

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null || engine.isSimulation()) {
            xpAwarded = false;
            return;
        }

        if (xpAwarded || !engine.isCombatOver()) return;
        xpAwarded = true;
        awardCombatXp();
    }

    private void awardCombatXp() {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) return;

        float xp = ShipLevelConfig.xpPerCombat();
        BattleAPI battle = playerFleet.getBattle();
        if (battle != null && battle.wasFleetDefeated(playerFleet, battle.getNonPlayerCombined())) {
            xp *= ShipLevelConfig.xpLossMultiplier();
        }

        Collection<SkillNode> allNodes = SkillTree.getAllNodes().values();
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            ShipSkillData data = ShipSkillDataManager.get(member.getId());
            int opCostPerNode = SkillNodeOpCost.perNode(member.getHullSpec());
            ShipLevelSystem.awardXp(data, xp, ShipLevelConfig.xpBase(), ShipLevelConfig.xpGrowth(),
                    ShipLevelConfig.maxLevel(), allNodes, opCostPerNode);
        }
    }
}
