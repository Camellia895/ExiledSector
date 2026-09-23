package exiledsector.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillTreeStatPanelTest {

    @Test
    void buttonHitTestAcceptsThePointAtTheButtonsCenter() {
        assertTrue(SkillTreeStatPanel.isWithinButton(100f, 200f, 20f, 100f, 200f));
    }

    @Test
    void buttonHitTestAcceptsPointsOnTheEdgeOfTheButton() {
        assertTrue(SkillTreeStatPanel.isWithinButton(100f, 200f, 20f, 110f, 210f));
        assertTrue(SkillTreeStatPanel.isWithinButton(100f, 200f, 20f, 90f, 190f));
    }

    @Test
    void buttonHitTestRejectsAPointJustOutsideTheButton() {
        assertFalse(SkillTreeStatPanel.isWithinButton(100f, 200f, 20f, 111f, 200f));
        assertFalse(SkillTreeStatPanel.isWithinButton(100f, 200f, 20f, 100f, 211f));
    }
}
