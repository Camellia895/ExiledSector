package exiledsector.ui.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;

public final class SpriteDraw {

    private SpriteDraw() {
    }

    public static void drawAtCenter(SpriteCache cache, String path, Vector2f center, Vector2f size, Color tint, float alphaMult) {
        SpriteAPI sprite = prepare(cache, path, size, tint, alphaMult);
        if (sprite == null) return;
        sprite.renderAtCenter(center.x, center.y);
    }

    public static void drawAtCenter(SpriteCache cache, String path, Vector2f center, Vector2f size, Color tint, float alphaMult, float angleDeg) {
        SpriteAPI sprite = prepare(cache, path, size, tint, alphaMult);
        if (sprite == null) return;
        sprite.setAngle(angleDeg);
        sprite.renderAtCenter(center.x, center.y);
    }

    public static void drawAdditiveAtCenter(SpriteCache cache, String path, Vector2f center, Vector2f size, Color tint, float alphaMult) {
        SpriteAPI sprite = prepare(cache, path, size, tint, alphaMult);
        if (sprite == null) return;
        sprite.setAdditiveBlend();
        sprite.renderAtCenter(center.x, center.y);
    }

    public static void drawAdditiveAtCenter(SpriteCache cache, String path, Vector2f center, Vector2f size, Color tint, float alphaMult, float angleDeg) {
        SpriteAPI sprite = prepare(cache, path, size, tint, alphaMult);
        if (sprite == null) return;
        sprite.setAdditiveBlend();
        sprite.setAngle(angleDeg);
        sprite.renderAtCenter(center.x, center.y);
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
