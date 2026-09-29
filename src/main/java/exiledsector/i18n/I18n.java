package exiledsector.i18n;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class I18n {

    public static final String CATALOGUE_DIRECTORY = "data/strings/exiledSector/";

    private static final String MISSING_FILE_MESSAGE = "resource, not found in";
    private static final Logger LOG = Logger.getLogger(I18n.class);
    private static final ThreadLocal<Boolean> GAME_TEXT = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private static volatile Catalogue ui = new Catalogue(LocaleChain.ENGLISH, Map.of());
    private static volatile Catalogue game = ui;

    private I18n() {
    }

    public static Catalogue catalogue() {
        return GAME_TEXT.get() ? game : ui;
    }

    public static String locale() {
        return catalogue().locale();
    }

    public static Languages languages() {
        return new Languages(ui.locale(), game.locale());
    }

    public static void install(Catalogue catalogue) {
        install(catalogue, catalogue);
    }

    public static void install(Catalogue uiCatalogue, Catalogue gameCatalogue) {
        ui = uiCatalogue;
        game = gameCatalogue;
    }

    public static void forGameText(Runnable action) {
        forGameText(() -> {
            action.run();
            return null;
        });
    }

    public static <T> T forGameText(Supplier<T> action) {
        boolean previous = GAME_TEXT.get();
        GAME_TEXT.set(Boolean.TRUE);
        try {
            return action.get();
        } finally {
            GAME_TEXT.set(previous);
        }
    }

    public static void load(String locale) {
        load(new Languages(locale, locale));
    }

    public static void load(Languages languages) {
        Catalogue uiCatalogue = read(languages.ui());
        Catalogue gameCatalogue = languages.game().equals(languages.ui()) ? uiCatalogue : read(languages.game());
        install(uiCatalogue, gameCatalogue);
        LOG.info("Exiled Sector language " + languages.ui() + " (" + uiCatalogue.size() + " strings), game-rendered text "
                + languages.game());
    }

    public static String path(String locale) {
        return CATALOGUE_DIRECTORY + locale + ".json";
    }

    private static Catalogue read(String locale) {
        return Catalogue.compose(locale, I18n::readFile);
    }

    private static Map<String, String> readFile(String locale) {
        try {
            JSONObject json = Global.getSettings().getMergedJSON(path(locale));
            return json == null ? Map.of() : flatten(json);
        } catch (RuntimeException e) {
            if (!isMissingFile(e) || LocaleChain.ENGLISH.equals(locale)) {
                logUnreadable(locale, e);
            }
            return Map.of();
        } catch (IOException | JSONException e) {
            logUnreadable(locale, e);
            return Map.of();
        }
    }

    private static boolean isMissingFile(RuntimeException e) {
        return e.getMessage() != null && e.getMessage().contains(MISSING_FILE_MESSAGE);
    }

    private static void logUnreadable(String locale, Exception e) {
        LOG.error("Failed to load Exiled Sector strings for " + locale + " from " + path(locale)
                + "; those strings fall back to English", e);
    }

    public static Map<String, String> flatten(JSONObject json) {
        Map<String, String> entries = new LinkedHashMap<>();
        Iterator<?> keys = json.keys();
        while (keys.hasNext()) {
            String key = String.valueOf(keys.next());
            Object value = json.opt(key);
            if (value instanceof String text) {
                entries.put(key, text);
            } else {
                LOG.warn("Ignoring non-text Exiled Sector string " + key + " in a catalogue file");
            }
        }
        return entries;
    }
}
