package exiledsector.skills;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkillNodeTest {

    @Test
    void descriptionDelegatesToTheTypesEffect() {
        SkillType type = new SkillType("hull", "Hull", "graphics/hullmods/reinforced_bulkheads.png", 2, 500, SkillEffect.HULL, 10f);
        SkillNode node = new SkillNode("hull_1", type, List.of(), 0f, 0f);

        assertEquals("Increases hull points by 10%.", node.getDescription());
    }

    @Test
    void descriptionIsEmptyForATypeWithNoEffectDefinedYet() {
        SkillType type = new SkillType("cosmetic", "Cosmetic", "graphics/hullmods/flux_coil_adjunct.png", 2, 500, null, 0f);
        SkillNode node = new SkillNode("cosmetic_1", type, List.of(), 0f, 0f);

        assertEquals("", node.getDescription());
    }
}
