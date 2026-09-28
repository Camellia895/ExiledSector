package exiledsector.ui;

import exiledsector.skills.DescriptionLine;
import org.junit.jupiter.api.Test;
import org.lazywizard.lazylib.ui.LazyFont;
import org.mockito.InOrder;

import java.awt.Color;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HighlightedTooltipTextTest {

    private static final Color GREEN = SkillTreePanelStyle.POSITIVE_STAT_COLOR;
    private static final Color RED = SkillTreePanelStyle.NEGATIVE_STAT_COLOR;

    private LazyFont.DrawableString build(DescriptionLine... paragraphs) {
        LazyFont font = mock(LazyFont.class);
        LazyFont.DrawableString drawable = mock(LazyFont.DrawableString.class);
        when(font.wrapString(anyString(), anyFloat(), anyFloat(), anyFloat())).thenAnswer(call -> call.getArgument(0));
        when(font.createText(anyString(), any(Color.class), anyFloat())).thenReturn(drawable);
        new SkillTreePanelStyle().buildHighlightedWrappedText(font, List.of(paragraphs), 20f, 480f, 800f, Color.WHITE);
        return drawable;
    }

    @Test
    void colourChangesArePlacedOneCharacterLateToCompensateForLazyFontApplyingThemEarly() {
        LazyFont.DrawableString drawable = build(new DescriptionLine("Increases flux capacity by 15%.", false));

        InOrder order = inOrder(drawable);
        order.verify(drawable).setText("");
        order.verify(drawable).append("Increases flux capacity by 1");
        order.verify(drawable).append("5%.", GREEN);
        order.verify(drawable).append(" ");
    }

    @Test
    void aHighlightAtTheStartOfALaterParagraphIsStillColoured() {
        LazyFont.DrawableString drawable = build(new DescriptionLine("Intro.", false),
                new DescriptionLine("100% more flux dissipation.", false));

        InOrder order = inOrder(drawable);
        order.verify(drawable).append("Intro.\n\n1");
        order.verify(drawable).append("00% ", GREEN);
        order.verify(drawable).append("more flux dissipation. ");
    }

    @Test
    void aHighlightEndingBeforeALineBreakResetsOnTheNextDrawnCharacter() {
        LazyFont.DrawableString drawable = build(new DescriptionLine("Increases armor by 10%", false),
                new DescriptionLine("Next.", false));

        InOrder order = inOrder(drawable);
        order.verify(drawable).append("Increases armor by 1");
        order.verify(drawable).append("0%\n\nN", GREEN);
        order.verify(drawable).append("ext. ");
    }

    @Test
    void lowerIsBetterLinesSwapGreenAndRed() {
        LazyFont.DrawableString drawable = build(new DescriptionLine("Decreases shield upkeep by 20%.", true));

        InOrder order = inOrder(drawable);
        order.verify(drawable).append("Decreases shield upkeep by 2");
        order.verify(drawable).append("0%.", GREEN);
    }

    @Test
    void lowerIsBetterTurnsAMoreIncreaseRed() {
        LazyFont.DrawableString drawable = build(new DescriptionLine("25% more weapon recoil.", true));

        InOrder order = inOrder(drawable);
        order.verify(drawable).append("2");
        order.verify(drawable).append("5% ", RED);
    }
}
