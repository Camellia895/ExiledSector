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

final class SkillTreePassivePointsBar {

    private static final String BUY_ICON_PATH = "graphics/ui/icons/fleettab/ship_store.png";

    private final FleetMemberAPI member;
    private final BaseRefitButton refitButton;
    private final SkillTreeReadoutBar bar = new SkillTreeReadoutBar(SkillTreePassivePointsBar.class);
    private final SkillTreePointButton buyButton = new SkillTreePointButton(SkillTreePassivePointsBar.class, BUY_ICON_PATH);

    SkillTreePassivePointsBar(FleetMemberAPI member, BaseRefitButton refitButton) {
        this.member = member;
        this.refitButton = refitButton;
    }

    void advance(float amount) {
        buyButton.advance(amount);
    }

    void render(PositionAPI position, float mouseX, float mouseY, boolean mouseKnown, float alphaMult) {
        ShipSkillData data = ShipSkillDataManager.get(member.getId());

        float barLeft = position.getX() + SkillTreeRefitButton.SHIP_CARD_MARGIN;
        float barTop = position.getY() + position.getHeight() - SkillTreeRefitButton.SHIP_CARD_MARGIN;
        float barBottom = barTop - SkillTreeReadoutBar.BAR_HEIGHT;

        bar.render(barLeft, barBottom, data.getSpentPassivePoints(), data.getPurchasedPassivePoints(), alphaMult);

        float buttonX = barLeft + SkillTreeReadoutBar.BAR_WIDTH + SkillTreePointButton.GAP;
        float buttonY = buttonY(barBottom);
        boolean hovered = mouseKnown && buyButton.contains(buttonX, buttonY, mouseX, mouseY);
        buyButton.render(buttonX, buttonY, hovered, alphaMult);
    }

    boolean handleClick(PositionAPI position, float mouseX, float mouseY) {
        float barLeft = position.getX() + SkillTreeRefitButton.SHIP_CARD_MARGIN;
        float barTop = position.getY() + position.getHeight() - SkillTreeRefitButton.SHIP_CARD_MARGIN;
        float barBottom = barTop - SkillTreeReadoutBar.BAR_HEIGHT;
        float buttonX = barLeft + SkillTreeReadoutBar.BAR_WIDTH + SkillTreePointButton.GAP;
        float buttonY = buttonY(barBottom);

        if (!buyButton.contains(buttonX, buttonY, mouseX, mouseY)) return false;

        try {
            MutableCharacterStatsAPI captainStats = member.getCaptain() != null ? member.getCaptain().getStats() : null;
            int totalOp = member.getHullSpec().getOrdnancePoints(captainStats);
            int usedOp = member.getVariant().computeOPCost(captainStats);
            ShipSkillData data = ShipSkillDataManager.get(member.getId());
            int availableOp = totalOp - usedOp - data.getOpSpentOnPassivePoints();
            int opCostPerPoint = PassivePointExchangeRates.opCostPerPassivePoint(member.getHullSpec());

            if (data.buyPassivePoint(opCostPerPoint, availableOp)) {
                buyButton.startGlow();
                refreshStats();
            }
        } catch (RuntimeException e) {
            Logger.getLogger(SkillTreePassivePointsBar.class).error("Failed to buy a passive point", e);
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
