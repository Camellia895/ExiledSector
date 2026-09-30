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
                for (SkillTypeEffect effect : appliedEffects(data, type, hullSize)) {
                    effects.add(effect.effect());
                }
            }
        }
        return effects;
    }

    public static List<SkillTypeEffect> appliedEffects(ShipSkillData data, SkillType type, HullSize hullSize) {
        List<SkillTypeEffect> applied = new ArrayList<>();
        for (SkillTypeEffect effect : type.effectsFor(hullSize)) {
            if (!data.isNpcBuild() || effect.effect().appliesToNpcShips()) {
                applied.add(effect);
            }
        }
        return applied;
    }
}
