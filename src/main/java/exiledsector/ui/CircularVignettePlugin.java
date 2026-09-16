package exiledsector.ui;

import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

/**
 * Draws a black overlay on top of a square icon so it reads as a circular
 * porthole: fully transparent inside a fixed inner radius (icon untouched),
 * fading linearly to opaque black by a fixed outer radius, then flat opaque
 * black the rest of the way to this plugin's own panel boundary. Both fade
 * radii are fixed circles (not dependent on angle), which is what makes the
 * shape read as a true circle instead of a fade that only shows up in the
 * corners.
 *
 * This plugin's own panel is intentionally sized larger than the icon it's
 * masking (see VignettedIcon's padding) - the sprite it's layered over has
 * been observed rendering wider than its nominal box in some cases, so the
 * solid-black fill needs real margin beyond the icon's own bounds to
 * reliably cover that overflow rather than just the icon's exact edges.
 */
public class CircularVignettePlugin extends BaseCustomUIPanelPlugin {

    private static final int SEGMENTS = 48;
    private static final float INNER_RADIUS_FRACTION = 0.88f;
    private static final float OUTER_RADIUS_FRACTION = 1f;

    private final float iconSize;
    private PositionAPI position;

    public CircularVignettePlugin(float iconSize) {
        this.iconSize = iconSize;
    }

    @Override
    public void positionChanged(PositionAPI position) {
        this.position = position;
    }

    @Override
    public void render(float alphaMult) {
        if (position == null) return;

        float cx = position.getX() + position.getWidth() / 2f;
        float cy = position.getY() + position.getHeight() / 2f;
        float halfWidth = position.getWidth() / 2f;
        float halfHeight = position.getHeight() / 2f;
        float iconHalf = iconSize / 2f;
        float innerRadius = iconHalf * INNER_RADIUS_FRACTION;
        float outerRadius = iconHalf * OUTER_RADIUS_FRACTION;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Ring 1: the actual fade, transparent -> opaque, fixed radii.
        GL11.glBegin(GL11.GL_QUAD_STRIP);
        for (int i = 0; i <= SEGMENTS; i++) {
            float angle = (float) (2 * Math.PI * i / SEGMENTS);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            Misc.setColor(Color.BLACK, 0f);
            GL11.glVertex2f(cx + cos * innerRadius, cy + sin * innerRadius);

            Misc.setColor(Color.BLACK, alphaMult);
            GL11.glVertex2f(cx + cos * outerRadius, cy + sin * outerRadius);
        }
        GL11.glEnd();

        // Ring 2: solid black from the outer fade radius all the way out to
        // the square's own boundary, so the corners are just as black as the
        // edges instead of showing the icon's square corners through.
        Misc.setColor(Color.BLACK, alphaMult);
        GL11.glBegin(GL11.GL_QUAD_STRIP);
        for (int i = 0; i <= SEGMENTS; i++) {
            float angle = (float) (2 * Math.PI * i / SEGMENTS);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            float boundary = boundaryRadius(cos, sin, halfWidth, halfHeight);

            GL11.glVertex2f(cx + cos * outerRadius, cy + sin * outerRadius);
            GL11.glVertex2f(cx + cos * boundary, cy + sin * boundary);
        }
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
    }

    /**
     * Distance from the center to the square's own boundary along the given
     * direction - used only for the solid corner-fill ring, so it reaches
     * exactly to the square's edges/corners with no gaps or overshoot.
     */
    private static float boundaryRadius(float cos, float sin, float halfWidth, float halfHeight) {
        float rx = cos != 0f ? halfWidth / Math.abs(cos) : Float.MAX_VALUE;
        float ry = sin != 0f ? halfHeight / Math.abs(sin) : Float.MAX_VALUE;
        return Math.min(rx, ry);
    }
}
