package exiledsector.ui;

import lunalib.lunaRefit.BaseRefitButton;
import lunalib.lunaRefit.LunaRefitManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillTreeRefitButtonTest {

    private final SkillTreeRefitButton button = new SkillTreeRefitButton();

    @AfterEach
    void tearDown() {
        // addButton() registers into LunaRefitManager's static registry -
        // clean it up so it doesn't leak into other tests.
        BaseRefitButton registered = LunaRefitManager.getFirstButtonOfClass(SkillTreeRefitButton.class);
        if (registered != null) {
            // removeButton() isn't @JvmStatic, so it's reached through the
            // Kotlin object's singleton INSTANCE field from Java.
            LunaRefitManager.INSTANCE.removeButton(registered);
        }
    }

    @Test
    void buttonIsNamedSkillTree() {
        assertEquals("Skill Tree", button.getButtonName(null, null));
    }

    @Test
    void usesTheVanillaSkillsCodexIcon() {
        assertEquals("graphics/icons/codex/skills.png", button.getIconName(null, null));
    }

    @Test
    void hasPanelSoClickingOpensTheOverlay() {
        assertTrue(button.hasPanel(null, null, null));
    }

    @Test
    void addButtonRegistersAnInstanceWithLunaRefitManager() {
        SkillTreeRefitButton.addButton();

        assertTrue(LunaRefitManager.hasButtonOfClass(SkillTreeRefitButton.class));
    }
}
