package exiledsector.ui;

import com.fs.starfarer.api.ui.PositionAPI;
import exiledsector.skills.progression.ShipOpBudget;

final class SkillTreeOrdnancePointsBar {

    private final SkillTreeReadoutBar bar = new SkillTreeReadoutBar(SkillTreeOrdnancePointsBar.class);

    private int totalPoints;
    private int spentPoints;

    void advance(float amount, ShipOpBudget budget, PositionAPI position, float mouseX, float mouseY, boolean mouseKnown) {
        totalPoints = budget.total;
        spentPoints = budget.used;

        boolean hovered = mouseKnown && isHovered(position, mouseX, mouseY);
        bar.advance(amount, spentPoints, totalPoints, hovered);
    }

    void render(PositionAPI position, float alphaMult) {
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
}
