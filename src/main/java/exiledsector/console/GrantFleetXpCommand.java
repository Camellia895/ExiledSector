package exiledsector.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.ShipLevelConfig;
import exiledsector.skills.ShipLevelSystem;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillNodeOpCost;
import exiledsector.skills.SkillTree;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

import java.util.Collection;

// Optional Console Commands integration (see data/console/commands.csv). Never referenced from any
// other Exiled Sector class, so this only ever loads if Console Commands' own plugin instantiates
// it - the mod works fine with Console Commands absent.
public class GrantFleetXpCommand implements BaseCommand {

    private static final float DEFAULT_XP = 1000f;

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        float xp;
        if (args.isEmpty()) {
            xp = DEFAULT_XP;
        } else {
            try {
                xp = Float.parseFloat(args.trim());
            } catch (NumberFormatException e) {
                return CommandResult.BAD_SYNTAX;
            }
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            Console.showMessage("No player fleet found.");
            return CommandResult.ERROR;
        }

        Collection<SkillNode> allNodes = SkillTree.getAllNodes().values();
        int shipCount = 0;
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            ShipSkillData data = ShipSkillDataManager.get(member.getId());
            int opCostPerNode = SkillNodeOpCost.perNode(member.getHullSpec());
            ShipLevelSystem.awardXp(data, xp, ShipLevelConfig.xpBase(), ShipLevelConfig.xpGrowth(),
                    ShipLevelConfig.xpGrowthCutoffLevel(), ShipLevelConfig.maxLevel(), allNodes, opCostPerNode);
            shipCount++;
        }

        Console.showMessage("Granted " + (int) xp + " XP to " + shipCount
                + (shipCount == 1 ? " ship" : " ships") + " in the fleet.");
        return CommandResult.SUCCESS;
    }
}
