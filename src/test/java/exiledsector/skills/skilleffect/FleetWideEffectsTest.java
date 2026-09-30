package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.hullmods.PhaseField;
import exiledsector.effects.SkillTreeHullMod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FleetWideEffectsTest {

    private MockedStatic<Global> globalMock;
    private CampaignFleetAPI playerFleet;
    private StatBonus detectedRange;

    @BeforeEach
    void setUp() {
        SectorAPI sector = mock(SectorAPI.class);
        playerFleet = mock(CampaignFleetAPI.class);
        MutableFleetStatsAPI fleetStats = mock(MutableFleetStatsAPI.class);
        detectedRange = mock(StatBonus.class);
        FleetDataAPI fleetData = mock(FleetDataAPI.class);
        when(sector.getPlayerFleet()).thenReturn(playerFleet);
        when(playerFleet.getStats()).thenReturn(fleetStats);
        when(playerFleet.getFleetData()).thenReturn(fleetData);
        when(playerFleet.isPlayerFleet()).thenReturn(true);
        when(playerFleet.isTransponderOn()).thenReturn(true);
        when(fleetStats.getDetectedRangeMod()).thenReturn(detectedRange);
        when(fleetData.getMembersListCopy()).thenReturn(List.of());
        globalMock = Mockito.mockStatic(Global.class);
        globalMock.when(Global::getSector).thenReturn(sector);

        FleetWideEffects.markPhaseFieldStale();
        FleetWideEffects.recomputeExtendedPhaseFieldIfStale();
        clearInvocations(detectedRange);
    }

    @AfterEach
    void tearDown() {
        globalMock.close();
    }

    @Test
    void doesNotRecomputeWhileNothingHasChanged() {
        FleetWideEffects.recomputeExtendedPhaseFieldIfStale();
        FleetWideEffects.recomputeExtendedPhaseFieldIfStale();

        verify(detectedRange, never()).unmodifyMult(PhaseField.MOD_KEY);
    }

    @Test
    void recomputesOnceAfterBeingMarkedStale() {
        FleetWideEffects.markPhaseFieldStale();

        FleetWideEffects.recomputeExtendedPhaseFieldIfStale();
        FleetWideEffects.recomputeExtendedPhaseFieldIfStale();

        verify(detectedRange, times(1)).unmodifyMult(PhaseField.MOD_KEY);
    }

    @Test
    void recomputesWhenTheTransponderIsToggled() {
        when(playerFleet.isTransponderOn()).thenReturn(false);

        FleetWideEffects.recomputeExtendedPhaseFieldIfStale();

        verify(detectedRange, times(1)).unmodifyMult(PhaseField.MOD_KEY);
    }

    @Test
    void recomputesWhenVanillaPutsItsOwnPhaseFieldModifierBack() {
        when(detectedRange.getMultBonus(PhaseField.MOD_KEY)).thenReturn(mock(MutableStat.StatMod.class));

        FleetWideEffects.recomputeExtendedPhaseFieldIfStale();

        verify(detectedRange, times(1)).unmodifyMult(PhaseField.MOD_KEY);
    }

    @Test
    void onlyAPlayerFleetSyncMarksThePhaseFieldStale() {
        CampaignFleetAPI npcFleet = mock(CampaignFleetAPI.class);
        SkillTreeHullMod hullMod = new SkillTreeHullMod();

        hullMod.onFleetSync(npcFleet);
        FleetWideEffects.recomputeExtendedPhaseFieldIfStale();
        verify(detectedRange, never()).unmodifyMult(PhaseField.MOD_KEY);

        hullMod.onFleetSync(playerFleet);
        FleetWideEffects.recomputeExtendedPhaseFieldIfStale();
        verify(detectedRange, times(1)).unmodifyMult(PhaseField.MOD_KEY);
    }
}
