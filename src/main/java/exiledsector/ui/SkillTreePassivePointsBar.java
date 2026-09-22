package exiledsector.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.VanillaHullBaselines;
import exiledsector.ui.util.GLDraw;
import exiledsector.ui.util.SpriteCache;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;

import java.awt.Color;

import static exiledsector.ui.SkillTreePanelStyle.FONT_LINE_HEIGHT_FACTOR;

/**
 * Frame pieces are graphics/ui/bgs/panel00_* - the vanilla dark-navy, cyan-bordered
 * readout panel (9-slice: 4 corners, 4 edges, 1 tileable center), used untinted so it
 * keeps its native colouring. The bright "used" fill is a flat inset rectangle drawn on
 * top of it, so the frame's rounded corners and cyan glow border wrap the whole bar while
 * the flat colour underneath tracks the used/total fraction.
 *
 * Label text uses orbitron20aabold, the vanilla bold HUD-numeral font (see e.g. the
 * campaign clock), instead of the mod's usual insignia body-text font - a much closer
 * match to the blocky, bold readout look of the reference images.
 */
final class SkillTreePassivePointsBar {

    private static final String FRAME_PREFIX = "graphics/ui/bgs/panel00";
    private static final String FONT_PATH = "graphics/fonts/orbitron20aabold.fnt";

    private static final float BAR_WIDTH = 400f;
    private static final float BAR_HEIGHT = 60f;
    private static final float CORNER_SIZE = 32f;
    private static final float FILL_INSET = 18f;
    private static final float FOOTPRINT_GAP = 8f;
    private static final float FONT_SIZE = 20f;

    private static final Color FILL_COLOR = new Color(50, 190, 250);
    private static final Color OVERFLOW_FILL_COLOR = new Color(220, 90, 70);
    private static final Color FRAME_TINT = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(255, 150, 30);

    private final FleetMemberAPI member;
    private final SpriteCache spriteCache = new SpriteCache(SkillTreePassivePointsBar.class);
    private LazyFont barFont;
    private boolean fontLoadFailed = false;
    private String lastLabel;
    private LazyFont.DrawableString labelText;

    SkillTreePassivePointsBar(FleetMemberAPI member) {
        this.member = member;
    }

    static float getFootprintHeight() {
        return BAR_HEIGHT + FOOTPRINT_GAP;
    }

    void render(PositionAPI position, float alphaMult) {
        LazyFont font = getFont();
        if (font == null) return;

        int totalPoints;
        int spentPoints;
        try {
            totalPoints = VanillaHullBaselines.passivePointsFor(member.getHullSpec());
            spentPoints = ShipSkillDataManager.get(member.getId()).getSpentPassivePoints();
        } catch (RuntimeException e) {
            Logger.getLogger(SkillTreePassivePointsBar.class).error("Failed to compute passive point stats", e);
            return;
        }

        float barLeft = position.getX() + SkillTreeRefitButton.SHIP_CARD_MARGIN;
        float barTop = position.getY() + position.getHeight() - SkillTreeRefitButton.SHIP_CARD_MARGIN;
        float barBottom = barTop - BAR_HEIGHT;

        drawFrame(barLeft, barBottom, BAR_WIDTH, BAR_HEIGHT, alphaMult);

        boolean overCapacity = spentPoints > totalPoints;
        float fraction = totalPoints > 0 ? Math.min(1f, spentPoints / (float) totalPoints) : 0f;
        float innerLeft = barLeft + FILL_INSET;
        float innerBottom = barBottom + FILL_INSET;
        float innerWidth = Math.max(0f, BAR_WIDTH - FILL_INSET * 2f);
        float innerHeight = Math.max(0f, BAR_HEIGHT - FILL_INSET * 2f);
        float fillWidth = overCapacity ? innerWidth : innerWidth * fraction;
        if (fillWidth > 0f) {
            GLDraw.fillQuad(innerLeft, innerBottom, fillWidth, innerHeight,
                    overCapacity ? OVERFLOW_FILL_COLOR : FILL_COLOR, alphaMult);
        }

        String label = "Passive Points " + spentPoints + " / " + totalPoints;
        LazyFont.DrawableString text = getLabelText(font, label);
        float textWidth = font.calcWidth(label, FONT_SIZE);
        float textHeight = FONT_SIZE * FONT_LINE_HEIGHT_FACTOR;
        text.draw(barLeft + (BAR_WIDTH - textWidth) / 2f, barBottom + (BAR_HEIGHT + textHeight) / 2f);
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
        if (barFont == null && !fontLoadFailed) {
            try {
                barFont = LazyFont.loadFont(FONT_PATH);
            } catch (FontException e) {
                Logger.getLogger(SkillTreePassivePointsBar.class).error("Failed to load font " + FONT_PATH, e);
                fontLoadFailed = true;
            }
        }
        return barFont;
    }

    private LazyFont.DrawableString getLabelText(LazyFont font, String label) {
        if (!label.equals(lastLabel)) {
            lastLabel = label;
            labelText = SkillTreePanelStyle.buildSimpleText(font, label, FONT_SIZE, TEXT_COLOR);
        }
        return labelText;
    }
}
