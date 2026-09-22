package exiledsector.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import lunalib.lunaRefit.BaseRefitButton;
import lunalib.lunaRefit.LunaRefitManager;

import java.awt.Color;
import java.util.Collections;

public class SkillTreeRefitButton extends BaseRefitButton {

    private static final float SCREEN_FRACTION = 1f;
    static final float SHIP_CARD_ICON_SIZE = 128f;
    static final float SHIP_CARD_MARGIN = 16f;

    public static void addButton() {
        LunaRefitManager.addRefitButton(new SkillTreeRefitButton());
    }

    @Override
    public float getPanelWidth(FleetMemberAPI member, ShipVariantAPI variant) {
        return Global.getSettings().getScreenWidth() * SCREEN_FRACTION;
    }

    @Override
    public float getPanelHeight(FleetMemberAPI member, ShipVariantAPI variant) {
        return Global.getSettings().getScreenHeight() * SCREEN_FRACTION;
    }

    @Override
    public String getButtonName(FleetMemberAPI member, ShipVariantAPI variant) {
        return "Skill Tree";
    }

    @Override
    public String getIconName(FleetMemberAPI member, ShipVariantAPI variant) {
        return "graphics/icons/codex/skills.png";
    }

    @Override
    public boolean hasPanel(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
        return true;
    }

    @Override
    public void initPanel(CustomPanelAPI backgroundPanel, FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
        float panelWidth = getPanelWidth(member, variant);
        float panelHeight = getPanelHeight(member, variant);

        TooltipMakerAPI shipCard = backgroundPanel.createUIElement(SHIP_CARD_ICON_SIZE, SHIP_CARD_ICON_SIZE, false);
        shipCard.addShipList(1, 1, SHIP_CARD_ICON_SIZE, new Color(0, 0, 0, 0), Collections.singletonList(member), 0f);
        float shipCardHeight = shipCard.getPrev().getPosition().getHeight();

        TooltipMakerAPI element = backgroundPanel.createUIElement(panelWidth, panelHeight, false);
        backgroundPanel.addUIElement(element);
        element.getPosition().inTL(0f, 0f);

        CustomPanelAPI canvas = Global.getSettings().createCustom(panelWidth, panelHeight, new SkillTreeCanvasPlugin(member, shipCardHeight, this));
        element.addCustom(canvas, 0f).getPosition().inTL(0f, 0f);

        backgroundPanel.addUIElement(shipCard);
        shipCard.getPosition().inTL(SHIP_CARD_MARGIN, SHIP_CARD_MARGIN + SkillTreePassivePointsBar.getFootprintHeight());
    }
}
