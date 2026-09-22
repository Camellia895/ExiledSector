package exiledsector.effects;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.mission.FleetSide;
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
        awardCombatXp(engine);
    }

    private void awardCombatXp(CombatEngineAPI engine) {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) return;

        float xp = enemyDeploymentPointsDestroyed(engine) * ShipLevelConfig.xpPerDeploymentPoint();
        BattleAPI battle = playerFleet.getBattle();
        if (battle != null && battle.wasFleetDefeated(playerFleet, battle.getNonPlayerCombined())) {
            xp *= ShipLevelConfig.xpLossMultiplier();
        }

        Collection<SkillNode> allNodes = SkillTree.getAllNodes().values();
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            ShipSkillData data = ShipSkillDataManager.get(member.getId());
            int opCostPerNode = SkillNodeOpCost.perNode(member.getHullSpec());
            ShipLevelSystem.awardXp(data, xp, ShipLevelConfig.xpBase(), ShipLevelConfig.xpGrowth(),
                    ShipLevelConfig.xpGrowthCutoffLevel(), ShipLevelConfig.maxLevel(), allNodes, opCostPerNode);
        }
    }

    private float enemyDeploymentPointsDestroyed(CombatEngineAPI engine) {
        CombatFleetManagerAPI enemyManager = engine.getFleetManager(FleetSide.ENEMY);
        if (enemyManager == null) return 0f;

        float total = 0f;
        for (FleetMemberAPI destroyed : enemyManager.getDestroyedCopy()) {
            total += destroyed.getDeploymentPointsCost();
        }
        return total;
    }
}
