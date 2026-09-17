package exiledsector.persistence;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import exiledsector.skills.ShipSkillData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShipSkillDataManagerTest {

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

    @Test
    void createsFreshDataForAnUnknownShip() {
        ShipSkillData data = ShipSkillDataManager.get("ship-a");

        assertNotNull(data);
        assertNotNull(data.getAllocatedNodeIds());
    }

    @Test
    void returnsTheSameInstanceOnRepeatedLookupsForTheSameShip() {
        ShipSkillData first = ShipSkillDataManager.get("ship-a");
        ShipSkillData second = ShipSkillDataManager.get("ship-a");

        assertSame(first, second);
    }

    @Test
    void keepsDataSeparateForDifferentShips() {
        ShipSkillData shipA = ShipSkillDataManager.get("ship-a");
        ShipSkillData shipB = ShipSkillDataManager.get("ship-b");

        shipA.addXp(500f);

        assertNotNull(shipB);
        assertNotEquals(shipA.getXp(), shipB.getXp());
    }

    @Test
    void survivesAcrossLookupsViaThePersistentDataMap() {
        ShipSkillData data = ShipSkillDataManager.get("ship-a");
        data.addXp(1000f);

        ShipSkillData reread = ShipSkillDataManager.get("ship-a");

        assertEquals(1000f, reread.getXp());
    }
}
