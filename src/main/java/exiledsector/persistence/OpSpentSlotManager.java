package exiledsector.persistence;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public final class OpSpentSlotManager {

    public static final int SLOT_COUNT = 1000;

    private static final String SLOTS_KEY = "exiledSector_opSpentSlots";
    private static final String NEXT_SLOT_KEY = "exiledSector_opSpentNextSlot";

    private OpSpentSlotManager() {
    }

    public static int slotFor(String shipId) {
        Map<String, Object> persistentData = Global.getSector().getPersistentData();

        Map<String, Integer> slots = getSlots(persistentData);
        Integer slot = slots.get(shipId);
        if (slot != null) {
            return slot;
        }

        int assigned = nextFreeSlot(persistentData);
        slots.put(shipId, assigned);
        if (assigned >= SLOT_COUNT) {
            Logger.getLogger(OpSpentSlotManager.class).error("[ExiledSector] Ran out of OP-reservation hullmod "
                    + "slots (pool size " + SLOT_COUNT + "); ship " + shipId + " assigned slot " + assigned
                    + ", which has no backing hullmod spec and will not reserve ordnance points.");
        }
        return assigned;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Integer> getSlots(Map<String, Object> persistentData) {
        Map<String, Integer> slots = (Map<String, Integer>) persistentData.get(SLOTS_KEY);
        if (slots == null) {
            slots = new HashMap<>();
            persistentData.put(SLOTS_KEY, slots);
        }
        return slots;
    }

    private static int nextFreeSlot(Map<String, Object> persistentData) {
        Integer next = (Integer) persistentData.get(NEXT_SLOT_KEY);
        int assigned = next == null ? 0 : next;
        persistentData.put(NEXT_SLOT_KEY, assigned + 1);
        return assigned;
    }
}
