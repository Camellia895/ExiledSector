package exiledsector.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import lunalib.lunaRefit.BaseRefitButton;
import lunalib.lunaRefit.LunaRefitManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    void panelWidthIs80PercentOfScreenWidth() {
        SettingsAPI settings = mock(SettingsAPI.class);
        when(settings.getScreenWidth()).thenReturn(1920f);

        try (MockedStatic<Global> globalMock = Mockito.mockStatic(Global.class)) {
            globalMock.when(Global::getSettings).thenReturn(settings);

            assertEquals(1536f, button.getPanelWidth(null, null));
        }
    }

    @Test
    void panelHeightIs80PercentOfScreenHeight() {
        SettingsAPI settings = mock(SettingsAPI.class);
        when(settings.getScreenHeight()).thenReturn(1080f);

        try (MockedStatic<Global> globalMock = Mockito.mockStatic(Global.class)) {
            globalMock.when(Global::getSettings).thenReturn(settings);

            assertEquals(864f, button.getPanelHeight(null, null));
        }
    }
}
