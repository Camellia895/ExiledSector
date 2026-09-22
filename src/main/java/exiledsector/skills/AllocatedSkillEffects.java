package exiledsector.skills;

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
        for (String nodeId : data.getAllocatedNodeIds()) {
            SkillNode node = SkillTree.get(nodeId);
            if (node == null) continue;

            SkillType type = node.resolveEffectiveType(data);
            if (type.getVanillaHullModId() != null) continue;

            for (SkillTypeEffect effect : type.getEffects()) {
                effects.add(effect.effect());
            }
        }
        return effects;
    }
}
