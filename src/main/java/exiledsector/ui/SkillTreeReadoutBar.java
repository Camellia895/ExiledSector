package exiledsector.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import exiledsector.ui.util.GLDraw;
import exiledsector.ui.util.SpriteCache;
import org.lazywizard.lazylib.ui.LazyFont;

import java.awt.Color;

import static exiledsector.ui.SkillTreePanelStyle.FONT_LINE_HEIGHT_FACTOR;

final class SkillTreeReadoutBar {

    private static final String FRAME_PREFIX = "graphics/ui/bgs/panel00";
    private static final String FONT_PATH = "graphics/fonts/orbitron20aabold.fnt";

    static final float BAR_WIDTH = 400f;
    static final float BAR_HEIGHT = 60f;
    private static final float CORNER_SIZE = 32f;
    private static final float FILL_INSET = 18f;
    private static final float FONT_SIZE = 20f;

    private static final Color FILL_COLOR = new Color(50, 190, 250);
    private static final Color OVERFLOW_FILL_COLOR = new Color(220, 90, 70);
    private static final Color FRAME_TINT = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(0xFF, 0xD2, 0x00);

    private final SpriteCache spriteCache;
    private LazyFont cachedFont;
    private boolean fontLoadFailed = false;
    private String lastLabel;
    private LazyFont.DrawableString labelText;

    SkillTreeReadoutBar(Class<?> owner) {
        this.spriteCache = new SpriteCache(owner);
    }

    void render(float left, float bottom, int spent, int total, float alphaMult) {
        LazyFont font = getFont();
        if (font == null) return;

        drawFrame(left, bottom, BAR_WIDTH, BAR_HEIGHT, alphaMult);

        boolean overCapacity = spent > total;
        float fraction = total > 0 ? Math.min(1f, spent / (float) total) : 0f;
        float innerLeft = left + FILL_INSET;
        float innerBottom = bottom + FILL_INSET;
        float innerWidth = Math.max(0f, BAR_WIDTH - FILL_INSET * 2f);
        float innerHeight = Math.max(0f, BAR_HEIGHT - FILL_INSET * 2f);
        float fillWidth = overCapacity ? innerWidth : innerWidth * fraction;
        if (fillWidth > 0f) {
            GLDraw.fillQuad(innerLeft, innerBottom, fillWidth, innerHeight,
                    overCapacity ? OVERFLOW_FILL_COLOR : FILL_COLOR, alphaMult);
        }

        String label = spent + " / " + total;
        LazyFont.DrawableString text = getLabelText(font, label);
        float textWidth = font.calcWidth(label, FONT_SIZE);
        float textHeight = FONT_SIZE * FONT_LINE_HEIGHT_FACTOR;
        text.draw(left + (BAR_WIDTH - textWidth) / 2f, bottom + (BAR_HEIGHT + textHeight) / 2f);
    }

    private void drawFrame(float x, float y, float width, float height, float alphaMult) {
        float cornerW = Math.min(CORNER_SIZE, width / 2f);
        float cornerH = Math.min(CORNER_SIZE, height / 2f);
        float centerWidth = Math.max(0f, width - cornerW * 2f);
        float centerHeight = Math.max(0f, height - cornerH * 2f);

        drawPiece(FRAME_PREFIX + "_top_left.png", x, y + height - cornerH, cornerW, cornerH, alphaMult);
        drawPiece(FRAME_PREFIX + "_top_right.png", x + width - cornerW, y + height - cornerH, cornerW, cornerH, alphaMult);
        drawPiece(FRAME_PREFIX + "_bot_left.png", x, y, cornerW, cornerH, alphaMult);
        drawPiece(FRAME_PREFIX + "_bot_right.png", x + width - cornerW, y, cornerW, cornerH, alphaMult);
        drawPiece(FRAME_PREFIX + "_top.png", x + cornerW, y + height - cornerH, centerWidth, cornerH, alphaMult);
        drawPiece(FRAME_PREFIX + "_bot.png", x + cornerW, y, centerWidth, cornerH, alphaMult);
        drawPiece(FRAME_PREFIX + "_left.png", x, y + cornerH, cornerW, centerHeight, alphaMult);
        drawPiece(FRAME_PREFIX + "_right.png", x + width - cornerW, y + cornerH, cornerW, centerHeight, alphaMult);
        drawPiece(FRAME_PREFIX + "_center.png", x + cornerW, y + cornerH, centerWidth, centerHeight, alphaMult);
    }

    private void drawPiece(String path, float x, float y, float width, float height, float alphaMult) {
        if (width <= 0f || height <= 0f) return;
        if (!spriteCache.ensureLoaded(path)) return;

        SpriteAPI sprite = Global.getSettings().getSprite(path);
        sprite.setSize(width, height);
        sprite.setAlphaMult(alphaMult);
        sprite.setColor(FRAME_TINT);
        sprite.renderAtCenter(x + width / 2f, y + height / 2f);
    }

    private LazyFont getFont() {
        if (cachedFont == null && !fontLoadFailed) {
            cachedFont = SkillTreePanelStyle.loadFontOrNull(FONT_PATH);
            fontLoadFailed = cachedFont == null;
        }
        return cachedFont;
    }

    private LazyFont.DrawableString getLabelText(LazyFont font, String label) {
        if (!label.equals(lastLabel)) {
            lastLabel = label;
            labelText = SkillTreePanelStyle.buildSimpleText(font, label, FONT_SIZE, TEXT_COLOR);
        }
        return labelText;
    }
}
