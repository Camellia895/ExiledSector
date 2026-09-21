package exiledsector.ui.node;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTier;
import exiledsector.ui.SkillTreePanelStyle;
import exiledsector.ui.belt.AuroraBeltRenderer;
import exiledsector.ui.belt.RingBeltRenderer;
import exiledsector.ui.belt.WormholeBandRenderer;
import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static exiledsector.ui.node.SkillTreeNodeGeometry.ICON_INSET_RATIO;
import static exiledsector.ui.node.SkillTreeNodeGeometry.NODE_CONNECTOR_GLOW_HALO_ALPHA;
import static exiledsector.ui.node.SkillTreeNodeGeometry.NODE_CONNECTOR_GLOW_HALO_THICKNESS;
import static exiledsector.ui.node.SkillTreeNodeGeometry.NODE_CONNECTOR_GLOW_LINE_THICKNESS;
import static exiledsector.ui.node.SkillTreeNodeGeometry.NODE_CONNECTOR_LINE_THICKNESS;
import static exiledsector.ui.node.SkillTreeNodeGeometry.RING_DULL_ALPHA;
import static exiledsector.ui.node.SkillTreeNodeGeometry.RING_DULL_COLOR;
import static exiledsector.ui.node.SkillTreeNodeGeometry.beltInnerRadius;
import static exiledsector.ui.node.SkillTreeNodeGeometry.beltOuterRadius;
import static exiledsector.ui.node.SkillTreeNodeGeometry.donutGapRadius;
import static exiledsector.ui.node.SkillTreeNodeGeometry.donutRadius;

final class SkillTreeNodeRingRenderer {

    private static final int RING_SEGMENTS = 32;
    private static final float RING_LINE_THICKNESS = 1.5f;

    private static final String[] RING_STACK_TEXTURES = {
            "graphics/fx/wormhole_ring_bright2.png",
            "graphics/fx/wormhole_ring_bright3.png"
    };

    private static final float RING_INSTANCE_MIN_ROTATION_SPEED_DEG = 20f;
    private static final float RING_INSTANCE_MAX_ROTATION_SPEED_DEG = 60f;
    private static final float RING_INSTANCE_JITTER_RATIO = 0.05f;
    private static final float RING_INSTANCE_BASE_ALPHA = 0.5f;
    private static final float RING_MIN_RADIUS_FRACTION = 0.62f;
    private static final float UNALLOCATED_ALPHA_MULT = 0.45f;

    private static final Color RING_PINK_COLOR = new Color(255, 60, 220);
    private static final float RING_PINK_SCALE_RATIO = 0.85f;

    private static final String GLOW_TEXTURE_PATH = "graphics/fx/star_halo.png";
    private static final Color AMBIENT_GLOW_COLOR = new Color(255, 170, 255);
    private static final float AMBIENT_GLOW_ALPHA = 1f;
    private static final float AMBIENT_GLOW_SIZE_RATIO = 3.2f;

    private static final float NOTABLE_RING_OUTER_RADIUS_RATIO = 1.05f;
    private static final float NOTABLE_RING_RADIUS_DECAY = 0.88f;
    private static final int NOTABLE_RING_COUNT = 10;

    private static final String DEFAULT_KEYSTONE_RING_BELT_PATH = "graphics/planets/ring_band_asteroids.png";
    private static final float KEYSTONE_BELT_WIDTH_RATIO = 1.1f;

    private static final String AURORA_TEXTURE_PATH = "graphics/planets/aurorae.png";
    private static final Color DEFAULT_AURORA_COLOR = new Color(140, 120, 255);

    private static final float PULSE_DURATION = 0.5f;
    private static final float PULSE_START_RADIUS_FRACTION = 1f;
    private static final float PULSE_END_RADIUS_FRACTION = 2.2f;

    private static final float BREATHING_PERIOD_SECONDS = 2.2f;
    private static final float BREATHING_MIN_ALPHA = 0.35f;
    private static final float BREATHING_MAX_ALPHA = 1f;

    // Trimmed port of vanilla's JumpPoint/DynamicRingBand wormhole visual (decompiled from
    // com.fs.starfarer.campaign.JumpPoint). Vanilla drives open/close via a Fader with a
    // 1-second linear ramp, squared into an eased "openness" 0..1 that fades the energy
    // bands and central flash out while fading the ring stack in, and grows the whole
    // visual from a small minimum scale up to full size as it opens - we reproduce that
    // exact choreography. Absolute campaign-scale sizes/ring-counts (radius 50, 100 rings)
    // don't transfer to a UI-node-sized icon, so those are re-derived as ratios of the
    // node's own footprint instead, the same adaptation already applied to the belt/aurora
    // renderers elsewhere in this package.
    private static final float WORMHOLE_MIN_SCALE = 0.25f;
    private static final float WORMHOLE_FADE_DURATION_SECONDS = 1f;

    private static final String WORMHOLE_CORONA_TEXTURE_PATH = "graphics/fx/wormhole_corona.png";
    private static final float WORMHOLE_CORONA_SIZE_RATIO = 2.2f;
    private static final int WORMHOLE_CORONA_COUNT = 6;
    private static final float WORMHOLE_CORONA_ORBIT_RATIO = 0.12f;
    private static final float WORMHOLE_CORONA_ROTATION_SPEED_DEG = 12f;
    private static final float WORMHOLE_CORONA_PULSE_SPEED_DEG = 90f;
    private static final float WORMHOLE_CORONA_PULSE_SIZE_RATIO = 0.1f;

    private static final float WORMHOLE_RING_OUTER_RADIUS_RATIO = 1.55f;
    private static final int WORMHOLE_RING_COUNT = 16;
    private static final float WORMHOLE_RING_RADIUS_DECAY = 0.94f;

    private static final String WORMHOLE_BAND_TEXTURE_PATH = "graphics/fx/portal_textures_small.png";
    private static final float WORMHOLE_BAND_INNER_RADIUS_RATIO = 0.45f;
    private static final float WORMHOLE_BAND_THICKNESS_RATIO = 0.35f;
    private static final float WORMHOLE_BAND_ROTATION_SPEED_DEG = 5f;
    private static final float WORMHOLE_BAND_ALPHA = 0.75f;

    private static final String WORMHOLE_GLOW_TEXTURE_PATH = "graphics/fx/hit_glow.png";
    private static final float WORMHOLE_GLOW_SIZE_RATIO = 1.3f;
    private static final float WORMHOLE_GLOW_ALPHA = 0.67f;

    private final SkillTreePanelStyle style;
    private final Set<String> loadedSprites = new HashSet<>();
    private final Map<String, Float> pulseElapsed = new HashMap<>();
    private final Map<String, List<RingInstance>> ringStacks = new HashMap<>();
    private final Map<String, List<RingInstance>> pinkRingStacks = new HashMap<>();
    private final Map<String, Boolean> wormholeAllocated = new HashMap<>();
    private final Map<String, Float> wormholeOpenness = new HashMap<>();
    private float breathingPhase = 0f;
    private float elapsedSeconds = 0f;

    SkillTreeNodeRingRenderer(SkillTreePanelStyle style) {
        this.style = style;
    }

    void advance(float amount) {
        breathingPhase = (breathingPhase + amount) % BREATHING_PERIOD_SECONDS;
        elapsedSeconds += amount;

        if (!pulseElapsed.isEmpty()) {
            Iterator<Map.Entry<String, Float>> it = pulseElapsed.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Float> entry = it.next();
                float elapsed = entry.getValue() + amount;
                if (elapsed >= PULSE_DURATION) {
                    it.remove();
                } else {
                    entry.setValue(elapsed);
                }
            }
        }

        advanceWormholeOpenness(amount);
    }

    private void advanceWormholeOpenness(float amount) {
        if (wormholeAllocated.isEmpty()) return;
        float rate = amount / WORMHOLE_FADE_DURATION_SECONDS;
        for (Map.Entry<String, Boolean> entry : wormholeAllocated.entrySet()) {
            float target = Boolean.TRUE.equals(entry.getValue()) ? 1f : 0f;
            float current = wormholeOpenness.computeIfAbsent(entry.getKey(), id -> target);
            float next = target > current ? Math.min(target, current + rate) : Math.max(target, current - rate);
            wormholeOpenness.put(entry.getKey(), next);
        }
    }

    void startPulse(String nodeId) {
        pulseElapsed.put(nodeId, 0f);
    }

    void draw(float cx, float cy, float footprintSize, float alphaMult, boolean allocated, boolean breathing, float zoom, SkillNode node) {
        SkillTier tier = node.getType().getTier();
        String nodeId = node.getId();
        float half = footprintSize / 2f;
        float scale = tier.getSizeMultiplier();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        float ringRadius = donutRadius(footprintSize);
        if (tier == SkillTier.NOTABLE) {
            float stateAlpha = allocated ? 1f : UNALLOCATED_ALPHA_MULT;
            drawRingStack(cx, cy, footprintSize * NOTABLE_RING_OUTER_RADIUS_RATIO, nodeId,
                    NOTABLE_RING_COUNT, NOTABLE_RING_RADIUS_DECAY, stateAlpha, alphaMult);
            drawAmbientGlow(cx, cy, footprintSize, stateAlpha, alphaMult);
        } else if (tier == SkillTier.KEYSTONE) {
            float stateAlpha = allocated ? 1f : UNALLOCATED_ALPHA_MULT;
            String ringBeltPath = resolveRingBeltPath(node);
            float ringBeltWidth = resolveRingBeltWidth(node);
            if (AURORA_TEXTURE_PATH.equals(ringBeltPath)) {
                drawKeystoneAuroraBelt(cx, cy, footprintSize, ringBeltWidth, resolveRingBeltColor(node), stateAlpha, alphaMult);
            } else {
                drawKeystoneRingBelt(cx, cy, footprintSize, ringBeltWidth, ringBeltPath, stateAlpha, alphaMult);
            }
        } else if (tier == SkillTier.WORMHOLE) {
            wormholeAllocated.put(nodeId, allocated);
            drawWormhole(cx, cy, footprintSize, alphaMult, nodeId, resolveWormholeColor(node));
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (tier == SkillTier.NOTABLE || tier == SkillTier.KEYSTONE) {
            float iconRadius = footprintSize * ICON_INSET_RATIO / 2f;
            drawSingleDonut(cx, cy, iconRadius, allocated, breathing, zoom, alphaMult);
        } else if (tier != SkillTier.WORMHOLE) {
            drawNodeDonut(cx, cy, ringRadius, scale, zoom, allocated, alphaMult);

            if (breathing) {
                float breathingT = (float) (0.5 + 0.5 * Math.sin(2 * Math.PI * breathingPhase / BREATHING_PERIOD_SECONDS));
                float breathingAlpha = (BREATHING_MIN_ALPHA + (BREATHING_MAX_ALPHA - BREATHING_MIN_ALPHA) * breathingT) * alphaMult;
                GL11.glLineWidth(NODE_CONNECTOR_GLOW_LINE_THICKNESS * scale * zoom);
                drawRingOutline(cx, cy, ringRadius, style.getAccentColor(), breathingAlpha);
            }
        }

        if (tier != SkillTier.WORMHOLE) {
            Float pulseSeconds = pulseElapsed.get(nodeId);
            if (pulseSeconds != null) {
                GL11.glLineWidth(RING_LINE_THICKNESS * zoom);
                float progress = pulseSeconds / PULSE_DURATION;
                float radiusFraction = PULSE_START_RADIUS_FRACTION + (PULSE_END_RADIUS_FRACTION - PULSE_START_RADIUS_FRACTION) * progress;
                drawRingOutline(cx, cy, half * radiusFraction, style.getAccentColor(), (1f - progress) * alphaMult);
            }
        }

        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawNodeDonut(float cx, float cy, float radius, float scale, float zoom, boolean allocated, float alphaMult) {
        if (allocated) {
            GL11.glLineWidth(NODE_CONNECTOR_GLOW_HALO_THICKNESS * scale * zoom);
            drawRingOutline(cx, cy, radius, style.getAccentColor(), alphaMult * NODE_CONNECTOR_GLOW_HALO_ALPHA);
            GL11.glLineWidth(NODE_CONNECTOR_GLOW_LINE_THICKNESS * scale * zoom);
            drawRingOutline(cx, cy, radius, style.getAccentColor(), alphaMult);
            return;
        }

        float gapRadius = donutGapRadius(scale, zoom);
        GL11.glLineWidth(NODE_CONNECTOR_LINE_THICKNESS * scale * zoom);
        drawRingOutline(cx, cy, radius - gapRadius, RING_DULL_COLOR, alphaMult * RING_DULL_ALPHA);
        drawRingOutline(cx, cy, radius + gapRadius, RING_DULL_COLOR, alphaMult * RING_DULL_ALPHA);
    }

    private void drawSingleDonut(float cx, float cy, float radius, boolean allocated, boolean breathing, float zoom, float alphaMult) {
        Color color;
        float alpha;
        if (allocated) {
            color = style.getAccentColor();
            alpha = alphaMult;
        } else if (breathing) {
            float breathingT = (float) (0.5 + 0.5 * Math.sin(2 * Math.PI * breathingPhase / BREATHING_PERIOD_SECONDS));
            color = style.getAccentColor();
            alpha = (BREATHING_MIN_ALPHA + (BREATHING_MAX_ALPHA - BREATHING_MIN_ALPHA) * breathingT) * alphaMult;
        } else {
            color = RING_DULL_COLOR;
            alpha = alphaMult * RING_DULL_ALPHA;
        }

        GL11.glLineWidth(NODE_CONNECTOR_GLOW_LINE_THICKNESS * zoom);
        drawRingOutline(cx, cy, radius, color, alpha);
    }

    private void drawRingStack(float cx, float cy, float outerRadius, String nodeId,
                                int count, float radiusDecay, float stateAlpha, float alphaMult) {
        drawRingStackPass(cx, cy, outerRadius, ringStacks.computeIfAbsent(nodeId, id -> generateRingInstances(id, count, radiusDecay)),
                Color.WHITE, 1f, stateAlpha, alphaMult);
        drawRingStackPass(cx, cy, outerRadius, pinkRingStacks.computeIfAbsent(nodeId + "_pink", id -> generateRingInstances(id, count, radiusDecay)),
                RING_PINK_COLOR, RING_PINK_SCALE_RATIO, stateAlpha, alphaMult);
    }

    private void drawRingStackPass(float cx, float cy, float outerRadius, List<RingInstance> instances,
                                    Color color, float scaleRatio, float stateAlpha, float alphaMult) {
        float alpha = RING_INSTANCE_BASE_ALPHA * stateAlpha * alphaMult;

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        for (RingInstance instance : instances) {
            String path = RING_STACK_TEXTURES[instance.textureIndex];
            if (!ensureTextureLoaded(path)) continue;

            float radius = outerRadius * scaleRatio * instance.radiusFraction;
            float jitterMag = radius * RING_INSTANCE_JITTER_RATIO;

            float angle = instance.baseAngleDeg + elapsedSeconds * instance.rotationSpeedDeg;
            float wanderRad = instance.jitterPhase + elapsedSeconds * instance.jitterSpeed;
            float jx = (float) Math.cos(wanderRad) * jitterMag;
            float jy = (float) Math.sin(wanderRad) * jitterMag;
            float size = radius * 2f * instance.sizeJitter;

            SpriteAPI sprite = Global.getSettings().getSprite(path);
            sprite.setSize(size, size);
            sprite.setAngle(angle);
            sprite.setColor(color);
            sprite.setAlphaMult(alpha);
            sprite.renderAtCenter(cx + jx, cy + jy);
        }
    }

    private void drawAmbientGlow(float cx, float cy, float footprintSize, float stateAlpha, float alphaMult) {
        if (!ensureTextureLoaded(GLOW_TEXTURE_PATH)) return;

        float size = footprintSize * AMBIENT_GLOW_SIZE_RATIO;

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        SpriteAPI sprite = Global.getSettings().getSprite(GLOW_TEXTURE_PATH);
        sprite.setSize(size, size);
        sprite.setColor(AMBIENT_GLOW_COLOR);
        sprite.setAlphaMult(AMBIENT_GLOW_ALPHA * stateAlpha * alphaMult);
        sprite.renderAtCenter(cx, cy);
    }

    private void drawKeystoneRingBelt(float cx, float cy, float footprintSize, float widthRatio, String ringArtPath, float stateAlpha, float alphaMult) {
        if (!ensureTextureLoaded(ringArtPath)) return;
        SpriteAPI sprite = Global.getSettings().getSprite(ringArtPath);
        RingBeltRenderer.render(sprite, cx, cy, beltInnerRadius(footprintSize), beltOuterRadius(footprintSize, widthRatio),
                Color.WHITE, stateAlpha * alphaMult);
    }

    private void drawKeystoneAuroraBelt(float cx, float cy, float footprintSize, float widthRatio, Color tint, float stateAlpha, float alphaMult) {
        if (!ensureTextureLoaded(AURORA_TEXTURE_PATH)) return;
        SpriteAPI sprite = Global.getSettings().getSprite(AURORA_TEXTURE_PATH);
        AuroraBeltRenderer.render(sprite, cx, cy, beltInnerRadius(footprintSize), beltOuterRadius(footprintSize, widthRatio),
                tint, stateAlpha * alphaMult, elapsedSeconds);
    }

    private void drawWormhole(float cx, float cy, float footprintSize, float alphaMult, String nodeId, Color color) {
        float rawOpenness = wormholeOpenness.computeIfAbsent(nodeId,
                id -> Boolean.TRUE.equals(wormholeAllocated.get(id)) ? 1f : 0f);
        float openness = rawOpenness * rawOpenness;
        float visualScale = WORMHOLE_MIN_SCALE + (1f - WORMHOLE_MIN_SCALE) * openness;
        float baseRadius = footprintSize / 2f * visualScale;

        drawWormholeCorona(cx, cy, baseRadius, color, alphaMult);

        if (openness < 1f) {
            drawWormholeBands(cx, cy, baseRadius, color, (1f - openness) * WORMHOLE_BAND_ALPHA * alphaMult);
        }

        if (openness > 0f) {
            drawRingStackPass(cx, cy, baseRadius * 2f * WORMHOLE_RING_OUTER_RADIUS_RATIO,
                    ringStacks.computeIfAbsent(nodeId, id -> generateRingInstances(id, WORMHOLE_RING_COUNT, WORMHOLE_RING_RADIUS_DECAY)),
                    color, 1f, openness, alphaMult);
        }

        if (openness < 1f) {
            drawWormholeGlow(cx, cy, baseRadius, color, alphaMult, openness);
        }
    }

    private void drawWormholeCorona(float cx, float cy, float baseRadius, Color color, float alphaMult) {
        if (!ensureTextureLoaded(WORMHOLE_CORONA_TEXTURE_PATH)) return;
        float size = baseRadius * 2f * WORMHOLE_CORONA_SIZE_RATIO;
        float orbit = baseRadius * WORMHOLE_CORONA_ORBIT_RATIO;

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        SpriteAPI sprite = Global.getSettings().getSprite(WORMHOLE_CORONA_TEXTURE_PATH);
        sprite.setColor(color);
        sprite.setAlphaMult(alphaMult);
        for (int i = 0; i < WORMHOLE_CORONA_COUNT; i++) {
            float baseAngle = 360f * i / WORMHOLE_CORONA_COUNT;
            float angle = baseAngle + elapsedSeconds * WORMHOLE_CORONA_ROTATION_SPEED_DEG;
            float rad = (float) Math.toRadians(angle);
            float pulse = 1f + (float) Math.sin(Math.toRadians(elapsedSeconds * WORMHOLE_CORONA_PULSE_SPEED_DEG + baseAngle)) * WORMHOLE_CORONA_PULSE_SIZE_RATIO;

            sprite.setSize(size * pulse, size * pulse);
            sprite.setAngle(angle);
            sprite.renderAtCenter(cx + (float) Math.cos(rad) * orbit, cy + (float) Math.sin(rad) * orbit);
        }
    }

    private void drawWormholeBands(float cx, float cy, float baseRadius, Color color, float alphaMult) {
        if (!ensureTextureLoaded(WORMHOLE_BAND_TEXTURE_PATH)) return;
        SpriteAPI texture = Global.getSettings().getSprite(WORMHOLE_BAND_TEXTURE_PATH);
        float innerRadius = baseRadius * WORMHOLE_BAND_INNER_RADIUS_RATIO;
        float outerRadius = innerRadius + baseRadius * WORMHOLE_BAND_THICKNESS_RATIO;
        float rotationA = (elapsedSeconds * WORMHOLE_BAND_ROTATION_SPEED_DEG) % 360f;
        float rotationB = (-elapsedSeconds * WORMHOLE_BAND_ROTATION_SPEED_DEG) % 360f;

        WormholeBandRenderer.render(texture, cx, cy, innerRadius, outerRadius, 0, rotationA, color, alphaMult, elapsedSeconds);
        WormholeBandRenderer.render(texture, cx, cy, innerRadius, outerRadius, 1, rotationB, color, alphaMult, elapsedSeconds);
    }

    private void drawWormholeGlow(float cx, float cy, float baseRadius, Color color, float alphaMult, float openness) {
        if (!ensureTextureLoaded(WORMHOLE_GLOW_TEXTURE_PATH)) return;
        float size = baseRadius * 2f * WORMHOLE_GLOW_SIZE_RATIO;
        float closedness = 1f - openness;
        float alpha = WORMHOLE_GLOW_ALPHA * closedness * closedness * closedness * alphaMult;

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        SpriteAPI sprite = Global.getSettings().getSprite(WORMHOLE_GLOW_TEXTURE_PATH);
        sprite.setColor(color);
        sprite.setSize(size, size);
        sprite.setAlphaMult(alpha);
        sprite.renderAtCenter(cx, cy);
        sprite.renderAtCenter(cx, cy);
    }

    private static Color resolveWormholeColor(SkillNode node) {
        return parseHexColor(node.getWormholeColor(), Color.WHITE, node.getId(), "wormholeColor");
    }

    private static Color parseHexColor(String hex, Color fallback, String nodeId, String fieldName) {
        if (hex == null || hex.isEmpty()) return fallback;
        try {
            String cleaned = hex.startsWith("#") ? hex.substring(1) : hex;
            if (cleaned.length() == 6) cleaned = "FF" + cleaned;
            long argb = Long.parseLong(cleaned, 16);
            return new Color((int) argb, true);
        } catch (NumberFormatException e) {
            Logger.getLogger(SkillTreeNodeRingRenderer.class).warn("Invalid " + fieldName + " \"" + hex + "\" on node \"" + nodeId + "\", using default");
            return fallback;
        }
    }

    private static String resolveRingBeltPath(SkillNode node) {
        String path = node.getRingBeltPath();
        return path != null && !path.isEmpty() ? path : DEFAULT_KEYSTONE_RING_BELT_PATH;
    }

    private static Color resolveRingBeltColor(SkillNode node) {
        return parseHexColor(node.getRingBeltColor(), DEFAULT_AURORA_COLOR, node.getId(), "ringBeltColor");
    }

    private static float resolveRingBeltWidth(SkillNode node) {
        Float width = node.getRingBeltWidth();
        return width != null && width > 0f ? width : KEYSTONE_BELT_WIDTH_RATIO;
    }

    private static List<RingInstance> generateRingInstances(String seedKey, int count, float radiusDecay) {
        Random random = new Random(seedKey.hashCode());
        List<RingInstance> instances = new ArrayList<>(count);
        float rawMin = (float) Math.pow(radiusDecay, count - 1);
        float rawRange = 1f - rawMin;
        for (int i = 0; i < count; i++) {
            RingInstance instance = new RingInstance();
            instance.baseAngleDeg = random.nextFloat() * 360f;
            float speed = RING_INSTANCE_MIN_ROTATION_SPEED_DEG
                    + random.nextFloat() * (RING_INSTANCE_MAX_ROTATION_SPEED_DEG - RING_INSTANCE_MIN_ROTATION_SPEED_DEG);
            instance.rotationSpeedDeg = random.nextBoolean() ? speed : -speed;
            float raw = (float) Math.pow(radiusDecay, i);
            float t = rawRange > 0.0001f ? (raw - rawMin) / rawRange : 1f;
            instance.radiusFraction = RING_MIN_RADIUS_FRACTION + (1f - RING_MIN_RADIUS_FRACTION) * t;
            instance.jitterPhase = random.nextFloat() * (float) (Math.PI * 2);
            instance.jitterSpeed = 0.5f + random.nextFloat();
            instance.textureIndex = i % RING_STACK_TEXTURES.length;
            instance.sizeJitter = 0.9f + random.nextFloat() * 0.2f;
            instances.add(instance);
        }
        return instances;
    }

    private static final class RingInstance {
        float baseAngleDeg;
        float rotationSpeedDeg;
        float radiusFraction;
        float jitterPhase;
        float jitterSpeed;
        int textureIndex;
        float sizeJitter;
    }

    private void drawRingOutline(float cx, float cy, float radius, Color color, float alpha) {
        Misc.setColor(color, alpha);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int i = 0; i < RING_SEGMENTS; i++) {
            float angle = (float) (2 * Math.PI * i / RING_SEGMENTS);
            GL11.glVertex2f(cx + (float) Math.cos(angle) * radius, cy + (float) Math.sin(angle) * radius);
        }
        GL11.glEnd();
    }

    private boolean ensureTextureLoaded(String spritePath) {
        if (loadedSprites.add(spritePath)) {
            try {
                Global.getSettings().loadTexture(spritePath);
            } catch (IOException e) {
                Logger.getLogger(SkillTreeNodeRingRenderer.class).error("Failed to load texture " + spritePath, e);
                return false;
            }
        }
        return true;
    }
}
