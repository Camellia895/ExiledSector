package exiledsector.i18n;

import exiledsector.ui.TooltipHighlighter;

import java.util.ArrayList;
import java.util.List;

// TODO: delete once every description is built from catalogue templates
public final class LegacyHighlight {

    private LegacyHighlight() {
    }

    public static StyledText of(String text) {
        if (text == null) {
            return null;
        }
        List<StyledText.Span> spans = new ArrayList<>();
        for (TooltipHighlighter.Span span : TooltipHighlighter.find(text, false)) {
            spans.add(new StyledText.Span(span.start(), span.end(), style(span.highlight())));
        }
        return new StyledText(text, spans);
    }

    private static Style style(TooltipHighlighter.Highlight highlight) {
        return switch (highlight) {
            case POSITIVE -> Style.GOOD;
            case NEGATIVE -> Style.BAD;
            case HULLMOD -> Style.HULLMOD;
            case NODE -> Style.NODE;
        };
    }
}
