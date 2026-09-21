package exiledsector.ui.belt;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

final class RadialBandGL {

    private RadialBandGL() {
    }

    static void begin(SpriteAPI texture, float cx, float cy, int blendSrcFactor, int blendDstFactor, Color color, float alphaMult) {
        GL11.glPushMatrix();
        GL11.glTranslatef(cx, cy, 0f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        texture.bindTexture();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(blendSrcFactor, blendDstFactor);
        Misc.setColor(color, alphaMult);
    }

    static void end() {
        GL11.glPopMatrix();
    }

    static int computeSegments(float circumference, float pixelsPerSegment, float minSegments) {
        return (int) Math.max(minSegments, Math.round(circumference / pixelsPerSegment));
    }
}
