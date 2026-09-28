package exiledsector.ui.inspect;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.SkillDataResolver;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import exiledsector.skills.enemy.EnemyLayout;
import exiledsector.skills.enemy.EnemyLayoutEntry;
import exiledsector.skills.enemy.EnemyLayouts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShipTreeLookupTest {

    private static final String ENEMY_TAG = "exiledSector_enemyTree|bulwark|2|root_1,a_1";

    private MockedStatic<Global> globalMock;
    private Map<String, Object> persistentData;

    @BeforeEach
    void setUp() {
        SkillDataResolver.clearCache();
        SkillTree.getAllNodes().clear();
        SkillTree.getAllTypes().clear();
        SkillTree.register(new SkillNode("root_1", new SkillType.Builder("root", "Root", "a.png", SkillTier.ROOT).build(), List.of(), 0f, 0f));
        SkillTree.register(new SkillNode("a_1", new SkillType.Builder("a", "A", "a.png", SkillTier.SMALL).build(), List.of("root_1"), 0f, 0f));
        EnemyLayouts.register(Map.of("bulwark", new EnemyLayout("bulwark", "Bulwark", "root_1", List.of(), "",
                List.of(new EnemyLayoutEntry("a_1", null)))));
        persistentData = new HashMap<>();
        SectorAPI sector = mock(SectorAPI.class);
        when(sector.getPersistentData()).thenReturn(persistentData);
        globalMock = Mockito.mockStatic(Global.class);
        globalMock.when(Global::getSector).thenReturn(sector);
    }

    @AfterEach
    void tearDown() {
        globalMock.close();
        EnemyLayouts.register(Map.of());
        SkillDataResolver.clearCache();
        SkillTree.getAllNodes().clear();
        SkillTree.getAllTypes().clear();
    }

    private static FleetMemberAPI member(String id, boolean playerFleet, String... tags) {
        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn(id);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(variant.getTags()).thenReturn(List.of(tags));
        when(member.getVariant()).thenReturn(variant);
        CampaignFleetAPI fleet = mock(CampaignFleetAPI.class);
        when(fleet.isPlayerFleet()).thenReturn(playerFleet);
        FleetDataAPI fleetData = mock(FleetDataAPI.class);
        when(fleetData.getFleet()).thenReturn(fleet);
        when(member.getFleetData()).thenReturn(fleetData);
        return member;
    }

    @Test
    void aLevelledEnemyShowsItsTaggedTreeAndLayoutName() {
        ShipTreeLookup.ShipTree tree = ShipTreeLookup.find(member("npc", false, ENEMY_TAG));

        assertNotNull(tree);
        assertEquals("Bulwark", tree.layoutName());
        assertEquals(List.of("root_1", "a_1"), List.copyOf(tree.data().getAllocatedNodeIds()));
        assertTrue(persistentData.isEmpty());
    }

    @Test
    void anUnlevelledEnemyHasNothingToShowAndLeavesTheSaveUntouched() {
        assertNull(ShipTreeLookup.find(member("npc", false)));
        assertTrue(persistentData.isEmpty());
    }

    @Test
    void aPlayerShipShowsItsSavedTreeOnceItHasOne() {
        FleetMemberAPI ship = member("mine", true);
        assertNull(ShipTreeLookup.find(ship));

        ShipSkillDataManager.get("mine").incrementLevel();
        ShipTreeLookup.ShipTree tree = ShipTreeLookup.find(ship);

        assertNotNull(tree);
        assertNull(tree.layoutName());
        assertEquals(1, tree.data().getLevel());
    }

    @Test
    void onlyEnemyTaggedShipsCountAsLevelledEnemies() {
        assertTrue(ShipTreeLookup.isLevelledEnemy(member("npc", false, ENEMY_TAG)));
        assertFalse(ShipTreeLookup.isLevelledEnemy(member("npc", false)));
        assertFalse(ShipTreeLookup.isLevelledEnemy(null));
    }
}
