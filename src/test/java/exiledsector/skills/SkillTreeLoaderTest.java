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
            "capacitors", new SkillType("capacitors", "Capacitors", "graphics/hullmods/flux_coil_adjunct.png", 2, 500, null, 0f),
            "bare", new SkillType("bare", "Bare", "graphics/icons/skills/combat.png", 0, 0, null, 0f)
    );

    @Test
    void parsesAllFieldsOfANodeAndResolvesItsType() throws Exception {
        JSONObject root = new JSONObject("{ \"nodes\": [ {"
                + "\"id\": \"capacitors_1\","
                + "\"type\": \"capacitors\","
                + "\"prerequisites\": [\"vents_1\"],"
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
        assertEquals(List.of("vents_1"), node.getPrerequisiteNodeIds());
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

        assertTrue(node.getPrerequisiteNodeIds().isEmpty());
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
}
