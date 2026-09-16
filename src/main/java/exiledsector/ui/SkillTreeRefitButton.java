package exiledsector.ui;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import lunalib.lunaRefit.BaseRefitButton;
import lunalib.lunaRefit.LunaRefitManager;

/**
 * Adds a "Skill Tree" button to the ship refit screen via LunaLib's refit
 * button API (the only supported way to add a button there - the vanilla
 * refit screen has no plugin hook of its own). hasPanel()/initPanel() are
 * intentionally left at their defaults for now: LunaLib's own background
 * panel already renders as a plain black overlay with a border, which is
 * exactly the placeholder the skill tree UI starts as, and it already closes
 * on Escape back to the refit screen with no extra code needed here.
 */
public class SkillTreeRefitButton extends BaseRefitButton {

    public static void addButton() {
        LunaRefitManager.addRefitButton(new SkillTreeRefitButton());
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
}
