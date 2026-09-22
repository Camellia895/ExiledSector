package exiledsector.ui;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import org.apache.log4j.Logger;

final class SkillTreeOrdnancePointsBar {

    private final FleetMemberAPI member;
    private final SkillTreeReadoutBar bar = new SkillTreeReadoutBar(SkillTreeOrdnancePointsBar.class);

    SkillTreeOrdnancePointsBar(FleetMemberAPI member) {
        this.member = member;
    }

    void render(PositionAPI position, float alphaMult) {
        int totalPoints;
        int spentPoints;
        try {
            MutableCharacterStatsAPI captainStats = member.getCaptain() != null ? member.getCaptain().getStats() : null;
            totalPoints = member.getHullSpec().getOrdnancePoints(captainStats);
            spentPoints = member.getVariant().computeOPCost(captainStats);
        } catch (RuntimeException e) {
            Logger.getLogger(SkillTreeOrdnancePointsBar.class).error("Failed to compute ordnance point stats", e);
            return;
        }

        float barLeft = position.getX() + SkillTreeRefitButton.SHIP_CARD_MARGIN;
        float barTop = position.getY() + position.getHeight() - SkillTreeRefitButton.SHIP_CARD_MARGIN;
        float barBottom = barTop - SkillTreeReadoutBar.BAR_HEIGHT;

        bar.render(barLeft, barBottom, spentPoints, totalPoints, alphaMult);
    }
}
