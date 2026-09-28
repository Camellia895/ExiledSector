package exiledsector.skills.enemy;

import com.fs.starfarer.api.campaign.rules.MemoryAPI;

import java.util.HashMap;
import java.util.Map;

public final class EnemyTreeRecords {

    public static final String MEMORY_KEY = "$exiledSector_enemyTrees";
    public static final String NOT_LEVELLED = "-";

    private EnemyTreeRecords() {
    }

    // fleet memory is a raw Object store; this key is only ever written as a HashMap<String, String>
    @SuppressWarnings("unchecked")
    public static Map<String, String> of(MemoryAPI memory) {
        Object stored = memory.get(MEMORY_KEY);
        if (stored instanceof Map<?, ?>) {
            return (Map<String, String>) stored;
        }
        Map<String, String> records = new HashMap<>();
        memory.set(MEMORY_KEY, records);
        return records;
    }

    public static boolean isLevelled(String record) {
        return record != null && record.startsWith(EnemyTreeTag.PREFIX);
    }
}
