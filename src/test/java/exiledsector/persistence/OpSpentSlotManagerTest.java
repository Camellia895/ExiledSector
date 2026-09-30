package exiledsector.persistence;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpSpentSlotManagerTest {

    private MockedStatic<Global> globalMock;
    private Map<String, Object> persistentData;

    @BeforeEach
    void setUp() {
        persistentData = new HashMap<>();
        SectorAPI sector = mock(SectorAPI.class);
        when(sector.getPersistentData()).thenReturn(persistentData);
        globalMock = Mockito.mockStatic(Global.class);
        globalMock.when(Global::getSector).thenReturn(sector);
    }

    @AfterEach
    void tearDown() {
        globalMock.close();
    }

    // persistentData is a raw Object map; the slots key is only ever written as Map<String, Integer>
    @SuppressWarnings("unchecked")
    private Map<String, Integer> slots() {
        return (Map<String, Integer>) persistentData.computeIfAbsent("exiledSector_opSpentSlots", key -> new HashMap<String, Integer>());
    }

    @Test
    void eachShipKeepsTheLowestSlotThatWasFreeWhenItFirstNeededOne() {
        assertEquals(0, OpSpentSlotManager.slotFor("ship-a"));
        assertEquals(1, OpSpentSlotManager.slotFor("ship-b"));
        assertEquals(0, OpSpentSlotManager.slotFor("ship-a"));
    }

    @Test
    void existingSlotLooksUpAShipsSlotWithoutAssigningOne() {
        OpSpentSlotManager.slotFor("ship-a");

        assertEquals(0, OpSpentSlotManager.existingSlot("ship-a"));
        assertNull(OpSpentSlotManager.existingSlot("temporary-copy"));
        assertFalse(slots().containsKey("temporary-copy"));
    }

    @Test
    void aShipStuckOnASlotWithNoBackingHullModIsMovedToARealOne() {
        slots().put("ship-a", 0);
        slots().put("stranded", OpSpentSlotManager.SLOT_COUNT + 40);

        assertEquals(1, OpSpentSlotManager.slotFor("stranded"));
        assertEquals(1, slots().get("stranded"));
    }

    @Test
    void whenEverySlotIsHeldANewShipGetsNoSlotAndNothingIsStored() {
        for (int slot = 0; slot < OpSpentSlotManager.SLOT_COUNT; slot++) {
            slots().put("ship-" + slot, slot);
        }

        assertEquals(OpSpentSlotManager.SLOT_COUNT, OpSpentSlotManager.slotFor("one-too-many"));
        assertFalse(slots().containsKey("one-too-many"));
    }

    @Test
    void releaseUnlessDropsShipsThatNoLongerNeedASlotAndTheLegacyCounter() {
        slots().put("ship-a", 0);
        slots().put("temporary-copy", 1);
        slots().put("ship-b", 2);
        persistentData.put("exiledSector_opSpentNextSlot", 620);

        OpSpentSlotManager.releaseUnless(Set.of("ship-a", "ship-b")::contains);

        assertEquals(Map.of("ship-a", 0, "ship-b", 2), slots());
        assertFalse(persistentData.containsKey("exiledSector_opSpentNextSlot"));
        assertEquals(1, OpSpentSlotManager.slotFor("ship-c"));
    }
}
