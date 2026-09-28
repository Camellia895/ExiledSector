package exiledsector.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TooltipHighlighter {

    public enum Highlight {
        POSITIVE, NEGATIVE, HULLMOD, NODE
    }

    public record Span(int start, int end, Highlight highlight) {
    }

    private record Rule(Pattern pattern, Highlight highlight) {
    }

    private static final String NUMBER = "(\\d+(?:\\.\\d+)?%?)";
    private static final String SAME_SENTENCE = "[^.]*?\\s";

    private static final List<Rule> RULES = List.of(
            new Rule(Pattern.compile("\\b(?:Increases|increases|Increased|increased)\\s" + SAME_SENTENCE + "by\\s+" + NUMBER),
                    Highlight.POSITIVE),
            new Rule(Pattern.compile("\\b(?:Decreases|decreases|Reduces|reduces|Reduced|reduced)\\s" + SAME_SENTENCE + "by\\s+" + NUMBER),
                    Highlight.NEGATIVE),
            new Rule(Pattern.compile(NUMBER + "\\s+more\\b"), Highlight.POSITIVE),
            new Rule(Pattern.compile(NUMBER + "\\s+less\\b"), Highlight.NEGATIVE),
            new Rule(Pattern.compile("Mutually\\s+exclusive\\s+with\\s+(hullmods?):"), Highlight.HULLMOD),
            new Rule(Pattern.compile("Mutually\\s+exclusive\\s+with\\s+(nodes?):"), Highlight.NODE));

    private TooltipHighlighter() {
    }

    public static List<Span> find(String text) {
        List<Span> spans = new ArrayList<>();
        for (Rule rule : RULES) {
            Matcher matcher = rule.pattern().matcher(text);
            while (matcher.find()) {
                addIfFree(spans, new Span(matcher.start(1), matcher.end(1), rule.highlight()));
            }
        }
        spans.sort(Comparator.comparingInt(Span::start));
        return spans;
    }

    private static void addIfFree(List<Span> spans, Span candidate) {
        for (Span existing : spans) {
            if (candidate.start() < existing.end() && existing.start() < candidate.end()) {
                return;
            }
        }
        spans.add(candidate);
    }
}
