package exiledsector.skills;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShipSkillDataTest {

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
}
