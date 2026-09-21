package exiledsector.ui;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

/**
 * A direct port of vanilla's own planet-ring renderer
 * (com.fs.starfarer.api.impl.campaign.terrain.RingRenderer, decompiled straight out of
 * starfarer.api.jar - fully unobfuscated), trimmed of the "spiral" mode and the campaign
 * loc/factor coordinate scaling, neither of which apply to a small on-screen node decoration.
 * The one thing that's easy to get wrong by eye rather than by reading the source: the ring
 * texture's WIDTH is the radial cross-section (U 0 = inner edge, U 1 = outer edge, expected to
 * fade to transparent at both ends) and its HEIGHT tiles around the circumference via an
 * unbounded, ever-increasing V texture coordinate - this relies on the texture's default
 * GL_REPEAT wrap (never explicitly set here, exactly like vanilla's own code), not on any
 * manual per-tile chunking.
 * <p>
 * One thing NOT ported as-is: vanilla derives how many times the texture repeats from
 * {@code pixelsPerSegment * texWidthPx / thickness}, i.e. it assumes texture pixels and world
 * units are roughly 1:1 - true for a campaign-scale planet ring (thickness in the hundreds of
 * world units, a similar order of magnitude to the 256px texture), wildly untrue for a node
 * decoration a few dozen screen-pixels thick. Ported unmodified, that formula produces well over
 * a thousand repeats around a small ring - each repeat sub-pixel, which is what the mipmapped
 * "glitchy" noise actually was. Instead, tile count is derived from the texture's own aspect
 * ratio (height/width) so each repeat is roughly as long (along the ring) as the band is thick -
 * scale-independent, and reduces to the same proportionality vanilla relies on.
 */
final class RingBeltRenderer {
    private static final float PIXELS_PER_SEGMENT = 5f;
    private static final float TILE_DENSITY = 3f;

    private RingBeltRenderer() {
    }

    static void render(SpriteAPI texture, float cx, float cy, float innerRadius, float outerRadius, Color color, float alphaMult) {
        float middleRadius = (innerRadius + outerRadius) / 2f;
        float circumference = (float) (2 * Math.PI * middleRadius);
        float segments = Math.round(circumference / PIXELS_PER_SEGMENT);
        float anglePerSegment = (float) (2 * Math.PI) / segments;
        float thickness = outerRadius - innerRadius;

        float imageWidth = texture.getWidth();
        float imageHeight = texture.getHeight();
        float aspectRatio = imageHeight / imageWidth;
        float tileCount = Math.max(1f, TILE_DENSITY * circumference / (thickness * aspectRatio));
        float texPerSegment = tileCount / segments;

        GL11.glPushMatrix();
        GL11.glTranslatef(cx, cy, 0f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        texture.bindTexture();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Misc.setColor(color, alphaMult);

        float texProgress = 0f;
        GL11.glBegin(GL11.GL_QUAD_STRIP);
        for (int i = 0; i <= segments; i++) {
            float theta = anglePerSegment * i;
            float cos = (float) Math.cos(theta);
            float sin = (float) Math.sin(theta);

            GL11.glTexCoord2f(0f, texProgress);
            GL11.glVertex2f(cos * innerRadius, sin * innerRadius);
            GL11.glTexCoord2f(1f, texProgress);
            GL11.glVertex2f(cos * outerRadius, sin * outerRadius);
            texProgress += texPerSegment;
        }
        GL11.glEnd();
        GL11.glPopMatrix();
    }
}
