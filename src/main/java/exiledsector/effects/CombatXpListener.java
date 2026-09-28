package exiledsector.effects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.EngagementResultForFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.progression.ShipLevelConfig;
import exiledsector.skills.progression.ShipLevelSystem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CombatXpListener extends BaseCampaignEventListener {

    public CombatXpListener() {
        super(false);
    }

    @Override
    public void reportPlayerEngagement(EngagementResultAPI result) {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (result == null || result.getLastCombatDamageData() == null || playerFleet == null) {
            return;
        }
        float defeatedDp = enemyDeploymentPointsDefeated(result);
        boolean lost = !result.didPlayerWin();
        float xp = defeatedDp * ShipLevelConfig.xpPerDeploymentPoint();
        if (lost) {
            xp *= ShipLevelConfig.xpLossMultiplier();
        }

        Map<FleetMemberAPI, Integer> levelsBefore = levelsOf(playerFleet);
        ShipLevelSystem.awardXpToFleet(playerFleet, xp);
        report(new CombatXpReport(xp, defeatedDp, lost, levelUps(levelsBefore)));
    }

    private static float enemyDeploymentPointsDefeated(EngagementResultAPI result) {
        EngagementResultForFleetAPI enemy = result.didPlayerWin() ? result.getLoserResult() : result.getWinnerResult();
        if (enemy == null) {
            return 0f;
        }
        return deploymentPointsOf(enemy.getDestroyed()) + deploymentPointsOf(enemy.getDisabled());
    }

    private static float deploymentPointsOf(List<FleetMemberAPI> members) {
        float total = 0f;
        for (FleetMemberAPI member : members) {
            total += member.getDeploymentPointsCost();
        }
        return total;
    }

    private static Map<FleetMemberAPI, Integer> levelsOf(CampaignFleetAPI fleet) {
        Map<FleetMemberAPI, Integer> levels = new LinkedHashMap<>();
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            levels.put(member, ShipSkillDataManager.get(member.getId()).getLevel());
        }
        return levels;
    }

    private static List<String> levelUps(Map<FleetMemberAPI, Integer> levelsBefore) {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<FleetMemberAPI, Integer> entry : levelsBefore.entrySet()) {
            FleetMemberAPI member = entry.getKey();
            int level = ShipSkillDataManager.get(member.getId()).getLevel();
            if (level > entry.getValue()) {
                lines.add(member.getShipName() + " (" + member.getHullSpec().getHullNameWithDashClass()
                        + ") reached level " + level + ".");
            }
        }
        return lines;
    }

    private static void report(CombatXpReport report) {
        InteractionDialogAPI dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
        if (dialog == null || dialog.getTextPanel() == null) {
            return;
        }
        TextPanelAPI text = dialog.getTextPanel();
        text.setFontSmallInsignia();
        text.addPara("ExiledSector skill tree", Misc.getBasePlayerColor());
        text.setFontInsignia();
        String xp = report.xpText();
        String dp = report.dpText();
        if (report.lost()) {
            text.addPara("Every ship in your fleet earned %s XP from %s enemy deployment points destroyed or disabled, "
                    + "reduced because the battle was lost.", Misc.getHighlightColor(), xp, dp);
        } else {
            text.addPara("Every ship in your fleet earned %s XP from %s enemy deployment points destroyed or disabled.",
                    Misc.getHighlightColor(), xp, dp);
        }
        for (String levelUp : report.levelUps()) {
            text.addPara(levelUp, Misc.getPositiveHighlightColor());
        }
    }

    record CombatXpReport(float xp, float defeatedDp, boolean lost, List<String> levelUps) {

        String xpText() {
            return String.valueOf(Math.round(xp));
        }

        String dpText() {
            return String.valueOf(Math.round(defeatedDp));
        }
    }
}
