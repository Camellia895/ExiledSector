package exiledsector.effects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.ShipLevelConfig;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillTree;
import lunalib.lunaSettings.LunaSettings;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShipLevelScriptTest {

    private MockedStatic<Global> globalMock;
    private MockedStatic<LunaSettings> lunaSettingsMock;
    private SectorAPI sector;
    private CampaignFleetAPI playerFleet;
    private FleetDataAPI fleetData;
    private CombatEngineAPI engine;

    @BeforeEach
    void setUp() {
        Map<String, Object> persistentData = new HashMap<>();
        sector = mock(SectorAPI.class);
        when(sector.getPersistentData()).thenReturn(persistentData);
        playerFleet = mock(CampaignFleetAPI.class);
        fleetData = mock(FleetDataAPI.class);
        engine = mock(CombatEngineAPI.class);

        when(sector.getPlayerFleet()).thenReturn(playerFleet);
        when(playerFleet.getFleetData()).thenReturn(fleetData);
        when(fleetData.getMembersListCopy()).thenReturn(List.of());

        // LunaSettings must be mocked before Global: its static initializer calls Global.getLogger(...),
        // and forcing that initializer to run while Global is already intercepted (with getLogger
        // unstubbed, returning null) makes it throw and permanently poisons the class for the rest
        // of the JVM/test run.
        lunaSettingsMock = Mockito.mockStatic(LunaSettings.class);
        // Mockito's default unstubbed answer for a boxed-Integer/Float return type is 0, not null,
        // so every field must be explicitly stubbed to null here for ShipLevelConfig's defaults to apply.
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", ShipLevelConfig.MAX_LEVEL_FIELD_ID)).thenReturn(null);
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", ShipLevelConfig.XP_BASE_FIELD_ID)).thenReturn(null);
        lunaSettingsMock.when(() -> LunaSettings.getFloat("exiledSector", ShipLevelConfig.XP_GROWTH_FIELD_ID)).thenReturn(null);
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", ShipLevelConfig.XP_PER_COMBAT_FIELD_ID)).thenReturn(null);
        lunaSettingsMock.when(() -> LunaSettings.getFloat("exiledSector", ShipLevelConfig.XP_LOSS_MULTIPLIER_FIELD_ID)).thenReturn(null);
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", ShipLevelConfig.MAX_ALLOCATED_NODES_FIELD_ID)).thenReturn(null);

        globalMock = Mockito.mockStatic(Global.class);
        globalMock.when(Global::getSector).thenReturn(sector);
        globalMock.when(Global::getCombatEngine).thenReturn(null);

        SkillTree.getAllNodes().clear();
    }

    @AfterEach
    void tearDown() {
        globalMock.close();
        lunaSettingsMock.close();
        SkillTree.getAllNodes().clear();
    }

    private static FleetMemberAPI mockMember(String id) {
        FleetMemberAPI member = mock(FleetMemberAPI.class);
        ShipHullSpecAPI hullSpec = mock(ShipHullSpecAPI.class);
        when(member.getId()).thenReturn(id);
        when(member.getHullSpec()).thenReturn(hullSpec);
        when(hullSpec.getHullSize()).thenReturn(HullSize.FRIGATE);
        return member;
    }

    @Test
    void isNeverDone() {
        assertFalse(new ShipLevelScript().isDone());
    }

    @Test
    void doesNotRunWhilePaused() {
        assertFalse(new ShipLevelScript().runWhilePaused());
    }

    @Test
    void doesNothingWhenThereIsNoCombatEngine() {
        new ShipLevelScript().advance(0.01f);

        globalMock.verify(Global::getSector, never());
    }

    @Test
    void doesNothingDuringSimulationCombat() {
        globalMock.when(Global::getCombatEngine).thenReturn(engine);
        when(engine.isSimulation()).thenReturn(true);

        new ShipLevelScript().advance(0.01f);

        globalMock.verify(Global::getSector, never());
    }

    @Test
    void doesNothingWhileCombatIsStillInProgress() {
        globalMock.when(Global::getCombatEngine).thenReturn(engine);
        when(engine.isSimulation()).thenReturn(false);
        when(engine.isCombatOver()).thenReturn(false);

        new ShipLevelScript().advance(0.01f);

        globalMock.verify(Global::getSector, never());
    }

    @Test
    void awardsXpToEveryFleetMemberWhenCombatEnds() {
        globalMock.when(Global::getCombatEngine).thenReturn(engine);
        when(engine.isSimulation()).thenReturn(false);
        when(engine.isCombatOver()).thenReturn(true);
        FleetMemberAPI member = mockMember("ship-a");
        when(fleetData.getMembersListCopy()).thenReturn(List.of(member));

        new ShipLevelScript().advance(0.01f);

        ShipSkillData data = ShipSkillDataManager.get("ship-a");
        assertEquals(40f, data.getXp());
    }

    @Test
    void appliesTheLossMultiplierWhenTheFleetWasDefeated() {
        globalMock.when(Global::getCombatEngine).thenReturn(engine);
        when(engine.isSimulation()).thenReturn(false);
        when(engine.isCombatOver()).thenReturn(true);
        FleetMemberAPI member = mockMember("ship-a");
        when(fleetData.getMembersListCopy()).thenReturn(List.of(member));

        BattleAPI battle = mock(BattleAPI.class);
        CampaignFleetAPI nonPlayerCombined = mock(CampaignFleetAPI.class);
        when(playerFleet.getBattle()).thenReturn(battle);
        when(battle.getNonPlayerCombined()).thenReturn(nonPlayerCombined);
        when(battle.wasFleetDefeated(playerFleet, nonPlayerCombined)).thenReturn(true);

        new ShipLevelScript().advance(0.01f);

        ShipSkillData data = ShipSkillDataManager.get("ship-a");
        assertEquals(20f, data.getXp());
    }

    @Test
    void onlyAwardsXpOnceWhileCombatOverStaysTrueAcrossAdvances() {
        globalMock.when(Global::getCombatEngine).thenReturn(engine);
        when(engine.isSimulation()).thenReturn(false);
        when(engine.isCombatOver()).thenReturn(true);
        FleetMemberAPI member = mockMember("ship-a");
        when(fleetData.getMembersListCopy()).thenReturn(List.of(member));

        ShipLevelScript script = new ShipLevelScript();
        script.advance(0.01f);
        script.advance(0.01f);

        ShipSkillData data = ShipSkillDataManager.get("ship-a");
        assertEquals(40f, data.getXp());
    }

    @Test
    void resetsAfterCombatEndsSoANewCombatCanAwardXpAgain() {
        globalMock.when(Global::getCombatEngine).thenReturn(engine);
        when(engine.isSimulation()).thenReturn(false);
        when(engine.isCombatOver()).thenReturn(true);
        FleetMemberAPI member = mockMember("ship-a");
        when(fleetData.getMembersListCopy()).thenReturn(List.of(member));

        ShipLevelScript script = new ShipLevelScript();
        script.advance(0.01f);

        globalMock.when(Global::getCombatEngine).thenReturn(null);
        script.advance(0.01f);

        CombatEngineAPI secondEngine = mock(CombatEngineAPI.class);
        when(secondEngine.isSimulation()).thenReturn(false);
        when(secondEngine.isCombatOver()).thenReturn(true);
        globalMock.when(Global::getCombatEngine).thenReturn(secondEngine);
        script.advance(0.01f);

        ShipSkillData data = ShipSkillDataManager.get("ship-a");
        // Two 40-XP awards total 80, which crosses the default 60-XP level-1 threshold: the ship
        // levels up once, consuming 60 XP, leaving 20 remaining.
        assertEquals(1, data.getLevel());
        assertEquals(20f, data.getXp());
        verify(fleetData, times(2)).getMembersListCopy();
    }
}
