package exiledsector.ui;

import com.fs.starfarer.api.Global;
import exiledsector.skills.DescriptionLine;
import exiledsector.ui.util.FallbackSupport;
import exiledsector.ui.util.GLDraw;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SkillTreePanelStyle {

    public static final String TOOLTIP_FONT_PATH = "graphics/fonts/orbitron20aabold.fnt";
    public static final float TOOLTIP_TITLE_FONT_SIZE = 24f;
    public static final float TOOLTIP_BODY_FONT_SIZE = 20f;
    public static final Color TOOLTIP_TITLE_COLOR = Color.WHITE;
    public static final Color TOOLTIP_BODY_COLOR = new Color(230, 230, 230);
    public static final float TOOLTIP_MAX_TEXT_WIDTH = 480f;
    public static final float FONT_LINE_HEIGHT_FACTOR = 1f;
    public static final Color TOOLTIP_BACKGROUND_COLOR = Color.BLACK;
    public static final float TOOLTIP_BORDER_THICKNESS = 2f;
    public static final Color GLOW_COLOR = new Color(120, 200, 255);
    public static final Color POSITIVE_STAT_COLOR = new Color(0x98, 0xFB, 0x00);
    public static final Color NEGATIVE_STAT_COLOR = new Color(0xFC, 0x63, 0x00);

    private static final Color FALLBACK_LOW_TECH_COLOR = new Color(255, 160, 60);
    private static final Color FALLBACK_HIGH_TECH_COLOR = new Color(90, 190, 255);
    private static final String LOW_TECH_DESIGN_TYPE = "Low Tech";
    private static final String HIGH_TECH_DESIGN_TYPE = "High Tech";
    // TODO: drop this if LazyLib stops applying DrawableString colour changes one character early
    private static final int LAZYFONT_COLOR_INDEX_OFFSET = 1;
    private static final String PARAGRAPH_SEPARATOR = "\n\n";

    private static final Color DEFAULT_ACCENT_COLOR = GLOW_COLOR;
    private static final int COLOR_QUANTIZE_STEP = 24;
    private static final int MIN_ALPHA_TO_SAMPLE = 128;

    private static final float TOOLTIP_PADDING = 10f;
    private static final float TOOLTIP_WIDTH_SAFETY_MARGIN = 8f;
    private static final float TOOLTIP_TITLE_BODY_GAP = 6f;
    private static final float TOOLTIP_CURSOR_OFFSET = 18f;
    private static final float TOOLTIP_TITLE_BOLD_OFFSET = 1f;
    private static final float TOOLTIP_TABLE_GAP = 14f;
    private static final float TOOLTIP_SCREEN_MARGIN = 4f;

    private final String accentIconPath;
    private LazyFont tooltipFont;
    private boolean tooltipFontLoadFailed = false;
    private Color accentColor;
    private Color lowTechColor;
    private Color highTechColor;

    public SkillTreePanelStyle(String accentIconPath) {
        this.accentIconPath = accentIconPath;
    }

    public LazyFont getFont() {
        if (tooltipFont == null && !tooltipFontLoadFailed) {
            tooltipFont = loadFontOrNull(TOOLTIP_FONT_PATH);
            tooltipFontLoadFailed = tooltipFont == null;
        }
        return tooltipFont;
    }

    public static LazyFont loadFontOrNull(String path) {
        try {
            return LazyFont.loadFont(path);
        } catch (FontException e) {
            Logger.getLogger(SkillTreePanelStyle.class).error("Failed to load font " + path, e);
            return null;
        }
    }

    public void drawTooltipBackground(float x, float y, float width, float height, float alphaMult, Color borderColor) {
        GLDraw.fillQuad(x, y, width, height, TOOLTIP_BACKGROUND_COLOR, alphaMult);
        GLDraw.strokeQuad(x, y, width, height, borderColor, TOOLTIP_BORDER_THICKNESS, alphaMult);
    }

    public void drawTitleBodyTooltip(TooltipText title, TooltipText body, float mouseX, float mouseY, float alphaMult) {
        drawTitleBodyTooltip(title, body, List.of(), mouseX, mouseY, alphaMult);
    }

    public void drawTitleBodyTooltip(TooltipText title, TooltipText body, List<SkillTreeTooltipTable> tables,
                                     float mouseX, float mouseY, float alphaMult) {
        float contentWidth = Math.max(title.width, body.width);
        float tablesHeight = 0f;
        for (SkillTreeTooltipTable table : tables) {
            contentWidth = Math.max(contentWidth, table.width());
            tablesHeight += TOOLTIP_TABLE_GAP + table.height();
        }
        float boxWidth = contentWidth + TOOLTIP_PADDING * 2f + TOOLTIP_WIDTH_SAFETY_MARGIN;
        float boxHeight = title.height + TOOLTIP_TITLE_BODY_GAP + body.height + tablesHeight + TOOLTIP_PADDING * 2f;
        float boxX = mouseX + TOOLTIP_CURSOR_OFFSET;
        float boxY = Math.max(TOOLTIP_SCREEN_MARGIN, mouseY - boxHeight - TOOLTIP_CURSOR_OFFSET);

        drawTooltipBackground(boxX, boxY, boxWidth, boxHeight, alphaMult, getAccentColor());

        float titleY = boxY + boxHeight - TOOLTIP_PADDING;
        float bodyY = titleY - title.height - TOOLTIP_TITLE_BODY_GAP;
        float titleX = boxX + (boxWidth - title.width) / 2f;
        title.drawable.draw(titleX, titleY);
        title.drawable.draw(titleX + TOOLTIP_TITLE_BOLD_OFFSET, titleY);
        body.drawable.draw(boxX + TOOLTIP_PADDING, bodyY);

        float tableTop = bodyY - body.height;
        float tableWidth = boxWidth - TOOLTIP_PADDING * 2f;
        for (SkillTreeTooltipTable table : tables) {
            tableTop -= TOOLTIP_TABLE_GAP;
            table.draw(boxX + TOOLTIP_PADDING, tableTop, tableWidth, alphaMult);
            tableTop -= table.height();
        }
    }

    public Color getAccentColor() {
        if (accentColor == null) {
            accentColor = computeDominantColor(accentIconPath);
        }
        return accentColor;
    }

    private static Color computeDominantColor(String path) {
        if (path == null || path.isEmpty()) return DEFAULT_ACCENT_COLOR;
        return FallbackSupport.getOrFallback(() -> computeDominantColorOrThrow(path), DEFAULT_ACCENT_COLOR,
                Logger.getLogger(SkillTreePanelStyle.class), "Failed to read " + path + " for accent colour");
    }

    private static Color computeDominantColorOrThrow(String path) throws IOException {
        try (InputStream in = Global.getSettings().openStream(path)) {
            BufferedImage image = ImageIO.read(in);
            if (image == null) return DEFAULT_ACCENT_COLOR;

            Map<Integer, Integer> bucketCounts = new HashMap<>();
            Map<Integer, int[]> bucketSums = new HashMap<>();
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    int alpha = (argb >>> 24) & 0xFF;
                    if (alpha < MIN_ALPHA_TO_SAMPLE) continue;

                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int b = argb & 0xFF;
                    int bucket = (quantize(r) << 16) | (quantize(g) << 8) | quantize(b);

                    bucketCounts.merge(bucket, 1, Integer::sum);
                    int[] sum = bucketSums.computeIfAbsent(bucket, key -> new int[3]);
                    sum[0] += r;
                    sum[1] += g;
                    sum[2] += b;
                }
            }

            if (bucketCounts.isEmpty()) return DEFAULT_ACCENT_COLOR;

            Map.Entry<Integer, Integer> mostCommon = null;
            for (Map.Entry<Integer, Integer> entry : bucketCounts.entrySet()) {
                if (mostCommon == null || entry.getValue() > mostCommon.getValue()) {
                    mostCommon = entry;
                }
            }
            if (mostCommon == null) return DEFAULT_ACCENT_COLOR;

            int[] sum = bucketSums.get(mostCommon.getKey());
            int pixelCount = mostCommon.getValue();
            return new Color(sum[0] / pixelCount, sum[1] / pixelCount, sum[2] / pixelCount);
        }
    }

    private static int quantize(int channel) {
        return (channel / COLOR_QUANTIZE_STEP) * COLOR_QUANTIZE_STEP;
    }

    public Color highlightColor(TooltipHighlighter.Highlight highlight) {
        return switch (highlight) {
            case POSITIVE -> POSITIVE_STAT_COLOR;
            case NEGATIVE -> NEGATIVE_STAT_COLOR;
            case HULLMOD -> lowTechColor();
            case NODE -> highTechColor();
        };
    }

    private Color lowTechColor() {
        if (lowTechColor == null) {
            lowTechColor = designTypeColor(LOW_TECH_DESIGN_TYPE, FALLBACK_LOW_TECH_COLOR);
        }
        return lowTechColor;
    }

    private Color highTechColor() {
        if (highTechColor == null) {
            highTechColor = designTypeColor(HIGH_TECH_DESIGN_TYPE, FALLBACK_HIGH_TECH_COLOR);
        }
        return highTechColor;
    }

    private static Color designTypeColor(String designType, Color fallback) {
        return FallbackSupport.getOrFallback(() -> Global.getSettings().getDesignTypeColor(designType), fallback,
                Logger.getLogger(SkillTreePanelStyle.class), "Failed to read the " + designType + " design type colour");
    }

    public TooltipText buildHighlightedWrappedText(LazyFont font, List<DescriptionLine> paragraphs, float fontSize,
                                                   float maxWidth, float maxHeight, Color color) {
        StringBuilder wrapped = new StringBuilder();
        List<TooltipHighlighter.Span> spans = new ArrayList<>();
        for (DescriptionLine paragraph : paragraphs) {
            if (!wrapped.isEmpty()) {
                wrapped.append(PARAGRAPH_SEPARATOR);
            }
            String wrappedParagraph = font.wrapString(paragraph.text(), fontSize, maxWidth, maxHeight);
            int offset = wrapped.length();
            for (TooltipHighlighter.Span span : TooltipHighlighter.find(wrappedParagraph, paragraph.lowerIsBetter())) {
                spans.add(new TooltipHighlighter.Span(span.start() + offset, span.end() + offset, span.highlight()));
            }
            wrapped.append(wrappedParagraph);
        }
        TooltipText measured = buildMeasuredText(font, wrapped.toString(), fontSize, color,
                LazyFont.TextAlignment.LEFT, LazyFont.TextAnchor.TOP_LEFT);
        appendHighlighted(measured.drawable, wrapped + " ", spans);
        return measured;
    }

    private void appendHighlighted(LazyFont.DrawableString drawable, String text, List<TooltipHighlighter.Span> spans) {
        drawable.setText("");
        int cursor = 0;
        for (TooltipHighlighter.Span span : spans) {
            int start = colourChangeIndex(text, span.start());
            int end = colourChangeIndex(text, span.end());
            drawable.append(text.substring(cursor, start));
            drawable.append(text.substring(start, end), highlightColor(span.highlight()));
            cursor = end;
        }
        drawable.append(text.substring(cursor));
    }

    private static int colourChangeIndex(String text, int boundary) {
        int drawnBoundary = boundary;
        while (drawnBoundary < text.length() && text.charAt(drawnBoundary) == '\n') {
            drawnBoundary++;
        }
        return Math.min(text.length(), drawnBoundary + LAZYFONT_COLOR_INDEX_OFFSET);
    }

    public static TooltipText buildWrappedText(LazyFont font, String rawText, float fontSize, float maxWidth, float maxHeight, Color color) {
        return buildMeasuredText(font, font.wrapString(rawText, fontSize, maxWidth, maxHeight), fontSize, color,
                LazyFont.TextAlignment.LEFT, LazyFont.TextAnchor.TOP_LEFT);
    }

    public static TooltipText buildJoinedText(LazyFont font, List<String> lines, float fontSize, Color color) {
        return buildMeasuredText(font, String.join("\n", lines), fontSize, color,
                LazyFont.TextAlignment.LEFT, LazyFont.TextAnchor.TOP_LEFT);
    }

    private static TooltipText buildMeasuredText(LazyFont font, String text, float fontSize, Color color,
                                                   LazyFont.TextAlignment alignment, LazyFont.TextAnchor anchor) {
        String[] lines = text.split("\n", -1);
        float width = 0f;
        for (String line : lines) {
            width = Math.max(width, font.calcWidth(line, fontSize));
        }
        float height = lines.length * fontSize * FONT_LINE_HEIGHT_FACTOR;

        LazyFont.DrawableString drawable = font.createText(text, color, fontSize);
        drawable.setAlignment(alignment);
        drawable.setAnchor(anchor);
        return new TooltipText(drawable, width, height);
    }

    public static LazyFont.DrawableString buildSimpleText(LazyFont font, String text, float fontSize, Color color) {
        return buildSimpleText(font, text, fontSize, color, LazyFont.TextAnchor.TOP_LEFT);
    }

    public static LazyFont.DrawableString buildSimpleText(LazyFont font, String text, float fontSize, Color color, LazyFont.TextAnchor anchor) {
        LazyFont.DrawableString drawable = font.createText(text, color, fontSize);
        drawable.setAlignment(LazyFont.TextAlignment.LEFT);
        drawable.setAnchor(anchor);
        return drawable;
    }

    public static final class TooltipText {
        public final LazyFont.DrawableString drawable;
        public final float width;
        public final float height;

        public TooltipText(LazyFont.DrawableString drawable, float width, float height) {
            this.drawable = drawable;
            this.width = width;
            this.height = height;
        }
    }
}
