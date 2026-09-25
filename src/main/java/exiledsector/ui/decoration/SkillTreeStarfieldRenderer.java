package exiledsector.ui.decoration;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import exiledsector.ui.SkillTreePanelStyle;
import exiledsector.ui.util.SpriteCache;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SkillTreeStarfieldRenderer {

    private static final String[] STAR_SPRITE_PATHS = {
            "graphics/backgrounds/star0.png",
            "graphics/backgrounds/star1.png",
            "graphics/backgrounds/star2.png",
            "graphics/backgrounds/star3.png"
    };

    private static final float REFERENCE_WIDTH = 1920f;
    private static final float REFERENCE_HEIGHT = 1080f;

    private static final class LayerSpec {
        final int baseCount;
        final float parallaxFactor;
        final float baseSize;
        final float baseAlpha;

        LayerSpec(int baseCount, float parallaxFactor, float baseSize, float baseAlpha) {
            this.baseCount = baseCount;
            this.parallaxFactor = parallaxFactor;
            this.baseSize = baseSize;
            this.baseAlpha = baseAlpha;
        }
    }

    private static final LayerSpec[] LAYER_SPECS = {
            new LayerSpec(70, 0.15f, 8f, 0.45f),
            new LayerSpec(45, 0.3f, 12f, 0.65f),
            new LayerSpec(25, 0.5f, 18f, 0.9f)
    };

    private static final Color STANDARD_STAR_COLOR = Color.WHITE;

    private static final class Star {
        float baseX;
        float baseY;
        int spriteIndex;
        float size;
        float baseAlpha;
        float twinkleSpeed;
        float twinklePhase;
        boolean useAccentColor;
    }

    private final SkillTreePanelStyle style;
    private final SpriteCache spriteCache = new SpriteCache(SkillTreeStarfieldRenderer.class);

    private boolean initialized = false;
    private float fieldWidth;
    private float fieldHeight;
    private List<Star>[] layers;
    private float elapsedTime = 0f;

    public SkillTreeStarfieldRenderer(SkillTreePanelStyle style) {
        this.style = style;
    }

    public void advance(float amount) {
        elapsedTime += amount;
    }

    public void render(PositionAPI position, float panX, float panY, float alphaMult) {
        if (position == null) return;

        if (!initialized) {
            initStars(position.getWidth(), position.getHeight());
        }

        float panelCenterX = position.getX() + position.getWidth() / 2f;
        float panelCenterY = position.getY() + position.getHeight() / 2f;
        Color accentColor = style.getAccentColor();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for (int i = 0; i < LAYER_SPECS.length; i++) {
            LayerSpec spec = LAYER_SPECS[i];
            float offsetX = panX * spec.parallaxFactor;
            float offsetY = panY * spec.parallaxFactor;

            for (Star star : layers[i]) {
                String path = STAR_SPRITE_PATHS[star.spriteIndex];
                if (!spriteCache.ensureLoaded(path)) continue;

                float wrappedX = wrap(star.baseX + offsetX, fieldWidth);
                float wrappedY = wrap(star.baseY + offsetY, fieldHeight);
                float screenX = panelCenterX + wrappedX - fieldWidth / 2f;
                float screenY = panelCenterY + wrappedY - fieldHeight / 2f;

                float twinkle = 0.6f + 0.4f * (float) Math.sin(elapsedTime * star.twinkleSpeed + star.twinklePhase);

                SpriteAPI sprite = Global.getSettings().getSprite(path);
                sprite.setSize(star.size, star.size);
                sprite.setColor(star.useAccentColor ? accentColor : STANDARD_STAR_COLOR);
                sprite.setAlphaMult(alphaMult * star.baseAlpha * twinkle);
                sprite.renderAtCenter(screenX, screenY);
            }
        }

        GL11.glDisable(GL11.GL_BLEND);
    }

    // generic array creation isn't allowed directly; the raw List[] is only ever populated with List<Star>
    @SuppressWarnings("unchecked")
    private void initStars(float panelWidth, float panelHeight) {
        fieldWidth = panelWidth * 1.2f;
        fieldHeight = panelHeight * 1.2f;
        float areaScale = (fieldWidth * fieldHeight) / (REFERENCE_WIDTH * REFERENCE_HEIGHT);

        Random random = new Random();
        layers = new List[LAYER_SPECS.length];
        for (int i = 0; i < LAYER_SPECS.length; i++) {
            LayerSpec spec = LAYER_SPECS[i];
            int count = Math.max(4, Math.round(spec.baseCount * areaScale));
            List<Star> stars = new ArrayList<>(count);
            for (int j = 0; j < count; j++) {
                Star star = new Star();
                star.baseX = random.nextFloat() * fieldWidth;
                star.baseY = random.nextFloat() * fieldHeight;
                star.spriteIndex = random.nextInt(STAR_SPRITE_PATHS.length);
                star.size = spec.baseSize * (0.75f + random.nextFloat() * 0.5f);
                star.baseAlpha = spec.baseAlpha * (0.8f + random.nextFloat() * 0.2f);
                star.twinkleSpeed = 0.5f + random.nextFloat();
                star.twinklePhase = random.nextFloat() * (float) (Math.PI * 2);
                star.useAccentColor = random.nextBoolean();
                stars.add(star);
            }
            layers[i] = stars;
        }
        initialized = true;
    }

    private static float wrap(float value, float size) {
        float wrapped = value % size;
        if (wrapped < 0) wrapped += size;
        return wrapped;
    }
}
