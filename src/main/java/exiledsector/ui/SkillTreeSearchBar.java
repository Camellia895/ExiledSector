package exiledsector.ui;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import exiledsector.ui.node.NodeSearch;
import exiledsector.ui.util.BorderedPanel;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.input.Keyboard;

import java.awt.Color;

final class SkillTreeSearchBar {

    static final int MAX_QUERY_LENGTH = 32;
    private static final float WIDTH = 360f;
    private static final float HEIGHT = 40f;
    private static final float TOP_MARGIN = 16f;
    private static final float TEXT_PADDING = 22f;
    private static final float FONT_SIZE = SkillTreePanelStyle.TOOLTIP_BODY_FONT_SIZE;
    private static final float CARET_BLINK_SECONDS = 0.5f;
    private static final String PLACEHOLDER = "Search nodes...";
    private static final String CARET = "|";
    private static final Color TEXT_COLOR = SkillTreePanelStyle.TOOLTIP_BODY_COLOR;
    private static final Color PLACEHOLDER_COLOR = new Color(130, 130, 130);

    private final NodeSearch search;
    private final SkillTreePanelStyle style;
    private final BorderedPanel panel = new BorderedPanel(SkillTreeSearchBar.class);
    private LazyFont.DrawableString text;
    private String renderedText;
    private Color renderedColor;
    private boolean focused;
    private float caretSeconds;

    SkillTreeSearchBar(NodeSearch search, SkillTreePanelStyle style) {
        this.search = search;
        this.style = style;
    }

    boolean isFocused() {
        return focused;
    }

    boolean handleClick(PositionAPI position, float x, float y) {
        focused = contains(position, x, y);
        caretSeconds = 0f;
        return focused;
    }

    boolean handleKey(InputEventAPI event) {
        if (!focused) {
            return false;
        }
        if (event.isKeyDownEvent()) {
            handleKeyDown(event.getEventValue(), event.getEventChar());
        }
        return true;
    }

    private void handleKeyDown(int keyCode, char character) {
        String query = search.getQuery();
        if (keyCode == Keyboard.KEY_BACK) {
            search.setQuery(query.isEmpty() ? query : query.substring(0, query.length() - 1));
        } else if (keyCode == Keyboard.KEY_ESCAPE) {
            search.setQuery("");
            focused = false;
        } else if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            focused = false;
        } else if (isPrintable(character) && query.length() < MAX_QUERY_LENGTH) {
            search.setQuery(query + character);
        }
        caretSeconds = 0f;
    }

    private static boolean isPrintable(char character) {
        return !Character.isISOControl(character);
    }

    void advance(float amount) {
        caretSeconds += amount;
    }

    void render(PositionAPI position, float alphaMult) {
        float x = left(position);
        float y = bottom(position);
        panel.draw(x, y, WIDTH, HEIGHT, alphaMult);
        LazyFont.DrawableString drawable = textFor(displayText());
        if (drawable == null) {
            return;
        }
        Color color = search.getQuery().isEmpty() && !focused ? PLACEHOLDER_COLOR : TEXT_COLOR;
        if (!color.equals(renderedColor)) {
            drawable.setBaseColor(color);
            renderedColor = color;
        }
        drawable.draw(x + TEXT_PADDING, y + HEIGHT / 2f + FONT_SIZE / 2f);
    }

    private String displayText() {
        String query = search.getQuery();
        if (!focused) {
            return query.isEmpty() ? PLACEHOLDER : query;
        }
        boolean caretVisible = ((int) (caretSeconds / CARET_BLINK_SECONDS)) % 2 == 0;
        return caretVisible ? query + CARET : query;
    }

    private LazyFont.DrawableString textFor(String value) {
        LazyFont font = style.getFont();
        if (font == null) {
            return null;
        }
        if (text == null) {
            text = SkillTreePanelStyle.buildSimpleText(font, value, FONT_SIZE, TEXT_COLOR);
            text.setMaxWidth(WIDTH - TEXT_PADDING * 2f);
        } else if (!value.equals(renderedText)) {
            text.setText(value);
        }
        renderedText = value;
        return text;
    }

    private static boolean contains(PositionAPI position, float x, float y) {
        float left = left(position);
        float bottom = bottom(position);
        return x >= left && x <= left + WIDTH && y >= bottom && y <= bottom + HEIGHT;
    }

    private static float left(PositionAPI position) {
        return position.getX() + (position.getWidth() - WIDTH) / 2f;
    }

    private static float bottom(PositionAPI position) {
        return position.getY() + position.getHeight() - TOP_MARGIN - HEIGHT;
    }
}
