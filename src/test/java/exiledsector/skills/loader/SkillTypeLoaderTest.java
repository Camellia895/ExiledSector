package exiledsector.skills.loader;

import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillType;
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
                + "\"effects\": [ { \"effect\": \"HULL_PERCENT\", \"magnitude\": 10 } ]"
                + "} ] }");

        Map<String, SkillType> types = SkillTypeLoader.parseSkillTypes(root);

        SkillType hull = types.get("hull");
        assertEquals("Reinforced Hull", hull.getDisplayName());
        assertEquals("graphics/hullmods/reinforced_bulkheads.png", hull.getIconPath());
        assertEquals(1, hull.getEffects().size());
        assertEquals(DefenseSkillEffect.HULL_PERCENT, hull.getEffects().get(0).effect());
        assertEquals(10f, hull.getEffects().get(0).magnitude());
    }

    @Test
    void parsesMultipleEffectsInOrder() throws Exception {
        JSONObject root = new JSONObject("{ \"skillTypes\": [ {"
                + "\"id\": \"heavyarmor\","
                + "\"name\": \"Heavy Armor\","
                + "\"icon\": \"graphics/icons/notable_hullmods/heavy_armor.png\","
                + "\"effects\": [ { \"effect\": \"ARMOR_PERCENT\", \"magnitude\": 15 }, { \"effect\": \"HULL_PERCENT\", \"magnitude\": 5 } ]"
                + "} ] }");

        SkillType heavyArmor = SkillTypeLoader.parseSkillTypes(root).get("heavyarmor");

        assertEquals(2, heavyArmor.getEffects().size());
        assertEquals(DefenseSkillEffect.ARMOR_PERCENT, heavyArmor.getEffects().get(0).effect());
        assertEquals(DefenseSkillEffect.HULL_PERCENT, heavyArmor.getEffects().get(1).effect());
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
    void missingExclusiveHullModsFieldMeansNoExclusions() throws Exception {
        JSONObject root = new JSONObject("{ \"skillTypes\": [ {"
                + "\"id\": \"hull\","
                + "\"name\": \"Hull\","
                + "\"icon\": \"a.png\""
                + "} ] }");

        SkillType hull = SkillTypeLoader.parseSkillTypes(root).get("hull");

        assertTrue(hull.getExclusiveHullModIds().isEmpty());
    }

    @Test
    void parsesExclusiveHullModsIntoAnOrderedList() throws Exception {
        JSONObject root = new JSONObject("{ \"skillTypes\": [ {"
                + "\"id\": \"heavyarmor\","
                + "\"name\": \"Heavy Armor\","
                + "\"icon\": \"a.png\","
                + "\"exclusiveHullMods\": [\"armoredcladding\", \"heavyarmor\"]"
                + "} ] }");

        SkillType heavyArmor = SkillTypeLoader.parseSkillTypes(root).get("heavyarmor");

        assertEquals(2, heavyArmor.getExclusiveHullModIds().size());
        assertEquals("armoredcladding", heavyArmor.getExclusiveHullModIds().get(0));
        assertEquals("heavyarmor", heavyArmor.getExclusiveHullModIds().get(1));
    }

    @Test
    void vanillaHullModIsImplicitlyExclusiveWithItself() throws Exception {
        JSONObject root = new JSONObject("{ \"skillTypes\": [ {"
                + "\"id\": \"escort_package\","
                + "\"name\": \"Escort Package\","
                + "\"icon\": \"a.png\","
                + "\"vanillaHullMod\": \"escort_package\""
                + "} ] }");

        SkillType escortPackage = SkillTypeLoader.parseSkillTypes(root).get("escort_package");

        assertEquals(1, escortPackage.getExclusiveHullModIds().size());
        assertEquals("escort_package", escortPackage.getExclusiveHullModIds().get(0));
    }

    @Test
    void missingExclusiveSkillTypesFieldMeansNoExclusions() throws Exception {
        JSONObject root = new JSONObject("{ \"skillTypes\": [ {"
                + "\"id\": \"hull\","
                + "\"name\": \"Hull\","
                + "\"icon\": \"a.png\""
                + "} ] }");

        SkillType hull = SkillTypeLoader.parseSkillTypes(root).get("hull");

        assertTrue(hull.getExclusiveSkillTypeIds().isEmpty());
    }

    @Test
    void parsesExclusiveSkillTypesIntoAnOrderedList() throws Exception {
        JSONObject root = new JSONObject("{ \"skillTypes\": [ {"
                + "\"id\": \"frontemitter\","
                + "\"name\": \"Shield Conversion - Front\","
                + "\"icon\": \"a.png\","
                + "\"exclusiveSkillTypes\": [\"adaptiveshields\", \"shield_shunt\"]"
                + "} ] }");

        SkillType frontEmitter = SkillTypeLoader.parseSkillTypes(root).get("frontemitter");

        assertEquals(2, frontEmitter.getExclusiveSkillTypeIds().size());
        assertEquals("adaptiveshields", frontEmitter.getExclusiveSkillTypeIds().get(0));
        assertEquals("shield_shunt", frontEmitter.getExclusiveSkillTypeIds().get(1));
    }

    @Test
    void missingLockedFieldDefaultsToFalse() throws Exception {
        JSONObject root = new JSONObject("{ \"skillTypes\": [ {"
                + "\"id\": \"hull\","
                + "\"name\": \"Hull\","
                + "\"icon\": \"a.png\""
                + "} ] }");

        SkillType hull = SkillTypeLoader.parseSkillTypes(root).get("hull");

        assertFalse(hull.isLocked());
    }

    @Test
    void parsesLockedTrue() throws Exception {
        JSONObject root = new JSONObject("{ \"skillTypes\": [ {"
                + "\"id\": \"escort_package\","
                + "\"name\": \"Escort Package\","
                + "\"icon\": \"a.png\","
                + "\"vanillaHullMod\": \"escort_package\","
                + "\"locked\": true"
                + "} ] }");

        SkillType escortPackage = SkillTypeLoader.parseSkillTypes(root).get("escort_package");

        assertTrue(escortPackage.isLocked());
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
