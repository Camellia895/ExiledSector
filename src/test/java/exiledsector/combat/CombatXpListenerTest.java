package exiledsector.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.EngagementResultForFleetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.persistence.ShipSkillDataManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CombatXpListenerTest {

    private MockedStatic<Global> globalMock;

    @BeforeEach
    void setUp() {
        Map<String, Object> persistentData = new HashMap<>();
        SectorAPI sector = mock(SectorAPI.class);
        when(sector.getPersistentData()).thenReturn(persistentData);

        globalMock = Mockito.mockStatic(Global.class);
        globalMock.when(Global::getSector).thenReturn(sector);
    }

    @AfterEach
    void tearDown() {
        globalMock.close();
    }

    @Test
    void awardsXpToEveryDeployedShipWhenPlayerWins() {
        FleetMemberAPI shipA = mockMember("ship-a");
        FleetMemberAPI shipB = mockMember("ship-b");

        EngagementResultForFleetAPI winner = mock(EngagementResultForFleetAPI.class);
        when(winner.isPlayer()).thenReturn(true);
        when(winner.getDeployed()).thenReturn(List.of(shipA, shipB));

        EngagementResultAPI result = mock(EngagementResultAPI.class);
        when(result.getWinnerResult()).thenReturn(winner);

        new CombatXpListener().reportPlayerEngagement(result);

        assertEquals(25f, ShipSkillDataManager.get("ship-a").getXp());
        assertEquals(25f, ShipSkillDataManager.get("ship-b").getXp());
    }

    @Test
    void awardsXpFromTheLoserFleetWhenPlayerLoses() {
        FleetMemberAPI shipA = mockMember("ship-a");

        EngagementResultForFleetAPI winner = mock(EngagementResultForFleetAPI.class);
        when(winner.isPlayer()).thenReturn(false);

        EngagementResultForFleetAPI loser = mock(EngagementResultForFleetAPI.class);
        when(loser.getDeployed()).thenReturn(List.of(shipA));

        EngagementResultAPI result = mock(EngagementResultAPI.class);
        when(result.getWinnerResult()).thenReturn(winner);
        when(result.getLoserResult()).thenReturn(loser);

        new CombatXpListener().reportPlayerEngagement(result);

        assertEquals(25f, ShipSkillDataManager.get("ship-a").getXp());
    }

    private FleetMemberAPI mockMember(String id) {
        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn(id);
        return member;
    }
}
