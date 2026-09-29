package exiledsector.i18n;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class I18nTest {

    private MockedStatic<Global> globalMock;
    private SettingsAPI settings;

    @BeforeEach
    void setUp() {
        settings = mock(SettingsAPI.class);
        globalMock = Mockito.mockStatic(Global.class);
        globalMock.when(Global::getSettings).thenReturn(settings);
    }

    @AfterEach
    void tearDown() {
        globalMock.close();
    }

    @Test
    void loadsTheLocaleOverEnglishAndSkipsFilesNoModProvides() throws Exception {
        when(settings.getMergedJSON("data/strings/exiledSector/en.json"))
                .thenReturn(new JSONObject("{\"a\":\"English A\",\"b\":\"English B\"}"));
        when(settings.getMergedJSON("data/strings/exiledSector/zh.json")).thenThrow(new IOException("absent"));
        when(settings.getMergedJSON("data/strings/exiledSector/zh_CN.json"))
                .thenReturn(new JSONObject("{\"a\":\"中文甲\"}"));

        I18n.load("zh_CN");

        assertEquals("zh_CN", I18n.locale());
        assertEquals("中文甲", Translation.text("a"));
        assertEquals("English B", Translation.text("b"));
    }

    @Test
    void aMissingEnglishFileLeavesAnEmptyCatalogueInsteadOfCrashing() throws Exception {
        when(settings.getMergedJSON("data/strings/exiledSector/en.json")).thenThrow(new IOException("absent"));

        I18n.load("en");

        assertEquals("[[a]]", Translation.text("a"));
    }

    @Test
    void gameDrawnTextUsesTheGameCatalogueOnlyInsideItsScope() {
        I18n.install(new Catalogue("zh_CN", Map.of("a", "中文")), new Catalogue("en", Map.of("a", "English")));

        assertEquals("中文", Translation.text("a"));
        assertEquals("English", I18n.forGameText(() -> Translation.text("a")));
        assertEquals("English", Translation.gameText("a"));
        assertThrows(IllegalStateException.class, () -> I18n.forGameText(() -> {
            throw new IllegalStateException("boom");
        }));
        assertEquals("中文", Translation.text("a"));
        assertEquals(new Languages("zh_CN", "en"), I18n.languages());
    }

    @Test
    void ignoresValuesThatAreNotText() throws Exception {
        when(settings.getMergedJSON("data/strings/exiledSector/en.json"))
                .thenReturn(new JSONObject("{\"a\":\"text\",\"b\":3,\"c\":{\"d\":\"e\"}}"));

        I18n.load("en");

        assertEquals(1, I18n.catalogue().size());
    }
}
