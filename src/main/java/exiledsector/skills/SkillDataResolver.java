package exiledsector.skills;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.enemy.EnemyTreeTag;

import java.util.HashMap;
import java.util.Map;

public final class SkillDataResolver {

    private static final Map<String, ShipSkillData> ENEMY_TREES = new HashMap<>();

    private SkillDataResolver() {
    }

    public static ShipSkillData resolve(FleetMemberAPI member, ShipVariantAPI variant) {
        String enemyTag = EnemyTreeTag.find(variant);
        if (enemyTag != null) {
            return ENEMY_TREES.computeIfAbsent(enemyTag, SkillDataResolver::decodeOrEmpty);
        }
        return member == null ? null : ShipSkillDataManager.get(member.getId());
    }

    public static boolean isEnemyTree(ShipVariantAPI variant) {
        return EnemyTreeTag.find(variant) != null;
    }

    public static void clearCache() {
        ENEMY_TREES.clear();
    }

    private static ShipSkillData decodeOrEmpty(String tag) {
        ShipSkillData data = EnemyTreeTag.decode(tag);
        if (data == null) {
            data = new ShipSkillData();
            data.markEnemyBuild();
        }
        return data;
    }
}
