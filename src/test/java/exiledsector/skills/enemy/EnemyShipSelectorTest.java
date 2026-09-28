package exiledsector.skills.enemy;

import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import lunalib.lunaSettings.LunaSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnemyShipSelectorTest {

    private MockedStatic<LunaSettings> lunaSettingsMock;

    @BeforeEach
    void setUp() {
        lunaSettingsMock = Mockito.mockStatic(LunaSettings.class, invocation -> null);
    }

    @AfterEach
    void tearDown() {
        lunaSettingsMock.close();
    }

    private static Random rolling(float value) {
        return new Random() {
            @Override
            public float nextFloat() {
                return value;
            }
        };
    }

    private static FleetMemberAPI member(boolean officered, boolean flagship) {
        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getVariant()).thenReturn(mock(ShipVariantAPI.class));
        PersonAPI captain = mock(PersonAPI.class);
        when(captain.isDefault()).thenReturn(!officered);
        when(member.getCaptain()).thenReturn(captain);
        when(member.isFlagship()).thenReturn(flagship);
        return member;
    }

    private void configure(String fieldId, Boolean value) {
        lunaSettingsMock.when(() -> LunaSettings.getBoolean("exiledSector", fieldId)).thenReturn(value);
    }

    @Test
    void ordinaryCombatShipsAreCandidates() {
        assertTrue(EnemyShipSelector.isCandidate(member(false, false)));
    }

    @Test
    void fightersStationsCiviliansAlliesMothballedAndVariantlessShipsAreNotCandidates() {
        FleetMemberAPI fighter = member(false, false);
        when(fighter.isFighterWing()).thenReturn(true);
        FleetMemberAPI station = member(false, false);
        when(station.isStation()).thenReturn(true);
        FleetMemberAPI civilian = member(false, false);
        when(civilian.isCivilian()).thenReturn(true);
        FleetMemberAPI ally = member(false, false);
        when(ally.isAlly()).thenReturn(true);
        FleetMemberAPI mothballed = member(false, false);
        when(mothballed.isMothballed()).thenReturn(true);
        FleetMemberAPI variantless = member(false, false);
        when(variantless.getVariant()).thenReturn(null);

        assertFalse(EnemyShipSelector.isCandidate(fighter));
        assertFalse(EnemyShipSelector.isCandidate(station));
        assertFalse(EnemyShipSelector.isCandidate(civilian));
        assertFalse(EnemyShipSelector.isCandidate(ally));
        assertFalse(EnemyShipSelector.isCandidate(mothballed));
        assertFalse(EnemyShipSelector.isCandidate(variantless));
        assertFalse(EnemyShipSelector.isCandidate(null));
    }

    @Test
    void officeredShipsAndTheFlagshipAreAlwaysChosenByDefault() {
        assertTrue(EnemyShipSelector.isChosen(member(true, false), rolling(0.99f)));
        assertTrue(EnemyShipSelector.isChosen(member(false, true), rolling(0.99f)));
    }

    @Test
    void otherShipsAreChosenByTheChanceRoll() {
        assertTrue(EnemyShipSelector.isChosen(member(false, false), rolling(0.29f)));
        assertFalse(EnemyShipSelector.isChosen(member(false, false), rolling(0.30f)));
    }

    @Test
    void turningTheTogglesOffLeavesOfficeredShipsAndTheFlagshipToTheChanceRoll() {
        configure(EnemyTreeConfig.OFFICERED_SHIPS_FIELD_ID, false);
        configure(EnemyTreeConfig.FLAGSHIP_FIELD_ID, false);

        assertFalse(EnemyShipSelector.isChosen(member(true, false), rolling(0.99f)));
        assertFalse(EnemyShipSelector.isChosen(member(false, true), rolling(0.99f)));
        assertTrue(EnemyShipSelector.isChosen(member(true, true), rolling(0.1f)));
    }

    @Test
    void aShipWithoutACaptainIsNotOfficered() {
        FleetMemberAPI member = member(false, false);
        when(member.getCaptain()).thenReturn(null);

        assertFalse(EnemyShipSelector.isChosen(member, rolling(0.99f)));
    }
}
