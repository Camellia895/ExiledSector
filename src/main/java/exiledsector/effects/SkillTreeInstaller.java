package exiledsector.effects;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.skills.skilleffect.LogisticsSkillEffect;

public class SkillTreeInstaller implements EveryFrameScript {

    private static final float CHECK_INTERVAL_SECONDS = 1f;

    private float timeSinceLastCheck = CHECK_INTERVAL_SECONDS;

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
        timeSinceLastCheck += amount;
        if (timeSinceLastCheck < CHECK_INTERVAL_SECONDS) return;
        timeSinceLastCheck = 0f;

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) return;

        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (!member.getVariant().hasHullMod(SkillTreeHullMod.ID)) {
                member.getVariant().addPermaMod(SkillTreeHullMod.ID);
            }
            new SkillTreeHullMod().applyEffectsBeforeShipCreation(member.getHullSpec().getHullSize(), member.getStats(), SkillTreeHullMod.ID);
        }

        LogisticsSkillEffect.recomputeExtendedPhaseField();
    }
}
