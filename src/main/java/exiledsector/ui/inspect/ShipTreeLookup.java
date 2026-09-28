package exiledsector.ui.inspect;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillDataResolver;
import exiledsector.skills.npc.NpcLayout;
import exiledsector.skills.npc.NpcLayouts;
import exiledsector.skills.npc.NpcTreeTag;

public final class ShipTreeLookup {

    public record ShipTree(ShipSkillData data, String layoutName) {
    }

    private ShipTreeLookup() {
    }

    public static ShipTree find(FleetMemberAPI member) {
        if (member == null) {
            return null;
        }
        String tag = NpcTreeTag.find(member.getVariant());
        if (tag != null) {
            return new ShipTree(SkillDataResolver.resolve(member, member.getVariant()), layoutName(NpcTreeTag.layoutId(tag)));
        }
        if (!isInPlayerFleet(member)) {
            return null;
        }
        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        return data.isBlank() ? null : new ShipTree(data, null);
    }

    public static boolean isLevelledNpc(FleetMemberAPI member) {
        return member != null && NpcTreeTag.find(member.getVariant()) != null;
    }

    private static String layoutName(String layoutId) {
        NpcLayout layout = layoutId == null ? null : NpcLayouts.find(layoutId);
        return layout != null ? layout.name() : layoutId;
    }

    private static boolean isInPlayerFleet(FleetMemberAPI member) {
        FleetDataAPI fleetData = member.getFleetData();
        CampaignFleetAPI fleet = fleetData == null ? null : fleetData.getFleet();
        return fleet != null && fleet.isPlayerFleet();
    }
}
