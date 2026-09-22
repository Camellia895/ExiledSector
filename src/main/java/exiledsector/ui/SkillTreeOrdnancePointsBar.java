package exiledsector.ui;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import exiledsector.skills.ShipOpBudget;
import org.apache.log4j.Logger;

final class SkillTreeOrdnancePointsBar {

    private final FleetMemberAPI member;
    private final ShipVariantAPI variant;
    private final SkillTreeReadoutBar bar = new SkillTreeReadoutBar(SkillTreeOrdnancePointsBar.class);

    SkillTreeOrdnancePointsBar(FleetMemberAPI member, ShipVariantAPI variant) {
        this.member = member;
        this.variant = variant;
    }

    void render(PositionAPI position, float alphaMult) {
        int totalPoints;
        int spentPoints;
        try {
            ShipOpBudget budget = ShipOpBudget.of(member, variant);
            totalPoints = budget.total;
            spentPoints = budget.used;
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
