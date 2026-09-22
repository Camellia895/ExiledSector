package exiledsector.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;
import exiledsector.ui.util.GLDraw;
import exiledsector.ui.util.SpriteCache;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

final class SkillTreeReadoutBar {

    private static final String GLOW_LINE_TEXTURE = "graphics/hud/line4x4.png";
    private static final String FONT_PATH = "graphics/fonts/orbitron20aabold.fnt";

    private static final String STANDARD_COLOR_KEY = "progressBarStandardColor";
    private static final String OVERFLOW_COLOR_KEY = "progressBarOverflowColor";
    private static final Color FALLBACK_FILL_COLOR = new Color(0, 121, 216);
    private static final Color FALLBACK_OVERFLOW_COLOR = new Color(220, 90, 70);
    private static final Color TEXT_COLOR = new Color(0xFD, 0xD0, 0x00);
    private static final Color TEXT_SHADOW_COLOR = new Color(0, 0, 0, 200);
    private static final float TEXT_SHADOW_OFFSET_X = 1f;
    private static final float TEXT_SHADOW_OFFSET_Y = -1f;

    static final float BAR_WIDTH = 297f;
    static final float BAR_HEIGHT = 30f;
    private static final float FONT_SIZE = 20f;

    private static final float EDGE_LINE_WIDTH = 2f;
    private static final float CORNER_ACCENT_LENGTH = 15f;
    private static final float LEADING_EDGE_GLOW_WIDTH = 8f;
    private static final float PROGRESS_EASE_SPEED = 10f;

    private static final float HOVER_FADE_IN = 0.05f;
    private static final float HOVER_FADE_OUT = 0.25f;

    private final SpriteCache spriteCache;
    private final FaderUtil hoverFader = new FaderUtil(HOVER_FADE_IN, HOVER_FADE_OUT);

    private LazyFont cachedFont;
    private boolean fontLoadFailed = false;
    private String lastLabel;
    private LazyFont.DrawableString labelText;
    private LazyFont.DrawableString labelShadowText;

    private Color fillColor;
    private Color overflowColor;

    private boolean initialized = false;
    private float displayedSpent;
    private float displayedTotal;

    SkillTreeReadoutBar(Class<?> owner) {
        this.spriteCache = new SpriteCache(owner);
    }

    void advance(float amount, int spent, int total, boolean hovered) {
        if (!initialized) {
            displayedSpent = spent;
            displayedTotal = total;
            initialized = true;
        }
        float ease = Math.min(1f, amount * PROGRESS_EASE_SPEED);
        displayedSpent += (spent - displayedSpent) * ease;
        displayedTotal += (total - displayedTotal) * ease;

        if (hovered) {
            hoverFader.fadeIn();
        } else {
            hoverFader.fadeOut();
        }
        hoverFader.advance(amount);
    }

    void render(float left, float bottom, int spent, int total, float alphaMult) {
        LazyFont font = getFont();
        if (font == null) return;
        if (!initialized) {
            displayedSpent = spent;
            displayedTotal = total;
        }

        Color fill = getFillColor();
        Color overflow = getOverflowColor();
        float glowBoost = hoverFader.getBrightness();

        boolean overCapacity = displayedSpent > displayedTotal;
        float fraction = displayedTotal > 0f ? Math.min(1f, displayedSpent / displayedTotal) : 0f;
        float fillWidth = overCapacity ? BAR_WIDTH : BAR_WIDTH * fraction;
        Color barColor = overCapacity ? overflow : fill;

        GLDraw.fillQuad(left, bottom, BAR_WIDTH, BAR_HEIGHT, Color.BLACK, alphaMult);

        if (fillWidth > 0f) {
            GLDraw.fillQuad(left, bottom, fillWidth, BAR_HEIGHT, barColor, alphaMult * (0.85f + 0.15f * glowBoost));
            drawLeadingEdgeGlow(left + fillWidth, bottom, barColor, alphaMult, glowBoost);
        }

        drawEdgeBevel(left, bottom, barColor, alphaMult, glowBoost);
        drawCornerAccents(left, bottom, barColor, alphaMult);

        String label = Math.round(displayedSpent) + " / " + Math.round(displayedTotal);
        drawLabel(font, label, left, bottom, alphaMult);
    }

    private void drawLeadingEdgeGlow(float edgeX, float bottom, Color color, float alphaMult, float glowBoost) {
        if (!spriteCache.ensureLoaded(GLOW_LINE_TEXTURE)) return;

        SpriteAPI sprite = Global.getSettings().getSprite(GLOW_LINE_TEXTURE);
        sprite.setAdditiveBlend();
        sprite.setColor(color);
        sprite.setSize(LEADING_EDGE_GLOW_WIDTH, BAR_HEIGHT);
        sprite.setAlphaMult(alphaMult * (0.35f + 0.65f * glowBoost));
        sprite.renderAtCenter(edgeX, bottom + BAR_HEIGHT / 2f);
    }

    private void drawEdgeBevel(float left, float bottom, Color color, float alphaMult, float glowBoost) {
        float top = bottom + BAR_HEIGHT;
        drawVerticalBevelLine(left, bottom, top, color, alphaMult, glowBoost);
        drawVerticalBevelLine(left + BAR_WIDTH, bottom, top, color, alphaMult, glowBoost);
    }

    private void drawVerticalBevelLine(float x, float bottom, float top, Color color, float alphaMult, float glowBoost) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glLineWidth(EDGE_LINE_WIDTH);

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Misc.setColor(color, 0.5f * alphaMult);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(x, bottom);
        GL11.glVertex2f(x, top);
        GL11.glEnd();

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        Misc.setColor(color, Math.min(1f, 1f + glowBoost) * alphaMult);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(x + 1f, bottom);
        GL11.glVertex2f(x + 1f, top);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawCornerAccents(float left, float bottom, Color color, float alphaMult) {
        float right = left + BAR_WIDTH;
        float top = bottom + BAR_HEIGHT;
        fadingLine(left + 1f, bottom, left + CORNER_ACCENT_LENGTH, bottom, color, alphaMult);
        fadingLine(left + 1f, top, left + CORNER_ACCENT_LENGTH, top, color, alphaMult);
        fadingLine(right - 1f, bottom, right - CORNER_ACCENT_LENGTH, bottom, color, alphaMult);
        fadingLine(right - 1f, top, right - CORNER_ACCENT_LENGTH, top, color, alphaMult);
    }

    private void fadingLine(float x0, float y0, float x1, float y1, Color color, float alphaMult) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(1f);
        GL11.glBegin(GL11.GL_LINES);
        glColorAlpha(color, alphaMult);
        GL11.glVertex2f(x0, y0);
        glColorAlpha(color, 0f);
        GL11.glVertex2f(x1, y1);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void glColorAlpha(Color color, float alpha) {
        GL11.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(),
                (byte) (int) (255f * Math.max(0f, Math.min(1f, alpha))));
    }

    private void drawLabel(LazyFont font, String label, float left, float bottom, float alphaMult) {
        float textWidth = font.calcWidth(label, FONT_SIZE);
        float textHeight = FONT_SIZE * SkillTreePanelStyle.FONT_LINE_HEIGHT_FACTOR;
        float textX = left + (BAR_WIDTH - textWidth) / 2f;
        float textY = bottom + (BAR_HEIGHT + textHeight) / 2f;

        buildLabelTextIfNeeded(font, label);
        labelShadowText.draw(textX + TEXT_SHADOW_OFFSET_X, textY + TEXT_SHADOW_OFFSET_Y);
        labelText.draw(textX, textY);
    }

    private Color getFillColor() {
        if (fillColor == null) {
            fillColor = colorOrFallback(STANDARD_COLOR_KEY, FALLBACK_FILL_COLOR);
        }
        return fillColor;
    }

    private Color getOverflowColor() {
        if (overflowColor == null) {
            overflowColor = colorOrFallback(OVERFLOW_COLOR_KEY, FALLBACK_OVERFLOW_COLOR);
        }
        return overflowColor;
    }

    private Color colorOrFallback(String settingsKey, Color fallback) {
        try {
            Color color = Global.getSettings().getColor(settingsKey);
            return color != null ? color : fallback;
        } catch (RuntimeException e) {
            Logger.getLogger(SkillTreeReadoutBar.class).error("Failed to read settings colour " + settingsKey, e);
            return fallback;
        }
    }

    private LazyFont getFont() {
        if (cachedFont == null && !fontLoadFailed) {
            cachedFont = SkillTreePanelStyle.loadFontOrNull(FONT_PATH);
            fontLoadFailed = cachedFont == null;
        }
        return cachedFont;
    }

    private void buildLabelTextIfNeeded(LazyFont font, String label) {
        if (label.equals(lastLabel)) return;
        lastLabel = label;
        labelText = SkillTreePanelStyle.buildSimpleText(font, label, FONT_SIZE, TEXT_COLOR);
        labelShadowText = SkillTreePanelStyle.buildSimpleText(font, label, FONT_SIZE, TEXT_SHADOW_COLOR);
    }
}
