package exiledsector.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.util.FlickerUtilV2;
import com.fs.starfarer.api.util.Misc;
import exiledsector.skills.Cloud;
import exiledsector.skills.SkillTree;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SkillTreeCloudRenderer {

    private static final String SPRITE_CAT = "terrain";
    private static final String CLOUD_SPRITE_KEY = "deep_hyperspace";
    private static final String GLOW_SPRITE_KEY = "deep_hyperspace_glow";
    private static final int ATLAS_CELLS = 4;

    private static final float TILE_SIZE = 200f;
    // Matches vanilla's HyperspaceTerrainPlugin.getTileRenderSize(): each tile quad is drawn far
    // larger than the grid spacing it's placed on, so neighboring tiles overlap heavily and blend
    // into a continuous mass instead of showing as a visible mosaic of individual atlas cells.
    private static final float TILE_RENDER_SIZE_MULT = 2.5f;
    private static final float TILE_JITTER_FRACTION = 0.25f;
    private static final float FADE_START_FRACTION = 0.6f;

    private static final float CRACKLE_MIN_WAIT = 2.5f;
    private static final float CRACKLE_MAX_WAIT = 6f;
    private static final float CRACKLE_RADIUS_PER_POINT = 500f;
    private static final float CRACKLE_SPRITE_SIZE = 220f;

    private static final class CrackleTracker {
        final float offsetX;
        final float offsetY;
        final int atlasCol;
        final int atlasRow;
        FlickerUtilV2 flicker;
        float wait;

        CrackleTracker(float offsetX, float offsetY, Random rand) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.atlasCol = rand.nextInt(ATLAS_CELLS);
            this.atlasRow = rand.nextInt(ATLAS_CELLS);
            this.wait = randomWait(rand);
        }

        float getBrightness() {
            return flicker != null ? flicker.getBrightness() : 0f;
        }

        static float randomWait(Random rand) {
            return CRACKLE_MIN_WAIT + rand.nextFloat() * (CRACKLE_MAX_WAIT - CRACKLE_MIN_WAIT);
        }
    }

    private final Map<Cloud, List<CrackleTracker>> trackersByCloud = new LinkedHashMap<>();
    private final Random random = new Random();
    private boolean initialized = false;
    private SpriteAPI cloudSprite;
    private SpriteAPI glowSprite;

    private void ensureInitialized() {
        if (initialized) return;
        initialized = true;
        cloudSprite = Global.getSettings().getSprite(SPRITE_CAT, CLOUD_SPRITE_KEY);
        glowSprite = Global.getSettings().getSprite(SPRITE_CAT, GLOW_SPRITE_KEY);

        for (Cloud cloud : SkillTree.getClouds()) {
            int numPoints = Math.max(1, Math.round(cloud.getRadius() / CRACKLE_RADIUS_PER_POINT));
            List<CrackleTracker> cloudTrackers = new ArrayList<>();
            for (int i = 0; i < numPoints; i++) {
                float angle = random.nextFloat() * (float) (Math.PI * 2);
                float dist = random.nextFloat() * cloud.getRadius() * 0.8f;
                float offsetX = (float) Math.cos(angle) * dist;
                float offsetY = (float) Math.sin(angle) * dist;
                cloudTrackers.add(new CrackleTracker(offsetX, offsetY, random));
            }
            trackersByCloud.put(cloud, cloudTrackers);
        }
    }

    public void advance(float amount) {
        ensureInitialized();
        for (List<CrackleTracker> cloudTrackers : trackersByCloud.values()) {
            for (CrackleTracker tracker : cloudTrackers) {
                if (tracker.flicker != null) {
                    tracker.flicker.advance(amount);
                    if (tracker.flicker.getBrightness() <= 0) {
                        tracker.flicker = null;
                        tracker.wait = CrackleTracker.randomWait(random);
                    }
                    continue;
                }
                tracker.wait -= amount;
                if (tracker.wait <= 0) {
                    tracker.flicker = new FlickerUtilV2();
                    tracker.flicker.newBurst();
                }
            }
        }
    }

    public void render(float centerX, float centerY, float zoom, float alphaMult, PositionAPI position) {
        if (position == null) return;
        ensureInitialized();
        if (cloudSprite == null || glowSprite == null) return;

        List<Cloud> clouds = SkillTree.getClouds();
        if (clouds.isEmpty()) return;

        renderCloudBases(clouds, centerX, centerY, zoom, alphaMult);
        renderCrackles(clouds, centerX, centerY, zoom, alphaMult);
    }

    private void renderCloudBases(List<Cloud> clouds, float centerX, float centerY, float zoom, float alphaMult) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        cloudSprite.bindTexture();

        float baseTexX = cloudSprite.getTexX();
        float baseTexY = cloudSprite.getTexY();
        float baseTexW = cloudSprite.getTexWidth();
        float baseTexH = cloudSprite.getTexHeight();
        float cellTexW = baseTexW / ATLAS_CELLS;
        float cellTexH = baseTexH / ATLAS_CELLS;

        float tileSize = TILE_SIZE * zoom;
        float renderSize = tileSize * TILE_RENDER_SIZE_MULT;
        float jitterRange = renderSize * TILE_JITTER_FRACTION;

        for (Cloud cloud : clouds) {
            float cx = centerX + cloud.getX() * zoom;
            float cy = centerY - cloud.getY() * zoom;
            float radius = cloud.getRadius() * zoom;
            if (radius <= 0 || tileSize <= 0) continue;

            Color tint = cloud.isVanillaColor() ? Color.white : decodeColor(cloud.getColorHex());

            // Grid bounds are padded beyond the cloud's own radius since each tile's rendered
            // quad (renderSize, jittered) reaches well past its own grid cell.
            float pad = renderSize;
            int minGX = (int) Math.floor((cx - radius - pad) / tileSize);
            int maxGX = (int) Math.ceil((cx + radius + pad) / tileSize);
            int minGY = (int) Math.floor((cy - radius - pad) / tileSize);
            int maxGY = (int) Math.ceil((cy + radius + pad) / tileSize);

            for (int gx = minGX; gx <= maxGX; gx++) {
                for (int gy = minGY; gy <= maxGY; gy++) {
                    Random rand = new Random(hashTile(cloud.getId(), gx, gy));
                    int col = rand.nextInt(ATLAS_CELLS);
                    int row = rand.nextInt(ATLAS_CELLS);
                    float angleDeg = rand.nextFloat() * 360f;
                    float jx = -jitterRange / 2f + jitterRange * rand.nextFloat();
                    float jy = -jitterRange / 2f + jitterRange * rand.nextFloat();

                    float tileCenterX = (gx + 0.5f) * tileSize + jx;
                    float tileCenterY = (gy + 0.5f) * tileSize + jy;
                    float quadDist = dist(tileCenterX, tileCenterY, cx, cy);
                    if (quadDist - renderSize * 0.5f > radius) continue;

                    float a = cornerAlpha(tileCenterX, tileCenterY, cx, cy, radius);
                    if (a <= 0) continue;

                    float u0 = baseTexX + col * cellTexW;
                    float v0 = baseTexY + row * cellTexH;

                    float vw = renderSize / 2f, vh = renderSize / 2f;
                    float rad = angleDeg * (float) (Math.PI / 180.0);
                    float cosA = (float) Math.cos(rad), sinA = (float) Math.sin(rad);

                    Misc.setColor(tint, a * alphaMult);
                    GL11.glBegin(GL11.GL_QUADS);
                    GL11.glTexCoord2f(u0, v0);
                    GL11.glVertex2f(tileCenterX + (-vw * cosA + vh * sinA), tileCenterY + (-vw * sinA - vh * cosA));

                    GL11.glTexCoord2f(u0, v0 + cellTexH);
                    GL11.glVertex2f(tileCenterX + (-vw * cosA - vh * sinA), tileCenterY + (-vw * sinA + vh * cosA));

                    GL11.glTexCoord2f(u0 + cellTexW, v0 + cellTexH);
                    GL11.glVertex2f(tileCenterX + (vw * cosA - vh * sinA), tileCenterY + (vw * sinA + vh * cosA));

                    GL11.glTexCoord2f(u0 + cellTexW, v0);
                    GL11.glVertex2f(tileCenterX + (vw * cosA + vh * sinA), tileCenterY + (vw * sinA - vh * cosA));
                    GL11.glEnd();
                }
            }
        }

        GL11.glDisable(GL11.GL_BLEND);
    }

    private void renderCrackles(List<Cloud> clouds, float centerX, float centerY, float zoom, float alphaMult) {
        float glowTexX = glowSprite.getTexX();
        float glowTexY = glowSprite.getTexY();
        float glowCellW = glowSprite.getTexWidth() / ATLAS_CELLS;
        float glowCellH = glowSprite.getTexHeight() / ATLAS_CELLS;

        for (Cloud cloud : clouds) {
            List<CrackleTracker> cloudTrackers = trackersByCloud.get(cloud);
            if (cloudTrackers == null) continue;

            Color tint = cloud.isVanillaColor() ? Color.white : decodeColor(cloud.getColorHex());
            float cx = centerX + cloud.getX() * zoom;
            float cy = centerY - cloud.getY() * zoom;

            for (CrackleTracker tracker : cloudTrackers) {
                float brightness = tracker.getBrightness();
                if (brightness <= 0) continue;

                float px = cx + tracker.offsetX * zoom;
                float py = cy + tracker.offsetY * zoom;

                glowSprite.setAdditiveBlend();
                glowSprite.setColor(tint);
                glowSprite.setAlphaMult(brightness * alphaMult);
                glowSprite.setSize(CRACKLE_SPRITE_SIZE * zoom, CRACKLE_SPRITE_SIZE * zoom);
                glowSprite.setTexX(glowTexX + tracker.atlasCol * glowCellW);
                glowSprite.setTexY(glowTexY + tracker.atlasRow * glowCellH);
                glowSprite.setTexWidth(glowCellW);
                glowSprite.setTexHeight(glowCellH);
                glowSprite.renderAtCenter(px, py);
            }
        }
    }

    private static float cornerAlpha(float px, float py, float cx, float cy, float radius) {
        float d = dist(px, py, cx, cy);
        float fadeStart = radius * FADE_START_FRACTION;
        if (d <= fadeStart) return 1f;
        if (d >= radius) return 0f;
        return 1f - (d - fadeStart) / (radius - fadeStart);
    }

    private static float dist(float x0, float y0, float x1, float y1) {
        float dx = x1 - x0, dy = y1 - y0;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private static long hashTile(String cloudId, int gx, int gy) {
        long h = cloudId.hashCode();
        h = h * 73856093L ^ (long) gx * 19349663L ^ (long) gy * 83492791L;
        return h;
    }

    private static Color decodeColor(String hex) {
        try {
            return Color.decode(hex);
        } catch (NumberFormatException e) {
            return Color.white;
        }
    }
}
