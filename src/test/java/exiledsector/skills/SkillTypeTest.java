package exiledsector.skills;

import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import exiledsector.skills.skilleffect.SkillEffect;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SkillTypeTest {

    private static SkillType type(List<SkillTypeEffect> effects, List<HullSizeSkillEffect> hullSizeEffects) {
        return new SkillType.Builder("id", "Name", "a.png", SkillTier.SMALL)
                .effects(effects)
                .hullSizeEffects(hullSizeEffects)
                .build();
    }

    @Test
    void visitsEachRegularEffectWithItsMagnitude() {
        SkillEffect a = mock(SkillEffect.class);
        SkillEffect b = mock(SkillEffect.class);
        SkillType type = type(List.of(new SkillTypeEffect(a, 1f), new SkillTypeEffect(b, 2f)), List.of());

        List<Object[]> seen = new ArrayList<>();
        type.forEachEffect(HullSize.FRIGATE, (effect, magnitude) -> seen.add(new Object[]{effect, magnitude}));

        assertEquals(2, seen.size());
        assertEquals(a, seen.get(0)[0]);
        assertEquals(1f, seen.get(0)[1]);
        assertEquals(b, seen.get(1)[0]);
        assertEquals(2f, seen.get(1)[1]);
    }

    @Test
    void visitsEachHullSizeEffectResolvedForTheGivenHullSize() {
        SkillEffect effect = mock(SkillEffect.class);
        SkillType type = type(List.of(), List.of(new HullSizeSkillEffect(effect, 1f, 2f, 3f, 4f)));

        List<Float> magnitudes = new ArrayList<>();
        type.forEachEffect(HullSize.CRUISER, (e, magnitude) -> magnitudes.add(magnitude));

        assertEquals(List.of(3f), magnitudes);
    }

    @Test
    void visitsRegularEffectsBeforeHullSizeEffects() {
        SkillEffect regular = mock(SkillEffect.class);
        SkillEffect sized = mock(SkillEffect.class);
        SkillType type = type(List.of(new SkillTypeEffect(regular, 1f)),
                List.of(new HullSizeSkillEffect(sized, 1f, 1f, 1f, 1f)));

        List<SkillEffect> order = new ArrayList<>();
        type.forEachEffect(HullSize.FRIGATE, (effect, magnitude) -> order.add(effect));

        assertEquals(List.of(regular, sized), order);
    }

    @Test
    void installedHullModsAreAlsoExclusiveWithoutBeingListedTwice() {
        SkillType type = new SkillType.Builder("t", "T", "a.png", SkillTier.NOTABLE)
                .exclusiveHullModIds(List.of("frontshield", "militarized_subsystems"))
                .installedHullModIds(List.of("militarized_subsystems"))
                .build();

        assertEquals(List.of("frontshield", "militarized_subsystems"), type.getExclusiveHullModIds());
        assertEquals(List.of("militarized_subsystems"), type.getInstalledHullModIds());
    }

    @Test
    void tagsDefaultToAnEmptyListWhenNotSetOrSetToNull() {
        SkillType unset = new SkillType.Builder("t", "T", "a.png", SkillTier.SMALL).build();
        SkillType nulled = new SkillType.Builder("t", "T", "a.png", SkillTier.SMALL).tags(null).build();

        assertEquals(List.of(), unset.getTags());
        assertEquals(List.of(), nulled.getTags());
    }

    @Test
    void keepsTagsInTheGivenOrder() {
        SkillType type = new SkillType.Builder("t", "T", "a.png", SkillTier.SMALL)
                .tags(List.of("shield", "req_shields"))
                .build();

        assertEquals(List.of("shield", "req_shields"), type.getTags());
    }

    @Test
    void doesNothingWhenBothListsAreEmpty() {
        SkillType type = type(List.of(), List.of());

        List<Object> seen = new ArrayList<>();
        type.forEachEffect(HullSize.FRIGATE, (effect, magnitude) -> seen.add(effect));

        assertTrue(seen.isEmpty());
    }

    @Test
    void theEquivalentHullmodIsTheVanillaHullmodWhenSet() {
        SkillType type = new SkillType.Builder("t", "T", "a.png", SkillTier.KEYSTONE)
                .vanillaHullModId("ballistic_rangefinder")
                .exclusiveHullModIds(List.of("other"))
                .build();

        assertEquals("ballistic_rangefinder", type.getEquivalentHullModId());
    }

    @Test
    void theEquivalentHullmodIsOtherwiseTheFirstExclusiveHullmod() {
        SkillType type = new SkillType.Builder("t", "T", "a.png", SkillTier.NOTABLE)
                .exclusiveHullModIds(List.of("dedicated_targeting_core", "targetingunit"))
                .build();

        assertEquals("dedicated_targeting_core", type.getEquivalentHullModId());
    }

    @Test
    void aTypeWithoutHullmodLinksHasNoEquivalentHullmod() {
        SkillType type = new SkillType.Builder("t", "T", "a.png", SkillTier.SMALL)
                .installedHullModIds(List.of("militarized_subsystems"))
                .build();

        assertNull(type.getEquivalentHullModId());
    }
}
