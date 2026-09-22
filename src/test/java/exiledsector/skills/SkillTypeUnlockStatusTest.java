package exiledsector.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.util.DynamicStatsAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class SkillTypeUnlockStatusTest {

    private MockedStatic<Global> globalMock;
    private SectorAPI sector;
    private CharacterDataAPI playerCharacter;

    @BeforeEach
    void setUp() {
        sector = mock(SectorAPI.class);
        playerCharacter = mock(CharacterDataAPI.class);
        when(sector.getCharacterData()).thenReturn(playerCharacter);

        globalMock = Mockito.mockStatic(Global.class);
        globalMock.when(Global::getSector).thenReturn(sector);
    }

    @AfterEach
    void tearDown() {
        globalMock.close();
    }

    private static SkillType typeWithLock(String lockedUntilHullMod) {
        return new SkillType("t", "T", "a.png", List.of(), List.of(), SkillTier.NOTABLE,
                null, null, null, List.of(), List.of(), List.of(), lockedUntilHullMod);
    }

    @Test
    void nodeWithNoLockedUntilHullModIsNeverLocked() {
        SkillType type = typeWithLock(null);

        assertFalse(SkillTypeUnlockStatus.isLocked(type));
        globalMock.verify(Global::getSector, never());
    }

    @Test
    void isLockedWhenPlayerDoesNotKnowTheHullMod() {
        SkillType type = typeWithLock("escort_package");
        when(playerCharacter.knowsHullMod("escort_package")).thenReturn(false);

        assertTrue(SkillTypeUnlockStatus.isLocked(type));
    }

    @Test
    void isUnlockedWhenPlayerKnowsTheHullMod() {
        SkillType type = typeWithLock("escort_package");
        when(playerCharacter.knowsHullMod("escort_package")).thenReturn(true);

        assertFalse(SkillTypeUnlockStatus.isLocked(type));
    }

    @Test
    void failsClosedWhenLookupThrows() {
        SkillType type = typeWithLock("escort_package");
        when(sector.getCharacterData()).thenThrow(new RuntimeException("boom"));

        assertTrue(SkillTypeUnlockStatus.isLocked(type));
    }

    private void stubNeuralLinkStatFlag(float modifiedValue) {
        PersonAPI person = mock(PersonAPI.class);
        MutableCharacterStatsAPI stats = mock(MutableCharacterStatsAPI.class);
        DynamicStatsAPI dynamic = mock(DynamicStatsAPI.class);
        StatBonus mod = mock(StatBonus.class);
        when(playerCharacter.getPerson()).thenReturn(person);
        when(person.getStats()).thenReturn(stats);
        when(stats.getDynamic()).thenReturn(dynamic);
        when(dynamic.getMod("has_neural_link")).thenReturn(mod);
        when(mod.getFlatBonus()).thenReturn(modifiedValue);
    }

    @Test
    void neuralInterfaceNodeIsUnlockedByTheNeuralLinkStatFlagEvenWithoutKnowingTheHullMod() {
        SkillType type = typeWithLock("neural_interface");
        when(playerCharacter.knowsHullMod("neural_interface")).thenReturn(false);
        stubNeuralLinkStatFlag(1f);

        assertFalse(SkillTypeUnlockStatus.isLocked(type));
    }

    @Test
    void neuralInterfaceNodeStaysLockedWithoutTheHullModOrTheStatFlag() {
        SkillType type = typeWithLock("neural_interface");
        when(playerCharacter.knowsHullMod("neural_interface")).thenReturn(false);
        stubNeuralLinkStatFlag(0f);

        assertTrue(SkillTypeUnlockStatus.isLocked(type));
    }

    @Test
    void neuralLinkStatFlagDoesNotUnlockAnUnrelatedHullMod() {
        SkillType type = typeWithLock("escort_package");
        when(playerCharacter.knowsHullMod("escort_package")).thenReturn(false);
        stubNeuralLinkStatFlag(1f);

        assertTrue(SkillTypeUnlockStatus.isLocked(type));
    }
}
