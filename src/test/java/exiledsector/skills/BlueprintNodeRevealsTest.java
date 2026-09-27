package exiledsector.skills;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlueprintNodeRevealsTest {

    private MockedStatic<SkillTree> skillTreeMock;

    @BeforeEach
    void setUp() {
        SkillType gated = new SkillType.Builder("heavyarmor", "Heavy Armor", "", SkillTier.NOTABLE)
                .unlockConditions(List.of(UnlockCondition.blueprint(BlueprintCategory.HULLMOD, "heavyarmor")))
                .build();
        SkillType option = new SkillType.Builder("eccm", "ECCM", "", SkillTier.NOTABLE)
                .unlockConditions(List.of(UnlockCondition.blueprint(BlueprintCategory.HULLMOD, "eccm")))
                .build();
        SkillType picker = new SkillType.Builder("pick", "Pick", "", SkillTier.NOTABLE)
                .optionalOptionIds(List.of("eccm"))
                .build();
        SkillType levelGated = new SkillType.Builder("veteran", "Veteran", "", SkillTier.SMALL)
                .unlockConditions(List.of(UnlockCondition.minShipLevel(5)))
                .build();
        Map<String, SkillNode> nodes = new LinkedHashMap<>();
        nodes.put("heavyarmor_1", new SkillNode("heavyarmor_1", gated, List.of(), 0f, 0f));
        nodes.put("pick_1", new SkillNode("pick_1", picker, List.of(), 0f, 0f));
        nodes.put("veteran_1", new SkillNode("veteran_1", levelGated, List.of(), 0f, 0f));

        skillTreeMock = Mockito.mockStatic(SkillTree.class);
        skillTreeMock.when(SkillTree::getAllNodes).thenReturn(nodes);
        skillTreeMock.when(() -> SkillTree.getType("eccm")).thenReturn(option);
    }

    @AfterEach
    void tearDown() {
        skillTreeMock.close();
    }

    @Test
    void aHullModWhoseBlueprintGatesANodeRevealsIt() {
        assertTrue(BlueprintNodeReveals.revealsAnyNode("heavyarmor"));
    }

    @Test
    void aBlueprintGatingOnlyAnOptionalNodesOptionStillCounts() {
        assertTrue(BlueprintNodeReveals.revealsAnyNode("eccm"));
    }

    @Test
    void unrelatedOrMissingHullModsRevealNothing() {
        assertFalse(BlueprintNodeReveals.revealsAnyNode("hardenedshieldemitter"));
        assertFalse(BlueprintNodeReveals.revealsAnyNode(null));
    }
}
