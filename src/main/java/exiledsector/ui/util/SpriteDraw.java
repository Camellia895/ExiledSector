package exiledsector.ui.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;

import java.awt.Color;

public final class SpriteDraw {

    private SpriteDraw() {
    }

    public static boolean drawAtCenter(SpriteCache cache, String path, float cx, float cy,
                                        float width, float height, Color tint, float alphaMult) {
        SpriteAPI sprite = prepare(cache, path, width, height, tint, alphaMult);
        if (sprite == null) return false;
        sprite.renderAtCenter(cx, cy);
        return true;
    }

    public static boolean drawAtCenter(SpriteCache cache, String path, float cx, float cy,
                                        float width, float height, Color tint, float alphaMult, float angleDeg) {
        SpriteAPI sprite = prepare(cache, path, width, height, tint, alphaMult);
        if (sprite == null) return false;
        sprite.setAngle(angleDeg);
        sprite.renderAtCenter(cx, cy);
        return true;
    }

    private static SpriteAPI prepare(SpriteCache cache, String path, float width, float height, Color tint, float alphaMult) {
        if (!cache.ensureLoaded(path)) return null;
        SpriteAPI sprite = Global.getSettings().getSprite(path);
        sprite.setSize(width, height);
        if (tint != null) sprite.setColor(tint);
        sprite.setAlphaMult(alphaMult);
        return sprite;
    }
}
