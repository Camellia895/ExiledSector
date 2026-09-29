package exiledsector.i18n;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PseudoLocale {

    public static final String LOCALE = "en_XA";

    private static final String METADATA_PREFIX = "meta.";

    private PseudoLocale() {
    }

    public static String apply(String text) {
        return text == null ? null : "[" + text + "~]";
    }

    static Map<String, String> apply(Map<String, String> entries) {
        Map<String, String> pseudo = new LinkedHashMap<>();
        entries.forEach((key, value) -> pseudo.put(key, key.startsWith(METADATA_PREFIX) ? value : apply(value)));
        return pseudo;
    }
}
