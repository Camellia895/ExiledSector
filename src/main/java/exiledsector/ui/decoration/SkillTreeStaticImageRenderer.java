package exiledsector.ui.decoration;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import exiledsector.skills.SkillTree;
import exiledsector.ui.util.SpriteCache;
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
            if (path == null || path.isEmpty() || !spriteCache.ensureLoaded(path)) continue;

            SpriteAPI sprite = Global.getSettings().getSprite(path);
            float screenX = centerX + image.getX() * zoom;
            float screenY = centerY - image.getY() * zoom;
            sprite.setSize(image.getWidth() * zoom, image.getHeight() * zoom);
            sprite.setAlphaMult(alphaMult);
            sprite.setAngle(image.getRotation() + image.getRotationSpeed() * elapsedSeconds);
            sprite.renderAtCenter(screenX, screenY);
        }

        GL11.glDisable(GL11.GL_BLEND);
    }
}
