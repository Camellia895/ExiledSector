package exiledsector.ui;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import exiledsector.effects.SkillTreeHullMod;
import exiledsector.skills.ShipOpBudget;
import org.apache.log4j.Logger;

final class SkillTreeOrdnancePointsBar {

    private final FleetMemberAPI member;
    private final ShipVariantAPI variant;
    private final SkillTreeReadoutBar bar = new SkillTreeReadoutBar(SkillTreeOrdnancePointsBar.class);

    private int totalPoints;
    private int spentPoints;

    SkillTreeOrdnancePointsBar(FleetMemberAPI member, ShipVariantAPI variant) {
        this.member = member;
        this.variant = variant;
    }

    void advance(float amount, PositionAPI position, float mouseX, float mouseY, boolean mouseKnown) {
        if (!refreshBudget()) return;

        boolean hovered = mouseKnown && isHovered(position, mouseX, mouseY);
        bar.advance(amount, spentPoints, totalPoints, hovered);
    }

    void render(PositionAPI position, float alphaMult) {
        if (!refreshBudget()) return;

        float barLeft = barLeft(position);
        float barBottom = barBottom(position);
        bar.render(barLeft, barBottom, spentPoints, totalPoints, alphaMult);
    }

    boolean isHovered(PositionAPI position, float x, float y) {
        return position != null && SkillTreeReadoutBar.containsPoint(barLeft(position), barBottom(position), x, y);
    }

    private float barLeft(PositionAPI position) {
        return position.getX() + SkillTreeRefitButton.SHIP_CARD_MARGIN;
    }

    private float barBottom(PositionAPI position) {
        float barTop = position.getY() + position.getHeight() - SkillTreeRefitButton.SHIP_CARD_MARGIN;
        return barTop - SkillTreeReadoutBar.BAR_HEIGHT;
    }

    private boolean refreshBudget() {
        try {
            SkillTreeHullMod.syncOpSpentHullMod(member, variant);
            ShipOpBudget budget = ShipOpBudget.of(member, variant);
            totalPoints = budget.total;
            spentPoints = budget.used;
            return true;
        } catch (RuntimeException e) {
            Logger.getLogger(SkillTreeOrdnancePointsBar.class).error("Failed to compute ordnance point stats", e);
            return false;
        }
    }
}
