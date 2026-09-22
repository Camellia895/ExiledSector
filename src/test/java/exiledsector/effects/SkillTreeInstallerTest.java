package exiledsector.effects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.skilleffect.DefenseSkillEffect;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import exiledsector.skills.SkillTypeEffect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SkillTreeInstallerTest {

    private MockedStatic<Global> globalMock;
    private SectorAPI sector;
    private CampaignFleetAPI playerFleet;
    private FleetDataAPI fleetData;

    @BeforeEach
    void setUp() {
        Map<String, Object> persistentData = new HashMap<>();
        sector = mock(SectorAPI.class);
        when(sector.getPersistentData()).thenReturn(persistentData);

        playerFleet = mock(CampaignFleetAPI.class);
        fleetData = mock(FleetDataAPI.class);
        when(playerFleet.getFleetData()).thenReturn(fleetData);

        globalMock = Mockito.mockStatic(Global.class);
        globalMock.when(Global::getSector).thenReturn(sector);
        when(sector.getPlayerFleet()).thenReturn(playerFleet);
        when(fleetData.getMembersListCopy()).thenReturn(List.of());

        SkillTree.getAllNodes().clear();
    }

    @AfterEach
    void tearDown() {
        globalMock.close();
        SkillTree.getAllNodes().clear();
    }

    private static FleetMemberAPI mockMember(String id, boolean hasHullMod) {
        FleetMemberAPI member = mock(FleetMemberAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        ShipHullSpecAPI hullSpec = mock(ShipHullSpecAPI.class);
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(member.getId()).thenReturn(id);
        when(member.getVariant()).thenReturn(variant);
        when(member.getHullSpec()).thenReturn(hullSpec);
        when(member.getStats()).thenReturn(stats);
        when(stats.getFleetMember()).thenReturn(member);
        when(hullSpec.getHullSize()).thenReturn(HullSize.FRIGATE);
        when(variant.hasHullMod(SkillTreeHullMod.ID)).thenReturn(hasHullMod);
        return member;
    }

    @Test
    void isNeverDone() {
        assertFalse(new SkillTreeInstaller().isDone());
    }

    @Test
    void doesNotRunWhilePaused() {
        assertFalse(new SkillTreeInstaller().runWhilePaused());
    }

    @Test
    void firstAdvanceChecksImmediatelyEvenWithATinyAmount() {
        SkillTreeInstaller installer = new SkillTreeInstaller();

        installer.advance(0.01f);

        globalMock.verify(Global::getSector, times(1));
    }

    @Test
    void doesNotCheckAgainUntilTheIntervalElapsesAfterACheck() {
        SkillTreeInstaller installer = new SkillTreeInstaller();
        installer.advance(0.01f);

        installer.advance(0.5f);
        globalMock.verify(Global::getSector, times(1));

        installer.advance(0.5f);
        globalMock.verify(Global::getSector, times(2));
    }

    @Test
    void doesNothingWhenThereIsNoPlayerFleet() {
        when(sector.getPlayerFleet()).thenReturn(null);
        SkillTreeInstaller installer = new SkillTreeInstaller();

        installer.advance(0.01f);

        verify(fleetData, never()).getMembersListCopy();
    }

    @Test
    void addsTheHullModOnlyToShipsMissingIt() {
        FleetMemberAPI hasIt = mockMember("ship-with-mod", true);
        FleetMemberAPI missingIt = mockMember("ship-without-mod", false);
        when(fleetData.getMembersListCopy()).thenReturn(List.of(hasIt, missingIt));

        new SkillTreeInstaller().advance(0.01f);

        verify(hasIt.getVariant(), never()).addPermaMod(SkillTreeHullMod.ID);
        verify(missingIt.getVariant()).addPermaMod(SkillTreeHullMod.ID);
    }

    @Test
    void reappliesEffectsOnEveryQualifyingTickRegardlessOfWhetherTheModWasJustAdded() {
        SkillType hullType = new SkillType("hull", "Hull", "graphics/hullmods/reinforced_bulkheads.png", 2, List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)), SkillTier.SMALL, null, null, null);
        SkillNode hullNode = new SkillNode("hull_1", hullType, List.of(), 0f, 0f);
        SkillTree.register(hullNode);

        FleetMemberAPI member = mockMember("ship-a", true);
        ShipSkillDataManager.get("ship-a").allocate(hullNode, 1);
        StatBonus hullBonus = mock(StatBonus.class);
        when(member.getStats().getHullBonus()).thenReturn(hullBonus);
        when(fleetData.getMembersListCopy()).thenReturn(List.of(member));

        new SkillTreeInstaller().advance(0.01f);

        verify(hullBonus).modifyPercent("exiledSector_skill_hull_1", 10f);
    }
}
