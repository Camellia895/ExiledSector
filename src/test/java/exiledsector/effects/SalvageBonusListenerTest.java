package exiledsector.effects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.DynamicStatsAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SalvageBonusListenerTest {

    private MockedStatic<Global> globalMock;
    private FleetDataAPI fleetData;
    private MutableStat salvageMult;

    @BeforeEach
    void setUp() {
        SectorAPI sector = mock(SectorAPI.class);
        CampaignFleetAPI playerFleet = mock(CampaignFleetAPI.class);
        fleetData = mock(FleetDataAPI.class);
        when(sector.getPlayerFleet()).thenReturn(playerFleet);
        when(playerFleet.getFleetData()).thenReturn(fleetData);
        MutableFleetStatsAPI fleetStats = mock(MutableFleetStatsAPI.class);
        DynamicStatsAPI fleetDynamic = mock(DynamicStatsAPI.class);
        when(playerFleet.getStats()).thenReturn(fleetStats);
        when(fleetStats.getDynamic()).thenReturn(fleetDynamic);
        salvageMult = mock(MutableStat.class);
        when(fleetDynamic.getStat(Stats.BATTLE_SALVAGE_MULT_FLEET)).thenReturn(salvageMult);

        globalMock = Mockito.mockStatic(Global.class);
        globalMock.when(Global::getSector).thenReturn(sector);
    }

    @AfterEach
    void tearDown() {
        globalMock.close();
    }

    private static FleetMemberAPI memberContributing(float percent) {
        FleetMemberAPI member = mock(FleetMemberAPI.class);
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        DynamicStatsAPI dynamic = mock(DynamicStatsAPI.class);
        when(member.getStats()).thenReturn(stats);
        when(stats.getDynamic()).thenReturn(dynamic);
        when(dynamic.getValue("exiledSector_postBattleSalvageContribution", 0f)).thenReturn(percent);
        return member;
    }

    @Test
    void eachEngagementTotalsTheSalvageContributionsOfTheShipsStillInTheFleet() {
        List<FleetMemberAPI> members = List.of(memberContributing(25f), memberContributing(10f));
        when(fleetData.getMembersListCopy()).thenReturn(members);
        SalvageBonusListener listener = new SalvageBonusListener();

        listener.reportPlayerEngagement(mock(EngagementResultAPI.class));

        verify(salvageMult).modifyFlat("exiledSector_postBattleSalvage", 0.35f);
    }

    @Test
    void theBonusDropsOnceTheContributingShipHasLeftTheFleet() {
        List<FleetMemberAPI> withSalvager = List.of(memberContributing(25f));
        List<FleetMemberAPI> withoutSalvager = List.of(memberContributing(0f));
        when(fleetData.getMembersListCopy()).thenReturn(withSalvager);
        SalvageBonusListener listener = new SalvageBonusListener();
        listener.reportPlayerEngagement(mock(EngagementResultAPI.class));

        when(fleetData.getMembersListCopy()).thenReturn(withoutSalvager);
        listener.reportPlayerEngagement(mock(EngagementResultAPI.class));

        InOrder order = inOrder(salvageMult);
        order.verify(salvageMult).modifyFlat("exiledSector_postBattleSalvage", 0.25f);
        order.verify(salvageMult).modifyFlat("exiledSector_postBattleSalvage", 0f);
    }
}
