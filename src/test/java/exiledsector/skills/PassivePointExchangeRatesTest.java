package exiledsector.skills;

import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import lunalib.lunaSettings.LunaSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PassivePointExchangeRatesTest {

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
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", "exiledSector_opRatioFrigate")).thenReturn(null);
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", "exiledSector_opRatioDestroyer")).thenReturn(null);
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", "exiledSector_opRatioCruiser")).thenReturn(null);
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", "exiledSector_opRatioCapital")).thenReturn(null);
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", "exiledSector_opRatioUndefined")).thenReturn(null);

        assertEquals(1, PassivePointExchangeRates.opCostPerPassivePoint(HullSize.FRIGATE));
        assertEquals(2, PassivePointExchangeRates.opCostPerPassivePoint(HullSize.DESTROYER));
        assertEquals(3, PassivePointExchangeRates.opCostPerPassivePoint(HullSize.CRUISER));
        assertEquals(4, PassivePointExchangeRates.opCostPerPassivePoint(HullSize.CAPITAL_SHIP));
        assertEquals(4, PassivePointExchangeRates.opCostPerPassivePoint(HullSize.FIGHTER));
        assertEquals(4, PassivePointExchangeRates.opCostPerPassivePoint((HullSize) null));
    }

    @Test
    void usesTheConfiguredLunaLibSettingWhenPresent() {
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", "exiledSector_opRatioFrigate")).thenReturn(7);
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", "exiledSector_opRatioCapital")).thenReturn(20);

        assertEquals(7, PassivePointExchangeRates.opCostPerPassivePoint(HullSize.FRIGATE));
        assertEquals(20, PassivePointExchangeRates.opCostPerPassivePoint(HullSize.CAPITAL_SHIP));
    }

    @Test
    void resolvesTheRateFromAHullSpecsHullSize() {
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", "exiledSector_opRatioCruiser")).thenReturn(null);

        ShipHullSpecAPI hull = mock(ShipHullSpecAPI.class);
        when(hull.getHullSize()).thenReturn(HullSize.CRUISER);

        assertEquals(3, PassivePointExchangeRates.opCostPerPassivePoint(hull));
    }
}
