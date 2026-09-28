package exiledsector.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TreeViewportTest {

    @Test
    void worldCoordinatesAreScaledByZoomAroundTheCenterWithYPointingUp() {
        TreeViewport viewport = new TreeViewport(400f, 300f, 2f);

        assertEquals(400f, viewport.screenX(0f));
        assertEquals(300f, viewport.screenY(0f));
        assertEquals(420f, viewport.screenX(10f));
        assertEquals(280f, viewport.screenY(10f));
        assertEquals(390f, viewport.screenX(-5f));
    }
}
