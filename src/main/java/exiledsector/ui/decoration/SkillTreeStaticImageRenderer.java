package exiledsector.ui.decoration;

import com.fs.starfarer.api.ui.PositionAPI;
import exiledsector.skills.SkillTree;
import exiledsector.ui.util.SpriteCache;
import exiledsector.ui.util.SpriteDraw;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class SkillTreeStaticImageRenderer {

    private final SpriteCache spriteCache = new SpriteCache(SkillTreeStaticImageRenderer.class);
    private float elapsedSeconds = 0f;

    public void advance(float amount) {
        elapsedSeconds += amount;
    }

    public void render(float centerX, float centerY, float zoom, float alphaMult, PositionAPI position) {
        if (position == null) return;

        List<StaticImage> images = SkillTree.getStaticImages();
        if (images.isEmpty()) return;

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for (StaticImage image : images) {
            String path = image.getImagePath();
            if (path == null || path.isEmpty()) continue;

            float screenX = centerX + image.getX() * zoom;
            float screenY = centerY - image.getY() * zoom;
            // The editor authors rotation as an SVG rotate() transform, which is clockwise-positive
            // because SVG's Y-axis points down. This renderer's world space is Y-up (see the Y flip
            // above), where increasing angle is counter-clockwise - so the authored value must be
            // negated here to land in the same visual orientation shown in the editor.
            float angleDeg = -(image.getRotation() + image.getRotationSpeed() * elapsedSeconds);
            SpriteDraw.drawAtCenter(spriteCache, path, screenX, screenY,
                    image.getWidth() * zoom, image.getHeight() * zoom, null, alphaMult, angleDeg);
        }

        GL11.glDisable(GL11.GL_BLEND);
    }
}
