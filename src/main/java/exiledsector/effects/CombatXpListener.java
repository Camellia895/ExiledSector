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
import exiledsector.skills.ShipLevelConfig;
import exiledsector.skills.ShipLevelSystem;

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
        float destroyedDp = enemyDeploymentPointsDestroyed(result);
        boolean lost = !result.didPlayerWin();
        float xp = destroyedDp * ShipLevelConfig.xpPerDeploymentPoint();
        if (lost) {
            xp *= ShipLevelConfig.xpLossMultiplier();
        }

        Map<FleetMemberAPI, Integer> levelsBefore = levelsOf(playerFleet);
        ShipLevelSystem.awardXpToFleet(playerFleet, xp);
        report(new CombatXpReport(xp, destroyedDp, lost, levelUps(levelsBefore)));
    }

    private static float enemyDeploymentPointsDestroyed(EngagementResultAPI result) {
        EngagementResultForFleetAPI enemy = result.didPlayerWin() ? result.getLoserResult() : result.getWinnerResult();
        if (enemy == null) {
            return 0f;
        }
        float total = 0f;
        for (FleetMemberAPI destroyed : enemy.getDestroyed()) {
            total += destroyed.getDeploymentPointsCost();
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
            text.addPara("Every ship in your fleet earned %s XP from %s enemy deployment points destroyed, "
                    + "reduced because the battle was lost.", Misc.getHighlightColor(), xp, dp);
        } else {
            text.addPara("Every ship in your fleet earned %s XP from %s enemy deployment points destroyed.",
                    Misc.getHighlightColor(), xp, dp);
        }
        for (String levelUp : report.levelUps()) {
            text.addPara(levelUp, Misc.getPositiveHighlightColor());
        }
    }

    record CombatXpReport(float xp, float destroyedDp, boolean lost, List<String> levelUps) {

        String xpText() {
            return String.valueOf(Math.round(xp));
        }

        String dpText() {
            return String.valueOf(Math.round(destroyedDp));
        }
    }
}
