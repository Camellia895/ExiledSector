package exiledsector.skills;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                + "\"y\": 180"
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
    void parsesMultipleStaticImagesInOrder() throws Exception {
        JSONObject root = new JSONObject("{ \"nodes\": [], \"staticImages\": ["
                + "{\"id\": \"a\", \"x\": 0, \"y\": 0, \"width\": 100, \"height\": 100, \"imagePath\": \"a.png\"},"
                + "{\"id\": \"b\", \"x\": 0, \"y\": 0, \"width\": 100, \"height\": 100, \"imagePath\": \"b.png\"}"
                + "] }");

        List<StaticImage> images = SkillTreeLoader.parseStaticImages(root);

        assertEquals(List.of("a", "b"), List.of(images.get(0).getId(), images.get(1).getId()));
    }
}
