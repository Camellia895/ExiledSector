package exiledsector.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import exiledsector.skills.RingBelt;
import exiledsector.skills.SkillTree;
import org.apache.log4j.Logger;

import java.awt.Color;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SkillTreeRingBeltRenderer {

    private final Set<String> loadedSprites = new HashSet<>();
    private final Set<String> failedSprites = new HashSet<>();

    public void render(float centerX, float centerY, float zoom, float alphaMult, PositionAPI position) {
        if (position == null) return;

        List<RingBelt> ringBelts = SkillTree.getRingBelts();
        if (ringBelts.isEmpty()) return;

        for (RingBelt belt : ringBelts) {
            String path = belt.getRingArtPath();
            if (path == null || path.isEmpty() || !ensureTextureLoaded(path)) continue;

            SpriteAPI sprite = Global.getSettings().getSprite(path);
            float screenX = centerX + belt.getX() * zoom;
            float screenY = centerY - belt.getY() * zoom;
            RingBeltRenderer.render(sprite, screenX, screenY, belt.getInnerRadius() * zoom, belt.getOuterRadius() * zoom,
                    Color.WHITE, alphaMult);
        }
    }

    private boolean ensureTextureLoaded(String path) {
        if (loadedSprites.contains(path)) return true;
        if (failedSprites.contains(path)) return false;
        try {
            Global.getSettings().loadTexture(path);
            loadedSprites.add(path);
            return true;
        } catch (IOException e) {
            Logger.getLogger(SkillTreeRingBeltRenderer.class).error("Failed to load ring belt texture " + path, e);
            failedSprites.add(path);
            return false;
        }
    }
}
