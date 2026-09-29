package exiledsector.i18n;

import java.util.List;
import java.util.Map;

public final class Translation {

    private Translation() {
    }

    public static String text(String key) {
        return styled(key).plain();
    }

    public static StyledText styled(String key) {
        return I18n.catalogue().template(key).render(Map.of());
    }

    public static Message msg(String key) {
        return new Message(key);
    }

    public static boolean has(String key) {
        return I18n.catalogue().has(key);
    }

    public static String data(String key, String englishSource) {
        return has(key) ? text(key) : englishSource;
    }

    public static StyledText dataStyled(String key, String englishSource) {
        if (has(key)) {
            return styled(key);
        }
        return englishSource == null ? null : StyledText.parse(englishSource);
    }

    public static StyledText list(List<StyledText> items) {
        return StyledText.join(styled("format.list.sep"), items);
    }
}
