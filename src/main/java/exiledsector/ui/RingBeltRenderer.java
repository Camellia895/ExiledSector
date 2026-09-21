package exiledsector.ui;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

final class RingBeltRenderer {
    private static final float PIXELS_PER_SEGMENT = 5f;
    private static final float MAX_TILE_COUNT = 60f;

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
        float tileCount = Math.min(MAX_TILE_COUNT, Math.max(1f, circumference / (thickness * aspectRatio)));
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
