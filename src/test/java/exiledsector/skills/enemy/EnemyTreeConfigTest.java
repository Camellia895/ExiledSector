package exiledsector.skills.enemy;

import lunalib.lunaSettings.LunaSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnemyTreeConfigTest {

    private MockedStatic<LunaSettings> lunaSettingsMock;

    @BeforeEach
    void setUp() {
        lunaSettingsMock = Mockito.mockStatic(LunaSettings.class, invocation -> null);
    }

    @AfterEach
    void tearDown() {
        lunaSettingsMock.close();
    }

    @Test
    void fallsBackToTheDefaultsWhenNothingIsConfigured() {
        assertTrue(EnemyTreeConfig.isEnabled());
        assertTrue(EnemyTreeConfig.levelsOfficeredShips());
        assertTrue(EnemyTreeConfig.levelsFlagship());
        assertEquals(0.3f, EnemyTreeConfig.otherShipChance(), 0.0001f);
    }

    @Test
    void usesTheConfiguredSettings() {
        lunaSettingsMock.when(() -> LunaSettings.getBoolean("exiledSector", EnemyTreeConfig.ENABLED_FIELD_ID)).thenReturn(false);
        lunaSettingsMock.when(() -> LunaSettings.getBoolean("exiledSector", EnemyTreeConfig.OFFICERED_SHIPS_FIELD_ID)).thenReturn(false);
        lunaSettingsMock.when(() -> LunaSettings.getBoolean("exiledSector", EnemyTreeConfig.FLAGSHIP_FIELD_ID)).thenReturn(false);
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", EnemyTreeConfig.OTHER_SHIP_CHANCE_FIELD_ID)).thenReturn(75);

        assertFalse(EnemyTreeConfig.isEnabled());
        assertFalse(EnemyTreeConfig.levelsOfficeredShips());
        assertFalse(EnemyTreeConfig.levelsFlagship());
        assertEquals(0.75f, EnemyTreeConfig.otherShipChance(), 0.0001f);
    }

    @Test
    void theOtherShipChanceIsClampedToAValidProbability() {
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", EnemyTreeConfig.OTHER_SHIP_CHANCE_FIELD_ID)).thenReturn(250);
        assertEquals(1f, EnemyTreeConfig.otherShipChance(), 0.0001f);

        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", EnemyTreeConfig.OTHER_SHIP_CHANCE_FIELD_ID)).thenReturn(-10);
        assertEquals(0f, EnemyTreeConfig.otherShipChance(), 0.0001f);
    }
}
