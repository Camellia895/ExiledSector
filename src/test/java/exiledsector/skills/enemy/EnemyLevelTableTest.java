package exiledsector.skills.enemy;

import exiledsector.skills.enemy.EnemyLevelTable.NodeRange;
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

class EnemyLevelTableTest {

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
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", EnemyLevelTable.minNodesFieldId(level))).thenReturn(min);
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", EnemyLevelTable.maxNodesFieldId(level))).thenReturn(max);
    }

    @Test
    void everyLevelDefaultsToTheDesignTable() {
        for (int level = 1; level <= 15; level++) {
            assertEquals(new NodeRange(DEFAULTS[level - 1][0], DEFAULTS[level - 1][1]), EnemyLevelTable.range(level),
                    "player level " + level);
            assertEquals(DEFAULTS[level - 1][0], EnemyLevelTable.defaultMinNodes(level));
            assertEquals(DEFAULTS[level - 1][1], EnemyLevelTable.defaultMaxNodes(level));
        }
    }

    @Test
    void configuredValuesOverrideTheDefaults() {
        configure(7, 20, 30);

        assertEquals(new NodeRange(20, 30), EnemyLevelTable.range(7));
        assertEquals(new NodeRange(11, 16), EnemyLevelTable.range(6));
    }

    @Test
    void aMinimumAboveItsMaximumIsSwapped() {
        configure(3, 9, 4);

        assertEquals(new NodeRange(4, 9), EnemyLevelTable.range(3));
    }

    @Test
    void onlyOneSideConfiguredKeepsTheOtherDefault() {
        configure(10, null, 40);

        assertEquals(new NodeRange(13, 40), EnemyLevelTable.range(10));
    }

    @Test
    void playerLevelsOutsideTheTableUseTheNearestRow() {
        assertEquals(EnemyLevelTable.range(15), EnemyLevelTable.range(16));
        assertEquals(EnemyLevelTable.range(15), EnemyLevelTable.range(40));
        assertEquals(EnemyLevelTable.range(1), EnemyLevelTable.range(0));
        assertEquals(EnemyLevelTable.range(1), EnemyLevelTable.range(-3));
    }

    @Test
    void valuesOutsideTheAllowedNodeRangeAreClamped() {
        configure(5, -4, 999);

        assertEquals(new NodeRange(EnemyLevelTable.MIN_NODES, EnemyLevelTable.MAX_NODES), EnemyLevelTable.range(5));
    }

    @Test
    void rollsStayInsideTheRangeAndReachBothEnds() {
        Random random = new Random(42);
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            int nodes = EnemyLevelTable.roll(4, random);
            assertTrue(nodes >= 7 && nodes <= 10, "rolled " + nodes);
            seen.add(nodes);
        }

        assertEquals(Set.of(7, 8, 9, 10), seen);
    }

    @Test
    void aFixedRangeAlwaysRollsThatValue() {
        configure(2, 5, 5);

        assertEquals(5, EnemyLevelTable.roll(2, new Random(1)));
    }

    @Test
    void rollsAreDeterministicForTheSameSeed() {
        assertEquals(EnemyLevelTable.roll(12, new Random(7)), EnemyLevelTable.roll(12, new Random(7)));
    }
}
