package exiledsector.skills;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillTreeTest {

    private static final SkillType TYPE = new SkillType("hull", "Hull", "a.png", 2, 500,
            List.of(new SkillTypeEffect(SkillEffect.HULL, 1f)), SkillTier.SMALL, null, null, null);

    @BeforeEach
    void setUp() {
        SkillTree.getAllNodes().clear();
    }

    @AfterEach
    void tearDown() {
        SkillTree.getAllNodes().clear();
    }

    @Test
    void getReturnsNullForAnUnregisteredNode() {
        assertNull(SkillTree.get("does_not_exist"));
    }

    @Test
    void registerMakesANodeRetrievableById() {
        SkillNode node = new SkillNode("hull_1", TYPE, List.of(), 0f, 0f);

        SkillTree.register(node);

        assertSame(node, SkillTree.get("hull_1"));
    }

    @Test
    void registeringANodeWithAnExistingIdReplacesIt() {
        SkillNode original = new SkillNode("hull_1", TYPE, List.of(), 0f, 0f);
        SkillNode replacement = new SkillNode("hull_1", TYPE, List.of(), 100f, 100f);
        SkillTree.register(original);

        SkillTree.register(replacement);

        assertSame(replacement, SkillTree.get("hull_1"));
        assertEquals(1, SkillTree.getAllNodes().size());
    }

    @Test
    void getAllNodesReflectsEveryRegisteredNode() {
        SkillTree.register(new SkillNode("hull_1", TYPE, List.of(), 0f, 0f));
        SkillTree.register(new SkillNode("hull_2", TYPE, List.of(), 10f, 10f));

        assertEquals(2, SkillTree.getAllNodes().size());
        assertTrue(SkillTree.getAllNodes().containsKey("hull_1"));
        assertTrue(SkillTree.getAllNodes().containsKey("hull_2"));
    }
}
