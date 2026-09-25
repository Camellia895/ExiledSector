package exiledsector.ui.belt;

import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public final class RingBeltRenderer {
    private static final float PIXELS_PER_SEGMENT = 5f;
    private static final float MAX_TILE_COUNT = 60f;

    private RingBeltRenderer() {
    }

    public static void render(SpriteAPI texture, RadialBand band, Color color, float alphaMult) {
        render(texture, band, color, alphaMult, 0f);
    }

    public static void render(SpriteAPI texture, RadialBand band, Color color, float alphaMult, float rotationDeg) {
        float innerRadius = band.innerRadius();
        float outerRadius = band.outerRadius();
        float middleRadius = (innerRadius + outerRadius) / 2f;
        float circumference = (float) (2 * Math.PI * middleRadius);
        int segments = RadialBandGL.computeSegments(circumference, PIXELS_PER_SEGMENT, 0);
        float anglePerSegment = (float) (2 * Math.PI) / segments;
        float thickness = outerRadius - innerRadius;

        float imageWidth = texture.getWidth();
        float imageHeight = texture.getHeight();
        float aspectRatio = imageHeight / imageWidth;
        float tileCount = Math.min(MAX_TILE_COUNT, Math.max(1f, circumference / (thickness * aspectRatio)));
        float texPerSegment = tileCount / segments;

        RadialBandGL.begin(texture, band.center().x, band.center().y, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, color, alphaMult);
        GL11.glRotatef(rotationDeg, 0f, 0f, 1f);

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
        RadialBandGL.end();
    }
}
