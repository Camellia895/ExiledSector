package exiledsector.skills;

import exiledsector.skills.skilleffect.DefenseSkillEffect;
import exiledsector.skills.skilleffect.FighterSkillEffect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class SkillNodeTest {

    @BeforeEach
    void setUp() {
        SkillTree.getAllTypes().clear();
    }

    @AfterEach
    void tearDown() {
        SkillTree.getAllTypes().clear();
    }

    @Test
    void descriptionDelegatesToTheTypesEffect() {
        SkillType type = new SkillType("hull", "Hull", "graphics/hullmods/reinforced_bulkheads.png", 2, 500,
                List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)), SkillTier.SMALL, null, null, null);
        SkillNode node = new SkillNode("hull_1", type, List.of(), 0f, 0f);

        assertEquals("Increases hull points by 10%.", node.getDescription());
    }

    @Test
    void descriptionJoinsMultipleEffectsOnSeparateLines() {
        SkillType type = new SkillType("heavyarmor", "Heavy Armor", "graphics/icons/notable_hullmods/heavy_armor.png", 4, 2000,
                List.of(new SkillTypeEffect(DefenseSkillEffect.ARMOR_PERCENT, 15f), new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 5f)),
                SkillTier.NOTABLE, null, null, null);
        SkillNode node = new SkillNode("heavyarmor_1", type, List.of(), 0f, 0f);

        assertEquals("Increases armor rating by 15%.\n\nIncreases hull points by 5%.", node.getDescription());
    }

    @Test
    void descriptionPutsDeallocationWarningsLastRegardlessOfEffectOrder() {
        SkillType type = new SkillType("converted_hangar", "Converted Hangar", "graphics/icons/notable_hullmods/converted_hangar.png", 4, 2000,
                List.of(new SkillTypeEffect(FighterSkillEffect.FIGHTER_BAYS_FLAT, 1f), new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 5f)),
                SkillTier.KEYSTONE, null, null, null);
        SkillNode node = new SkillNode("converted_hangar_1", type, List.of(), 0f, 0f);

        assertEquals(
                "Increases number of fighter bays by 1.\n\nIncreases hull points by 5%.\n\nCannot be unallocated without at least 1 empty fighter bay.",
                node.getDescription());
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
                List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)), SkillTier.SMALL, null, "Custom flavor text.", null);
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

    @Test
    void resolveEffectiveTypeReturnsItsOwnTypeWhenNotOptional() {
        SkillType type = new SkillType("hull", "Hull", "graphics/hullmods/reinforced_bulkheads.png", 2, 500,
                List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)), SkillTier.SMALL, null, null, null);
        SkillNode node = new SkillNode("hull_1", type, List.of(), 0f, 0f);

        assertSame(type, node.resolveEffectiveType(new ShipSkillData()));
    }

    @Test
    void resolveEffectiveTypeReturnsThePlaceholderWhenOptionalAndUnselected() {
        SkillType placeholder = new SkillType("slot", "Optional Skill", "a.png", 0, 0,
                List.of(), List.of(), SkillTier.SMALL, null, null, null, List.of("hull", "armor"));
        SkillNode node = new SkillNode("slot_1", placeholder, List.of(), 0f, 0f);

        assertSame(placeholder, node.resolveEffectiveType(new ShipSkillData()));
    }

    @Test
    void resolveEffectiveTypeReturnsTheSelectedOptionsTypeOnceChosen() {
        SkillType placeholder = new SkillType("slot", "Optional Skill", "a.png", 0, 0,
                List.of(), List.of(), SkillTier.SMALL, null, null, null, List.of("hull"));
        SkillType hullOption = new SkillType("hull", "Hull", "graphics/hullmods/reinforced_bulkheads.png", 2, 500,
                List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)), SkillTier.SMALL, null, null, null);
        SkillTree.registerType(hullOption);
        SkillNode node = new SkillNode("slot_1", placeholder, List.of(), 0f, 0f);
        ShipSkillData data = new ShipSkillData();
        data.selectOption(node, hullOption);

        assertSame(hullOption, node.resolveEffectiveType(data));
    }
}
