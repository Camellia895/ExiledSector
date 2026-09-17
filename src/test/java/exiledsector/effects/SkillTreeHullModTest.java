package exiledsector.effects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillEffect;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SkillTreeHullModTest {

    private MockedStatic<Global> globalMock;

    @BeforeEach
    void setUp() {
        Map<String, Object> persistentData = new HashMap<>();
        SectorAPI sector = mock(SectorAPI.class);
        when(sector.getPersistentData()).thenReturn(persistentData);

        globalMock = Mockito.mockStatic(Global.class);
        globalMock.when(Global::getSector).thenReturn(sector);

        SkillTree.getAllNodes().clear();
    }

    @AfterEach
    void tearDown() {
        globalMock.close();
        SkillTree.getAllNodes().clear();
    }

    @Test
    void appliesTheEffectOfEachAllocatedNodeWithOneRegisteredOnTheTree() {
        SkillType hullType = new SkillType("hull", "Reinforced Hull", "graphics/hullmods/reinforced_bulkheads.png", 2, 500, SkillEffect.HULL, 10f);
        SkillNode hullNode = new SkillNode("hull_1", hullType, List.of(), 0f, 0f);
        SkillTree.register(hullNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(hullNode);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(stats.getFleetMember()).thenReturn(member);
        com.fs.starfarer.api.combat.StatBonus hullStatBonus = mock(com.fs.starfarer.api.combat.StatBonus.class);
        when(stats.getHullBonus()).thenReturn(hullStatBonus);

        new SkillTreeHullMod().applyEffectsBeforeShipCreation(HullSize.FRIGATE, stats, "exiledSector_core");

        verify(hullStatBonus).modifyPercent("exiledSector_skill_hull_1", 10f);
    }

    @Test
    void skipsNodesWithNoEffectDefinedYet() {
        SkillType cosmeticType = new SkillType("capacitors", "Capacitors", "graphics/hullmods/flux_coil_adjunct.png", 2, 500, null, 0f);
        SkillNode cosmeticNode = new SkillNode("capacitors_1", cosmeticType, List.of(), 0f, 0f);
        SkillTree.register(cosmeticNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(cosmeticNode);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(stats.getFleetMember()).thenReturn(member);

        new SkillTreeHullMod().applyEffectsBeforeShipCreation(HullSize.FRIGATE, stats, "exiledSector_core");

        verify(stats, never()).getHullBonus();
    }

    @Test
    void doesNothingWhenTheShipHasNoFleetMember() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(stats.getFleetMember()).thenReturn(null);

        new SkillTreeHullMod().applyEffectsBeforeShipCreation(HullSize.FRIGATE, stats, "exiledSector_core");

        verify(stats, never()).getHullBonus();
    }
}
