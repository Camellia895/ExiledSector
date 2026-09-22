package exiledsector.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import exiledsector.ui.util.SpriteCache;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

final class SkillTreePointButton {

    static final float SIZE = SkillTreeReadoutBar.BAR_HEIGHT / 2f;
    static final float GAP = 12f;

    private static final String GLOW_TEXTURE_PATH = "graphics/fx/hit_glow.png";
    private static final float GLOW_DURATION = 0.35f;
    private static final float GLOW_SIZE_RATIO = 2.4f;
    private static final Color GLOW_COLOR = new Color(90, 180, 255);

    private static final Color TINT = Color.WHITE;
    private static final Color HOVER_TINT = new Color(180, 230, 255);

    private final String iconPath;
    private final SpriteCache spriteCache;
    private final SpriteCache glowSpriteCache;
    private float glowElapsed = -1f;

    SkillTreePointButton(Class<?> owner, String iconPath) {
        this.iconPath = iconPath;
        this.spriteCache = new SpriteCache(owner);
        this.glowSpriteCache = new SpriteCache(owner);
    }

    void advance(float amount) {
        if (glowElapsed < 0f) return;
        glowElapsed += amount;
        if (glowElapsed >= GLOW_DURATION) {
            glowElapsed = -1f;
        }
    }

    void startGlow() {
        glowElapsed = 0f;
    }

    void render(float x, float y, boolean hovered, float alphaMult) {
        if (glowElapsed >= 0f) {
            drawGlow(x, y, alphaMult);
        }

        if (!spriteCache.ensureLoaded(iconPath)) return;

        SpriteAPI sprite = Global.getSettings().getSprite(iconPath);
        sprite.setSize(SIZE, SIZE);
        sprite.setAlphaMult(alphaMult);
        sprite.setColor(hovered ? HOVER_TINT : TINT);
        sprite.renderAtCenter(x + SIZE / 2f, y + SIZE / 2f);
    }

    private void drawGlow(float x, float y, float alphaMult) {
        if (!glowSpriteCache.ensureLoaded(GLOW_TEXTURE_PATH)) return;

        float progress = glowElapsed / GLOW_DURATION;
        float glowAlpha = (1f - progress) * alphaMult;
        float size = SIZE * GLOW_SIZE_RATIO;

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        SpriteAPI glow = Global.getSettings().getSprite(GLOW_TEXTURE_PATH);
        glow.setSize(size, size);
        glow.setColor(GLOW_COLOR);
        glow.setAlphaMult(glowAlpha);
        glow.renderAtCenter(x + SIZE / 2f, y + SIZE / 2f);

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    boolean contains(float x, float y, float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + SIZE && mouseY >= y && mouseY <= y + SIZE;
    }
}
