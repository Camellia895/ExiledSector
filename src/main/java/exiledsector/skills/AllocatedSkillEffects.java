package exiledsector.skills;

import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.skilleffect.SkillEffect;

import java.util.ArrayList;
import java.util.List;

public final class AllocatedSkillEffects {

    private AllocatedSkillEffects() {
    }

    public static List<SkillEffect> forMember(FleetMemberAPI member) {
        List<SkillEffect> effects = new ArrayList<>();
        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        HullSize hullSize = member.getHullSpec().getHullSize();
        for (String nodeId : data.getAllocatedNodeIds()) {
            SkillNode node = SkillTree.get(nodeId);
            if (node == null) continue;

            SkillType type = node.resolveEffectiveType(data);
            if (type.getVanillaHullModId() != null) continue;

            type.forEachEffect(hullSize, (effect, magnitude) -> effects.add(effect));
        }
        return effects;
    }
}
