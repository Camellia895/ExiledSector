package exiledsector.effects;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

public class SkillTreeInstaller implements EveryFrameScript {

    private static final float CHECK_INTERVAL_SECONDS = 1f;

    private float timeSinceLastCheck = 0f;

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

        for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            if (!member.getVariant().hasHullMod(SkillTreeHullMod.ID)) {
                member.getVariant().addPermaMod(SkillTreeHullMod.ID);
            }
        }
    }
}
