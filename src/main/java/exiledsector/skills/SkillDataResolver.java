package exiledsector.skills;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.npc.NpcSkillTreeBuilder;
import exiledsector.skills.npc.NpcTreeTag;

import java.util.HashMap;
import java.util.Map;

public final class SkillDataResolver {

    private static final Map<String, ShipSkillData> NPC_TREES = new HashMap<>();

    private SkillDataResolver() {
    }

    public static ShipSkillData resolve(FleetMemberAPI member, ShipVariantAPI variant) {
        String npcTag = NpcTreeTag.find(variant);
        if (npcTag != null) {
            return NPC_TREES.computeIfAbsent(npcTag, SkillDataResolver::decodeOrEmpty);
        }
        if (member == null) {
            return null;
        }
        ShipSkillData saved = ShipSkillDataManager.find(member.getId());
        return saved != null ? saved : new ShipSkillData();
    }

    public static boolean isNpcTree(ShipVariantAPI variant) {
        return NpcTreeTag.find(variant) != null;
    }

    public static void clearCache() {
        NPC_TREES.clear();
    }

    private static ShipSkillData decodeOrEmpty(String tag) {
        ShipSkillData data = NpcTreeTag.decode(tag);
        return data != null ? data : NpcSkillTreeBuilder.emptyTree();
    }
}
