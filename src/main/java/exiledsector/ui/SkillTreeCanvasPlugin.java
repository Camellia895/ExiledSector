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
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SkillTreeCanvasPlugin extends BaseCustomUIPanelPlugin {

    private static final float MIN_ZOOM = 0.4f;
    private static final float MAX_ZOOM = 2.5f;
    private static final float ZOOM_STEP = 1.1f;

    private static final float SYMBOL_SIZE = 128f;
    private static final float NODE_SIZE = 64f;

    private static final int VIGNETTE_SEGMENTS = 48;
    private static final float VIGNETTE_INNER_FRACTION = 0.88f;
    private static final float VIGNETTE_OUTER_FRACTION = 1f;
    private static final float VIGNETTE_MARGIN_FRACTION = 0.375f;

    private static final String TOOLTIP_FONT_PATH = "graphics/fonts/insignia15LTaa.fnt";
    private static final float TOOLTIP_TITLE_FONT_SIZE = 16f;
    private static final float TOOLTIP_BODY_FONT_SIZE = 14f;
    private static final float TOOLTIP_MAX_TEXT_WIDTH = 240f;
    private static final float TOOLTIP_MAX_TEXT_HEIGHT = 200f;
    private static final float TOOLTIP_WIDTH_SAFETY_MARGIN = 8f;
    private static final float TOOLTIP_LINE_HEIGHT_FACTOR = 1.25f;
    private static final float TOOLTIP_PADDING = 10f;
    private static final float TOOLTIP_TITLE_BODY_GAP = 6f;
    private static final float TOOLTIP_CURSOR_OFFSET = 18f;
    private static final Color TOOLTIP_TITLE_COLOR = Color.WHITE;
    private static final Color TOOLTIP_BODY_COLOR = Color.LIGHT_GRAY;
    private static final Color TOOLTIP_BACKGROUND_COLOR = Color.BLACK;
    private static final float TOOLTIP_BORDER_THICKNESS = 2f;
    private static final Color DEFAULT_TOOLTIP_BORDER_COLOR = Color.LIGHT_GRAY;
    private static final int COLOR_QUANTIZE_STEP = 24;
    private static final int MIN_ALPHA_TO_SAMPLE = 128;

    private final String symbolPath;
    private final Set<String> loadedSprites = new HashSet<>();

    private final Map<String, TooltipText> tooltipTitles = new HashMap<>();
    private final Map<String, TooltipText> tooltipBodies = new HashMap<>();
    private LazyFont tooltipFont;
    private boolean tooltipFontLoadFailed = false;
    private Color symbolDominantColor;

    private PositionAPI position;
    private boolean dragging = false;
    private float panX = 0f;
    private float panY = 0f;
    private float zoom = 1f;
    private float mouseX = 0f;
    private float mouseY = 0f;
    private boolean mouseKnown = false;

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
            } else if (event.isMouseMoveEvent()) {
                mouseX = event.getX();
                mouseY = event.getY();
                mouseKnown = true;
                if (dragging) {
                    panX += event.getDX();
                    panY += event.getDY();
                    event.consume();
                }
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

        SkillNode hovered = null;
        for (SkillNode node : SkillTree.getAllNodes().values()) {
            float nodeX = centerX + node.getOffsetX() * zoom;
            float nodeY = centerY - node.getOffsetY() * zoom;
            drawIcon(node.getIconPath(), nodeX, nodeY, NODE_SIZE * zoom, alphaMult);

            if (!dragging && mouseKnown && isMouseOverNode(nodeX, nodeY)) {
                hovered = node;
            }
        }

        if (hovered != null) {
            renderTooltip(hovered, alphaMult);
        }
    }

    private boolean isMouseOverNode(float nodeX, float nodeY) {
        float halfSize = NODE_SIZE * zoom / 2f;
        return Math.abs(mouseX - nodeX) <= halfSize && Math.abs(mouseY - nodeY) <= halfSize;
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

    private void renderTooltip(SkillNode node, float alphaMult) {
        LazyFont font = getTooltipFont();
        if (font == null) return;

        TooltipText title = tooltipTitles.computeIfAbsent(node.getId(),
                id -> buildTooltipText(font, node.getDisplayName(), TOOLTIP_TITLE_FONT_SIZE, TOOLTIP_TITLE_COLOR));
        TooltipText body = tooltipBodies.computeIfAbsent(node.getId(),
                id -> buildTooltipText(font, node.getDescription(), TOOLTIP_BODY_FONT_SIZE, TOOLTIP_BODY_COLOR));

        float boxWidth = Math.max(title.width, body.width) + TOOLTIP_PADDING * 2f + TOOLTIP_WIDTH_SAFETY_MARGIN;
        float boxHeight = title.height + TOOLTIP_TITLE_BODY_GAP + body.height + TOOLTIP_PADDING * 2f;
        float boxX = mouseX + TOOLTIP_CURSOR_OFFSET;
        float boxY = mouseY - boxHeight - TOOLTIP_CURSOR_OFFSET;

        drawTooltipBackground(boxX, boxY, boxWidth, boxHeight, alphaMult, getSymbolDominantColor());

        float titleY = boxY + boxHeight - TOOLTIP_PADDING;
        float bodyY = titleY - title.height - TOOLTIP_TITLE_BODY_GAP;
        title.drawable.draw(boxX + TOOLTIP_PADDING, titleY);
        body.drawable.draw(boxX + TOOLTIP_PADDING, bodyY);
    }

    private TooltipText buildTooltipText(LazyFont font, String rawText, float fontSize, Color color) {
        String wrapped = font.wrapString(rawText, fontSize, TOOLTIP_MAX_TEXT_WIDTH, TOOLTIP_MAX_TEXT_HEIGHT);
        String[] lines = wrapped.split("\n", -1);

        float width = 0f;
        for (String line : lines) {
            width = Math.max(width, font.calcWidth(line, fontSize));
        }
        float height = lines.length * fontSize * TOOLTIP_LINE_HEIGHT_FACTOR;

        LazyFont.DrawableString drawable = font.createText(wrapped, color, fontSize);
        drawable.setAlignment(LazyFont.TextAlignment.LEFT);
        drawable.setAnchor(LazyFont.TextAnchor.TOP_LEFT);
        return new TooltipText(drawable, width, height);
    }

    private static final class TooltipText {
        final LazyFont.DrawableString drawable;
        final float width;
        final float height;

        TooltipText(LazyFont.DrawableString drawable, float width, float height) {
            this.drawable = drawable;
            this.width = width;
            this.height = height;
        }
    }

    private LazyFont getTooltipFont() {
        if (tooltipFont == null && !tooltipFontLoadFailed) {
            try {
                tooltipFont = LazyFont.loadFont(TOOLTIP_FONT_PATH);
            } catch (FontException e) {
                Logger.getLogger(SkillTreeCanvasPlugin.class).error("Failed to load tooltip font " + TOOLTIP_FONT_PATH, e);
                tooltipFontLoadFailed = true;
            }
        }
        return tooltipFont;
    }

    private void drawTooltipBackground(float x, float y, float width, float height, float alphaMult, Color borderColor) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Misc.setColor(TOOLTIP_BACKGROUND_COLOR, alphaMult);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();

        Misc.setColor(borderColor, alphaMult);
        GL11.glLineWidth(TOOLTIP_BORDER_THICKNESS);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
    }

    private Color getSymbolDominantColor() {
        if (symbolDominantColor == null) {
            symbolDominantColor = computeDominantColor(symbolPath);
        }
        return symbolDominantColor;
    }

    private Color computeDominantColor(String path) {
        try (InputStream in = Global.getSettings().openStream(path)) {
            BufferedImage image = ImageIO.read(in);
            if (image == null) return DEFAULT_TOOLTIP_BORDER_COLOR;

            Map<Integer, Integer> bucketCounts = new HashMap<>();
            Map<Integer, int[]> bucketSums = new HashMap<>();
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    int alpha = (argb >>> 24) & 0xFF;
                    if (alpha < MIN_ALPHA_TO_SAMPLE) continue;

                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int b = argb & 0xFF;
                    int bucket = (quantize(r) << 16) | (quantize(g) << 8) | quantize(b);

                    bucketCounts.merge(bucket, 1, Integer::sum);
                    int[] sum = bucketSums.computeIfAbsent(bucket, key -> new int[3]);
                    sum[0] += r;
                    sum[1] += g;
                    sum[2] += b;
                }
            }

            if (bucketCounts.isEmpty()) return DEFAULT_TOOLTIP_BORDER_COLOR;

            Map.Entry<Integer, Integer> mostCommon = null;
            for (Map.Entry<Integer, Integer> entry : bucketCounts.entrySet()) {
                if (mostCommon == null || entry.getValue() > mostCommon.getValue()) {
                    mostCommon = entry;
                }
            }

            int[] sum = bucketSums.get(mostCommon.getKey());
            int pixelCount = mostCommon.getValue();
            return new Color(sum[0] / pixelCount, sum[1] / pixelCount, sum[2] / pixelCount);
        } catch (IOException e) {
            Logger.getLogger(SkillTreeCanvasPlugin.class).error("Failed to read " + path + " for tooltip border colour", e);
            return DEFAULT_TOOLTIP_BORDER_COLOR;
        }
    }

    private static int quantize(int channel) {
        return (channel / COLOR_QUANTIZE_STEP) * COLOR_QUANTIZE_STEP;
    }

    private static float boundaryRadius(float cos, float sin, float halfWidth, float halfHeight) {
        float rx = cos != 0f ? halfWidth / Math.abs(cos) : Float.MAX_VALUE;
        float ry = sin != 0f ? halfHeight / Math.abs(sin) : Float.MAX_VALUE;
        return Math.min(rx, ry);
    }
}
