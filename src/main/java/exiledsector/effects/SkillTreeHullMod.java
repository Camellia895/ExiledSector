package exiledsector.effects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.HullSizeSkillEffect;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import exiledsector.skills.SkillTypeEffect;

public class SkillTreeHullMod extends BaseHullMod {

    public static final String ID = "exiledSector_core";

    private static final String MOD_ID_PREFIX = "exiledSector_skill_";

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        FleetMemberAPI member = stats.getFleetMember();
        if (member == null) return;

        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        for (String nodeId : data.getAllocatedNodeIds()) {
            SkillNode node = SkillTree.get(nodeId);
            if (node == null) continue;

            SkillType type = node.resolveEffectiveType(data);

            String vanillaHullModId = type.getVanillaHullModId();
            if (vanillaHullModId != null) {
                HullModSpecAPI spec = Global.getSettings().getHullModSpec(vanillaHullModId);
                if (spec == null) continue;
                HullModEffect vanillaEffect = spec.getEffect();
                if (vanillaEffect != null) {
                    vanillaEffect.applyEffectsBeforeShipCreation(hullSize, stats, vanillaHullModId);
                }
                continue;
            }

            for (SkillTypeEffect effect : type.getEffects()) {
                effect.effect().apply(stats, MOD_ID_PREFIX + node.getId(), effect.magnitude());
            }
            for (HullSizeSkillEffect effect : type.getHullSizeEffects()) {
                effect.effect().apply(stats, MOD_ID_PREFIX + node.getId(), effect.valueFor(hullSize));
            }
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        FleetMemberAPI member = ship.getMutableStats().getFleetMember();
        if (member == null) return;

        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        for (String nodeId : data.getAllocatedNodeIds()) {
            SkillNode node = SkillTree.get(nodeId);
            if (node == null) continue;

            SkillType type = node.resolveEffectiveType(data);

            String vanillaHullModId = type.getVanillaHullModId();
            if (vanillaHullModId != null) {
                HullModSpecAPI spec = Global.getSettings().getHullModSpec(vanillaHullModId);
                if (spec == null) continue;
                HullModEffect vanillaEffect = spec.getEffect();
                if (vanillaEffect != null) {
                    vanillaEffect.applyEffectsAfterShipCreation(ship, vanillaHullModId);
                }
                continue;
            }

            for (SkillTypeEffect effect : type.getEffects()) {
                effect.effect().applyAfterShipCreation(ship, MOD_ID_PREFIX + node.getId(), effect.magnitude());
            }
            for (HullSizeSkillEffect effect : type.getHullSizeEffects()) {
                effect.effect().applyAfterShipCreation(ship, MOD_ID_PREFIX + node.getId(), effect.valueFor(ship.getHullSize()));
            }
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        FleetMemberAPI member = ship.getMutableStats().getFleetMember();
        if (member == null) return;

        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        HullSize hullSize = ship.getHullSize();
        for (String nodeId : data.getAllocatedNodeIds()) {
            SkillNode node = SkillTree.get(nodeId);
            if (node == null) continue;

            SkillType type = node.resolveEffectiveType(data);
            if (type.getVanillaHullModId() != null) continue;

            for (SkillTypeEffect effect : type.getEffects()) {
                if (effect.effect().isConditional()) {
                    effect.effect().advanceInCombat(ship, MOD_ID_PREFIX + node.getId(), effect.magnitude());
                }
            }
            for (HullSizeSkillEffect effect : type.getHullSizeEffects()) {
                if (effect.effect().isConditional()) {
                    effect.effect().advanceInCombat(ship, MOD_ID_PREFIX + node.getId(), effect.valueFor(hullSize));
                }
            }
        }
    }
}
