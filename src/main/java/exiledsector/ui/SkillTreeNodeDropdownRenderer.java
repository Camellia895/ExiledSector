package exiledsector.ui;

import com.fs.starfarer.api.util.Misc;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static exiledsector.ui.SkillTreePanelStyle.FONT_LINE_HEIGHT_FACTOR;
import static exiledsector.ui.SkillTreePanelStyle.GLOW_COLOR;
import static exiledsector.ui.SkillTreePanelStyle.TOOLTIP_BODY_COLOR;
import static exiledsector.ui.SkillTreePanelStyle.TOOLTIP_BODY_FONT_SIZE;

final class SkillTreeNodeDropdownRenderer {

    private static final float DROPDOWN_FONT_SIZE = TOOLTIP_BODY_FONT_SIZE;
    private static final float DROPDOWN_ROW_PADDING = 8f;
    private static final float DROPDOWN_ROW_GAP = 2f;
    private static final float DROPDOWN_TOP_OFFSET = 24f;
    private static final float DROPDOWN_HOVER_ALPHA = 0.35f;

    private final SkillTreePanelStyle style;
    private final Map<String, LazyFont.DrawableString> dropdownRowText = new HashMap<>();
    private SkillNode openNode;

    SkillTreeNodeDropdownRenderer(SkillTreePanelStyle style) {
        this.style = style;
    }

    boolean isOpen() {
        return openNode != null;
    }

    void open(SkillNode node) {
        openNode = node;
    }

    SkillNode getOpenNode() {
        return openNode;
    }

    void close() {
        openNode = null;
    }

    SkillType findOptionAt(float centerX, float centerY, float zoom, float x, float y) {
        LazyFont font = style.getFont();
        if (font == null) return null;
        for (DropdownRow row : computeRows(centerX, centerY, zoom, font)) {
            if (row.contains(x, y)) {
                return row.option;
            }
        }
        return null;
    }

    void render(float centerX, float centerY, float zoom, float mouseX, float mouseY, boolean mouseKnown, float alphaMult) {
        if (openNode == null) return;
        LazyFont font = style.getFont();
        if (font == null) return;

        List<DropdownRow> rows = computeRows(centerX, centerY, zoom, font);
        if (rows.isEmpty()) return;

        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (DropdownRow row : rows) {
            minX = Math.min(minX, row.x);
            minY = Math.min(minY, row.y);
            maxX = Math.max(maxX, row.x + row.width);
            maxY = Math.max(maxY, row.y + row.height);
        }
        style.drawTooltipBackground(minX, minY, maxX - minX, maxY - minY, alphaMult, style.getAccentColor());

        for (DropdownRow row : rows) {
            if (mouseKnown && row.contains(mouseX, mouseY)) {
                drawDropdownRowHighlight(row, alphaMult);
            }
            LazyFont.DrawableString text = getDropdownRowText(font, row.option);
            float textY = row.y + row.height / 2f + (DROPDOWN_FONT_SIZE * FONT_LINE_HEIGHT_FACTOR) / 2f;
            text.draw(row.x + DROPDOWN_ROW_PADDING, textY);
        }
    }

    private List<DropdownRow> computeRows(float centerX, float centerY, float zoom, LazyFont font) {
        List<DropdownRow> rows = new ArrayList<>();
        if (openNode == null) return rows;

        List<SkillType> options = new ArrayList<>();
        for (String optionId : openNode.getType().getOptionalOptionIds()) {
            SkillType option = SkillTree.getType(optionId);
            if (option != null) options.add(option);
        }
        if (options.isEmpty()) return rows;

        float nodeX = centerX + openNode.getOffsetX() * zoom;
        float nodeY = centerY - openNode.getOffsetY() * zoom;

        float width = 0f;
        for (SkillType option : options) {
            width = Math.max(width, font.calcWidth(option.getDisplayName(), DROPDOWN_FONT_SIZE));
        }
        width += DROPDOWN_ROW_PADDING * 2f;

        float rowHeight = DROPDOWN_FONT_SIZE * FONT_LINE_HEIGHT_FACTOR + DROPDOWN_ROW_PADDING * 2f;
        float x = nodeX - width / 2f;
        float topY = nodeY - DROPDOWN_TOP_OFFSET;

        for (int i = 0; i < options.size(); i++) {
            float rowTop = topY - i * (rowHeight + DROPDOWN_ROW_GAP);
            rows.add(new DropdownRow(options.get(i), x, rowTop - rowHeight, width, rowHeight));
        }
        return rows;
    }

    private void drawDropdownRowHighlight(DropdownRow row, float alphaMult) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Misc.setColor(GLOW_COLOR, DROPDOWN_HOVER_ALPHA * alphaMult);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(row.x, row.y);
        GL11.glVertex2f(row.x + row.width, row.y);
        GL11.glVertex2f(row.x + row.width, row.y + row.height);
        GL11.glVertex2f(row.x, row.y + row.height);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_BLEND);
    }

    private LazyFont.DrawableString getDropdownRowText(LazyFont font, SkillType option) {
        return dropdownRowText.computeIfAbsent(option.getId(), id -> {
            LazyFont.DrawableString text = font.createText(option.getDisplayName(), TOOLTIP_BODY_COLOR, DROPDOWN_FONT_SIZE);
            text.setAnchor(LazyFont.TextAnchor.TOP_LEFT);
            text.setAlignment(LazyFont.TextAlignment.LEFT);
            return text;
        });
    }

    private static final class DropdownRow {
        final SkillType option;
        final float x;
        final float y;
        final float width;
        final float height;

        DropdownRow(SkillType option, float x, float y, float width, float height) {
            this.option = option;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        boolean contains(float px, float py) {
            return px >= x && px <= x + width && py >= y && py <= y + height;
        }
    }
}
