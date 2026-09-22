package exiledsector.ui.belt;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public final class WormholeBandRenderer {
    private static final float PIXELS_PER_SEGMENT = 5f;
    private static final float BAND_WIDTH_IN_TEXTURE = 64f;
    private static final float MAX_TILE_COUNT = 60f;
    private static final float WOBBLE_RATIO = 0.06f;
    private static final float MAX_SAFE_WOBBLE_FRACTION = 0.3f;
    private static final float PHASE_DEG_PER_SEC = 30f;
    private static final float WOBBLE_FREQUENCY = 6f;
    private static final float MIN_SAMPLES_PER_WOBBLE_CYCLE = 16f;
    private static final float MIN_SEGMENTS_FOR_WOBBLE = WOBBLE_FREQUENCY * MIN_SAMPLES_PER_WOBBLE_CYCLE;

    private WormholeBandRenderer() {
    }

    public static void render(SpriteAPI texture, float cx, float cy, float innerRadius, float outerRadius,
                               int bandSlot, float rotationDeg, Color color, float alphaMult, float elapsedSeconds) {
        float circumference = (float) (2 * Math.PI * (innerRadius + outerRadius) / 2f);
        float segments = Math.max(MIN_SEGMENTS_FOR_WOBBLE, Math.round(circumference / PIXELS_PER_SEGMENT));
        float anglePerSegment = (float) (2 * Math.PI) / segments;
        float thickness = outerRadius - innerRadius;

        float texWidth = texture.getTextureWidth();
        float imageWidth = texture.getWidth();
        float imageHeight = texture.getHeight();
        float aspectRatio = imageHeight / BAND_WIDTH_IN_TEXTURE;
        float tileCount = Math.min(MAX_TILE_COUNT, Math.max(1f, circumference / (thickness * aspectRatio)));
        float texPerSegment = tileCount / segments;
        float wobble = Math.min(thickness * WOBBLE_RATIO, outerRadius * MAX_SAFE_WOBBLE_FRACTION / WOBBLE_FREQUENCY);

        float leftTX = bandSlot * texWidth * BAND_WIDTH_IN_TEXTURE / imageWidth;
        float rightTX = (bandSlot + 1f) * texWidth * BAND_WIDTH_IN_TEXTURE / imageWidth - 0.001f;
        float phaseAngleRad = (float) Math.toRadians((elapsedSeconds * PHASE_DEG_PER_SEC) % 360f);

        GL11.glPushMatrix();
        GL11.glTranslatef(cx, cy, 0f);
        GL11.glRotatef(rotationDeg, 0f, 0f, 1f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        texture.bindTexture();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        Misc.setColor(color, alphaMult);

        float texProgress = 0f;
        GL11.glBegin(GL11.GL_QUAD_STRIP);
        for (int i = 0; i <= segments; i++) {
            int segIndex = i % (int) segments;
            float theta = anglePerSegment * segIndex;
            float wobbleOffset = (float) Math.sin(phaseAngleRad + segIndex * anglePerSegment * WOBBLE_FREQUENCY) * wobble;

            float cos = (float) Math.cos(theta);
            float sin = (float) Math.sin(theta);
            float x1 = cos * (innerRadius + wobbleOffset);
            float y1 = sin * (innerRadius + wobbleOffset);
            float x2 = cos * (outerRadius + wobbleOffset);
            float y2 = sin * (outerRadius + wobbleOffset);

            GL11.glTexCoord2f(leftTX, texProgress);
            GL11.glVertex2f(x1, y1);
            GL11.glTexCoord2f(rightTX, texProgress);
            GL11.glVertex2f(x2, y2);
            texProgress += texPerSegment;
        }
        GL11.glEnd();
        GL11.glPopMatrix();
    }
}
