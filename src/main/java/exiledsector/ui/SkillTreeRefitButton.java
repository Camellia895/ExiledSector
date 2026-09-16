package exiledsector.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import lunalib.lunaRefit.BaseRefitButton;
import lunalib.lunaRefit.LunaRefitManager;
import lunalib.lunaUI.elements.LunaSpriteElement;

public class SkillTreeRefitButton extends BaseRefitButton {

    private static final float SCREEN_FRACTION = 0.8f;
    private static final float SYMBOL_SIZE = 128f;

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
        String symbolPath = ShipSymbolResolver.resolveSymbolPath(member, variant);

        TooltipMakerAPI element = backgroundPanel.createUIElement(SYMBOL_SIZE, SYMBOL_SIZE, false);
        backgroundPanel.addUIElement(element);
        float x = (getPanelWidth(member, variant) - SYMBOL_SIZE) / 2f;
        float y = (getPanelHeight(member, variant) - SYMBOL_SIZE) / 2f;
        element.getPosition().inTL(x, y);

        new LunaSpriteElement(symbolPath, LunaSpriteElement.ScalingTypes.STRETCH_SPRITE, element, SYMBOL_SIZE, SYMBOL_SIZE);
    }
}
