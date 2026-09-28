package exiledsector.ui;

import exiledsector.skills.enemy.EnemyLevelTable;
import exiledsector.skills.enemy.EnemyTreeConfig;
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
    void registersAMinAndMaxNodeFieldForEveryPlayerLevelOnTheEnemyScalingTab() {
        for (int level = EnemyLevelTable.MIN_PLAYER_LEVEL; level <= EnemyLevelTable.MAX_PLAYER_LEVEL; level++) {
            int current = level;
            settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.addInt(eq("exiledSector"),
                    eq(EnemyLevelTable.minNodesFieldId(current)), anyString(), anyString(),
                    eq(EnemyLevelTable.defaultMinNodes(current)), eq(EnemyLevelTable.MIN_NODES), eq(EnemyLevelTable.MAX_NODES),
                    eq(ExiledSectorSettings.ENEMY_SCALING_TAB)));
            settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.addInt(eq("exiledSector"),
                    eq(EnemyLevelTable.maxNodesFieldId(current)), anyString(), anyString(),
                    eq(EnemyLevelTable.defaultMaxNodes(current)), eq(EnemyLevelTable.MIN_NODES), eq(EnemyLevelTable.MAX_NODES),
                    eq(ExiledSectorSettings.ENEMY_SCALING_TAB)));
        }
    }

    @Test
    void registersTheEnemyTreeTogglesOnTheEnemyScalingTab() {
        settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.addBoolean(eq("exiledSector"),
                eq(EnemyTreeConfig.ENABLED_FIELD_ID), anyString(), anyString(), eq(true), eq(ExiledSectorSettings.ENEMY_SCALING_TAB)));
        settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.addBoolean(eq("exiledSector"),
                eq(EnemyTreeConfig.OFFICERED_SHIPS_FIELD_ID), anyString(), anyString(), eq(true), eq(ExiledSectorSettings.ENEMY_SCALING_TAB)));
        settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.addBoolean(eq("exiledSector"),
                eq(EnemyTreeConfig.FLAGSHIP_FIELD_ID), anyString(), anyString(), eq(true), eq(ExiledSectorSettings.ENEMY_SCALING_TAB)));
        settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.addInt(eq("exiledSector"),
                eq(EnemyTreeConfig.OTHER_SHIP_CHANCE_FIELD_ID), anyString(), anyString(), eq(30), eq(0), eq(100),
                eq(ExiledSectorSettings.ENEMY_SCALING_TAB)));
    }

    @Test
    void refreshesAfterRegisteringEverything() {
        settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.refresh("exiledSector"));
    }
}
