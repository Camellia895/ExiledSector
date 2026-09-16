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
    // The crest sprite has been observed rendering wider than SYMBOL_SIZE in
    // some cases (root cause not yet pinned down); the masked area is padded
    // well beyond the icon's own bounds so the vignette's solid black fill
    // reliably swallows that overflow instead of leaving it exposed.
    private static final float MASK_PADDING = 48f;
    private static final float MASK_SIZE = SYMBOL_SIZE + MASK_PADDING * 2f;

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

        TooltipMakerAPI element = backgroundPanel.createUIElement(MASK_SIZE, MASK_SIZE, false);
        backgroundPanel.addUIElement(element);
        float x = (getPanelWidth(member, variant) - MASK_SIZE) / 2f;
        float y = (getPanelHeight(member, variant) - MASK_SIZE) / 2f;
        element.getPosition().inTL(x, y);

        LunaSpriteElement sprite = new LunaSpriteElement(symbolPath, LunaSpriteElement.ScalingTypes.STRETCH_SPRITE, element, SYMBOL_SIZE, SYMBOL_SIZE);
        sprite.getPosition().inTL(MASK_PADDING, MASK_PADDING);

        // Added after the sprite so it renders on top and actually covers
        // the icon's edges, rather than being hidden behind it. Sized to
        // the full padded MASK_SIZE (not just SYMBOL_SIZE) so its solid
        // black fill extends past the icon's own bounds - see MASK_PADDING.
        CustomPanelAPI vignette = Global.getSettings().createCustom(MASK_SIZE, MASK_SIZE, new CircularVignettePlugin(SYMBOL_SIZE));
        element.addCustom(vignette, 0f).getPosition().inTL(0f, 0f);
    }
}
