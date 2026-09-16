package exiledsector.effects;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTree;

public class SkillTreeHullMod extends BaseHullMod {

    public static final String ID = "exiledSector_core";

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        FleetMemberAPI member = stats.getFleetMember();
        if (member == null) return;

        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        for (String nodeId : data.getUnlockedNodeIds()) {
            SkillNode node = SkillTree.get(nodeId);
            if (node == null) continue;
            // TODO: apply this node's stat bonuses to `stats` once nodes are defined.
        }
    }
}
