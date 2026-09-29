package exiledsector.persistence;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShipSkillDataManagerTest {

    private MockedStatic<Global> globalMock;

    @BeforeEach
    void setUp() {
        Map<String, Object> persistentData = new HashMap<>();
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

        shipA.allocate(node("armor_1"), 1);

        assertNotNull(shipB);
        assertNotEquals(shipA.getSpentOp(1), shipB.getSpentOp(1));
    }

    @Test
    void survivesAcrossLookupsViaThePersistentDataMap() {
        ShipSkillData data = ShipSkillDataManager.get("ship-a");
        data.allocate(node("armor_1"), 1);

        ShipSkillData reread = ShipSkillDataManager.get("ship-a");

        assertEquals(1, reread.getSpentOp(1));
    }

    private static SkillNode node(String id) {
        SkillType type = new SkillType.Builder(id, id, "graphics/hullmods/heavy_armor.png", SkillTier.SMALL)
                .effects(List.of())
                .build();
        return new SkillNode(id, type, List.of(), 0f, 0f);
    }

    @Test
    void putReplacesTheStoredTreeForAShip() {
        ShipSkillData replacement = new ShipSkillData();
        ShipSkillDataManager.get("ship-a");

        ShipSkillDataManager.put("ship-a", replacement);

        assertSame(replacement, ShipSkillDataManager.get("ship-a"));
    }
}
