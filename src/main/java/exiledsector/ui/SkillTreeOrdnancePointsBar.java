package exiledsector.ui;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import exiledsector.effects.SkillTreeHullMod;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.PassivePointExchangeRates;
import lunalib.lunaRefit.BaseRefitButton;
import org.apache.log4j.Logger;

final class SkillTreeOrdnancePointsBar {

    private static final float GAP = 8f;
    private static final String SELL_ICON_PATH = "graphics/ui/icons/fleettab/ship_take.png";

    private final FleetMemberAPI member;
    private final BaseRefitButton refitButton;
    private final SkillTreeReadoutBar bar = new SkillTreeReadoutBar(SkillTreeOrdnancePointsBar.class);
    private final SkillTreePointButton sellButton = new SkillTreePointButton(SkillTreeOrdnancePointsBar.class, SELL_ICON_PATH);

    SkillTreeOrdnancePointsBar(FleetMemberAPI member, BaseRefitButton refitButton) {
        this.member = member;
        this.refitButton = refitButton;
    }

    void advance(float amount) {
        sellButton.advance(amount);
    }

    void render(PositionAPI position, float mouseX, float mouseY, boolean mouseKnown, float alphaMult) {
        int totalPoints;
        int spentPoints;
        try {
            MutableCharacterStatsAPI captainStats = member.getCaptain() != null ? member.getCaptain().getStats() : null;
            ShipSkillData data = ShipSkillDataManager.get(member.getId());
            totalPoints = member.getHullSpec().getOrdnancePoints(captainStats);
            spentPoints = member.getVariant().computeOPCost(captainStats) + data.getOpSpentOnPassivePoints();
        } catch (RuntimeException e) {
            Logger.getLogger(SkillTreeOrdnancePointsBar.class).error("Failed to compute ordnance point stats", e);
            return;
        }

        float barLeft = position.getX() + SkillTreeRefitButton.SHIP_CARD_MARGIN;
        float barTop = position.getY() + position.getHeight() - SkillTreeRefitButton.SHIP_CARD_MARGIN
                - SkillTreeReadoutBar.BAR_HEIGHT - GAP;
        float barBottom = barTop - SkillTreeReadoutBar.BAR_HEIGHT;

        bar.render(barLeft, barBottom, spentPoints, totalPoints, alphaMult);

        float buttonX = barLeft + SkillTreeReadoutBar.BAR_WIDTH + SkillTreePointButton.GAP;
        float buttonY = buttonY(barBottom);
        boolean hovered = mouseKnown && sellButton.contains(buttonX, buttonY, mouseX, mouseY);
        sellButton.render(buttonX, buttonY, hovered, alphaMult);
    }

    boolean handleClick(PositionAPI position, float mouseX, float mouseY) {
        float barTop = position.getY() + position.getHeight() - SkillTreeRefitButton.SHIP_CARD_MARGIN
                - SkillTreeReadoutBar.BAR_HEIGHT - GAP;
        float barBottom = barTop - SkillTreeReadoutBar.BAR_HEIGHT;
        float barLeft = position.getX() + SkillTreeRefitButton.SHIP_CARD_MARGIN;
        float buttonX = barLeft + SkillTreeReadoutBar.BAR_WIDTH + SkillTreePointButton.GAP;
        float buttonY = buttonY(barBottom);

        if (!sellButton.contains(buttonX, buttonY, mouseX, mouseY)) return false;

        try {
            ShipSkillData data = ShipSkillDataManager.get(member.getId());
            int opCostPerPoint = PassivePointExchangeRates.opCostPerPassivePoint(member.getHullSpec());

            if (data.sellPassivePoint(opCostPerPoint)) {
                sellButton.startGlow();
                refreshStats();
            }
        } catch (RuntimeException e) {
            Logger.getLogger(SkillTreeOrdnancePointsBar.class).error("Failed to sell a passive point", e);
        }
        return true;
    }

    private void refreshStats() {
        new SkillTreeHullMod().applyEffectsBeforeShipCreation(member.getHullSpec().getHullSize(), member.getStats(), SkillTreeHullMod.ID);
        member.setStatUpdateNeeded(true);
        member.updateStats();
        if (refitButton != null) {
            refitButton.refreshVariant();
        }
    }

    private static float buttonY(float barBottom) {
        return barBottom + (SkillTreeReadoutBar.BAR_HEIGHT - SkillTreePointButton.SIZE) / 2f;
    }
}
