package exiledsector.ui.inspect;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillDataResolver;
import exiledsector.skills.enemy.EnemyLayout;
import exiledsector.skills.enemy.EnemyLayouts;
import exiledsector.skills.enemy.EnemyTreeTag;

public final class ShipTreeLookup {

    public record ShipTree(ShipSkillData data, String layoutName) {
    }

    private ShipTreeLookup() {
    }

    public static ShipTree find(FleetMemberAPI member) {
        if (member == null) {
            return null;
        }
        String tag = EnemyTreeTag.find(member.getVariant());
        if (tag != null) {
            return new ShipTree(SkillDataResolver.resolve(member, member.getVariant()), layoutName(EnemyTreeTag.layoutId(tag)));
        }
        if (!isInPlayerFleet(member)) {
            return null;
        }
        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        return data.isBlank() ? null : new ShipTree(data, null);
    }

    public static boolean isLevelledEnemy(FleetMemberAPI member) {
        return member != null && EnemyTreeTag.find(member.getVariant()) != null;
    }

    private static String layoutName(String layoutId) {
        EnemyLayout layout = layoutId == null ? null : EnemyLayouts.find(layoutId);
        return layout != null ? layout.name() : layoutId;
    }

    private static boolean isInPlayerFleet(FleetMemberAPI member) {
        FleetDataAPI fleetData = member.getFleetData();
        CampaignFleetAPI fleet = fleetData == null ? null : fleetData.getFleet();
        return fleet != null && fleet.isPlayerFleet();
    }
}
