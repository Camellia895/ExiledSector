package exiledsector.ui.node;

import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class NodeSearchTest {

    private final ShipSkillData data = mock(ShipSkillData.class);

    private static SkillNode node(String id, String name) {
        SkillType type = new SkillType.Builder(id, name, "", SkillTier.SMALL).build();
        return new SkillNode(id + "_1", type, List.of(), 0f, 0f);
    }

    @Test
    void anEmptyQueryMatchesNothingAndDimsNothing() {
        NodeSearch search = new NodeSearch();
        SkillNode armor = node("armor", "Heavy Armor");

        assertFalse(search.isActive());
        assertFalse(search.matches(armor, data));
        assertEquals(1f, search.nodeAlpha(armor, data));
        assertEquals(1f, search.backgroundAlpha());
    }

    @Test
    void aSingleCharacterMatchesNamesContainingItIgnoringCase() {
        NodeSearch search = new NodeSearch();
        SkillNode armor = node("armor", "Heavy Armor");
        SkillNode flux = node("flux", "Flux Coil");

        search.setQuery("H");

        assertTrue(search.matches(armor, data));
        assertFalse(search.matches(flux, data));
        assertEquals(1f, search.nodeAlpha(armor, data));
        assertEquals(NodeSearch.DIM_ALPHA, search.nodeAlpha(flux, data));
        assertEquals(NodeSearch.DIM_ALPHA, search.backgroundAlpha());
    }

    @Test
    void connectorsStayBrightOnlyWhenBothEndsMatch() {
        NodeSearch search = new NodeSearch();
        SkillNode armor = node("armor", "Heavy Armor");
        SkillNode plating = node("plating", "Armor Plating");
        SkillNode flux = node("flux", "Flux Coil");

        search.setQuery("armor");

        assertEquals(1f, search.connectorAlpha(armor, plating, data));
        assertEquals(NodeSearch.DIM_ALPHA, search.connectorAlpha(armor, flux, data));
    }

    @Test
    void optionalNodesMatchOnAnyOfTheirOptionNames() {
        SkillType shields = new SkillType.Builder("shields", "Shield Upgrade", "", SkillTier.NOTABLE).build();
        SkillType optional = new SkillType.Builder("pick", "Pick One", "", SkillTier.NOTABLE)
                .optionalOptionIds(List.of("shields"))
                .build();
        SkillNode node = new SkillNode("pick_1", optional, List.of(), 0f, 0f);
        NodeSearch search = new NodeSearch();
        search.setQuery("shield");

        try (MockedStatic<SkillTree> tree = Mockito.mockStatic(SkillTree.class)) {
            tree.when(() -> SkillTree.getType("shields")).thenReturn(shields);

            assertTrue(search.matches(node, data));
        }
    }
}
