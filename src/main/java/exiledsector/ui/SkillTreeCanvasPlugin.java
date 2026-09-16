package exiledsector.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.util.Misc;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTree;
import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Renders the whole skill tree - central ship symbol plus every node - as a
 * single pannable, zoomable canvas. A real tree has far more content than
 * the static panel can show at once, so everything here is positioned
 * relative to one shared camera (panX/panY/zoom) rather than each icon being
 * its own independently laid-out UI element, which doesn't lend itself to
 * being rescaled/repositioned every frame. Click-and-drag pans; the scroll
 * wheel zooms, both only while the cursor is actually over the canvas.
 */
public class SkillTreeCanvasPlugin extends BaseCustomUIPanelPlugin {

    private static final float MIN_ZOOM = 0.4f;
    private static final float MAX_ZOOM = 2.5f;
    private static final float ZOOM_STEP = 1.1f;

    private static final float SYMBOL_SIZE = 128f;
    private static final float NODE_SIZE = 64f;

    private static final int VIGNETTE_SEGMENTS = 48;
    private static final float VIGNETTE_INNER_FRACTION = 0.88f;
    private static final float VIGNETTE_OUTER_FRACTION = 1f;
    // Extra radius (beyond the icon's own half-size) the vignette's solid
    // black fill extends to - covers sprites observed rendering wider than
    // their nominal box (see CircularVignettePlugin's original investigation).
    private static final float VIGNETTE_MARGIN_FRACTION = 0.375f;

    private final String symbolPath;
    // Sprites need Global.getSettings().loadTexture(path) called at least
    // once before getSprite(path) has anything to actually render - some
    // paths (like hullmod icons) happen to already be preloaded elsewhere
    // by the game and render fine without it, but others (faction crests)
    // aren't, and silently render nothing until loaded. Tracked per-instance
    // so it's only done once per path rather than every frame.
    private final Set<String> loadedSprites = new HashSet<>();

    private PositionAPI position;
    private boolean dragging = false;
    private float panX = 0f;
    private float panY = 0f;
    private float zoom = 1f;

    public SkillTreeCanvasPlugin(String symbolPath) {
        this.symbolPath = symbolPath;
    }

    @Override
    public void positionChanged(PositionAPI position) {
        this.position = position;
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        if (position == null) return;

        for (InputEventAPI event : events) {
            if (event.isConsumed()) continue;

            if (event.isLMBDownEvent() && position.containsEvent(event)) {
                dragging = true;
                event.consume();
            } else if (event.isLMBUpEvent()) {
                dragging = false;
            } else if (event.isMouseMoveEvent() && dragging) {
                panX += event.getDX();
                panY += event.getDY();
                event.consume();
            } else if (event.isMouseScrollEvent() && position.containsEvent(event)) {
                if (event.getEventValue() > 0) {
                    zoom = Math.min(MAX_ZOOM, zoom * ZOOM_STEP);
                } else {
                    zoom = Math.max(MIN_ZOOM, zoom / ZOOM_STEP);
                }
                event.consume();
            }
        }
    }

    @Override
    public void render(float alphaMult) {
        if (position == null) return;

        float centerX = position.getX() + position.getWidth() / 2f + panX;
        float centerY = position.getY() + position.getHeight() / 2f + panY;

        drawIcon(symbolPath, centerX, centerY, SYMBOL_SIZE * zoom, alphaMult);

        for (SkillNode node : SkillTree.getAllNodes().values()) {
            float nodeX = centerX + node.getOffsetX() * zoom;
            float nodeY = centerY + node.getOffsetY() * zoom;
            drawIcon(node.getIconPath(), nodeX, nodeY, NODE_SIZE * zoom, alphaMult);
        }
    }

    private void drawIcon(String spritePath, float cx, float cy, float size, float alphaMult) {
        if (loadedSprites.add(spritePath)) {
            try {
                Global.getSettings().loadTexture(spritePath);
            } catch (IOException e) {
                Logger.getLogger(SkillTreeCanvasPlugin.class).error("Failed to load texture " + spritePath, e);
                return;
            }
        }
        SpriteAPI sprite = Global.getSettings().getSprite(spritePath);
        sprite.setSize(size, size);
        sprite.setAlphaMult(alphaMult);
        sprite.renderAtCenter(cx, cy);

        drawVignette(cx, cy, size, alphaMult);
    }

    /** Same circular fade-to-black technique as the original CircularVignettePlugin, inlined for a per-frame direct-draw call instead of a nested UI element. */
    private void drawVignette(float cx, float cy, float iconSize, float alphaMult) {
        float half = iconSize / 2f;
        float innerRadius = half * VIGNETTE_INNER_FRACTION;
        float outerRadius = half * VIGNETTE_OUTER_FRACTION;
        float margin = iconSize * VIGNETTE_MARGIN_FRACTION;
        float boxHalfWidth = half + margin;
        float boxHalfHeight = half + margin;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Ring 1: the actual fade, transparent -> opaque, fixed radii.
        GL11.glBegin(GL11.GL_QUAD_STRIP);
        for (int i = 0; i <= VIGNETTE_SEGMENTS; i++) {
            float angle = (float) (2 * Math.PI * i / VIGNETTE_SEGMENTS);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            Misc.setColor(Color.BLACK, 0f);
            GL11.glVertex2f(cx + cos * innerRadius, cy + sin * innerRadius);

            Misc.setColor(Color.BLACK, alphaMult);
            GL11.glVertex2f(cx + cos * outerRadius, cy + sin * outerRadius);
        }
        GL11.glEnd();

        // Ring 2: solid black from the outer fade radius out to a padded
        // box boundary, so any sprite overflow beyond its nominal size is
        // still masked (see VIGNETTE_MARGIN_FRACTION).
        Misc.setColor(Color.BLACK, alphaMult);
        GL11.glBegin(GL11.GL_QUAD_STRIP);
        for (int i = 0; i <= VIGNETTE_SEGMENTS; i++) {
            float angle = (float) (2 * Math.PI * i / VIGNETTE_SEGMENTS);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            float boundary = boundaryRadius(cos, sin, boxHalfWidth, boxHalfHeight);

            GL11.glVertex2f(cx + cos * outerRadius, cy + sin * outerRadius);
            GL11.glVertex2f(cx + cos * boundary, cy + sin * boundary);
        }
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
    }

    private static float boundaryRadius(float cos, float sin, float halfWidth, float halfHeight) {
        float rx = cos != 0f ? halfWidth / Math.abs(cos) : Float.MAX_VALUE;
        float ry = sin != 0f ? halfHeight / Math.abs(sin) : Float.MAX_VALUE;
        return Math.min(rx, ry);
    }
}
