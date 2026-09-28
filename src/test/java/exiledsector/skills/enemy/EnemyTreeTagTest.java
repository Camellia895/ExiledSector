package exiledsector.skills.enemy;

import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import exiledsector.skills.tags.ShipProfile;
import exiledsector.skills.tags.WeaponKind;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnemyTreeTagTest {

    private static final ShipProfile FRIGATE =
            new ShipProfile(HullSize.FRIGATE, ShieldType.FRONT, 0, Set.of(WeaponKind.BALLISTIC), false);

    @BeforeEach
    void setUp() {
        RealSkillData.clear();
        register("root", type("root_type", SkillTier.ROOT));
        register("a", type("a_type", SkillTier.SMALL), "root");
        registerType(type("hull_option", SkillTier.SMALL));
        registerType(type("flux_option", SkillTier.SMALL));
        register("optional_1", new SkillType.Builder("optional", "optional", "a.png", SkillTier.SMALL)
                .optionalOptionIds(List.of("flux_option", "hull_option")).build(), "a");
        register("b", type("b_type", SkillTier.NOTABLE), "optional_1");
    }

    @AfterEach
    void tearDown() {
        RealSkillData.clear();
    }

    private static SkillType type(String id, SkillTier tier) {
        return new SkillType.Builder(id, id, "a.png", tier).build();
    }

    private static SkillType registerType(SkillType type) {
        SkillTree.getAllTypes().put(type.getId(), type);
        return type;
    }

    private static void register(String id, SkillType type, String... connectedTo) {
        registerType(type);
        SkillTree.getAllNodes().put(id, new SkillNode(id, type, List.of(connectedTo), 0f, 0f));
    }

    private static EnemyTreeBuild build(int nodeCount) {
        EnemyLayout layout = new EnemyLayout("bulwark", "Bulwark", "root", List.of(), "",
                List.of(new EnemyLayoutEntry("a", null), new EnemyLayoutEntry("optional_1", "hull_option"),
                        new EnemyLayoutEntry("b", null)));
        return EnemySkillTreeBuilder.build(layout, nodeCount, FRIGATE, EnemyHullMods.NONE);
    }

    private static ShipVariantAPI variantWithTags(String... tags) {
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(variant.getTags()).thenReturn(List.of(tags));
        return variant;
    }

    @Test
    void encodesTheLayoutLevelAndEveryAllocatedNodeWithItsOptionInOrder() {
        String tag = EnemyTreeTag.encode("bulwark", build(3).data());

        assertEquals("exiledSector_enemyTree|bulwark|3|root,a,optional_1=hull_option,b", tag);
    }

    @Test
    void decodingRestoresTheExactTreeTheBuilderMade() {
        ShipSkillData built = build(5).data();

        ShipSkillData restored = EnemyTreeTag.decode(EnemyTreeTag.encode("bulwark", built));

        assertEquals(List.copyOf(built.getAllocatedNodeIds()), List.copyOf(restored.getAllocatedNodeIds()));
        assertEquals("hull_option", restored.getOptionalSelection("optional_1"));
        assertEquals(built.getLevel(), restored.getLevel());
        assertEquals(built.getBankedFreeAllocations(), restored.getBankedFreeAllocations());
        assertEquals(2, restored.getBankedFreeAllocations());
        assertTrue(restored.isFreeNode("a"));
        assertTrue(restored.isFreeNode("b"));
        assertFalse(restored.isFreeNode("root"));
        assertEquals(0, restored.getSpentOp());
        assertTrue(restored.isEnemyBuild());
    }

    @Test
    void findsTheEnemyTreeTagAmongAVariantsTags() {
        String tag = EnemyTreeTag.encode("bulwark", build(1).data());

        assertEquals(tag, EnemyTreeTag.find(variantWithTags("exiledSector_installed_x", tag)));
        assertNull(EnemyTreeTag.find(variantWithTags("exiledSector_installed_x")));
        assertNull(EnemyTreeTag.find(null));
    }

    @Test
    void readsTheLayoutIdBackFromATag() {
        assertEquals("bulwark", EnemyTreeTag.layoutId(EnemyTreeTag.encode("bulwark", build(1).data())));
        assertNull(EnemyTreeTag.layoutId("something_else"));
    }

    @Test
    void malformedTagsDecodeToNull() {
        assertNull(EnemyTreeTag.decode(null));
        assertNull(EnemyTreeTag.decode("exiledSector_installed_heavyarmor"));
        assertNull(EnemyTreeTag.decode("exiledSector_enemyTree|bulwark|3"));
        assertNull(EnemyTreeTag.decode("exiledSector_enemyTree|bulwark|three|root,a"));
        assertNull(EnemyTreeTag.decode("exiledSector_enemyTree||3|root,a"));
    }

    @Test
    void anUnknownRootRestoresAnEmptyEnemyTree() {
        ShipSkillData restored = EnemyTreeTag.decode("exiledSector_enemyTree|bulwark|3|missing_root,a");

        assertTrue(restored.getAllocatedNodeIds().isEmpty());
        assertTrue(restored.isEnemyBuild());
    }

    @Test
    void unknownNodesAndInvalidOptionsAreSkipped() {
        ShipSkillData restored = EnemyTreeTag.decode("exiledSector_enemyTree|bulwark|3|root,a,gone_node,optional_1=bogus,b");

        assertEquals(List.of("root", "a", "b"), List.copyOf(restored.getAllocatedNodeIds()));
    }
}
