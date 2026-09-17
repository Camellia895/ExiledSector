package exiledsector.effects;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillEffect;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;

public class SkillTreeHullMod extends BaseHullMod {

    public static final String ID = "exiledSector_core";

    private static final String MOD_ID_PREFIX = "exiledSector_skill_";

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        FleetMemberAPI member = stats.getFleetMember();
        if (member == null) return;

        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        for (String nodeId : data.getUnlockedNodeIds()) {
            SkillNode node = SkillTree.get(nodeId);
            if (node == null) continue;

            SkillType type = node.getType();
            SkillEffect effect = type.getEffect();
            if (effect == null) continue;

            effect.apply(stats, MOD_ID_PREFIX + node.getId(), type.getMagnitude());
        }
    }
}
