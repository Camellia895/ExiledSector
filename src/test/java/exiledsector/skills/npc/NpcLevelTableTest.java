package exiledsector.skills.npc;

import exiledsector.skills.npc.NpcLevelTable.NodeRange;
import lunalib.lunaSettings.LunaSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NpcLevelTableTest {

    private static final int[][] DEFAULTS = {
            {1, 2}, {2, 3}, {4, 6}, {7, 10}, {11, 13}, {11, 16}, {12, 19}, {12, 22},
            {13, 25}, {13, 28}, {14, 29}, {14, 32}, {15, 35}, {15, 38}, {16, 42}};

    private MockedStatic<LunaSettings> lunaSettingsMock;

    @BeforeEach
    void setUp() {
        lunaSettingsMock = Mockito.mockStatic(LunaSettings.class, invocation -> null);
    }

    @AfterEach
    void tearDown() {
        lunaSettingsMock.close();
    }

    private void configure(int level, Integer min, Integer max) {
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", NpcLevelTable.minNodesFieldId(level))).thenReturn(min);
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", NpcLevelTable.maxNodesFieldId(level))).thenReturn(max);
    }

    @Test
    void everyLevelDefaultsToTheDesignTable() {
        for (int level = 1; level <= 15; level++) {
            assertEquals(new NodeRange(DEFAULTS[level - 1][0], DEFAULTS[level - 1][1]), NpcLevelTable.range(level),
                    "player level " + level);
            assertEquals(DEFAULTS[level - 1][0], NpcLevelTable.defaultMinNodes(level));
            assertEquals(DEFAULTS[level - 1][1], NpcLevelTable.defaultMaxNodes(level));
        }
    }

    @Test
    void configuredValuesOverrideTheDefaults() {
        configure(7, 20, 30);

        assertEquals(new NodeRange(20, 30), NpcLevelTable.range(7));
        assertEquals(new NodeRange(11, 16), NpcLevelTable.range(6));
    }

    @Test
    void aMinimumAboveItsMaximumIsSwapped() {
        configure(3, 9, 4);

        assertEquals(new NodeRange(4, 9), NpcLevelTable.range(3));
    }

    @Test
    void onlyOneSideConfiguredKeepsTheOtherDefault() {
        configure(10, null, 40);

        assertEquals(new NodeRange(13, 40), NpcLevelTable.range(10));
    }

    @Test
    void playerLevelsOutsideTheTableUseTheNearestRow() {
        assertEquals(NpcLevelTable.range(15), NpcLevelTable.range(16));
        assertEquals(NpcLevelTable.range(15), NpcLevelTable.range(40));
        assertEquals(NpcLevelTable.range(1), NpcLevelTable.range(0));
        assertEquals(NpcLevelTable.range(1), NpcLevelTable.range(-3));
    }

    @Test
    void valuesOutsideTheAllowedNodeRangeAreClamped() {
        configure(5, -4, 999);

        assertEquals(new NodeRange(NpcLevelTable.MIN_NODES, NpcLevelTable.MAX_NODES), NpcLevelTable.range(5));
    }

    @Test
    void rollsStayInsideTheRangeAndReachBothEnds() {
        Random random = new Random(42);
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            int nodes = NpcLevelTable.roll(4, random);
            assertTrue(nodes >= 7 && nodes <= 10, "rolled " + nodes);
            seen.add(nodes);
        }

        assertEquals(Set.of(7, 8, 9, 10), seen);
    }

    @Test
    void aFixedRangeAlwaysRollsThatValue() {
        configure(2, 5, 5);

        assertEquals(5, NpcLevelTable.roll(2, new Random(1)));
    }

    @Test
    void rollsAreDeterministicForTheSameSeed() {
        assertEquals(NpcLevelTable.roll(12, new Random(7)), NpcLevelTable.roll(12, new Random(7)));
    }
}
