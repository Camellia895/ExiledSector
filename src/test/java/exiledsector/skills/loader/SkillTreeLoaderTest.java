package exiledsector.skills.loader;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import exiledsector.ui.decoration.RingBelt;
import exiledsector.ui.decoration.StaticImage;
import exiledsector.ui.node.ConnectorCurve;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SkillTreeLoaderTest {

    private static final Map<String, SkillType> SKILL_TYPES = Map.of(
            "capacitors", new SkillType("capacitors", "Capacitors", "graphics/hullmods/flux_coil_adjunct.png", 2, 500, List.of(), SkillTier.SMALL, null, null, null),
            "bare", new SkillType("bare", "Bare", "graphics/icons/skills/combat.png", 0, 0, List.of(), SkillTier.SMALL, null, null, null)
    );

    @Test
    void parsesAllFieldsOfANodeAndResolvesItsType() throws Exception {
        JSONObject root = new JSONObject("{ \"nodes\": [ {"
                + "\"id\": \"capacitors_1\","
                + "\"type\": \"capacitors\","
                + "\"connectedTo\": [\"vents_1\"],"
                + "\"x\": -180,"
                + "\"y\": 180,"
                + "\"ringBeltPath\": \"graphics/planets/ring_band_ice.png\","
                + "\"ringBeltColor\": \"#8c78ff\","
                + "\"ringBeltWidth\": 1.4,"
                + "\"wormholeColor\": \"#ff5ad1\""
                + "} ] }");

        List<SkillNode> nodes = SkillTreeLoader.parseNodes(root, SKILL_TYPES);

        assertEquals(1, nodes.size());
        SkillNode node = nodes.get(0);
        assertEquals("capacitors_1", node.getId());
        assertEquals("Capacitors", node.getDisplayName());
        assertEquals("graphics/hullmods/flux_coil_adjunct.png", node.getIconPath());
        assertEquals(2, node.getOpCost());
        assertEquals(500f, node.getXpCost());
        assertEquals(List.of("vents_1"), node.getConnectedNodeIds());
        assertEquals(-180f, node.getOffsetX());
        assertEquals(180f, node.getOffsetY());
        assertEquals("graphics/planets/ring_band_ice.png", node.getRingBeltPath());
        assertEquals("#8c78ff", node.getRingBeltColor());
        assertEquals(1.4f, node.getRingBeltWidth());
        assertEquals("#ff5ad1", node.getWormholeColor());
    }

    @Test
    void missingOptionalFieldsFallBackToDefaults() throws Exception {
        JSONObject root = new JSONObject("{ \"nodes\": [ {"
                + "\"id\": \"bare_node\","
                + "\"type\": \"bare\""
                + "} ] }");

        SkillNode node = SkillTreeLoader.parseNodes(root, SKILL_TYPES).get(0);

        assertTrue(node.getConnectedNodeIds().isEmpty());
        assertEquals(0f, node.getOffsetX());
        assertEquals(0f, node.getOffsetY());
        assertEquals(null, node.getRingBeltPath());
        assertEquals(null, node.getRingBeltColor());
        assertEquals(null, node.getRingBeltWidth());
        assertEquals(null, node.getWormholeColor());
    }

    @Test
    void parsesMultipleNodesInOrder() throws Exception {
        JSONObject root = new JSONObject("{ \"nodes\": ["
                + "{\"id\": \"a\", \"type\": \"bare\"},"
                + "{\"id\": \"b\", \"type\": \"capacitors\"}"
                + "] }");

        List<SkillNode> nodes = SkillTreeLoader.parseNodes(root, SKILL_TYPES);

        assertEquals(List.of("a", "b"), List.of(nodes.get(0).getId(), nodes.get(1).getId()));
    }

    @Test
    void unknownSkillTypeThrows() throws Exception {
        JSONObject root = new JSONObject("{ \"nodes\": [ {"
                + "\"id\": \"mystery\","
                + "\"type\": \"does_not_exist\""
                + "} ] }");

        assertThrows(org.json.JSONException.class, () -> SkillTreeLoader.parseNodes(root, SKILL_TYPES));
    }

    @Test
    void missingConnectorCurvesFieldMeansNoCurves() throws Exception {
        JSONObject root = new JSONObject("{ \"nodes\": [] }");

        Map<String, ConnectorCurve> curves = SkillTreeLoader.parseConnectorCurves(root);

        assertTrue(curves.isEmpty());
    }

    @Test
    void parsesConnectorCurvesKeyedByCanonicalPair() throws Exception {
        JSONObject root = new JSONObject("{ \"nodes\": [], \"connectorCurves\": [ {"
                + "\"a\": \"capacitors_1\","
                + "\"b\": \"bare_node\","
                + "\"controlX\": 12.5,"
                + "\"controlY\": -8"
                + "} ] }");

        Map<String, ConnectorCurve> curves = SkillTreeLoader.parseConnectorCurves(root);

        assertEquals(1, curves.size());
        ConnectorCurve curve = curves.get(SkillTree.curveKey("capacitors_1", "bare_node"));
        assertEquals(12.5f, curve.getControlOffsetX());
        assertEquals(-8f, curve.getControlOffsetY());
        assertEquals(curves.get(SkillTree.curveKey("bare_node", "capacitors_1")), curve);
    }

    @Test
    void missingHiddenConnectorsFieldMeansNoneHidden() throws Exception {
        JSONObject root = new JSONObject("{ \"nodes\": [] }");

        Set<String> hidden = SkillTreeLoader.parseHiddenConnectors(root);

        assertTrue(hidden.isEmpty());
    }

    @Test
    void parsesHiddenConnectorsKeyedByCanonicalPair() throws Exception {
        JSONObject root = new JSONObject("{ \"nodes\": [], \"hiddenConnectors\": [ {"
                + "\"a\": \"capacitors_1\","
                + "\"b\": \"bare_node\""
                + "} ] }");

        Set<String> hidden = SkillTreeLoader.parseHiddenConnectors(root);

        assertEquals(1, hidden.size());
        assertTrue(hidden.contains(SkillTree.curveKey("capacitors_1", "bare_node")));
        assertTrue(hidden.contains(SkillTree.curveKey("bare_node", "capacitors_1")));
    }

    @Test
    void missingStaticImagesFieldMeansNoImages() throws Exception {
        JSONObject root = new JSONObject("{ \"nodes\": [] }");

        List<StaticImage> images = SkillTreeLoader.parseStaticImages(root);

        assertTrue(images.isEmpty());
    }

    @Test
    void parsesAllFieldsOfAStaticImage() throws Exception {
        JSONObject root = new JSONObject("{ \"nodes\": [], \"staticImages\": [ {"
                + "\"id\": \"image_1\","
                + "\"x\": 1800,"
                + "\"y\": 600,"
                + "\"width\": 900,"
                + "\"height\": 750,"
                + "\"imagePath\": \"graphics/backgrounds/static_images/hyperspace_cloud_03.png\","
                + "\"rotation\": 45"
                + "} ] }");

        List<StaticImage> images = SkillTreeLoader.parseStaticImages(root);

        assertEquals(1, images.size());
        StaticImage image = images.get(0);
        assertEquals("image_1", image.getId());
        assertEquals(1800f, image.getX());
        assertEquals(600f, image.getY());
        assertEquals(900f, image.getWidth());
        assertEquals(750f, image.getHeight());
        assertEquals("graphics/backgrounds/static_images/hyperspace_cloud_03.png", image.getImagePath());
        assertEquals(45f, image.getRotation());
    }

    @Test
    void rotationDefaultsToZeroWhenFieldMissing() throws Exception {
        JSONObject root = new JSONObject("{ \"nodes\": [], \"staticImages\": [ {"
                + "\"id\": \"image_1\","
                + "\"x\": 0,"
                + "\"y\": 0,"
                + "\"width\": 100,"
                + "\"height\": 100,"
                + "\"imagePath\": \"a.png\""
                + "} ] }");

        StaticImage image = SkillTreeLoader.parseStaticImages(root).get(0);

        assertEquals(0f, image.getRotation());
    }

    @Test
    void rotationSpeedDefaultsToZeroWhenFieldMissing() throws Exception {
        JSONObject root = new JSONObject("{ \"nodes\": [], \"staticImages\": [ {"
                + "\"id\": \"image_1\","
                + "\"x\": 0,"
                + "\"y\": 0,"
                + "\"width\": 100,"
                + "\"height\": 100,"
                + "\"imagePath\": \"a.png\""
                + "} ] }");

        StaticImage image = SkillTreeLoader.parseStaticImages(root).get(0);

        assertEquals(0f, image.getRotationSpeed());
    }

    @Test
    void parsesRotationSpeedWhenPresent() throws Exception {
        JSONObject root = new JSONObject("{ \"nodes\": [], \"staticImages\": [ {"
                + "\"id\": \"image_1\","
                + "\"x\": 0,"
                + "\"y\": 0,"
                + "\"width\": 100,"
                + "\"height\": 100,"
                + "\"imagePath\": \"a.png\","
                + "\"rotationSpeed\": 0.75"
                + "} ] }");

        StaticImage image = SkillTreeLoader.parseStaticImages(root).get(0);

        assertEquals(0.75f, image.getRotationSpeed());
    }

    @Test
    void parsesMultipleStaticImagesInOrder() throws Exception {
        JSONObject root = new JSONObject("{ \"nodes\": [], \"staticImages\": ["
                + "{\"id\": \"a\", \"x\": 0, \"y\": 0, \"width\": 100, \"height\": 100, \"imagePath\": \"a.png\"},"
                + "{\"id\": \"b\", \"x\": 0, \"y\": 0, \"width\": 100, \"height\": 100, \"imagePath\": \"b.png\"}"
                + "] }");

        List<StaticImage> images = SkillTreeLoader.parseStaticImages(root);

        assertEquals(List.of("a", "b"), List.of(images.get(0).getId(), images.get(1).getId()));
    }

    @Test
    void missingRingBeltsFieldMeansNoRingBelts() throws Exception {
        JSONObject root = new JSONObject("{ \"nodes\": [] }");

        List<RingBelt> ringBelts = SkillTreeLoader.parseRingBelts(root);

        assertTrue(ringBelts.isEmpty());
    }

    @Test
    void parsesAllFieldsOfARingBelt() throws Exception {
        JSONObject root = new JSONObject("{ \"nodes\": [], \"ringBelts\": [ {"
                + "\"id\": \"outer_ringbelt\","
                + "\"x\": 100,"
                + "\"y\": -50,"
                + "\"innerRadius\": 2850,"
                + "\"outerRadius\": 3150,"
                + "\"ringArtPath\": \"graphics/planets/ring_band_dust.png\""
                + "} ] }");

        List<RingBelt> ringBelts = SkillTreeLoader.parseRingBelts(root);

        assertEquals(1, ringBelts.size());
        RingBelt belt = ringBelts.get(0);
        assertEquals("outer_ringbelt", belt.getId());
        assertEquals(100f, belt.getX());
        assertEquals(-50f, belt.getY());
        assertEquals(2850f, belt.getInnerRadius());
        assertEquals(3150f, belt.getOuterRadius());
        assertEquals("graphics/planets/ring_band_dust.png", belt.getRingArtPath());
    }

    @Test
    void parsesMultipleRingBeltsInOrder() throws Exception {
        JSONObject root = new JSONObject("{ \"nodes\": [], \"ringBelts\": ["
                + "{\"id\": \"a\", \"x\": 0, \"y\": 0, \"innerRadius\": 100, \"outerRadius\": 200, \"ringArtPath\": \"a.png\"},"
                + "{\"id\": \"b\", \"x\": 0, \"y\": 0, \"innerRadius\": 100, \"outerRadius\": 200, \"ringArtPath\": \"b.png\"}"
                + "] }");

        List<RingBelt> ringBelts = SkillTreeLoader.parseRingBelts(root);

        assertEquals(List.of("a", "b"), List.of(ringBelts.get(0).getId(), ringBelts.get(1).getId()));
    }

    @Test
    void loadRingBeltsReturnsAnEmptyListWhenTheJsonFailsToLoad() throws Exception {
        SettingsAPI settings = mock(SettingsAPI.class);
        when(settings.loadJSON(anyString())).thenThrow(new IOException("boom"));

        try (MockedStatic<Global> globalMock = Mockito.mockStatic(Global.class)) {
            globalMock.when(Global::getSettings).thenReturn(settings);

            List<RingBelt> ringBelts = SkillTreeLoader.loadRingBelts();

            assertEquals(List.of(), ringBelts);
        }
    }
}
