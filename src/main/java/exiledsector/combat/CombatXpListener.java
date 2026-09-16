package exiledsector.combat;

import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.EngagementResultForFleetAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.persistence.ShipSkillDataManager;

public class CombatXpListener extends BaseCampaignEventListener {

    private static final float PLACEHOLDER_XP_PER_BATTLE = 25f;

    public CombatXpListener() {
        super(true);
    }

    @Override
    public void reportPlayerEngagement(EngagementResultAPI result) {
        EngagementResultForFleetAPI playerResult = result.getWinnerResult().isPlayer()
                ? result.getWinnerResult()
                : result.getLoserResult();

        for (FleetMemberAPI member : playerResult.getDeployed()) {
            // TODO: replace with a real per-ship performance-based XP formula.
            ShipSkillDataManager.get(member.getId()).addXp(PLACEHOLDER_XP_PER_BATTLE);
        }
    }
}
