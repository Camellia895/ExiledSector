package exiledsector.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import exiledsector.skills.SkillTree;
import exiledsector.skills.StaticImage;
import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SkillTreeStaticImageRenderer {

    private final Set<String> loadedSprites = new HashSet<>();
    private final Set<String> failedSprites = new HashSet<>();
    private float elapsedSeconds = 0f;

    public void advance(float amount) {
        // A single running clock, rather than per-image state - each image with a non-zero
        // rotationSpeed just reads its current angle off this clock at render time.
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
            if (path == null || path.isEmpty() || !ensureTextureLoaded(path)) continue;

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

    private boolean ensureTextureLoaded(String path) {
        if (loadedSprites.contains(path)) return true;
        if (failedSprites.contains(path)) return false;
        try {
            Global.getSettings().loadTexture(path);
            loadedSprites.add(path);
            return true;
        } catch (IOException e) {
            Logger.getLogger(SkillTreeStaticImageRenderer.class).error("Failed to load static image texture " + path, e);
            failedSprites.add(path);
            return false;
        }
    }
}
