package exiledsector.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

final class SkillTreePanelStyle {

    static final String TOOLTIP_FONT_PATH = "graphics/fonts/insignia15LTaa.fnt";
    static final float TOOLTIP_TITLE_FONT_SIZE = 20f;
    static final float TOOLTIP_BODY_FONT_SIZE = 20f;
    static final Color TOOLTIP_BODY_COLOR = new Color(230, 230, 230);
    static final float FONT_LINE_HEIGHT_FACTOR = 1f;
    static final Color TOOLTIP_BACKGROUND_COLOR = Color.BLACK;
    static final float TOOLTIP_BORDER_THICKNESS = 2f;
    static final Color GLOW_COLOR = new Color(120, 200, 255);

    private static final Color DEFAULT_ACCENT_COLOR = GLOW_COLOR;
    private static final int COLOR_QUANTIZE_STEP = 24;
    private static final int MIN_ALPHA_TO_SAMPLE = 128;
    private static final float HEADER_BACKGROUND_DARKEN_FACTOR = 0.2f;

    private final String symbolPath;
    private LazyFont tooltipFont;
    private boolean tooltipFontLoadFailed = false;
    private Color accentColor;
    private Color headerBackgroundColor;

    SkillTreePanelStyle(String symbolPath) {
        this.symbolPath = symbolPath;
    }

    LazyFont getFont() {
        if (tooltipFont == null && !tooltipFontLoadFailed) {
            try {
                tooltipFont = LazyFont.loadFont(TOOLTIP_FONT_PATH);
            } catch (FontException e) {
                Logger.getLogger(SkillTreePanelStyle.class).error("Failed to load tooltip font " + TOOLTIP_FONT_PATH, e);
                tooltipFontLoadFailed = true;
            }
        }
        return tooltipFont;
    }

    void drawTooltipBackground(float x, float y, float width, float height, float alphaMult, Color borderColor) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Misc.setColor(TOOLTIP_BACKGROUND_COLOR, alphaMult);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();

        Misc.setColor(borderColor, alphaMult);
        GL11.glLineWidth(TOOLTIP_BORDER_THICKNESS);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
    }

    Color getAccentColor() {
        if (accentColor == null) {
            accentColor = computeDominantColor(symbolPath);
        }
        return accentColor;
    }

    Color getHeaderBackgroundColor() {
        if (headerBackgroundColor == null) {
            Color accent = getAccentColor();
            headerBackgroundColor = new Color(
                    (int) (accent.getRed() * HEADER_BACKGROUND_DARKEN_FACTOR),
                    (int) (accent.getGreen() * HEADER_BACKGROUND_DARKEN_FACTOR),
                    (int) (accent.getBlue() * HEADER_BACKGROUND_DARKEN_FACTOR));
        }
        return headerBackgroundColor;
    }

    private static Color computeDominantColor(String path) {
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

            int[] sum = bucketSums.get(mostCommon.getKey());
            int pixelCount = mostCommon.getValue();
            return new Color(sum[0] / pixelCount, sum[1] / pixelCount, sum[2] / pixelCount);
        } catch (IOException e) {
            Logger.getLogger(SkillTreePanelStyle.class).error("Failed to read " + path + " for accent colour", e);
            return DEFAULT_ACCENT_COLOR;
        }
    }

    private static int quantize(int channel) {
        return (channel / COLOR_QUANTIZE_STEP) * COLOR_QUANTIZE_STEP;
    }

    static final class TooltipText {
        final LazyFont.DrawableString drawable;
        final float width;
        final float height;

        TooltipText(LazyFont.DrawableString drawable, float width, float height) {
            this.drawable = drawable;
            this.width = width;
            this.height = height;
        }
    }
}
