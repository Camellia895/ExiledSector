package exiledsector.ui;

import exiledsector.i18n.I18n;
import exiledsector.i18n.LanguageSetting;
import exiledsector.i18n.PseudoLeaks;
import exiledsector.i18n.PseudoLocale;
import exiledsector.i18n.RealCatalogue;
import exiledsector.skills.npc.NpcLevelTable;
import exiledsector.skills.npc.NpcTreeConfig;
import exiledsector.ui.inspect.NpcInspectConfig;
import lunalib.lunaSettings.LunaSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

class ExiledSectorSettingsTest {

    private MockedStatic<LunaSettings.SettingsCreator> settingsCreatorMock;
    private final List<String> registeredTexts = new ArrayList<>();

    @BeforeEach
    void setUp() {
        settingsCreatorMock = Mockito.mockStatic(LunaSettings.SettingsCreator.class, invocation -> {
            for (Object argument : invocation.getArguments()) {
                if (argument instanceof String text) {
                    registeredTexts.add(text);
                }
            }
            return Mockito.RETURNS_DEFAULTS.answer(invocation);
        });
        ExiledSectorSettings.registerLanguage();
        ExiledSectorSettings.register();
    }

    @AfterEach
    void tearDown() {
        settingsCreatorMock.close();
    }

    @Test
    void registersAMinAndMaxNodeFieldForEveryPlayerLevelOnTheNpcScalingTab() {
        for (int level = NpcLevelTable.MIN_PLAYER_LEVEL; level <= NpcLevelTable.MAX_PLAYER_LEVEL; level++) {
            int current = level;
            settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.addInt(eq("exiledSector"),
                    eq(NpcLevelTable.minNodesFieldId(current)), anyString(), anyString(),
                    eq(NpcLevelTable.defaultMinNodes(current)), eq(NpcLevelTable.MIN_NODES), eq(NpcLevelTable.MAX_NODES),
                    eq(ExiledSectorSettings.npcScalingTab())));
            settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.addInt(eq("exiledSector"),
                    eq(NpcLevelTable.maxNodesFieldId(current)), anyString(), anyString(),
                    eq(NpcLevelTable.defaultMaxNodes(current)), eq(NpcLevelTable.MIN_NODES), eq(NpcLevelTable.MAX_NODES),
                    eq(ExiledSectorSettings.npcScalingTab())));
        }
    }

    @Test
    void registersTheNpcTreeTogglesOnTheNpcScalingTab() {
        settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.addBoolean(eq("exiledSector"),
                eq(NpcTreeConfig.ENABLED_FIELD_ID), anyString(), anyString(), eq(true), eq(ExiledSectorSettings.npcScalingTab())));
        settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.addBoolean(eq("exiledSector"),
                eq(NpcTreeConfig.OFFICERED_SHIPS_FIELD_ID), anyString(), anyString(), eq(true), eq(ExiledSectorSettings.npcScalingTab())));
        settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.addBoolean(eq("exiledSector"),
                eq(NpcTreeConfig.FLAGSHIP_FIELD_ID), anyString(), anyString(), eq(true), eq(ExiledSectorSettings.npcScalingTab())));
        settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.addInt(eq("exiledSector"),
                eq(NpcTreeConfig.OTHER_SHIP_CHANCE_FIELD_ID), anyString(), anyString(), eq(30), eq(0), eq(100),
                eq(ExiledSectorSettings.npcScalingTab())));
    }

    @Test
    void noSettingTextContainsAPercentSignBecauseLunaLibFormatsItBeforeDisplaying() {
        assertFalse(registeredTexts.isEmpty());
        for (String text : registeredTexts) {
            assertFalse(text.contains("%"), () -> "LunaLib passes this through String.format: " + text);
        }
    }

    @Test
    void everySettingsTextComesFromTheCatalogue() {
        I18n.install(RealCatalogue.of(PseudoLocale.LOCALE));
        registeredTexts.clear();
        ExiledSectorSettings.registerLanguage();
        ExiledSectorSettings.register();
        String options = String.join(",", LanguageSetting.OPTIONS);
        for (String text : registeredTexts) {
            boolean untranslated = text.isEmpty() || text.startsWith("exiledSector") || text.equals(options)
                    || LanguageSetting.OPTIONS.contains(text);
            assertFalse(!untranslated && PseudoLeaks.hasLeak(text), () -> "Hardcoded settings text: " + text);
        }
    }

    @Test
    void refreshesAfterTheLanguageChoiceAndAgainAfterEverythingElse() {
        settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.refresh("exiledSector"), Mockito.times(2));
    }

    @Test
    void offersTheLanguageChoiceAsARadioDefaultingToAuto() {
        settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.addRadio(eq("exiledSector"), eq(LanguageSetting.FIELD_ID),
                anyString(), anyString(), eq(LanguageSetting.AUTO), eq("Auto,English,Simplified Chinese"), eq("")));
    }

    @Test
    void registersTheInspectKeybindOnTheNpcScalingTab() {
        settingsCreatorMock.verify(() -> LunaSettings.SettingsCreator.addKeybind(eq("exiledSector"),
                eq(NpcInspectConfig.KEYBIND_FIELD_ID), anyString(), anyString(), eq(NpcInspectConfig.DEFAULT_KEY),
                eq(ExiledSectorSettings.npcScalingTab())));
    }
}
