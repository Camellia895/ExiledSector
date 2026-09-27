package exiledsector.effects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.CombatDamageData;
import com.fs.starfarer.api.campaign.EngagementResultForFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
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

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CombatXpListenerTest {

    private MockedStatic<Global> globalMock;
    private MockedStatic<LunaSettings> lunaSettingsMock;
    private FleetDataAPI fleetData;
    private TextPanelAPI textPanel;

    @BeforeEach
    void setUp() {
        Map<String, Object> persistentData = new HashMap<>();
        SectorAPI sector = mock(SectorAPI.class);
        when(sector.getPersistentData()).thenReturn(persistentData);
        CampaignFleetAPI playerFleet = mock(CampaignFleetAPI.class);
        fleetData = mock(FleetDataAPI.class);
        when(sector.getPlayerFleet()).thenReturn(playerFleet);
        when(playerFleet.getFleetData()).thenReturn(fleetData);
        when(fleetData.getMembersListCopy()).thenReturn(List.of());

        CampaignUIAPI campaignUi = mock(CampaignUIAPI.class);
        InteractionDialogAPI dialog = mock(InteractionDialogAPI.class);
        textPanel = mock(TextPanelAPI.class);
        when(sector.getCampaignUI()).thenReturn(campaignUi);
        when(campaignUi.getCurrentInteractionDialog()).thenReturn(dialog);
        when(dialog.getTextPanel()).thenReturn(textPanel);

        lunaSettingsMock = Mockito.mockStatic(LunaSettings.class);
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", ShipLevelConfig.MAX_LEVEL_FIELD_ID)).thenReturn(null);
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", ShipLevelConfig.XP_BASE_FIELD_ID)).thenReturn(null);
        lunaSettingsMock.when(() -> LunaSettings.getFloat("exiledSector", ShipLevelConfig.XP_GROWTH_FIELD_ID)).thenReturn(null);
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", ShipLevelConfig.XP_GROWTH_CUTOFF_LEVEL_FIELD_ID)).thenReturn(null);
        lunaSettingsMock.when(() -> LunaSettings.getFloat("exiledSector", ShipLevelConfig.XP_PER_DEPLOYMENT_POINT_FIELD_ID)).thenReturn(null);
        lunaSettingsMock.when(() -> LunaSettings.getFloat("exiledSector", ShipLevelConfig.XP_LOSS_MULTIPLIER_FIELD_ID)).thenReturn(null);
        globalMock = Mockito.mockStatic(Global.class);
        globalMock.when(Global::getSector).thenReturn(sector);
        SettingsAPI settings = mock(SettingsAPI.class);
        when(settings.getColor("buttonShortcut")).thenReturn(Color.YELLOW);
        when(settings.getColor("textFriendColor")).thenReturn(Color.GREEN);
        globalMock.when(Global::getSettings).thenReturn(settings);
        FactionAPI playerFaction = mock(FactionAPI.class);
        when(playerFaction.getBaseUIColor()).thenReturn(Color.CYAN);
        when(sector.getPlayerFaction()).thenReturn(playerFaction);

        SkillTree.getAllNodes().clear();
    }

    @AfterEach
    void tearDown() {
        globalMock.close();
        lunaSettingsMock.close();
        SkillTree.getAllNodes().clear();
    }

    private static FleetMemberAPI member(String id) {
        FleetMemberAPI member = mock(FleetMemberAPI.class);
        ShipHullSpecAPI hullSpec = mock(ShipHullSpecAPI.class);
        when(member.getId()).thenReturn(id);
        when(member.getShipName()).thenReturn("ISS " + id);
        when(member.getHullSpec()).thenReturn(hullSpec);
        when(hullSpec.getHullSize()).thenReturn(HullSize.FRIGATE);
        when(hullSpec.getHullNameWithDashClass()).thenReturn("Wolf-class");
        return member;
    }

    private static EngagementResultAPI engagement(boolean playerWon, float enemyDpDestroyed) {
        EngagementResultAPI result = mock(EngagementResultAPI.class);
        EngagementResultForFleetAPI enemy = mock(EngagementResultForFleetAPI.class);
        FleetMemberAPI destroyed = mock(FleetMemberAPI.class);
        when(destroyed.getDeploymentPointsCost()).thenReturn(enemyDpDestroyed);
        when(enemy.getDestroyed()).thenReturn(List.of(destroyed));
        when(result.didPlayerWin()).thenReturn(playerWon);
        if (playerWon) {
            when(result.getLoserResult()).thenReturn(enemy);
        } else {
            when(result.getWinnerResult()).thenReturn(enemy);
        }
        when(result.getLastCombatDamageData()).thenReturn(mock(CombatDamageData.class));
        return result;
    }

    @Test
    void awardsEveryFleetMemberXpForEnemyDeploymentPointsDestroyed() {
        List<FleetMemberAPI> members = List.of(member("ship-a"), member("ship-b"));
        when(fleetData.getMembersListCopy()).thenReturn(members);

        new CombatXpListener().reportPlayerEngagement(engagement(true, 40f));

        assertEquals(40f, ShipSkillDataManager.get("ship-a").getXp());
        assertEquals(40f, ShipSkillDataManager.get("ship-b").getXp());
    }

    @Test
    void appliesTheLossMultiplierWhenThePlayerLost() {
        List<FleetMemberAPI> members = List.of(member("ship-a"));
        when(fleetData.getMembersListCopy()).thenReturn(members);

        new CombatXpListener().reportPlayerEngagement(engagement(false, 40f));

        assertEquals(40f * ShipLevelConfig.DEFAULT_XP_LOSS_MULTIPLIER, ShipSkillDataManager.get("ship-a").getXp());
        verify(textPanel).addPara(contains("reduced because the battle was lost"), eq(Color.YELLOW), any(String[].class));
    }

    @Test
    void ignoresAutoresolvedEngagementsWithoutCombatData() {
        List<FleetMemberAPI> members = List.of(member("ship-a"));
        when(fleetData.getMembersListCopy()).thenReturn(members);
        EngagementResultAPI autoresolved = engagement(true, 40f);
        when(autoresolved.getLastCombatDamageData()).thenReturn(null);

        new CombatXpListener().reportPlayerEngagement(autoresolved);

        assertEquals(0f, ShipSkillDataManager.get("ship-a").getXp());
        verify(textPanel, never()).addPara(anyString(), any(Color.class));
    }

    @Test
    void writesTheXpEarnedAndAnyLevelUpsToThePostBattleReport() {
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", ShipLevelConfig.XP_BASE_FIELD_ID)).thenReturn(30);
        List<FleetMemberAPI> members = List.of(member("ship-a"));
        when(fleetData.getMembersListCopy()).thenReturn(members);

        new CombatXpListener().reportPlayerEngagement(engagement(true, 40f));

        ShipSkillData data = ShipSkillDataManager.get("ship-a");
        assertTrue(data.getLevel() >= 1);
        verify(textPanel).addPara("ExiledSector skill tree", Color.CYAN);
        verify(textPanel).addPara(contains("earned %s XP"), eq(Color.YELLOW), eq("40"), eq("40"));
        verify(textPanel).addPara(contains("ISS ship-a (Wolf-class) reached level"), eq(Color.GREEN));
    }
}
