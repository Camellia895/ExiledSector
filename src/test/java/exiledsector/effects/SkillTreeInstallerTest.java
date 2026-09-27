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
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.loading.VariantSource;
import exiledsector.compat.SecondInCommandCompat;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.skilleffect.DefenseSkillEffect;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import exiledsector.skills.SkillTypeEffect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SkillTreeInstallerTest {

    private MockedStatic<Global> globalMock;
    private SectorAPI sector;
    private FleetDataAPI fleetData;

    @BeforeEach
    void setUp() {
        Map<String, Object> persistentData = new HashMap<>();
        sector = mock(SectorAPI.class);
        when(sector.getPersistentData()).thenReturn(persistentData);

        CampaignFleetAPI playerFleet = mock(CampaignFleetAPI.class);
        fleetData = mock(FleetDataAPI.class);
        when(playerFleet.getFleetData()).thenReturn(fleetData);
        MutableFleetStatsAPI fleetStats = mock(MutableFleetStatsAPI.class);
        when(playerFleet.getStats()).thenReturn(fleetStats);
        StatBonus detectedRangeMod = mock(StatBonus.class);
        when(fleetStats.getDetectedRangeMod()).thenReturn(detectedRangeMod);
        when(playerFleet.isTransponderOn()).thenReturn(true);

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
        when(variant.getSource()).thenReturn(VariantSource.REFIT);
        return member;
    }

    @Test
    void installsIntoAShipSpecificCopyWhenTheVariantIsNotAlreadyARefitVariant() {
        FleetMemberAPI member = mockMember("stock-ship", false);
        ShipVariantAPI shared = member.getVariant();
        ShipVariantAPI copy = mock(ShipVariantAPI.class);
        when(shared.getSource()).thenReturn(VariantSource.STOCK);
        when(shared.clone()).thenReturn(copy);
        when(fleetData.getMembersListCopy()).thenReturn(List.of(member));

        new SkillTreeInstaller().advance(0.01f);

        verify(copy).setSource(VariantSource.REFIT);
        verify(member).setVariant(copy, false, true);
        verify(copy).addPermaMod(SkillTreeHullMod.ID);
        verify(shared, never()).addPermaMod(SkillTreeHullMod.ID);
    }

    @Test
    void installsDirectlyIntoAnAlreadyShipSpecificRefitVariant() {
        FleetMemberAPI member = mockMember("refit-ship", false);
        when(fleetData.getMembersListCopy()).thenReturn(List.of(member));

        new SkillTreeInstaller().advance(0.01f);

        verify(member.getVariant(), never()).clone();
        verify(member, never()).setVariant(any(), anyBoolean(), anyBoolean());
        verify(member.getVariant()).addPermaMod(SkillTreeHullMod.ID);
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

        // once from SkillTreeInstaller.advance itself, once from LogisticsSkillEffect.recomputeExtendedPhaseField
        globalMock.verify(Global::getSector, times(2));
    }

    @Test
    void doesNotCheckAgainUntilTheIntervalElapsesAfterACheck() {
        SkillTreeInstaller installer = new SkillTreeInstaller();
        installer.advance(0.01f);

        installer.advance(0.5f);
        globalMock.verify(Global::getSector, times(2));

        installer.advance(0.5f);
        globalMock.verify(Global::getSector, times(4));
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
    void movesItsHullModBehindTheSecondInCommandControllerSoItAppliesAfterIt() {
        FleetMemberAPI member = mockMember("ship", true);
        ShipVariantAPI variant = member.getVariant();
        when(variant.hasHullMod(SecondInCommandCompat.CONTROLLER_HULLMOD_ID)).thenReturn(true);
        when(variant.getHullMods()).thenReturn(List.of(SkillTreeHullMod.ID, SecondInCommandCompat.CONTROLLER_HULLMOD_ID));
        when(fleetData.getMembersListCopy()).thenReturn(List.of(member));

        new SkillTreeInstaller().advance(0.01f);

        InOrder order = inOrder(variant);
        order.verify(variant).removePermaMod(SkillTreeHullMod.ID);
        order.verify(variant).addPermaMod(SkillTreeHullMod.ID);
    }

    @Test
    void leavesItsHullModInPlaceWhenItAlreadyAppliesAfterTheSecondInCommandController() {
        FleetMemberAPI member = mockMember("ship", true);
        ShipVariantAPI variant = member.getVariant();
        when(variant.hasHullMod(SecondInCommandCompat.CONTROLLER_HULLMOD_ID)).thenReturn(true);
        when(variant.getHullMods()).thenReturn(List.of(SecondInCommandCompat.CONTROLLER_HULLMOD_ID, SkillTreeHullMod.ID));
        when(fleetData.getMembersListCopy()).thenReturn(List.of(member));

        new SkillTreeInstaller().advance(0.01f);

        verify(variant, never()).removePermaMod(SkillTreeHullMod.ID);
        verify(variant, never()).addPermaMod(SkillTreeHullMod.ID);
    }

    @Test
    void reappliesEffectsOnEveryQualifyingTickRegardlessOfWhetherTheModWasJustAdded() {
        SkillType hullType = new SkillType.Builder("hull", "Hull", "graphics/hullmods/reinforced_bulkheads.png", SkillTier.SMALL)
                .effects(List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)))
                .build();
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
