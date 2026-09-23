package exiledsector.effects;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class SkillConflictWarningsTest {

    @Test
    void recordingForTwoDifferentVariantsKeepsBothEntries() {
        ShipVariantAPI a = mock(ShipVariantAPI.class);
        ShipVariantAPI b = mock(ShipVariantAPI.class);

        SkillConflictWarnings.record(a, "adaptiveshields", "Shield Conversion - Front");
        SkillConflictWarnings.record(b, "armoredcladding", "Reinforced Hull");

        assertEquals("adaptiveshields", SkillConflictWarnings.get(a).removedHullModId);
        assertEquals("armoredcladding", SkillConflictWarnings.get(b).removedHullModId);
    }

    @Test
    void clearRemovesOnlyTheGivenVariantsEntry() {
        ShipVariantAPI a = mock(ShipVariantAPI.class);
        ShipVariantAPI b = mock(ShipVariantAPI.class);
        SkillConflictWarnings.record(a, "adaptiveshields", "Shield Conversion - Front");
        SkillConflictWarnings.record(b, "armoredcladding", "Reinforced Hull");

        SkillConflictWarnings.clear(a);

        assertNull(SkillConflictWarnings.get(a));
        assertEquals("armoredcladding", SkillConflictWarnings.get(b).removedHullModId);
    }

    @Test
    void getReturnsNullForAVariantThatWasNeverRecorded() {
        ShipVariantAPI variant = mock(ShipVariantAPI.class);

        assertNull(SkillConflictWarnings.get(variant));
    }
}
