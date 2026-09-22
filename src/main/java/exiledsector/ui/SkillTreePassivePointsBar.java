package exiledsector.ui;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.VanillaHullBaselines;
import org.apache.log4j.Logger;

final class SkillTreePassivePointsBar {

    private final FleetMemberAPI member;
    private final SkillTreeReadoutBar bar = new SkillTreeReadoutBar(SkillTreePassivePointsBar.class);

    SkillTreePassivePointsBar(FleetMemberAPI member) {
        this.member = member;
    }

    void render(PositionAPI position, float alphaMult) {
        int totalPoints;
        int spentPoints;
        try {
            totalPoints = VanillaHullBaselines.passivePointsFor(member.getHullSpec());
            spentPoints = ShipSkillDataManager.get(member.getId()).getSpentPassivePoints();
        } catch (RuntimeException e) {
            Logger.getLogger(SkillTreePassivePointsBar.class).error("Failed to compute passive point stats", e);
            return;
        }

        float barLeft = position.getX() + SkillTreeRefitButton.SHIP_CARD_MARGIN;
        float barTop = position.getY() + position.getHeight() - SkillTreeRefitButton.SHIP_CARD_MARGIN;
        float barBottom = barTop - SkillTreeReadoutBar.BAR_HEIGHT;

        bar.render(barLeft, barBottom, spentPoints, totalPoints, alphaMult);
    }
}
