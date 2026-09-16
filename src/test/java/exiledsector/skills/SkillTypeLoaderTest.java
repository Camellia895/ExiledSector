package exiledsector.skills;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SkillTypeLoaderTest {

    @Test
    void parsesAllFieldsIncludingEffectAndMagnitude() throws Exception {
        JSONObject root = new JSONObject("{ \"skillTypes\": [ {"
                + "\"id\": \"hull\","
                + "\"name\": \"Reinforced Hull\","
                + "\"icon\": \"graphics/hullmods/reinforced_bulkheads.png\","
                + "\"opCost\": 2,"
                + "\"xpCost\": 500,"
                + "\"effect\": \"HULL\","
                + "\"magnitude\": 10"
                + "} ] }");

        Map<String, SkillType> types = SkillTypeLoader.parseSkillTypes(root);

        SkillType hull = types.get("hull");
        assertEquals("Reinforced Hull", hull.getDisplayName());
        assertEquals("graphics/hullmods/reinforced_bulkheads.png", hull.getIconPath());
        assertEquals(2, hull.getOpCost());
        assertEquals(500f, hull.getXpCost());
        assertEquals(SkillEffect.HULL, hull.getEffect());
        assertEquals(10f, hull.getMagnitude());
    }

    @Test
    void missingEffectFieldMeansCosmeticPlaceholder() throws Exception {
        JSONObject root = new JSONObject("{ \"skillTypes\": [ {"
                + "\"id\": \"capacitors\","
                + "\"name\": \"Capacitors\","
                + "\"icon\": \"graphics/hullmods/flux_coil_adjunct.png\""
                + "} ] }");

        SkillType capacitors = SkillTypeLoader.parseSkillTypes(root).get("capacitors");

        assertNull(capacitors.getEffect());
        assertEquals(0f, capacitors.getMagnitude());
        assertEquals(0, capacitors.getOpCost());
        assertEquals(0f, capacitors.getXpCost());
    }

    @Test
    void parsesMultipleTypesKeyedById() throws Exception {
        JSONObject root = new JSONObject("{ \"skillTypes\": ["
                + "{\"id\": \"a\", \"name\": \"A\", \"icon\": \"a.png\"},"
                + "{\"id\": \"b\", \"name\": \"B\", \"icon\": \"b.png\"}"
                + "] }");

        Map<String, SkillType> types = SkillTypeLoader.parseSkillTypes(root);

        assertEquals(2, types.size());
        assertEquals("A", types.get("a").getDisplayName());
        assertEquals("B", types.get("b").getDisplayName());
    }
}
