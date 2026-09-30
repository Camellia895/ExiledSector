package exiledsector.persistence;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import exiledsector.skills.ShipSkillData;

public class ShipSkillDataManager {

    private static final String DATA_KEY = "exiledSector_shipSkillData";

    private ShipSkillDataManager() {
    }

    // persistentData is a raw Object map; this key is only ever written as Map<String, ShipSkillData>
    @SuppressWarnings("unchecked")
    private static Map<String, ShipSkillData> getStore() {
        Map<String, Object> persistentData = Global.getSector().getPersistentData();
        return (Map<String, ShipSkillData>) persistentData.computeIfAbsent(DATA_KEY, key -> new HashMap<String, ShipSkillData>());
    }

    public static ShipSkillData get(String shipId) {
        return getStore().computeIfAbsent(shipId, key -> new ShipSkillData());
    }

    public static ShipSkillData find(String shipId) {
        return getStore().get(shipId);
    }

    public static boolean hasProgress(String shipId) {
        ShipSkillData data = find(shipId);
        return data != null && !data.isBlank();
    }

    public static void removeBlankRecords() {
        getStore().values().removeIf(ShipSkillData::isBlank);
    }

    public static void put(String shipId, ShipSkillData data) {
        getStore().put(shipId, data);
    }
}
