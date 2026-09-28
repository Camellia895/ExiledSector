package exiledsector.skills;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.persistence.ShipSkillDataManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SkillDataResolverTest {

    private static final String ENEMY_TAG = "exiledSector_enemyTree|bulwark|2|root,a";

    private MockedStatic<ShipSkillDataManager> dataManagerMock;

    @BeforeEach
    void setUp() {
        SkillDataResolver.clearCache();
        SkillTree.getAllNodes().clear();
        SkillTree.getAllTypes().clear();
        SkillType rootType = new SkillType.Builder("root_type", "Root", "a.png", SkillTier.ROOT).build();
        SkillType smallType = new SkillType.Builder("a_type", "A", "a.png", SkillTier.SMALL).build();
        SkillTree.register(new SkillNode("root", rootType, List.of(), 0f, 0f));
        SkillTree.register(new SkillNode("a", smallType, List.of("root"), 0f, 0f));
        dataManagerMock = Mockito.mockStatic(ShipSkillDataManager.class);
    }

    @AfterEach
    void tearDown() {
        dataManagerMock.close();
        SkillDataResolver.clearCache();
        SkillTree.getAllNodes().clear();
        SkillTree.getAllTypes().clear();
    }

    private static ShipVariantAPI variantWithTags(String... tags) {
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(variant.getTags()).thenReturn(List.of(tags));
        return variant;
    }

    private static FleetMemberAPI member(String id) {
        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn(id);
        return member;
    }

    @Test
    void anEnemyTaggedVariantResolvesToItsTaggedTreeWithoutTouchingTheSave() {
        ShipSkillData data = SkillDataResolver.resolve(member("npc-1"), variantWithTags(ENEMY_TAG));

        assertEquals(List.of("root", "a"), List.copyOf(data.getAllocatedNodeIds()));
        assertTrue(data.isEnemyBuild());
        dataManagerMock.verifyNoInteractions();
    }

    @Test
    void theSameTagIsDecodedOnlyOnce() {
        ShipSkillData first = SkillDataResolver.resolve(member("npc-1"), variantWithTags(ENEMY_TAG));
        ShipSkillData second = SkillDataResolver.resolve(null, variantWithTags(ENEMY_TAG));

        assertSame(first, second);
    }

    @Test
    void clearingTheCacheDecodesTagsAfresh() {
        ShipSkillData before = SkillDataResolver.resolve(null, variantWithTags(ENEMY_TAG));
        SkillDataResolver.clearCache();

        assertNotSame(before, SkillDataResolver.resolve(null, variantWithTags(ENEMY_TAG)));
    }

    @Test
    void aMalformedEnemyTagGivesAnEmptyEnemyTreeInsteadOfTheSavedOne() {
        ShipSkillData data = SkillDataResolver.resolve(member("npc-1"), variantWithTags("exiledSector_enemyTree|broken"));

        assertTrue(data.getAllocatedNodeIds().isEmpty());
        assertTrue(data.isEnemyBuild());
        dataManagerMock.verifyNoInteractions();
    }

    @Test
    void anUntaggedShipUsesItsSavedTree() {
        ShipSkillData saved = new ShipSkillData();
        dataManagerMock.when(() -> ShipSkillDataManager.get("ship-a")).thenReturn(saved);

        assertSame(saved, SkillDataResolver.resolve(member("ship-a"), variantWithTags("exiledSector_installed_x")));
        assertSame(saved, SkillDataResolver.resolve(member("ship-a"), null));
    }

    @Test
    void anUntaggedShipWithoutAFleetMemberHasNoTree() {
        assertNull(SkillDataResolver.resolve(null, variantWithTags()));
        assertNull(SkillDataResolver.resolve(null, null));
    }

    @Test
    void reportsWhetherAVariantCarriesAnEnemyTree() {
        assertTrue(SkillDataResolver.isEnemyTree(variantWithTags(ENEMY_TAG)));
        assertFalse(SkillDataResolver.isEnemyTree(variantWithTags("exiledSector_installed_x")));
        assertFalse(SkillDataResolver.isEnemyTree(null));
    }
}
