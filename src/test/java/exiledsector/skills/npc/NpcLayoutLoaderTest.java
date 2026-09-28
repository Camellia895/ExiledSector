package exiledsector.skills.npc;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NpcLayoutLoaderTest {

    private static Map<String, NpcLayout> parse(String json) throws Exception {
        return NpcLayoutLoader.parseLayouts(new JSONObject(json));
    }

    @Test
    void parsesAllFieldsOfALayout() throws Exception {
        Map<String, NpcLayout> layouts = parse("{ \"layouts\": { \"brawler\": {"
                + "\"name\": \"Brawler\","
                + "\"root\": \"root_low_tech_1\","
                + "\"requires\": [\"req_shields\", \"req_ballistic\"],"
                + "\"description\": \"Armour first.\","
                + "\"nodes\": [\"armor_1\"]"
                + "} } }");

        NpcLayout layout = layouts.get("brawler");
        assertEquals("brawler", layout.id());
        assertEquals("Brawler", layout.name());
        assertEquals("root_low_tech_1", layout.rootNodeId());
        assertEquals(List.of("req_shields", "req_ballistic"), layout.requires());
        assertEquals("Armour first.", layout.description());
    }

    @Test
    void parsesPlainNodeIdsAndOptionObjectsInOrder() throws Exception {
        Map<String, NpcLayout> layouts = parse("{ \"layouts\": { \"brawler\": {"
                + "\"root\": \"root_low_tech_1\","
                + "\"nodes\": [\"armor_1\", { \"id\": \"small_flux_optional_1\", \"option\": \"hull\" }, \"blast_doors_1\"]"
                + "} } }");

        assertEquals(List.of(
                new NpcLayoutEntry("armor_1", null),
                new NpcLayoutEntry("small_flux_optional_1", "hull"),
                new NpcLayoutEntry("blast_doors_1", null)), layouts.get("brawler").entries());
    }

    @Test
    void anOptionObjectWithoutAnOptionHasANullOption() throws Exception {
        Map<String, NpcLayout> layouts = parse("{ \"layouts\": { \"brawler\": {"
                + "\"root\": \"root_low_tech_1\", \"nodes\": [{ \"id\": \"small_flux_optional_1\" }] } } }");

        assertEquals(List.of(new NpcLayoutEntry("small_flux_optional_1", null)), layouts.get("brawler").entries());
    }

    @Test
    void missingOptionalFieldsFallBackToDefaults() throws Exception {
        Map<String, NpcLayout> layouts = parse("{ \"layouts\": { \"brawler\": {"
                + "\"root\": \"root_low_tech_1\", \"nodes\": [] } } }");

        NpcLayout layout = layouts.get("brawler");
        assertEquals("brawler", layout.name());
        assertEquals("", layout.description());
        assertTrue(layout.requires().isEmpty());
        assertTrue(layout.entries().isEmpty());
    }

    @Test
    void malformedLayoutsAreSkippedWhileValidOnesAreKept() throws Exception {
        Map<String, NpcLayout> layouts = parse("{ \"layouts\": {"
                + "\"no_root\": { \"nodes\": [\"armor_1\"] },"
                + "\"no_nodes\": { \"root\": \"root_low_tech_1\" },"
                + "\"nodes_not_array\": { \"root\": \"root_low_tech_1\", \"nodes\": \"armor_1\" },"
                + "\"number_entry\": { \"root\": \"root_low_tech_1\", \"nodes\": [\"armor_1\", 5] },"
                + "\"object_without_id\": { \"root\": \"root_low_tech_1\", \"nodes\": [{ \"option\": \"hull\" }] },"
                + "\"requires_not_array\": { \"root\": \"root_low_tech_1\", \"requires\": \"req_shields\", \"nodes\": [] },"
                + "\"not_an_object\": \"oops\","
                + "\"valid\": { \"root\": \"root_low_tech_1\", \"nodes\": [\"armor_1\"] }"
                + "} }");

        assertEquals(List.of("valid"), List.copyOf(layouts.keySet()));
    }

    @Test
    void aFileWithoutALayoutsObjectYieldsNoLayouts() throws Exception {
        assertTrue(parse("{ \"layout\": {} }").isEmpty());
        assertTrue(NpcLayoutLoader.parseLayouts(null).isEmpty());
    }

    @Test
    void layoutsAreOrderedById() throws Exception {
        Map<String, NpcLayout> layouts = parse("{ \"layouts\": {"
                + "\"zeta\": { \"root\": \"r\", \"nodes\": [] },"
                + "\"alpha\": { \"root\": \"r\", \"nodes\": [] },"
                + "\"mid\": { \"root\": \"r\", \"nodes\": [] }"
                + "} }");

        assertEquals(List.of("alpha", "mid", "zeta"), List.copyOf(layouts.keySet()));
    }

    @Test
    void theClearArrayMarkerLeftBehindByJsonMergingIsIgnored() throws Exception {
        Map<String, NpcLayout> layouts = parse("{ \"layouts\": { \"brawler\": {"
                + "\"root\": \"root_low_tech_1\","
                + "\"requires\": [\"core_clearArray\", \"req_shields\"],"
                + "\"nodes\": [\"core_clearArray\", \"armor_1\"]"
                + "} } }");

        NpcLayout layout = layouts.get("brawler");
        assertEquals(List.of("req_shields"), layout.requires());
        assertEquals(List.of(new NpcLayoutEntry("armor_1", null)), layout.entries());
    }

    @Test
    void loadReadsTheMergedJsonAcrossMods() throws Exception {
        SettingsAPI settings = mock(SettingsAPI.class);
        when(settings.getMergedJSON(NpcLayoutLoader.DATA_PATH)).thenReturn(new JSONObject(
                "{ \"layouts\": { \"brawler\": { \"root\": \"root_low_tech_1\", \"nodes\": [\"armor_1\"] } } }"));
        try (MockedStatic<Global> global = Mockito.mockStatic(Global.class)) {
            global.when(Global::getSettings).thenReturn(settings);

            assertEquals(List.of("brawler"), List.copyOf(NpcLayoutLoader.load().keySet()));
        }
    }

    @Test
    void loadReturnsNoLayoutsWhenNoModShipsTheFile() throws Exception {
        SettingsAPI settings = mock(SettingsAPI.class);
        when(settings.getMergedJSON(NpcLayoutLoader.DATA_PATH))
                .thenThrow(new RuntimeException("Error loading [" + NpcLayoutLoader.DATA_PATH + "] resource, not found"));
        try (MockedStatic<Global> global = Mockito.mockStatic(Global.class)) {
            global.when(Global::getSettings).thenReturn(settings);

            assertTrue(NpcLayoutLoader.load().isEmpty());
        }
    }
}
