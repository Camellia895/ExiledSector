package exiledsector.skills;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
    void canDeallocateIsTrueForAMemberOfASymmetricCircularLoopWhenTheRestStillReachTheRoot() {
        // a-b-c form a closed triangle (every edge listed on BOTH ends, matching how the real
        // editor stores connections), with only "a" bridging the loop back to the root. Removing
        // "b" (not the bridge) should still succeed since a and c remain connected to each other
        // and to the root via a.
        ShipSkillData data = new ShipSkillData();
        SkillNode a = node("a", List.of("root_low_tech_1", "b", "c"));
        SkillNode b = node("b", List.of("a", "c"));
        SkillNode c = node("c", List.of("a", "b"));
        data.allocate(a);
        data.allocate(b);
        data.allocate(c);

        assertTrue(data.canDeallocate(b, List.of(a, b, c), "root_low_tech_1"));
    }

    @Test
    void canDeallocateIsFalseForTheSoleBridgeOfASymmetricCircularLoop() {
        // Same triangle as above, but this time removing "a" - the only node touching the root -
        // must fail, since b and c would be stranded even though they still connect to each other.
        ShipSkillData data = new ShipSkillData();
        SkillNode a = node("a", List.of("root_low_tech_1", "b", "c"));
        SkillNode b = node("b", List.of("a", "c"));
        SkillNode c = node("c", List.of("a", "b"));
        data.allocate(a);
        data.allocate(b);
        data.allocate(c);

        assertFalse(data.canDeallocate(a, List.of(a, b, c), "root_low_tech_1"));
    }

    @Test
    void canDeallocateIsUnaffectedByAnUnrelatedNodeThatWasAlreadyStrandedBeforeThisRemoval() {
        // "orphan" is allocated but was never actually connected to the root (e.g. left over from
        // manual editor surgery, or a node whose only link broke some other way, unrelated to this
        // test). It is already stranded before we touch anything. Deallocating a completely
        // separate, properly-connected node ("leaf") must not be blocked by that pre-existing,
        // unrelated disconnection - only a NEW disconnection caused by this specific removal should
        // block it.
        ShipSkillData data = new ShipSkillData();
        SkillNode leaf = node("leaf", List.of("root_low_tech_1"));
        SkillNode orphan = node("orphan", List.of("nothingThatExists"));
        data.allocate(leaf);
        data.allocate(orphan);

        assertTrue(data.canDeallocate(leaf, List.of(leaf, orphan), "root_low_tech_1"));
    }

    @Test
    void canDeallocateIsTrueForEveryNonBridgeMemberOfATenNodeSymmetricLoop() {
        // N0..N9 form a closed ring (N0-N1-...-N9-N0), all edges symmetric like real editor data,
        // with only N0 bridging the ring back to the root. Every ring member except the bridge
        // itself should remain individually removable, since the rest of the ring still traces
        // back to root through N0 either "direction" around the remaining arc.
        int ringSize = 10;
        List<SkillNode> ring = new ArrayList<>();
        for (int i = 0; i < ringSize; i++) {
            String id = "ring" + i;
            List<String> neighbors = new ArrayList<>();
            neighbors.add("ring" + ((i - 1 + ringSize) % ringSize));
            neighbors.add("ring" + ((i + 1) % ringSize));
            if (i == 0) neighbors.add("root_low_tech_1");
            ring.add(node(id, neighbors));
        }

        ShipSkillData data = new ShipSkillData();
        for (SkillNode n : ring) {
            data.allocate(n);
        }

        for (int i = 1; i < ringSize; i++) {
            assertTrue(data.canDeallocate(ring.get(i), ring, "root_low_tech_1"),
                    "expected ring" + i + " to be deallocatable");
        }
        assertFalse(data.canDeallocate(ring.get(0), ring, "root_low_tech_1"),
                "ring0 is the sole bridge to root and must not be deallocatable while the rest of the ring is allocated");
    }

    @Test
    void canDeallocateIsFalseWhenASecondAllocatedRootOnlyReachesTheTrueRootThroughTheRemovedNode() {
        // secondRoot is a second, non-native allocated root (the multi-root-per-ship feature)
        // whose only real path back to the true starting root runs through "bridge". A second
        // root must not get an automatic free pass just for being ROOT tier - it has to trace
        // its own path back to the true root like any other node, so removing "bridge" (which
        // would strand secondRoot and its descendant from the true root) must be blocked.
        SkillType rootType = new SkillType("secondRootType", "Second Root", "a.png", 0, 0f, List.of(), SkillTier.ROOT, null, null, null);
        SkillNode secondRoot = new SkillNode("secondRoot", rootType, List.of("bridge", "descendant"), 0f, 0f);
        SkillNode bridge = node("bridge", List.of("root_low_tech_1", "secondRoot"));
        SkillNode descendant = node("descendant", List.of("secondRoot"));

        ShipSkillData data = new ShipSkillData();
        data.allocate(bridge);
        data.allocate(secondRoot);
        data.allocate(descendant);

        assertFalse(data.canDeallocate(bridge, List.of(bridge, secondRoot, descendant), "root_low_tech_1"));
    }

    @Test
    void canDeallocateIsTrueWhenASecondAllocatedRootStillTracesBackToTheTrueRootAfterRemoval() {
        // secondRoot has two independent bridges back to the true root; removing just one of
        // them still leaves it (and therefore everything hanging off it) properly connected.
        SkillType rootType = new SkillType("secondRootType", "Second Root", "a.png", 0, 0f, List.of(), SkillTier.ROOT, null, null, null);
        SkillNode secondRoot = new SkillNode("secondRoot", rootType, List.of("bridgeA", "bridgeB"), 0f, 0f);
        SkillNode bridgeA = node("bridgeA", List.of("root_low_tech_1", "secondRoot"));
        SkillNode bridgeB = node("bridgeB", List.of("root_low_tech_1", "secondRoot"));

        ShipSkillData data = new ShipSkillData();
        data.allocate(bridgeA);
        data.allocate(bridgeB);
        data.allocate(secondRoot);

        assertTrue(data.canDeallocate(bridgeA, List.of(bridgeA, bridgeB, secondRoot), "root_low_tech_1"));
    }

    @Test
    void reproduceReportedLoopLockup() {
        // Exact topology + allocation order reported: a ring of small_logistics_optional nodes
        // that passes through BOTH root_high_tech_1 (the ship's real starting root) and
        // root_low_tech_1 (a second, explicitly-allocated root), closing back on itself at
        // small_logistics_optional_3. connectedTo values copied verbatim from
        // data/skilltrees/ship_skill_tree.json.
        SkillNode rootLowTech = new SkillNode("root_low_tech_1",
                new SkillType("root_low_tech", "Root Low Tech", "a.png", 0, 0f, List.of(), SkillTier.ROOT, null, null, null),
                List.of("small_logistics_optional_11", "small_logistics_optional_31", "small_logistics_optional_14", "small_flux_optional_34"),
                0f, 0f);
        SkillNode n3 = node("small_logistics_optional_3", List.of("root_high_tech_1", "small_logistics_optional_6", "small_logistics_optional_24"));
        SkillNode n6 = node("small_logistics_optional_6", List.of("small_logistics_optional_8", "small_logistics_optional_3"));
        SkillNode n8 = node("small_logistics_optional_8", List.of("small_logistics_optional_6", "small_logistics_optional_12"));
        SkillNode n12 = node("small_logistics_optional_12", List.of("small_logistics_optional_8", "small_logistics_optional_11"));
        SkillNode n11 = node("small_logistics_optional_11", List.of("root_low_tech_1", "small_logistics_optional_26", "small_logistics_optional_12"));
        SkillNode n5 = node("small_logistics_optional_5", List.of("small_logistics_optional_7", "small_logistics_optional_25"));
        SkillNode n7 = node("small_logistics_optional_7", List.of("small_logistics_optional_5", "small_logistics_optional_10", "survey_generic_cost_reduction_1"));
        SkillNode n9 = node("small_logistics_optional_9", List.of("small_logistics_optional_13", "small_logistics_optional_25"));
        SkillNode n10 = node("small_logistics_optional_10", List.of("small_logistics_optional_7", "root_high_tech_1", "increased_sensor_range_2", "small_logistics_optional_23"));
        SkillNode n13 = node("small_logistics_optional_13", List.of("small_logistics_optional_9", "small_logistics_optional_14", "cargo_capacity_flat_2", "fuel_flat_2"));
        SkillNode n14 = node("small_logistics_optional_14", List.of("small_logistics_optional_13", "root_low_tech_1", "small_logistics_optional_22"));
        SkillNode n25 = node("small_logistics_optional_25", List.of("small_logistics_optional_4", "small_logistics_optional_9", "small_logistics_optional_5", "operations_center_1", "efficiency_overhaul_1", "converted_fighterbay_1"));

        List<SkillNode> allNodes = List.of(rootLowTech, n3, n6, n8, n12, n11, n5, n7, n9, n10, n13, n14, n25);

        ShipSkillData data = new ShipSkillData();
        String satisfiedRootId = "root_high_tech_1";
        // Allocate in the exact reported order.
        for (SkillNode n : List.of(n10, n7, n5, n25, n9, n13, n14, rootLowTech, n11, n12, n8, n6, n3)) {
            assertTrue(data.canAllocate(n, satisfiedRootId), "expected to be able to allocate " + n.getId());
            data.allocate(n);
        }

        // Per the report: after allocating small_logistics_optional_3, no node should be stuck -
        // every ring member except a true cut vertex should be deallocatable.
        for (SkillNode n : allNodes) {
            assertTrue(data.canDeallocate(n, allNodes, satisfiedRootId),
                    "expected " + n.getId() + " to be deallocatable (ring has two independent paths to root_high_tech_1)");
        }
    }

    @Test
    void reproduceReportedAcceleratedShieldsWheelLockup() {
        // The "Accelerated Shields" wheel: a 6-node ring (advancedshieldemitter_1 -
        // shield_raise_rate_1 - shield_raise_rate_2 - shield_turn_raise_rate_optional_1 -
        // shield_turn_rate_2 - shield_turn_rate_1 - back to advancedshieldemitter_1), bridging to
        // the rest of the tree solely through shield_turn_raise_rate_optional_1's connection to
        // small_flux_optional_25. connectedTo values copied verbatim from
        // data/skilltrees/ship_skill_tree.json. "small_flux_optional_25" stands in for "already
        // satisfied from outside the ring" here since only the ring's internal dynamics matter.
        SkillNode hub = node("advancedshieldemitter_1", List.of("shield_raise_rate_1", "shield_turn_rate_1"));
        SkillNode raise1 = node("shield_raise_rate_1", List.of("shield_raise_rate_2", "advancedshieldemitter_1"));
        SkillNode raise2 = node("shield_raise_rate_2", List.of("shield_raise_rate_1", "shield_turn_raise_rate_optional_1"));
        SkillNode bridge = node("shield_turn_raise_rate_optional_1", List.of("small_flux_optional_25", "shield_raise_rate_2", "shield_turn_rate_2"));
        SkillNode turn2 = node("shield_turn_rate_2", List.of("shield_turn_rate_1", "shield_turn_raise_rate_optional_1"));
        SkillNode turn1 = node("shield_turn_rate_1", List.of("shield_turn_rate_2", "advancedshieldemitter_1"));

        List<SkillNode> ring = List.of(hub, raise1, raise2, bridge, turn2, turn1);

        ShipSkillData data = new ShipSkillData();
        for (SkillNode n : ring) {
            data.allocate(n);
        }

        for (SkillNode n : ring) {
            if (n == bridge) continue;
            assertTrue(data.canDeallocate(n, ring, "small_flux_optional_25"),
                    "expected " + n.getId() + " to be deallocatable");
        }
        assertFalse(data.canDeallocate(bridge, ring, "small_flux_optional_25"),
                "shield_turn_raise_rate_optional_1 is the ring's sole bridge and must stay blocked while the rest of the ring is allocated");
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
