package exiledsector.ui;

import org.lazywizard.lazylib.ui.LazyFont;

import java.awt.Color;

final class SkillTreeInfoTooltipRenderer {

    private static final float TOOLTIP_MAX_TEXT_WIDTH = 480f;
    private static final float TOOLTIP_MAX_TITLE_HEIGHT = 200f;
    private static final float TOOLTIP_MAX_BODY_HEIGHT = 800f;
    private static final float TOOLTIP_WIDTH_SAFETY_MARGIN = 8f;
    private static final float TOOLTIP_PADDING = 10f;
    private static final float TOOLTIP_TITLE_BODY_GAP = 6f;
    private static final float TOOLTIP_CURSOR_OFFSET = 18f;
    private static final float TOOLTIP_TITLE_BOLD_OFFSET = 1f;
    private static final Color TOOLTIP_TITLE_COLOR = Color.WHITE;

    private final SkillTreePanelStyle style;
    private String cachedTitleText;
    private String cachedBodyText;
    private SkillTreePanelStyle.TooltipText title;
    private SkillTreePanelStyle.TooltipText body;

    SkillTreeInfoTooltipRenderer(SkillTreePanelStyle style) {
        this.style = style;
    }

    void render(String titleText, String bodyText, float mouseX, float mouseY, float alphaMult) {
        LazyFont font = style.getFont();
        if (font == null) return;

        if (title == null || !titleText.equals(cachedTitleText)) {
            title = SkillTreePanelStyle.buildWrappedText(font, titleText, SkillTreePanelStyle.TOOLTIP_TITLE_FONT_SIZE,
                    TOOLTIP_MAX_TEXT_WIDTH, TOOLTIP_MAX_TITLE_HEIGHT, TOOLTIP_TITLE_COLOR);
            cachedTitleText = titleText;
        }
        if (body == null || !bodyText.equals(cachedBodyText)) {
            body = SkillTreePanelStyle.buildWrappedText(font, bodyText, SkillTreePanelStyle.TOOLTIP_BODY_FONT_SIZE,
                    TOOLTIP_MAX_TEXT_WIDTH, TOOLTIP_MAX_BODY_HEIGHT, SkillTreePanelStyle.TOOLTIP_BODY_COLOR);
            cachedBodyText = bodyText;
        }

        float boxWidth = Math.max(title.width, body.width) + TOOLTIP_PADDING * 2f + TOOLTIP_WIDTH_SAFETY_MARGIN;
        float boxHeight = title.height + TOOLTIP_TITLE_BODY_GAP + body.height + TOOLTIP_PADDING * 2f;
        float boxX = mouseX + TOOLTIP_CURSOR_OFFSET;
        float boxY = mouseY - boxHeight - TOOLTIP_CURSOR_OFFSET;

        style.drawTooltipBackground(boxX, boxY, boxWidth, boxHeight, alphaMult, style.getAccentColor());

        float titleY = boxY + boxHeight - TOOLTIP_PADDING;
        float bodyY = titleY - title.height - TOOLTIP_TITLE_BODY_GAP;
        float titleX = boxX + (boxWidth - title.width) / 2f;
        title.drawable.draw(titleX, titleY);
        title.drawable.draw(titleX + TOOLTIP_TITLE_BOLD_OFFSET, titleY);
        body.drawable.draw(boxX + TOOLTIP_PADDING, bodyY);
    }
}
