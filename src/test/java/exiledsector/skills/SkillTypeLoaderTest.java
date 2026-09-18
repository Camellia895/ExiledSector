package exiledsector.skills;

import exiledsector.skills.skilleffect.DefenseSkillEffect;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillTypeLoaderTest {

    @Test
    void parsesAllFieldsIncludingEffectsList() throws Exception {
        JSONObject root = new JSONObject("{ \"skillTypes\": [ {"
                + "\"id\": \"hull\","
                + "\"name\": \"Reinforced Hull\","
                + "\"icon\": \"graphics/hullmods/reinforced_bulkheads.png\","
                + "\"opCost\": 2,"
                + "\"xpCost\": 500,"
                + "\"effects\": [ { \"effect\": \"HULL\", \"magnitude\": 10 } ]"
                + "} ] }");

        Map<String, SkillType> types = SkillTypeLoader.parseSkillTypes(root);

        SkillType hull = types.get("hull");
        assertEquals("Reinforced Hull", hull.getDisplayName());
        assertEquals("graphics/hullmods/reinforced_bulkheads.png", hull.getIconPath());
        assertEquals(2, hull.getOpCost());
        assertEquals(500f, hull.getXpCost());
        assertEquals(1, hull.getEffects().size());
        assertEquals(DefenseSkillEffect.HULL, hull.getEffects().get(0).effect());
        assertEquals(10f, hull.getEffects().get(0).magnitude());
    }

    @Test
    void parsesMultipleEffectsInOrder() throws Exception {
        JSONObject root = new JSONObject("{ \"skillTypes\": [ {"
                + "\"id\": \"heavyarmor\","
                + "\"name\": \"Heavy Armor\","
                + "\"icon\": \"graphics/icons/notable_hullmods/heavy_armor.png\","
                + "\"effects\": [ { \"effect\": \"ARMOR\", \"magnitude\": 15 }, { \"effect\": \"HULL\", \"magnitude\": 5 } ]"
                + "} ] }");

        SkillType heavyArmor = SkillTypeLoader.parseSkillTypes(root).get("heavyarmor");

        assertEquals(2, heavyArmor.getEffects().size());
        assertEquals(DefenseSkillEffect.ARMOR, heavyArmor.getEffects().get(0).effect());
        assertEquals(DefenseSkillEffect.HULL, heavyArmor.getEffects().get(1).effect());
    }

    @Test
    void missingEffectsFieldMeansCosmeticPlaceholder() throws Exception {
        JSONObject root = new JSONObject("{ \"skillTypes\": [ {"
                + "\"id\": \"capacitors\","
                + "\"name\": \"Capacitors\","
                + "\"icon\": \"graphics/hullmods/flux_coil_adjunct.png\""
                + "} ] }");

        SkillType capacitors = SkillTypeLoader.parseSkillTypes(root).get("capacitors");

        assertTrue(capacitors.getEffects().isEmpty());
        assertEquals(0, capacitors.getOpCost());
        assertEquals(0f, capacitors.getXpCost());
    }

    @Test
    void missingTierFieldDefaultsToSmall() throws Exception {
        JSONObject root = new JSONObject("{ \"skillTypes\": [ {"
                + "\"id\": \"capacitors\","
                + "\"name\": \"Capacitors\","
                + "\"icon\": \"graphics/hullmods/flux_coil_adjunct.png\""
                + "} ] }");

        SkillType capacitors = SkillTypeLoader.parseSkillTypes(root).get("capacitors");

        assertEquals(SkillTier.SMALL, capacitors.getTier());
        assertNull(capacitors.getVanillaHullModId());
    }

    @Test
    void parsesTierVanillaHullModAndTodoFields() throws Exception {
        JSONObject root = new JSONObject("{ \"skillTypes\": [ {"
                + "\"id\": \"escort_package\","
                + "\"name\": \"Escort Package\","
                + "\"icon\": \"graphics/icons/notable_hullmods/escort_package.png\","
                + "\"tier\": \"NOTABLE\","
                + "\"vanillaHullMod\": \"escort_package\","
                + "\"todo\": \"Needs a real mechanic\""
                + "} ] }");

        SkillType escortPackage = SkillTypeLoader.parseSkillTypes(root).get("escort_package");

        assertEquals(SkillTier.NOTABLE, escortPackage.getTier());
        assertEquals("escort_package", escortPackage.getVanillaHullModId());
        assertEquals("Needs a real mechanic", escortPackage.getTodo());
        assertTrue(escortPackage.getEffects().isEmpty());
    }

    @Test
    void parsesDescriptionOverride() throws Exception {
        JSONObject root = new JSONObject("{ \"skillTypes\": [ {"
                + "\"id\": \"hull\","
                + "\"name\": \"Hull\","
                + "\"icon\": \"a.png\","
                + "\"description\": \"Custom flavor text.\""
                + "} ] }");

        SkillType hull = SkillTypeLoader.parseSkillTypes(root).get("hull");

        assertEquals("Custom flavor text.", hull.getDescriptionOverride());
    }

    @Test
    void missingOptionalOptionsFieldMeansNotOptional() throws Exception {
        JSONObject root = new JSONObject("{ \"skillTypes\": [ {"
                + "\"id\": \"hull\","
                + "\"name\": \"Hull\","
                + "\"icon\": \"a.png\""
                + "} ] }");

        SkillType hull = SkillTypeLoader.parseSkillTypes(root).get("hull");

        assertTrue(hull.getOptionalOptionIds().isEmpty());
        assertFalse(hull.isOptional());
    }

    @Test
    void parsesOptionalOptionsIntoAnOrderedList() throws Exception {
        JSONObject root = new JSONObject("{ \"skillTypes\": [ {"
                + "\"id\": \"slot\","
                + "\"name\": \"Optional Skill\","
                + "\"icon\": \"a.png\","
                + "\"optionalOptions\": [\"hull\", \"armor\"]"
                + "} ] }");

        SkillType slot = SkillTypeLoader.parseSkillTypes(root).get("slot");

        assertTrue(slot.isOptional());
        assertEquals(2, slot.getOptionalOptionIds().size());
        assertEquals("hull", slot.getOptionalOptionIds().get(0));
        assertEquals("armor", slot.getOptionalOptionIds().get(1));
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
