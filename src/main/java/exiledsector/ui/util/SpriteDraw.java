package exiledsector.ui.util;

import com.fs.starfarer.api.graphics.SpriteAPI;

import java.awt.Color;

public final class SpriteDraw {

    private SpriteDraw() {
    }

    public static void drawAtCenter(SpriteCache cache, String path, float cx, float cy, float width, float height,
                                    Color tint, float alphaMult) {
        draw(cache, path, cx, cy, width, height, tint, alphaMult, 0f, false);
    }

    public static void drawAtCenter(SpriteCache cache, String path, float cx, float cy, float width, float height,
                                    Color tint, float alphaMult, float angleDeg) {
        draw(cache, path, cx, cy, width, height, tint, alphaMult, angleDeg, false);
    }

    public static void drawAdditiveAtCenter(SpriteCache cache, String path, float cx, float cy, float width, float height,
                                            Color tint, float alphaMult) {
        draw(cache, path, cx, cy, width, height, tint, alphaMult, 0f, true);
    }

    public static void drawAdditiveAtCenter(SpriteCache cache, String path, float cx, float cy, float width, float height,
                                            Color tint, float alphaMult, float angleDeg) {
        draw(cache, path, cx, cy, width, height, tint, alphaMult, angleDeg, true);
    }

    private static void draw(SpriteCache cache, String path, float cx, float cy, float width, float height,
                             Color tint, float alphaMult, float angleDeg, boolean additive) {
        SpriteAPI sprite = cache.sprite(path);
        if (sprite == null) return;
        sprite.setSize(width, height);
        sprite.setColor(tint != null ? tint : Color.WHITE);
        sprite.setAlphaMult(alphaMult);
        sprite.setAngle(angleDeg);
        if (additive) {
            sprite.setAdditiveBlend();
        } else {
            sprite.setNormalBlend();
        }
        sprite.renderAtCenter(cx, cy);
    }
}
