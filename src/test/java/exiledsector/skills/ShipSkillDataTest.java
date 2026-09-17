package exiledsector.skills;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShipSkillDataTest {

    private static SkillNode node(String id, List<String> prerequisiteIds) {
        SkillType type = new SkillType(id, id, "graphics/hullmods/heavy_armor.png", 2, 300f, null, 0f);
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

        assertTrue(data.canAllocate(node("root", List.of())));
    }

    @Test
    void canAllocateIsFalseWhenAPrerequisiteIsNotAllocated() {
        ShipSkillData data = new ShipSkillData();

        assertFalse(data.canAllocate(node("child", List.of("parent"))));
    }

    @Test
    void canAllocateIsTrueOnceEveryPrerequisiteIsAllocated() {
        ShipSkillData data = new ShipSkillData();
        SkillNode parent = node("parent", List.of());
        data.allocate(parent);

        assertTrue(data.canAllocate(node("child", List.of("parent"))));
    }

    @Test
    void canDeallocateIsTrueWhenNoAllocatedNodeDependsOnIt() {
        ShipSkillData data = new ShipSkillData();
        SkillNode parent = node("parent", List.of());
        data.allocate(parent);

        assertTrue(data.canDeallocate(parent, List.of(parent)));
    }

    @Test
    void canDeallocateIsFalseWhenAnAllocatedChildDependsOnIt() {
        ShipSkillData data = new ShipSkillData();
        SkillNode parent = node("parent", List.of());
        SkillNode child = node("child", List.of("parent"));
        data.allocate(parent);
        data.allocate(child);

        assertFalse(data.canDeallocate(parent, List.of(parent, child)));
    }

    @Test
    void toggleAllocatesAnUnallocatedNodeWhosePrerequisitesAreMet() {
        ShipSkillData data = new ShipSkillData();
        SkillNode root = node("root", List.of());

        data.toggle(root, List.of(root));

        assertTrue(data.isAllocated("root"));
    }

    @Test
    void toggleDoesNothingForAnUnallocatedNodeWithAnUnmetPrerequisite() {
        ShipSkillData data = new ShipSkillData();
        SkillNode parent = node("parent", List.of());
        SkillNode child = node("child", List.of("parent"));

        data.toggle(child, List.of(parent, child));

        assertFalse(data.isAllocated("child"));
    }

    @Test
    void toggleDeallocatesAnAllocatedNodeWithNoAllocatedChildren() {
        ShipSkillData data = new ShipSkillData();
        SkillNode root = node("root", List.of());
        data.allocate(root);

        data.toggle(root, List.of(root));

        assertFalse(data.isAllocated("root"));
    }

    @Test
    void toggleDoesNothingForAnAllocatedNodeWithAnAllocatedChild() {
        ShipSkillData data = new ShipSkillData();
        SkillNode parent = node("parent", List.of());
        SkillNode child = node("child", List.of("parent"));
        data.allocate(parent);
        data.allocate(child);

        data.toggle(parent, List.of(parent, child));

        assertTrue(data.isAllocated("parent"));
    }
}
