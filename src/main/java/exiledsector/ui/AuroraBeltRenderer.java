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
 * repeat is roughly as long (along the ring) as the band is thick - scale-independent.
 * <p>
 * The outer-edge wobble is what actually animates in steady state (texture content itself never
 * scrolls - texProgress restarts at 0 every frame - and vanilla's radius-pulse is flare-only,
 * zero for us). Vanilla ties its wobble distance to the same pixelsPerSegment=50 constant, which
 * happens to still read as "small but visible" against a campaign-scale ring; naively shrinking
 * that same constant 10x for tessellation (above) shrinks the wobble 10x too, to a fraction of a
 * screen pixel - present in the math, invisible on screen, which is why nothing appeared to move.
 * The wobble is instead sized as a fraction of the belt's own thickness, decoupled from
 * tessellation granularity, so it stays visible regardless of the ring's absolute size.
 * <p>
 * That wobble oscillates with angle at up to x10 cycles per revolution (the iter=0 pass's phase
 * multiplier). PIXELS_PER_SEGMENT=5 alone gives a small ring only ~80 segments total, i.e. ~8
 * samples per wobble cycle - well under the ~16+ a wave needs to read as smooth. MIN_SEGMENTS_FOR_WOBBLE
 * fixes that, but sampling density was never the whole story: between two adjacent segments, the
 * wobble vector's direction rotates by (anglePerSegment * frequency), so the outer vertex moves by
 * roughly (wobble * anglePerSegment * frequency) - purely from the wobble - versus a "base" gap of
 * (outerRadius * anglePerSegment) from the ring's own geometry. Their ratio, wobble * frequency /
 * outerRadius, is independent of segment count entirely: once it approaches 1, the wobble outruns
 * the ring's own circumference between consecutive vertices and the outer boundary folds back on
 * itself every cycle - a real self-intersecting star shape, not a sampling artifact, which is why
 * adding segments alone didn't fix the "edges intersecting" look. MAX_SAFE_WOBBLE_FRACTION caps the
 * wobble so that ratio stays comfortably under 1 for any ring size.
 */
final class AuroraBeltRenderer {
    private static final float PIXELS_PER_SEGMENT = 5f;
    private static final float BAND_WIDTH_IN_TEXTURE = 256f;
    private static final float TILE_DENSITY = 3f;
    private static final float WOBBLE_RATIO = 0.06f;
    private static final float MAX_SAFE_WOBBLE_FRACTION = 0.3f;
    private static final float PHASE_DEG_PER_SEC = 12f;
    private static final float MAX_WOBBLE_FREQUENCY = 10f;
    private static final float MIN_SAMPLES_PER_WOBBLE_CYCLE = 16f;
    private static final float MIN_SEGMENTS_FOR_WOBBLE = MAX_WOBBLE_FREQUENCY * MIN_SAMPLES_PER_WOBBLE_CYCLE;

    private AuroraBeltRenderer() {
    }

    static void render(SpriteAPI texture, float cx, float cy, float innerRadius, float outerRadius,
                        Color color, float alphaMult, float elapsedSeconds) {
        float phaseAngleDeg = (elapsedSeconds * PHASE_DEG_PER_SEC) % 360f;

        float circumference = (float) (2 * Math.PI * (innerRadius + outerRadius) / 2f);
        float segments = Math.max(MIN_SEGMENTS_FOR_WOBBLE, Math.round(circumference / PIXELS_PER_SEGMENT));
        float anglePerSegment = (float) (2 * Math.PI) / segments;
        float thickness = outerRadius - innerRadius;

        float texWidth = texture.getTextureWidth();
        float imageWidth = texture.getWidth();
        float imageHeight = texture.getHeight();
        float aspectRatio = imageHeight / BAND_WIDTH_IN_TEXTURE;
        float tileCount = Math.max(1f, TILE_DENSITY * circumference / (thickness * aspectRatio));
        float texPerSegment = tileCount / segments;
        float wobble = Math.min(thickness * WOBBLE_RATIO, outerRadius * MAX_SAFE_WOBBLE_FRACTION / MAX_WOBBLE_FREQUENCY);

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
