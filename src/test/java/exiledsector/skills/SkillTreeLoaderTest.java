package exiledsector.skills;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillTreeLoaderTest {

    @Test
    void parsesAllFieldsOfANode() throws Exception {
        JSONObject root = new JSONObject("{ \"nodes\": [ {"
                + "\"id\": \"capacitors_1\","
                + "\"name\": \"Capacitors\","
                + "\"icon\": \"graphics/hullmods/flux_coil_adjunct.png\","
                + "\"opCost\": 2,"
                + "\"xpCost\": 500,"
                + "\"prerequisites\": [\"vents_1\"],"
                + "\"x\": -180,"
                + "\"y\": 180"
                + "} ] }");

        List<SkillNode> nodes = SkillTreeLoader.parseNodes(root);

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
                + "\"name\": \"Bare Node\","
                + "\"icon\": \"graphics/icons/skills/combat.png\""
                + "} ] }");

        SkillNode node = SkillTreeLoader.parseNodes(root).get(0);

        assertTrue(node.getPrerequisiteNodeIds().isEmpty());
        assertEquals(0, node.getOpCost());
        assertEquals(0f, node.getXpCost());
        assertEquals(0f, node.getOffsetX());
        assertEquals(0f, node.getOffsetY());
    }

    @Test
    void parsesMultipleNodesInOrder() throws Exception {
        JSONObject root = new JSONObject("{ \"nodes\": ["
                + "{\"id\": \"a\", \"name\": \"A\", \"icon\": \"a.png\"},"
                + "{\"id\": \"b\", \"name\": \"B\", \"icon\": \"b.png\"}"
                + "] }");

        List<SkillNode> nodes = SkillTreeLoader.parseNodes(root);

        assertEquals(List.of("a", "b"), List.of(nodes.get(0).getId(), nodes.get(1).getId()));
    }
}
