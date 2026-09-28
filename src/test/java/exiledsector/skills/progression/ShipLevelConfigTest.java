package exiledsector.skills.progression;

import lunalib.lunaSettings.LunaSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShipLevelConfigTest {

    private MockedStatic<LunaSettings> lunaSettingsMock;

    @BeforeEach
    void setUp() {
        lunaSettingsMock = Mockito.mockStatic(LunaSettings.class);
    }

    @AfterEach
    void tearDown() {
        lunaSettingsMock.close();
    }

    @Test
    void fallsBackToTheDocumentedDefaultsWhenNoSettingIsConfigured() {
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", ShipLevelConfig.MAX_LEVEL_FIELD_ID)).thenReturn(null);
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", ShipLevelConfig.XP_BASE_FIELD_ID)).thenReturn(null);
        lunaSettingsMock.when(() -> LunaSettings.getFloat("exiledSector", ShipLevelConfig.XP_GROWTH_FIELD_ID)).thenReturn(null);
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", ShipLevelConfig.XP_GROWTH_CUTOFF_LEVEL_FIELD_ID)).thenReturn(null);
        lunaSettingsMock.when(() -> LunaSettings.getFloat("exiledSector", ShipLevelConfig.XP_PER_DEPLOYMENT_POINT_FIELD_ID)).thenReturn(null);
        lunaSettingsMock.when(() -> LunaSettings.getFloat("exiledSector", ShipLevelConfig.XP_LOSS_MULTIPLIER_FIELD_ID)).thenReturn(null);
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", ShipLevelConfig.MAX_ALLOCATED_NODES_FIELD_ID)).thenReturn(null);

        assertEquals(ShipLevelConfig.DEFAULT_MAX_LEVEL, ShipLevelConfig.maxLevel());
        assertEquals(ShipLevelConfig.DEFAULT_XP_BASE, ShipLevelConfig.xpBase());
        assertEquals(ShipLevelConfig.DEFAULT_XP_GROWTH, ShipLevelConfig.xpGrowth());
        assertEquals(ShipLevelConfig.DEFAULT_XP_GROWTH_CUTOFF_LEVEL, ShipLevelConfig.xpGrowthCutoffLevel());
        assertEquals(ShipLevelConfig.DEFAULT_XP_PER_DEPLOYMENT_POINT, ShipLevelConfig.xpPerDeploymentPoint());
        assertEquals(ShipLevelConfig.DEFAULT_XP_LOSS_MULTIPLIER, ShipLevelConfig.xpLossMultiplier());
        assertEquals(ShipLevelConfig.DEFAULT_MAX_ALLOCATED_NODES, ShipLevelConfig.maxAllocatedNodes());
    }

    @Test
    void usesTheConfiguredLunaLibSettingWhenPresent() {
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", ShipLevelConfig.MAX_LEVEL_FIELD_ID)).thenReturn(80);
        lunaSettingsMock.when(() -> LunaSettings.getFloat("exiledSector", ShipLevelConfig.XP_GROWTH_FIELD_ID)).thenReturn(1.5f);
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", ShipLevelConfig.XP_GROWTH_CUTOFF_LEVEL_FIELD_ID)).thenReturn(30);
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", ShipLevelConfig.MAX_ALLOCATED_NODES_FIELD_ID)).thenReturn(30);

        assertEquals(80, ShipLevelConfig.maxLevel());
        assertEquals(1.5f, ShipLevelConfig.xpGrowth());
        assertEquals(30, ShipLevelConfig.xpGrowthCutoffLevel());
        assertEquals(30, ShipLevelConfig.maxAllocatedNodes());
    }
}
