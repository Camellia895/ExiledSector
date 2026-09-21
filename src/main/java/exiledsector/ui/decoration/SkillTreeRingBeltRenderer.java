package exiledsector.ui.decoration;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import exiledsector.skills.SkillTree;
import exiledsector.ui.belt.RingBeltRenderer;
import exiledsector.ui.util.SpriteCache;

import java.awt.Color;
import java.util.List;

public class SkillTreeRingBeltRenderer {

    private final SpriteCache spriteCache = new SpriteCache(SkillTreeRingBeltRenderer.class);
    private float elapsedSeconds = 0f;

    public void advance(float amount) {
        elapsedSeconds += amount;
    }

    public void render(float centerX, float centerY, float zoom, float alphaMult, PositionAPI position) {
        if (position == null) return;

        List<RingBelt> ringBelts = SkillTree.getRingBelts();
        if (ringBelts.isEmpty()) return;

        for (RingBelt belt : ringBelts) {
            String path = belt.getRingArtPath();
            if (path == null || path.isEmpty() || !spriteCache.ensureLoaded(path)) continue;

            SpriteAPI sprite = Global.getSettings().getSprite(path);
            float screenX = centerX + belt.getX() * zoom;
            float screenY = centerY - belt.getY() * zoom;
            float rotationDeg = belt.getRotation() + belt.getRotationSpeed() * elapsedSeconds;
            RingBeltRenderer.render(sprite, screenX, screenY, belt.getInnerRadius() * zoom, belt.getOuterRadius() * zoom,
                    Color.WHITE, alphaMult, rotationDeg);
        }
    }
}
