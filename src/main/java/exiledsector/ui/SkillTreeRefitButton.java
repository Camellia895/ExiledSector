package exiledsector.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTree;
import lunalib.lunaRefit.BaseRefitButton;
import lunalib.lunaRefit.LunaRefitManager;

public class SkillTreeRefitButton extends BaseRefitButton {

    private static final float SCREEN_FRACTION = 0.8f;
    private static final float SYMBOL_SIZE = 128f;
    private static final float NODE_SIZE = 64f;

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
        float centerX = getPanelWidth(member, variant) / 2f;
        float centerY = getPanelHeight(member, variant) / 2f;

        VignettedIcon.addTo(backgroundPanel, symbolPath, SYMBOL_SIZE, centerX - SYMBOL_SIZE / 2f, centerY - SYMBOL_SIZE / 2f);

        for (SkillNode node : SkillTree.getAllNodes().values()) {
            float nodeX = centerX + node.getOffsetX() - NODE_SIZE / 2f;
            float nodeY = centerY + node.getOffsetY() - NODE_SIZE / 2f;
            VignettedIcon.addTo(backgroundPanel, node.getIconPath(), NODE_SIZE, nodeX, nodeY);
        }
    }
}
