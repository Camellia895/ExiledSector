package exiledsector.ui.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;

public final class SpriteDraw {

    private SpriteDraw() {
    }

    public static boolean drawAtCenter(SpriteCache cache, String path, Vector2f center, Vector2f size, Color tint, float alphaMult) {
        SpriteAPI sprite = prepare(cache, path, size, tint, alphaMult);
        if (sprite == null) return false;
        sprite.renderAtCenter(center.x, center.y);
        return true;
    }

    public static boolean drawAtCenter(SpriteCache cache, String path, Vector2f center, Vector2f size, Color tint, float alphaMult, float angleDeg) {
        SpriteAPI sprite = prepare(cache, path, size, tint, alphaMult);
        if (sprite == null) return false;
        sprite.setAngle(angleDeg);
        sprite.renderAtCenter(center.x, center.y);
        return true;
    }

    private static SpriteAPI prepare(SpriteCache cache, String path, Vector2f size, Color tint, float alphaMult) {
        if (!cache.ensureLoaded(path)) return null;
        SpriteAPI sprite = Global.getSettings().getSprite(path);
        sprite.setSize(size.x, size.y);
        if (tint != null) sprite.setColor(tint);
        sprite.setAlphaMult(alphaMult);
        return sprite;
    }
}
