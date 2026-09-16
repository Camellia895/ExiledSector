package exiledsector.persistence;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import exiledsector.skills.ShipSkillData;


public class ShipSkillDataManager {

    private static final String DATA_KEY = "exiledSector_shipSkillData";

    private ShipSkillDataManager() {
    }

    @SuppressWarnings("unchecked")
    private static Map<String, ShipSkillData> getStore() {
        Map<String, Object> persistentData = Global.getSector().getPersistentData();
        Map<String, ShipSkillData> store = (Map<String, ShipSkillData>) persistentData.get(DATA_KEY);
        if (store == null) {
            store = new HashMap<>();
            persistentData.put(DATA_KEY, store);
        }
        return store;
    }

    public static ShipSkillData get(String shipId) {
        Map<String, ShipSkillData> store = getStore();
        ShipSkillData data = store.get(shipId);
        if (data == null) {
            data = new ShipSkillData();
            store.put(shipId, data);
        }
        return data;
    }
}
