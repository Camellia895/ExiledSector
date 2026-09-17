package exiledsector.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class SkillNodeTest {

    @Test
    void descriptionDelegatesToTheTypesEffect() {
        SkillType type = new SkillType("hull", "Hull", "graphics/hullmods/reinforced_bulkheads.png", 2, 500, SkillEffect.HULL, 10f, SkillTier.SMALL, null);
        SkillNode node = new SkillNode("hull_1", type, List.of(), 0f, 0f);

        assertEquals("Increases hull points by 10%.", node.getDescription());
    }

    @Test
    void descriptionIsEmptyForATypeWithNoEffectDefinedYet() {
        SkillType type = new SkillType("cosmetic", "Cosmetic", "graphics/hullmods/flux_coil_adjunct.png", 2, 500, null, 0f, SkillTier.SMALL, null);
        SkillNode node = new SkillNode("cosmetic_1", type, List.of(), 0f, 0f);

        assertEquals("", node.getDescription());
    }

    @Test
    void hullSizeAwareDescriptionDelegatesToTheVanillaHullModWhenTypeSpecifiesOne() {
        SkillType type = new SkillType("safety_overrides", "Safety Overrides", "graphics/icons/skills/helmsmanship.png", 5, 3000, null, 0f, SkillTier.KEYSTONE, "safetyoverrides");
        SkillNode node = new SkillNode("safety_overrides_1", type, List.of(), 0f, 0f);

        try (var globalMock = mockStatic(Global.class)) {
            SettingsAPI settings = mock(SettingsAPI.class);
            globalMock.when(Global::getSettings).thenReturn(settings);
            HullModSpecAPI spec = mock(HullModSpecAPI.class);
            when(settings.getHullModSpec("safetyoverrides")).thenReturn(spec);
            when(spec.getDescription(HullSize.FRIGATE)).thenReturn("vanilla safety overrides text");

            assertEquals("vanilla safety overrides text", node.getDescription(HullSize.FRIGATE));
        }
    }

    @Test
    void hullSizeAwareDescriptionFallsBackToTheEffectWhenTypeHasNoVanillaHullMod() {
        SkillType type = new SkillType("hull", "Hull", "graphics/hullmods/reinforced_bulkheads.png", 2, 500, SkillEffect.HULL, 10f, SkillTier.SMALL, null);
        SkillNode node = new SkillNode("hull_1", type, List.of(), 0f, 0f);

        assertEquals("Increases hull points by 10%.", node.getDescription(HullSize.FRIGATE));
    }
}
