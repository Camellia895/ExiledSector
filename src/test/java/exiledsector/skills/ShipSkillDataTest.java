package exiledsector.skills;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShipSkillDataTest {

    @Test
    void startsWithNoProgress() {
        ShipSkillData data = new ShipSkillData();

        assertEquals(0f, data.getXp());
        assertEquals(0, data.getSpentOp());
        assertTrue(data.getUnlockedNodeIds().isEmpty());
    }

    @Test
    void addXpAccumulatesAcrossCalls() {
        ShipSkillData data = new ShipSkillData();

        data.addXp(10f);
        data.addXp(5f);

        assertEquals(15f, data.getXp());
    }

    @Test
    void unlockMarksNodeAndSpendsItsOpAndXpCost() {
        ShipSkillData data = new ShipSkillData();
        data.addXp(500f);
        SkillType type = new SkillType("armor", "Heavy Armor", "graphics/hullmods/heavy_armor.png", 2, 300f, null, 0f);
        SkillNode node = new SkillNode("armor_1", type, List.of(), 0f, 0f);

        data.unlock(node);

        assertTrue(data.isUnlocked("armor_1"));
        assertEquals(2, data.getSpentOp());
        // 500 banked minus the node's 300 XP cost.
        assertEquals(200f, data.getXp());
    }

    @Test
    void isUnlockedIsFalseForANodeThatWasNeverUnlocked() {
        ShipSkillData data = new ShipSkillData();

        assertFalse(data.isUnlocked("nonexistent"));
    }
}
