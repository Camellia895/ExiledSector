package exiledsector.i18n;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TextWrapper {

    @FunctionalInterface
    public interface Metrics {
        float width(String text);
    }

    private static final String NO_BREAK_BEFORE = "，。、；：？！）》」』】〉〕］｝…‥·・～—％%,.;:?!)]}'\"”’";
    private static final String NO_BREAK_AFTER = "（《「『【〈〔［｛“‘(";

    private TextWrapper() {
    }

    public static StyledText wrap(StyledText text, Metrics metrics, float fontSize, float maxWidth, float maxHeight) {
        return wrap(text, metrics, maxWidth, (int) (maxHeight / fontSize));
    }

    public static StyledText wrap(StyledText text, Metrics metrics, float maxWidth, int maxLines) {
        if (maxWidth <= 0f) {
            return StyledText.EMPTY;
        }
        String source = text.plain();
        Output out = new Output(source.length());
        int numLines = 0;
        int lineStart = 0;
        while (lineStart <= source.length()) {
            int lineEnd = source.indexOf('\n', lineStart);
            if (lineEnd < 0) {
                lineEnd = source.length();
            }
            if (isBlank(source, lineStart, lineEnd)) {
                out.append('\n', -1);
                numLines++;
            } else {
                int cursor = lineStart;
                while (!isBlank(source, cursor, lineEnd)) {
                    if (numLines >= maxLines) {
                        break;
                    }
                    int fits = fittingLength(source, cursor, lineEnd, metrics, maxWidth);
                    if (cursor + fits == lineEnd) {
                        out.copy(source, cursor, lineEnd);
                        out.append('\n', -1);
                        numLines++;
                        break;
                    }
                    int lastSpace = source.lastIndexOf(' ', cursor + fits - 1);
                    lastSpace = lastSpace >= cursor ? lastSpace : -1;
                    int cjkBreak = cjkBreak(source, cursor, cursor + fits, text.spans());
                    if (cjkBreak > lastSpace && cjkBreak > cursor) {
                        out.copy(source, cursor, cjkBreak);
                        out.append('\n', -1);
                        cursor = cjkBreak;
                    } else if (lastSpace >= 0) {
                        out.copy(source, cursor, lastSpace);
                        out.append('\n', lastSpace);
                        cursor = lastSpace + 1;
                    } else {
                        int split = Math.max(1, fittingLength("-" + source.substring(cursor, lineEnd), 0,
                                lineEnd - cursor + 1, metrics, maxWidth) - 1);
                        if (split < lineEnd - cursor) {
                            out.copy(source, cursor, cursor + split);
                            out.append('-', -1);
                            out.append('\n', -1);
                            cursor += split;
                        } else {
                            out.copy(source, cursor, lineEnd);
                            out.append('\n', -1);
                            numLines++;
                            break;
                        }
                    }
                    numLines++;
                }
            }
            lineStart = lineEnd + 1;
        }
        return out.result(text.spans());
    }

    private static boolean isBlank(String text, int start, int end) {
        for (int i = start; i < end; i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static int fittingLength(String text, int start, int end, Metrics metrics, float maxWidth) {
        for (int length = 1; start + length <= end; length++) {
            if (metrics.width(text.substring(start, start + length)) > maxWidth) {
                return length - 1;
            }
        }
        return end - start;
    }

    private static int cjkBreak(String text, int start, int end, List<StyledText.Span> spans) {
        int insideSpan = -1;
        for (int position = end; position > start; position--) {
            if (canBreakBefore(text, position)) {
                if (!insideSpan(spans, position)) {
                    return position;
                }
                if (insideSpan < 0) {
                    insideSpan = position;
                }
            }
        }
        return insideSpan;
    }

    private static boolean insideSpan(List<StyledText.Span> spans, int position) {
        for (StyledText.Span span : spans) {
            if (span.start() < position && position < span.end()) {
                return true;
            }
        }
        return false;
    }

    static boolean canBreakBefore(String text, int position) {
        if (position <= 0 || position >= text.length()) {
            return false;
        }
        char before = text.charAt(position - 1);
        char after = text.charAt(position);
        if (Character.isHighSurrogate(before) || Character.isLowSurrogate(after)
                || Character.isWhitespace(before) || Character.isWhitespace(after)) {
            return false;
        }
        if (!isCjk(text.codePointBefore(position)) && !isCjk(text.codePointAt(position))) {
            return false;
        }
        return NO_BREAK_BEFORE.indexOf(after) < 0 && NO_BREAK_AFTER.indexOf(before) < 0;
    }

    static boolean isCjk(int codePoint) {
        if (Character.isIdeographic(codePoint)) {
            return true;
        }
        Character.UnicodeBlock block = Character.UnicodeBlock.of(codePoint);
        return block == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || block == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || block == Character.UnicodeBlock.HIRAGANA
                || block == Character.UnicodeBlock.KATAKANA
                || block == Character.UnicodeBlock.GENERAL_PUNCTUATION && codePoint >= 0x2018 && codePoint <= 0x2026;
    }

    private static final class Output {
        private final StringBuilder text = new StringBuilder();
        private final int[] positions;

        Output(int sourceLength) {
            positions = new int[sourceLength];
            Arrays.fill(positions, -1);
        }

        void copy(String source, int start, int end) {
            for (int i = start; i < end; i++) {
                positions[i] = text.length();
                text.append(source.charAt(i));
            }
        }

        void append(char c, int sourceIndex) {
            if (sourceIndex >= 0) {
                positions[sourceIndex] = text.length();
            }
            text.append(c);
        }

        StyledText result(List<StyledText.Span> sourceSpans) {
            if (!text.isEmpty()) {
                text.setLength(text.length() - 1);
            }
            List<StyledText.Span> spans = new ArrayList<>();
            for (StyledText.Span span : sourceSpans) {
                int start = firstKept(span.start(), span.end());
                int end = Math.min(lastKept(span.start(), span.end()) + 1, text.length());
                if (start >= 0 && end > start) {
                    spans.add(new StyledText.Span(start, end, span.style()));
                }
            }
            return new StyledText(text.toString(), spans);
        }

        private int firstKept(int start, int end) {
            for (int i = start; i < end; i++) {
                if (positions[i] >= 0) {
                    return positions[i];
                }
            }
            return -1;
        }

        private int lastKept(int start, int end) {
            for (int i = end - 1; i >= start; i--) {
                if (positions[i] >= 0) {
                    return positions[i];
                }
            }
            return -1;
        }
    }
}
