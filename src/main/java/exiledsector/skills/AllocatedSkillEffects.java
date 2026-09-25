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
            collectEffects(nodeId, data, hullSize, effects);
        }
        return effects;
    }

    private static void collectEffects(String nodeId, ShipSkillData data, HullSize hullSize, List<SkillEffect> effects) {
        SkillNode node = SkillTree.get(nodeId);
        if (node == null) return;

        SkillType type = node.resolveEffectiveType(data);
        if (type.getVanillaHullModId() != null) return;

        type.forEachEffect(hullSize, (effect, magnitude) -> effects.add(effect));
    }
}
