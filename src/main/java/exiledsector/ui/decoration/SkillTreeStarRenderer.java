package exiledsector.ui.decoration;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.util.Misc;
import exiledsector.skills.SkillTree;
import exiledsector.ui.util.SpriteCache;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Sphere;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillTreeStarRenderer {

    private static final int SPHERE_DETAIL = 32;
    private static final float RIM_ALPHA_MULT = 0.37f;
    private static final String FALLBACK_STAR_TYPE = "star_yellow";

    private final SpriteCache spriteCache = new SpriteCache(SkillTreeStarRenderer.class);
    private final Sphere sphere = new Sphere();
    private final Map<String, Float> angleById = new HashMap<>();
    private Map<String, PlanetSpecAPI> specsByType;

    public SkillTreeStarRenderer() {
        sphere.setTextureFlag(true);
    }

    public void advance(float amount) {
        if (amount <= 0f) return;
        for (Star star : SkillTree.getStars()) {
            PlanetSpecAPI spec = resolveSpec(star.getStarType());
            if (spec == null) continue;
            float angle = normalizeAngle(angleById.getOrDefault(star.getId(), 0f) + spec.getRotation() * amount);
            angleById.put(star.getId(), angle);
        }
    }

    public void renderDisc(float centerX, float centerY, float zoom, float alphaMult, PositionAPI position) {
        if (position == null) return;
        List<Star> stars = SkillTree.getStars();
        if (stars.isEmpty()) return;

        for (Star star : stars) {
            PlanetSpecAPI spec = resolveSpec(star.getStarType());
            if (spec == null) continue;
            String texturePath = spec.getTexture();
            if (texturePath == null || texturePath.isEmpty() || !spriteCache.ensureLoaded(texturePath)) continue;

            SpriteAPI texture = Global.getSettings().getSprite(texturePath);
            float screenX = centerX + star.getX() * zoom;
            float screenY = centerY - star.getY() * zoom;
            float radius = star.getRadius() * zoom;
            float angle = angleById.getOrDefault(star.getId(), 0f);
            Color discColor = resolveColor(star, spec.getPlanetColor());

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_CULL_FACE);
            // The panel's screen-space projection mirrors world Y (see the -y flip below), which
            // reverses the sphere mesh's apparent winding relative to vanilla's unflipped 3D view.
            // TODO: if the star renders inside-out in-game, swap GL11.GL_CW <-> GL11.GL_CCW here.
            GL11.glFrontFace(GL11.GL_CW);
            GL11.glCullFace(GL11.GL_BACK);

            GL11.glPushMatrix();
            GL11.glTranslatef(screenX, screenY, 0f);
            GL11.glRotatef(spec.getTilt(), 0f, 0f, 1f);
            GL11.glRotatef(spec.getPitch(), 1f, 0f, 0f);
            GL11.glRotatef(angle, 0f, 1f, 0f);
            GL11.glRotatef(-90f, 1f, 0f, 0f);
            texture.bindTexture();

            Misc.setColor(discColor, alphaMult);
            sphere.draw(radius, SPHERE_DETAIL, SPHERE_DETAIL);
            Misc.setColor(discColor, alphaMult * RIM_ALPHA_MULT);
            sphere.draw(radius + 0.25f * zoom, SPHERE_DETAIL, SPHERE_DETAIL);
            sphere.draw(radius + 0.5f * zoom, SPHERE_DETAIL, SPHERE_DETAIL);

            GL11.glPopMatrix();

            GL11.glFrontFace(GL11.GL_CCW);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
        }
    }

    public void renderGlow(float centerX, float centerY, float zoom, float alphaMult, PositionAPI position) {
        if (position == null) return;
        List<Star> stars = SkillTree.getStars();
        if (stars.isEmpty()) return;

        for (Star star : stars) {
            PlanetSpecAPI spec = resolveSpec(star.getStarType());
            if (spec == null) continue;
            String coronaPath = spec.getCoronaTexture();
            if (coronaPath == null || coronaPath.isEmpty() || !spriteCache.ensureLoaded(coronaPath)) continue;

            float radius = star.getRadius() * zoom;
            float haloRadius = star.getRadius() * spec.getCoronaSize() * zoom;
            if (haloRadius <= radius) continue;

            SpriteAPI corona = Global.getSettings().getSprite(coronaPath);
            float screenX = centerX + star.getX() * zoom;
            float screenY = centerY - star.getY() * zoom;
            Color coronaColor = resolveColor(star, spec.getCoronaColor());

            corona.setBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            corona.setColor(coronaColor);
            corona.setAlphaMult(alphaMult);
            corona.setSize(haloRadius * 2f, haloRadius * 2f);
            corona.renderAtCenter(screenX, screenY);
        }
    }

    private static Color resolveColor(Star star, Color fallback) {
        String hex = star.getColor();
        if (hex == null || hex.isEmpty()) return fallback;
        try {
            String cleaned = hex.startsWith("#") ? hex.substring(1) : hex;
            if (cleaned.length() == 6) cleaned = "FF" + cleaned;
            long argb = Long.parseLong(cleaned, 16);
            Color parsed = new Color((int) argb, true);
            return new Color(parsed.getRed(), parsed.getGreen(), parsed.getBlue(), fallback.getAlpha());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private PlanetSpecAPI resolveSpec(String starType) {
        if (specsByType == null) {
            specsByType = new HashMap<>();
            for (PlanetSpecAPI spec : Global.getSettings().getAllPlanetSpecs()) {
                specsByType.put(spec.getPlanetType(), spec);
            }
        }
        PlanetSpecAPI spec = specsByType.get(starType);
        return spec != null ? spec : specsByType.get(FALLBACK_STAR_TYPE);
    }

    private static float normalizeAngle(float angle) {
        angle %= 360f;
        return angle < 0f ? angle + 360f : angle;
    }
}
