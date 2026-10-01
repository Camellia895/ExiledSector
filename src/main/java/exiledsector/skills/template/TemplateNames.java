package exiledsector.skills.template;

import java.util.Collection;
import java.util.Locale;

public final class TemplateNames {

    public static final int MAX_LENGTH = 32;

    public enum Problem {
        NONE, EMPTY, TOO_LONG, DUPLICATE
    }

    private TemplateNames() {
    }

    public static String normalise(String raw) {
        return raw == null ? "" : raw.strip();
    }

    public static Problem validate(String raw, String rootNodeId, Collection<SkillTreeTemplate> existing) {
        String name = normalise(raw);
        if (name.isEmpty()) {
            return Problem.EMPTY;
        }
        if (name.codePointCount(0, name.length()) > MAX_LENGTH) {
            return Problem.TOO_LONG;
        }
        String folded = name.toLowerCase(Locale.ROOT);
        for (SkillTreeTemplate template : existing) {
            if (template.rootNodeId().equals(rootNodeId) && template.name().toLowerCase(Locale.ROOT).equals(folded)) {
                return Problem.DUPLICATE;
            }
        }
        return Problem.NONE;
    }
}
