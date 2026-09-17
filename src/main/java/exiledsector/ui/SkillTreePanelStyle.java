package exiledsector.ui;

import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

final class SkillTreePanelStyle {

    static final String TOOLTIP_FONT_PATH = "graphics/fonts/insignia15LTaa.fnt";
    static final float TOOLTIP_TITLE_FONT_SIZE = 20f;
    static final float TOOLTIP_BODY_FONT_SIZE = 20f;
    static final Color TOOLTIP_BODY_COLOR = new Color(230, 230, 230);
    static final float FONT_LINE_HEIGHT_FACTOR = 1.25f;
    static final Color TOOLTIP_BACKGROUND_COLOR = Color.BLACK;
    static final float TOOLTIP_BORDER_THICKNESS = 2f;
    static final Color GLOW_COLOR = new Color(120, 200, 255);

    private LazyFont tooltipFont;
    private boolean tooltipFontLoadFailed = false;

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
