package exiledsector.i18n;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Catalogue {

    private static final Logger LOG = Logger.getLogger(Catalogue.class);

    private final String locale;
    private final Map<String, String> entries;
    private final Map<String, Template> templates = new ConcurrentHashMap<>();
    private final Set<String> reportedMissing = ConcurrentHashMap.newKeySet();

    public Catalogue(String locale, Map<String, String> entries) {
        this.locale = locale;
        this.entries = Collections.unmodifiableMap(new LinkedHashMap<>(entries));
    }

    public static Catalogue layered(String locale, List<Map<String, String>> lowestPriorityFirst) {
        Map<String, String> merged = new LinkedHashMap<>();
        for (Map<String, String> layer : lowestPriorityFirst) {
            merged.putAll(layer);
        }
        return new Catalogue(locale, merged);
    }

    public String locale() {
        return locale;
    }

    public boolean has(String key) {
        return entries.containsKey(key);
    }

    public String raw(String key) {
        return entries.get(key);
    }

    public Set<String> keys() {
        return entries.keySet();
    }

    public int size() {
        return entries.size();
    }

    Template template(String key) {
        return templates.computeIfAbsent(key, this::parseOrMissing);
    }

    private Template parseOrMissing(String key) {
        String raw = entries.get(key);
        if (raw == null) {
            if (reportedMissing.add(key)) {
                LOG.warn("Missing Exiled Sector string for locale " + locale + ": " + key);
            }
            return Template.parse("[[" + key + "]]");
        }
        return Template.parse(raw);
    }
}
