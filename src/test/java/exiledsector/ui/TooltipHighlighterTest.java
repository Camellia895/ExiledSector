package exiledsector.ui;

import exiledsector.ui.TooltipHighlighter.Highlight;
import exiledsector.ui.TooltipHighlighter.Span;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TooltipHighlighterTest {

    private static List<String> highlighted(String text) {
        List<String> parts = new ArrayList<>();
        for (Span span : TooltipHighlighter.find(text)) {
            parts.add(span.highlight() + ":" + text.substring(span.start(), span.end()));
        }
        return parts;
    }

    @Test
    void increasesAndMoreNumbersAreGreen() {
        assertEquals(List.of("POSITIVE:10%"), highlighted("Increases weapon damage by 10%."));
        assertEquals(List.of("POSITIVE:50"), highlighted("Increases weapon range by 50."));
        assertEquals(List.of("POSITIVE:25%"), highlighted("25% more beam weapon range."));
    }

    @Test
    void decreasesAndLessNumbersAreRed() {
        assertEquals(List.of("NEGATIVE:15%"), highlighted("Decreases shield upkeep by 15%."));
        assertEquals(List.of("NEGATIVE:25%"), highlighted("25% less weapon turn rate."));
        assertEquals(List.of("NEGATIVE:25%"),
                highlighted("Enemy ships within 1000 su have the target leading accuracy of their autofiring weapons reduced by 25%."));
    }

    @Test
    void numbersBeforeTheWordByAreNotHighlighted() {
        assertEquals(List.of("POSITIVE:10%"), highlighted("Increases damage dealt by fighters by 10%."));
        assertEquals(List.of("POSITIVE:0.5%"), highlighted("Increases flux capacity by 0.5%."));
    }

    @Test
    void highlightsSurviveLineWraps() {
        assertEquals(List.of("POSITIVE:20%"), highlighted("Increases non-beam energy\nweapon damage by\n20%."));
    }

    @Test
    void exclusivityLabelsAreColouredByKind() {
        String text = """
                Mutually exclusive with hullmods: Advanced Optics, High Scatter Amplifier.

                Mutually exclusive with node: High Scatter Amplifier.""";

        assertEquals(List.of("HULLMOD:hullmods", "NODE:node"), highlighted(text));
    }

    @Test
    void textWithoutStatChangesHasNoHighlights() {
        assertEquals(List.of(), highlighted("Significantly improved missile guidance algorithm."));
    }

    @Test
    void highlightsAreReturnedInTextOrder() {
        List<Span> spans = TooltipHighlighter.find("25% less recoil.\n\nIncreases armor by 10%.");

        assertEquals(Highlight.NEGATIVE, spans.get(0).highlight());
        assertEquals(Highlight.POSITIVE, spans.get(1).highlight());
    }
}
