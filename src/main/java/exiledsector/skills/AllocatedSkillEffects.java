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
        return forData(ShipSkillDataManager.get(member.getId()), member.getHullSpec().getHullSize());
    }

    public static List<SkillEffect> forData(ShipSkillData data, HullSize hullSize) {
        List<SkillEffect> effects = new ArrayList<>();
        for (AllocatedNode allocated : AllocatedNode.of(data)) {
            SkillType type = allocated.effectiveType();
            if (type.getVanillaHullModId() == null) {
                for (SkillTypeEffect effect : type.effectsFor(hullSize)) {
                    effects.add(effect.effect());
                }
            }
        }
        return effects;
    }
}
