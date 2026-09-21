package exiledsector.ui;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

/**
 * A trimmed port of vanilla's own aurora renderer
 * (com.fs.starfarer.api.impl.campaign.terrain.AuroraRenderer + MagneticFieldTerrainPlugin,
 * decompiled straight out of starfarer.api.jar - fully unobfuscated), stripped of the
 * flare/RangeBlockerUtil system (always absent for a decorative node ring - every delegate
 * callback it feeds into is a flat constant in steady state: thicknessMult 1, thicknessFlat 0,
 * innerOffsetMult/shortenMult 0, alphaMult 1) and driven by real elapsed seconds instead of the
 * campaign clock's in-game days.
 * <p>
 * What isn't obvious just from looking at aurorae.png, only from reading the actual source: the
 * texture (512x512) is split into two 256px-wide HALVES along its WIDTH, not sampled as arbitrary
 * vertical slices of the whole image. Each of the two overlapping render passes uses one whole
 * half as its own radial (inner-to-outer) cross-section - the two halves were never meant to be
 * mixed within a single pass, which is exactly what the previous implementation did and why it
 * looked broken. The two passes are also drawn 180 degrees apart (one full glRotatef between
 * them, not a texture-coordinate trick), with independent phase multipliers (x10 / x5) driving a
 * small outer-edge wobble - that high-frequency wobble, not any radius pulsing (which is
 * flare-only in vanilla and reduces to zero here), is what actually makes a steady-state aurora
 * look alive.
 * <p>
 * One deliberate departure from the source: vanilla's PIXELS_PER_SEGMENT (50), tile-count
 * formula, and outer-edge wobble distance are all sized for campaign-map planet rings spanning
 * thousands of world units, where texture pixels and world units are roughly 1:1. Scaled down to
 * a node-sized decoration without adjustment, that same math produces well over a thousand
 * texture repeats around a small ring (each sub-pixel) and a wobble many times the ring's own
 * size - that sub-pixel repeat noise, worse here than on the plain ring belt because additive
 * blending doesn't average it away the way alpha blending does, is what "glitchy" actually was.
 * Tile count is instead derived from the texture band's own aspect ratio (height/width) so each
 * repeat is roughly as long (along the ring) as the band is thick - scale-independent - and the
 * wobble distance is derived from segment size the same way RingBeltRenderer's tessellation is,
 * keeping every distance in this method proportionate to the small ring it's actually drawing.
 */
final class AuroraBeltRenderer {
    private static final float PIXELS_PER_SEGMENT = 5f;
    private static final float BAND_WIDTH_IN_TEXTURE = 256f;
    private static final float TILE_DENSITY = 3f;
    private static final float PHASE_DEG_PER_SEC = 12f;

    private AuroraBeltRenderer() {
    }

    static void render(SpriteAPI texture, float cx, float cy, float innerRadius, float outerRadius,
                        Color color, float alphaMult, float elapsedSeconds) {
        float phaseAngleDeg = (elapsedSeconds * PHASE_DEG_PER_SEC) % 360f;

        float circumference = (float) (2 * Math.PI * (innerRadius + outerRadius) / 2f);
        float segments = Math.round(circumference / PIXELS_PER_SEGMENT);
        float anglePerSegment = (float) (2 * Math.PI) / segments;
        float thickness = outerRadius - innerRadius;

        float texWidth = texture.getTextureWidth();
        float imageWidth = texture.getWidth();
        float imageHeight = texture.getHeight();
        float aspectRatio = imageHeight / BAND_WIDTH_IN_TEXTURE;
        float tileCount = Math.max(1f, TILE_DENSITY * circumference / (thickness * aspectRatio));
        float texPerSegment = tileCount / segments;

        GL11.glPushMatrix();
        GL11.glTranslatef(cx, cy, 0f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        texture.bindTexture();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        Misc.setColor(color, alphaMult);

        for (int iter = 0; iter < 2; iter++) {
            float bandIndex = iter == 0 ? 1f : 0f;
            float leftTX = bandIndex * texWidth * BAND_WIDTH_IN_TEXTURE / imageWidth;
            float rightTX = (bandIndex + 1f) * texWidth * BAND_WIDTH_IN_TEXTURE / imageWidth - 0.001f;
            float texProgress = 0f;

            GL11.glBegin(GL11.GL_QUAD_STRIP);
            for (int i = 0; i <= segments; i++) {
                int segIndex = (int) (i % segments);
                float theta = anglePerSegment * segIndex;
                float phaseAngleRad = iter == 0
                        ? (float) Math.toRadians(phaseAngleDeg) + segIndex * anglePerSegment * 10f
                        : (float) Math.toRadians(-phaseAngleDeg) + segIndex * anglePerSegment * 5f;

                float cos = (float) Math.cos(theta);
                float sin = (float) Math.sin(theta);
                float x1 = cos * innerRadius;
                float y1 = sin * innerRadius;
                float wobble = PIXELS_PER_SEGMENT * 0.33f;
                float x2 = cos * outerRadius + (float) Math.cos(phaseAngleRad) * wobble;
                float y2 = sin * outerRadius + (float) Math.sin(phaseAngleRad) * wobble;

                GL11.glTexCoord2f(leftTX, texProgress);
                GL11.glVertex2f(x1, y1);
                GL11.glTexCoord2f(rightTX, texProgress);
                GL11.glVertex2f(x2, y2);
                texProgress += texPerSegment;
            }
            GL11.glEnd();
            GL11.glRotatef(180f, 0f, 0f, 1f);
        }

        GL11.glPopMatrix();
    }
}
