package exiledsector.i18n;

import exiledsector.skills.npc.RealSkillData;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RealCatalogue {

    private static final Map<String, Catalogue> CACHE = new ConcurrentHashMap<>();

    private RealCatalogue() {
    }

    public static Catalogue english() {
        return of(LocaleChain.ENGLISH);
    }

    public static Catalogue of(String locale) {
        return CACHE.computeIfAbsent(locale, RealCatalogue::load);
    }

    public static Path file(String locale) {
        return RealSkillData.projectRoot().resolve(I18n.path(locale));
    }

    public static Map<String, String> entries(String locale) {
        Path path = file(locale);
        if (!Files.isRegularFile(path)) {
            return Map.of();
        }
        try {
            return I18n.flatten(new JSONObject(Files.readString(path, StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (JSONException e) {
            throw new IllegalStateException("Invalid JSON in " + path, e);
        }
    }

    private static Catalogue load(String locale) {
        return Catalogue.compose(locale, RealCatalogue::entries);
    }
}
