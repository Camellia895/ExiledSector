package exiledsector.skills;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkillNodeTest {

    @Test
    void descriptionDelegatesToTheTypesEffect() {
        SkillType type = new SkillType("hull", "Hull", "graphics/hullmods/reinforced_bulkheads.png", 2, 500,
                List.of(new SkillTypeEffect(SkillEffect.HULL, 10f)), SkillTier.SMALL, null, null, null);
        SkillNode node = new SkillNode("hull_1", type, List.of(), 0f, 0f);

        assertEquals("Increases hull points by 10%.", node.getDescription());
    }

    @Test
    void descriptionJoinsMultipleEffects() {
        SkillType type = new SkillType("heavyarmor", "Heavy Armor", "graphics/icons/notable_hullmods/heavy_armor.png", 4, 2000,
                List.of(new SkillTypeEffect(SkillEffect.ARMOR, 15f), new SkillTypeEffect(SkillEffect.HULL, 5f)),
                SkillTier.NOTABLE, null, null, null);
        SkillNode node = new SkillNode("heavyarmor_1", type, List.of(), 0f, 0f);

        assertEquals("Increases armor rating by 15%. Increases hull points by 5%.", node.getDescription());
    }

    @Test
    void descriptionIsEmptyForATypeWithNoEffectsAndNoOverride() {
        SkillType type = new SkillType("cosmetic", "Cosmetic", "graphics/hullmods/flux_coil_adjunct.png", 2, 500,
                List.of(), SkillTier.SMALL, null, null, null);
        SkillNode node = new SkillNode("cosmetic_1", type, List.of(), 0f, 0f);

        assertEquals("", node.getDescription());
    }

    @Test
    void descriptionOverrideTakesPriorityOverEffects() {
        SkillType type = new SkillType("hull", "Hull", "graphics/hullmods/reinforced_bulkheads.png", 2, 500,
                List.of(new SkillTypeEffect(SkillEffect.HULL, 10f)), SkillTier.SMALL, null, "Custom flavor text.", null);
        SkillNode node = new SkillNode("hull_1", type, List.of(), 0f, 0f);

        assertEquals("Custom flavor text.", node.getDescription());
    }

    @Test
    void descriptionIsEmptyForAVanillaPassthroughTypeWithNoEffectsYet() {
        SkillType type = new SkillType("escort_package", "Escort Package", "graphics/icons/notable_hullmods/escort_package.png", 4, 2000,
                List.of(), SkillTier.NOTABLE, "escort_package", null, "Needs a real mechanic");
        SkillNode node = new SkillNode("escort_package_1", type, List.of(), 0f, 0f);

        assertEquals("", node.getDescription());
    }
}
