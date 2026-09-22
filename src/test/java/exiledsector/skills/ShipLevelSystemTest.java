package exiledsector.skills;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShipLevelSystemTest {

    private static final float XP_BASE = 100f;
    private static final float XP_GROWTH = 2f;
    private static final int NO_GROWTH_CUTOFF = 50;
    private static final int OP_COST_PER_NODE = 3;

    @BeforeEach
    void setUp() {
        SkillTree.getAllTypes().clear();
    }

    @AfterEach
    void tearDown() {
        SkillTree.getAllTypes().clear();
    }

    private static SkillNode node(String id) {
        SkillType type = new SkillType(id, id, "a.png", List.of(), SkillTier.SMALL, null, null, null);
        return new SkillNode(id, type, List.of(), 0f, 0f);
    }

    @Test
    void xpToReachNextLevelGrowsExponentiallyWithLevel() {
        assertEquals(100f, ShipLevelSystem.xpToReachNextLevel(0, XP_BASE, XP_GROWTH, NO_GROWTH_CUTOFF));
        assertEquals(200f, ShipLevelSystem.xpToReachNextLevel(1, XP_BASE, XP_GROWTH, NO_GROWTH_CUTOFF));
        assertEquals(400f, ShipLevelSystem.xpToReachNextLevel(2, XP_BASE, XP_GROWTH, NO_GROWTH_CUTOFF));
    }

    @Test
    void xpToReachNextLevelLocksToTheCutoffTransitionCostFromThatLevelOnward() {
        // Cutoff 3 means the level 2->3 cost (100 * 2^2 = 400) is what every later transition should cost too.
        assertEquals(400f, ShipLevelSystem.xpToReachNextLevel(2, XP_BASE, XP_GROWTH, 3));
        assertEquals(400f, ShipLevelSystem.xpToReachNextLevel(3, XP_BASE, XP_GROWTH, 3));
        assertEquals(400f, ShipLevelSystem.xpToReachNextLevel(10, XP_BASE, XP_GROWTH, 3));
    }

    @Test
    void xpToReachNextLevelBeforeTheCutoffIsUnaffected() {
        assertEquals(100f, ShipLevelSystem.xpToReachNextLevel(0, XP_BASE, XP_GROWTH, 3));
        assertEquals(200f, ShipLevelSystem.xpToReachNextLevel(1, XP_BASE, XP_GROWTH, 3));
    }

    @Test
    void awardXpBelowTheThresholdOnlyAccumulatesXpWithoutLevelingUp() {
        ShipSkillData data = new ShipSkillData();

        ShipLevelSystem.awardXp(data, 50f, XP_BASE, XP_GROWTH, NO_GROWTH_CUTOFF, 50, List.of(), OP_COST_PER_NODE);

        assertEquals(0, data.getLevel());
        assertEquals(50f, data.getXp());
    }

    @Test
    void awardXpAtTheThresholdLevelsUpAndConvertsTheMostRecentAllocationToFree() {
        ShipSkillData data = new ShipSkillData();
        SkillNode a = node("a");
        data.allocate(a, OP_COST_PER_NODE);

        ShipLevelSystem.awardXp(data, XP_BASE, XP_BASE, XP_GROWTH, NO_GROWTH_CUTOFF, 50, List.of(a), OP_COST_PER_NODE);

        assertEquals(1, data.getLevel());
        assertEquals(0f, data.getXp());
        assertTrue(data.isFreeNode("a"));
        assertEquals(0, data.getSpentOp());
    }

    @Test
    void awardXpBanksACreditWhenNothingIsEligibleToConvert() {
        ShipSkillData data = new ShipSkillData();

        ShipLevelSystem.awardXp(data, XP_BASE, XP_BASE, XP_GROWTH, NO_GROWTH_CUTOFF, 50, List.of(), OP_COST_PER_NODE);

        assertEquals(1, data.getLevel());
        assertEquals(1, data.getBankedFreeAllocations());
    }

    @Test
    void awardXpCanTriggerMultipleLevelUpsFromASingleAward() {
        ShipSkillData data = new ShipSkillData();
        SkillNode a = node("a");
        SkillNode b = node("b");
        data.allocate(a, OP_COST_PER_NODE);
        data.allocate(b, OP_COST_PER_NODE);

        // 100 (level 0->1) + 200 (level 1->2) = 300 XP needed for two level-ups.
        ShipLevelSystem.awardXp(data, 300f, XP_BASE, XP_GROWTH, NO_GROWTH_CUTOFF, 50, List.of(a, b), OP_COST_PER_NODE);

        assertEquals(2, data.getLevel());
        assertEquals(0f, data.getXp());
        assertTrue(data.isFreeNode("a"));
        assertTrue(data.isFreeNode("b"));
    }

    @Test
    void awardXpUsesTheFlatCutoffCostPastTheCutoffLevel() {
        ShipSkillData data = new ShipSkillData();

        // Cutoff 3: level 2->3 costs 400 (100 * 2^2), and level 3->4 would normally cost 800
        // (100 * 2^3) but is locked to the same flat 400 by the cutoff.
        ShipLevelSystem.awardXp(data, 100f + 200f + 400f, XP_BASE, XP_GROWTH, 3, 50, List.of(), OP_COST_PER_NODE);
        assertEquals(3, data.getLevel());
        assertEquals(0f, data.getXp());

        ShipLevelSystem.awardXp(data, 400f, XP_BASE, XP_GROWTH, 3, 50, List.of(), OP_COST_PER_NODE);
        assertEquals(4, data.getLevel());
        assertEquals(0f, data.getXp());
    }

    @Test
    void awardXpDoesNotLevelPastTheConfiguredMaxLevel() {
        ShipSkillData data = new ShipSkillData();

        ShipLevelSystem.awardXp(data, 100000f, XP_BASE, XP_GROWTH, NO_GROWTH_CUTOFF, 2, List.of(), OP_COST_PER_NODE);

        assertEquals(2, data.getLevel());
    }

    @Test
    void awardXpDoesNothingWhenAlreadyAtMaxLevel() {
        ShipSkillData data = new ShipSkillData();
        ShipLevelSystem.awardXp(data, XP_BASE, XP_BASE, XP_GROWTH, NO_GROWTH_CUTOFF, 1, List.of(), OP_COST_PER_NODE);
        assertEquals(1, data.getLevel());
        assertEquals(0f, data.getXp());

        ShipLevelSystem.awardXp(data, 50f, XP_BASE, XP_GROWTH, NO_GROWTH_CUTOFF, 1, List.of(), OP_COST_PER_NODE);

        assertEquals(1, data.getLevel());
        assertEquals(0f, data.getXp());
    }
}
