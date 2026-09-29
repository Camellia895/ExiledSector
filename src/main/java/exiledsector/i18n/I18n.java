package exiledsector.i18n;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class I18n {

    public static final String CATALOGUE_DIRECTORY = "data/strings/exiledSector/";

    private static final Logger LOG = Logger.getLogger(I18n.class);

    private static volatile Catalogue active = new Catalogue(LocaleChain.ENGLISH, Map.of());

    private I18n() {
    }

    public static Catalogue catalogue() {
        return active;
    }

    public static String locale() {
        return active.locale();
    }

    public static void install(Catalogue catalogue) {
        active = catalogue;
    }

    public static void load(String locale) {
        List<String> chain = LocaleChain.highestPriorityFirst(locale);
        List<Map<String, String>> layers = new ArrayList<>();
        List<String> found = new ArrayList<>();
        for (String candidate : chain) {
            Map<String, String> entries = read(candidate);
            if (!entries.isEmpty()) {
                layers.add(entries);
                found.add(candidate);
            }
        }
        Collections.reverse(layers);
        install(Catalogue.layered(locale, layers));
        LOG.info("Exiled Sector language " + locale + " from " + found + " (" + active.size() + " strings)");
    }

    public static String path(String locale) {
        return CATALOGUE_DIRECTORY + locale + ".json";
    }

    private static Map<String, String> read(String locale) {
        try {
            JSONObject json = Global.getSettings().getMergedJSON(path(locale));
            return json == null ? Map.of() : flatten(json);
        } catch (Exception e) {
            if (LocaleChain.ENGLISH.equals(locale)) {
                LOG.error("Failed to load the Exiled Sector English strings from " + path(locale), e);
            }
            return Map.of();
        }
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
