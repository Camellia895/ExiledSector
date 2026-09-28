package exiledsector.effects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import exiledsector.skills.HullModNames;

import java.awt.Color;

public class SkillConflictWarningHullMod extends BaseHullMod {

    private static final String TITLE = "Conflict detected";
    private static final String TEXT_BEFORE_REMOVED = "The ";
    private static final String TEXT_BETWEEN = " hullmod has been removed due to the presence of the ";
    private static final String TEXT_AFTER_CAUSE = " skill.";

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        SkillConflictWarnings.Removal removal = SkillConflictWarnings.get(ship.getVariant());
        if (removal == null) return;

        String removedName = HullModNames.displayName(removal.removedHullModId);

        Color highlight = Global.getSettings().getColor("hColor");
        tooltip.addSectionHeading(TITLE, Alignment.MID, 15);
        tooltip.addPara(TEXT_BEFORE_REMOVED + removedName + TEXT_BETWEEN + removal.causeSkillDisplayName + TEXT_AFTER_CAUSE,
                10, highlight, removedName, removal.causeSkillDisplayName);
    }
}
