package exiledsector.ui;

import org.junit.jupiter.api.Test;

import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkillTreeSearchBarTest {

    private static final ToDoubleFunction<String> TEN_PIXELS_PER_CHARACTER = text -> text.codePointCount(0, text.length()) * 10.0;

    @Test
    void textThatFitsIsShownWhole() {
        assertEquals("armor", SkillTreeSearchBar.fitEnd("armor", TEN_PIXELS_PER_CHARACTER));
        assertEquals("armor", SkillTreeSearchBar.fitStart("armor", TEN_PIXELS_PER_CHARACTER));
    }

    @Test
    void aLongQueryKeepsItsEndSoTheLatestTypingStaysVisible() {
        String query = "abcdefghijklmnopqrstuvwxyz0123456789";

        assertEquals("fghijklmnopqrstuvwxyz0123456789", SkillTreeSearchBar.fitEnd(query, TEN_PIXELS_PER_CHARACTER));
    }

    @Test
    void aLongPlaceholderKeepsItsStart() {
        String placeholder = "abcdefghijklmnopqrstuvwxyz0123456789";

        assertEquals("abcdefghijklmnopqrstuvwxyz01234", SkillTreeSearchBar.fitStart(placeholder, TEN_PIXELS_PER_CHARACTER));
    }

    @Test
    void trimmingNeverSplitsACharacter() {
        String hanzi = "装甲".repeat(20);

        assertEquals("甲" + "装甲".repeat(15), SkillTreeSearchBar.fitEnd(hanzi, TEN_PIXELS_PER_CHARACTER));
    }
}
