package exiledsector.skills;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShipSkillDataTest {

    @BeforeEach
    void setUp() {
        SkillTree.getAllTypes().clear();
    }

    @AfterEach
    void tearDown() {
        SkillTree.getAllTypes().clear();
    }

    private static SkillNode node(String id, List<String> prerequisiteIds) {
        SkillType type = new SkillType(id, id, "graphics/hullmods/heavy_armor.png", 2, 300f, List.of(), SkillTier.SMALL, null, null, null);
        return new SkillNode(id, type, prerequisiteIds, 0f, 0f);
    }

    @Test
    void startsWithNoProgress() {
        ShipSkillData data = new ShipSkillData();

        assertEquals(0f, data.getXp());
        assertEquals(0, data.getSpentOp());
        assertTrue(data.getAllocatedNodeIds().isEmpty());
    }

    @Test
    void addXpAccumulatesAcrossCalls() {
        ShipSkillData data = new ShipSkillData();

        data.addXp(10f);
        data.addXp(5f);

        assertEquals(15f, data.getXp());
    }

    @Test
    void allocateMarksNodeAndSpendsItsOpAndXpCost() {
        ShipSkillData data = new ShipSkillData();
        data.addXp(500f);
        SkillNode node = node("armor_1", List.of());

        data.allocate(node);

        assertTrue(data.isAllocated("armor_1"));
        assertEquals(2, data.getSpentOp());
        assertEquals(200f, data.getXp());
    }

    @Test
    void deallocateRefundsTheNodesOpAndXpCost() {
        ShipSkillData data = new ShipSkillData();
        data.addXp(500f);
        SkillNode node = node("armor_1", List.of());
        data.allocate(node);

        data.deallocate(node);

        assertFalse(data.isAllocated("armor_1"));
        assertEquals(0, data.getSpentOp());
        assertEquals(500f, data.getXp());
    }

    @Test
    void isAllocatedIsFalseForANodeThatWasNeverAllocated() {
        ShipSkillData data = new ShipSkillData();

        assertFalse(data.isAllocated("nonexistent"));
    }

    @Test
    void canAllocateIsTrueWhenThereAreNoPrerequisites() {
        ShipSkillData data = new ShipSkillData();

        assertTrue(data.canAllocate(node("root", List.of()), null));
    }

    @Test
    void canAllocateIsFalseWhenAPrerequisiteIsNotAllocated() {
        ShipSkillData data = new ShipSkillData();

        assertFalse(data.canAllocate(node("child", List.of("parent")), null));
    }

    @Test
    void canAllocateIsTrueOnceEveryPrerequisiteIsAllocated() {
        ShipSkillData data = new ShipSkillData();
        SkillNode parent = node("parent", List.of());
        data.allocate(parent);

        assertTrue(data.canAllocate(node("child", List.of("parent")), null));
    }

    @Test
    void canAllocateIsFalseWithMultiplePrerequisitesWhenNoneAreAllocated() {
        ShipSkillData data = new ShipSkillData();

        assertFalse(data.canAllocate(node("child", List.of("b", "c")), null));
    }

    @Test
    void canAllocateIsTrueWithMultiplePrerequisitesWhenOnlyOneIsAllocated() {
        ShipSkillData data = new ShipSkillData();
        SkillNode b = node("b", List.of());
        data.allocate(b);

        assertTrue(data.canAllocate(node("a", List.of("b", "c")), null));
    }

    @Test
    void canAllocateIsTrueWithMultiplePrerequisitesWhenAllAreAllocated() {
        ShipSkillData data = new ShipSkillData();
        SkillNode b = node("b", List.of());
        SkillNode c = node("c", List.of());
        data.allocate(b);
        data.allocate(c);

        assertTrue(data.canAllocate(node("a", List.of("b", "c")), null));
    }

    @Test
    void canDeallocateIsTrueWhenNoAllocatedNodeDependsOnIt() {
        ShipSkillData data = new ShipSkillData();
        SkillNode parent = node("parent", List.of());
        data.allocate(parent);

        assertTrue(data.canDeallocate(parent, List.of(parent), null));
    }

    @Test
    void canDeallocateIsFalseWhenAnAllocatedChildDependsOnIt() {
        ShipSkillData data = new ShipSkillData();
        SkillNode parent = node("parent", List.of());
        SkillNode child = node("child", List.of("parent"));
        data.allocate(parent);
        data.allocate(child);

        assertFalse(data.canDeallocate(parent, List.of(parent, child), null));
    }

    @Test
    void canDeallocateIsTrueWhenAnAllocatedChildHasAnotherAllocatedPrerequisite() {
        ShipSkillData data = new ShipSkillData();
        SkillNode b = node("b", List.of());
        SkillNode c = node("c", List.of());
        SkillNode a = node("a", List.of("b", "c"));
        data.allocate(b);
        data.allocate(c);
        data.allocate(a);

        assertTrue(data.canDeallocate(b, List.of(a, b, c), null));
    }

    @Test
    void canDeallocateIsFalseWhenItIsTheOnlyAllocatedPrerequisiteOfAnAllocatedChild() {
        ShipSkillData data = new ShipSkillData();
        SkillNode b = node("b", List.of());
        SkillNode c = node("c", List.of());
        SkillNode a = node("a", List.of("b", "c"));
        data.allocate(b);
        data.allocate(a);

        assertFalse(data.canDeallocate(b, List.of(a, b, c), null));
    }

    @Test
    void toggleAllocatesAnUnallocatedNodeWhosePrerequisitesAreMet() {
        ShipSkillData data = new ShipSkillData();
        SkillNode root = node("root", List.of());

        data.toggle(root, List.of(root), null);

        assertTrue(data.isAllocated("root"));
    }

    @Test
    void toggleDoesNothingForAnUnallocatedNodeWithAnUnmetPrerequisite() {
        ShipSkillData data = new ShipSkillData();
        SkillNode parent = node("parent", List.of());
        SkillNode child = node("child", List.of("parent"));

        data.toggle(child, List.of(parent, child), null);

        assertFalse(data.isAllocated("child"));
    }

    @Test
    void toggleDeallocatesAnAllocatedNodeWithNoAllocatedChildren() {
        ShipSkillData data = new ShipSkillData();
        SkillNode root = node("root", List.of());
        data.allocate(root);

        data.toggle(root, List.of(root), null);

        assertFalse(data.isAllocated("root"));
    }

    @Test
    void toggleDoesNothingForAnAllocatedNodeWithAnAllocatedChild() {
        ShipSkillData data = new ShipSkillData();
        SkillNode parent = node("parent", List.of());
        SkillNode child = node("child", List.of("parent"));
        data.allocate(parent);
        data.allocate(child);

        data.toggle(parent, List.of(parent, child), null);

        assertTrue(data.isAllocated("parent"));
    }

    @Test
    void isSatisfiedIsTrueForAnAllocatedNode() {
        ShipSkillData data = new ShipSkillData();
        data.allocate(node("hull_1", List.of()));

        assertTrue(data.isSatisfied("hull_1", null));
    }

    @Test
    void isSatisfiedIsTrueForTheSatisfiedRootEvenIfNeverAllocated() {
        ShipSkillData data = new ShipSkillData();

        assertTrue(data.isSatisfied("root_low_tech_1", "root_low_tech_1"));
    }

    @Test
    void isSatisfiedIsFalseForAnUnsatisfiedRoot() {
        ShipSkillData data = new ShipSkillData();

        assertFalse(data.isSatisfied("root_high_tech_1", "root_low_tech_1"));
    }

    @Test
    void canAllocateIsTrueWhenPrerequisiteIsTheSatisfiedRootEvenIfNotAllocated() {
        ShipSkillData data = new ShipSkillData();
        SkillNode child = node("hull_1", List.of("root_low_tech_1"));

        assertTrue(data.canAllocate(child, "root_low_tech_1"));
    }

    @Test
    void canAllocateIsFalseWhenPrerequisiteIsAnUnsatisfiedRoot() {
        ShipSkillData data = new ShipSkillData();
        SkillNode child = node("hull_1", List.of("root_midline_1"));

        assertFalse(data.canAllocate(child, "root_low_tech_1"));
    }

    @Test
    void canDeallocateTreatsTheSatisfiedRootAsAnotherAllocatedPrerequisite() {
        ShipSkillData data = new ShipSkillData();
        SkillNode b = node("b", List.of());
        SkillNode a = node("a", List.of("b", "root_low_tech_1"));
        data.allocate(b);
        data.allocate(a);

        assertTrue(data.canDeallocate(b, List.of(a, b), "root_low_tech_1"));
    }

    @Test
    void canDeallocateIsFalseWhenTheOnlyAlternatePathIsItselfNotConnectedToTheRoot() {
        // a is the only link back to the root; b's "other" connection (x) is an allocated dead end
        // that isn't itself reachable from the root, so removing a would strand both b and x even
        // though b technically has another allocated neighbor. A check that only looks at b's
        // immediate neighbors (without confirming they trace back to the root) would wrongly allow
        // this.
        ShipSkillData data = new ShipSkillData();
        SkillNode a = node("a", List.of());
        SkillNode b = node("b", List.of("a", "x"));
        SkillNode x = node("x", List.of("b"));
        data.allocate(a);
        data.allocate(b);
        data.allocate(x);

        assertFalse(data.canDeallocate(a, List.of(a, b, x), null));
    }

    @Test
    void canDeallocateIsTrueWhenTheAlternatePathTracesBackToTheRoot() {
        // b's alternate connection (x) itself traces back to a second allocated anchor (a2), so
        // removing a1 leaves b and x still reachable overall.
        ShipSkillData data = new ShipSkillData();
        SkillNode a1 = node("a1", List.of());
        SkillNode a2 = node("a2", List.of());
        SkillNode b = node("b", List.of("a1", "x"));
        SkillNode x = node("x", List.of("b", "a2"));
        data.allocate(a1);
        data.allocate(a2);
        data.allocate(b);
        data.allocate(x);

        assertTrue(data.canDeallocate(a1, List.of(a1, a2, b, x), null));
    }

    @Test
    void getOptionalSelectionIsNullWhenNothingHasBeenSelected() {
        ShipSkillData data = new ShipSkillData();

        assertNull(data.getOptionalSelection("slot_1"));
    }

    @Test
    void selectOptionAllocatesTheSlotNodeAndSpendsTheChosenOptionsCost() {
        ShipSkillData data = new ShipSkillData();
        data.addXp(1000f);
        SkillNode slot = node("slot_1", List.of());
        SkillType chosenOption = new SkillType("hull", "Hull", "a.png", 3, 400f, List.of(), SkillTier.SMALL, null, null, null);

        data.selectOption(slot, chosenOption);

        assertTrue(data.isAllocated("slot_1"));
        assertEquals("hull", data.getOptionalSelection("slot_1"));
        assertEquals(3, data.getSpentOp());
        assertEquals(600f, data.getXp());
    }

    @Test
    void deallocateRefundsTheSelectedOptionsCostNotThePlaceholdersCost() {
        ShipSkillData data = new ShipSkillData();
        data.addXp(1000f);
        SkillNode slot = node("slot_1", List.of());
        SkillType chosenOption = new SkillType("hull", "Hull", "a.png", 3, 400f, List.of(), SkillTier.SMALL, null, null, null);
        SkillTree.registerType(chosenOption);
        data.selectOption(slot, chosenOption);

        data.deallocate(slot);

        assertFalse(data.isAllocated("slot_1"));
        assertNull(data.getOptionalSelection("slot_1"));
        assertEquals(0, data.getSpentOp());
        assertEquals(1000f, data.getXp());
    }

    @Test
    void togglingASelectedOptionalNodeOffClearsTheSelection() {
        ShipSkillData data = new ShipSkillData();
        data.addXp(1000f);
        SkillNode slot = node("slot_1", List.of());
        SkillType chosenOption = new SkillType("hull", "Hull", "a.png", 3, 400f, List.of(), SkillTier.SMALL, null, null, null);
        SkillTree.registerType(chosenOption);
        data.selectOption(slot, chosenOption);

        data.toggle(slot, List.of(slot), null);

        assertFalse(data.isAllocated("slot_1"));
        assertNull(data.getOptionalSelection("slot_1"));
    }
}
