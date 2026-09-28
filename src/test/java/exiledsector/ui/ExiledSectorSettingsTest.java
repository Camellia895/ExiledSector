package exiledsector.ui;

import exiledsector.skills.npc.NpcLevelTable;
import exiledsector.skills.npc.NpcTreeConfig;
import exiledsector.ui.inspect.NpcInspectConfig;
import lunalib.lunaSettings.LunaSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

class ExiledSectorSettingsTest {

    private MockedStatic<LunaSettings.SettingsCreator> settingsCreatorMock;

    @BeforeEach
    void setUp() {
        settingsCreatorMock = Mockito.mockStatic(LunaSettings.SettingsCreator.class);
        ExiledSectorSettings.register();
    }

    @AfterEach
    void tearDown() {
        settingsCreatorMock.close();
    }

    @Test
    void registersAMinAndMaxNodeFieldForEveryPlayerLevelOnTheNpcScalingTab() {
        for (int level = NpcLevelTable.MIN_PLAYER_LEVEL; level <= NpcLevelTable.MAX_PLAYER_LEVEL; level++) {
            int current = level;
            settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.addInt(eq("exiledSector"),
                    eq(NpcLevelTable.minNodesFieldId(current)), anyString(), anyString(),
                    eq(NpcLevelTable.defaultMinNodes(current)), eq(NpcLevelTable.MIN_NODES), eq(NpcLevelTable.MAX_NODES),
                    eq(ExiledSectorSettings.NPC_SCALING_TAB)));
            settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.addInt(eq("exiledSector"),
                    eq(NpcLevelTable.maxNodesFieldId(current)), anyString(), anyString(),
                    eq(NpcLevelTable.defaultMaxNodes(current)), eq(NpcLevelTable.MIN_NODES), eq(NpcLevelTable.MAX_NODES),
                    eq(ExiledSectorSettings.NPC_SCALING_TAB)));
        }
    }

    @Test
    void registersTheNpcTreeTogglesOnTheNpcScalingTab() {
        settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.addBoolean(eq("exiledSector"),
                eq(NpcTreeConfig.ENABLED_FIELD_ID), anyString(), anyString(), eq(true), eq(ExiledSectorSettings.NPC_SCALING_TAB)));
        settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.addBoolean(eq("exiledSector"),
                eq(NpcTreeConfig.OFFICERED_SHIPS_FIELD_ID), anyString(), anyString(), eq(true), eq(ExiledSectorSettings.NPC_SCALING_TAB)));
        settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.addBoolean(eq("exiledSector"),
                eq(NpcTreeConfig.FLAGSHIP_FIELD_ID), anyString(), anyString(), eq(true), eq(ExiledSectorSettings.NPC_SCALING_TAB)));
        settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.addInt(eq("exiledSector"),
                eq(NpcTreeConfig.OTHER_SHIP_CHANCE_FIELD_ID), anyString(), anyString(), eq(30), eq(0), eq(100),
                eq(ExiledSectorSettings.NPC_SCALING_TAB)));
    }

    @Test
    void refreshesAfterRegisteringEverything() {
        settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.refresh("exiledSector"));
    }

    @Test
    void registersTheInspectKeybindOnTheNpcScalingTab() {
        settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.addKeybind(eq("exiledSector"),
                eq(NpcInspectConfig.KEYBIND_FIELD_ID), anyString(), anyString(), eq(NpcInspectConfig.DEFAULT_KEY),
                eq(ExiledSectorSettings.NPC_SCALING_TAB)));
    }
}
