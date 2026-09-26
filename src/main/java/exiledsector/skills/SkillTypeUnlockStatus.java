package exiledsector.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import org.apache.log4j.Logger;

import java.util.List;

public final class SkillTypeUnlockStatus {

    private SkillTypeUnlockStatus() {
    }

    public static boolean isLocked(SkillType type, ShipSkillData data) {
        List<UnlockCondition> conditions = type.getUnlockConditions();
        if (conditions.isEmpty()) return false;

        for (UnlockCondition condition : conditions) {
            if (isSatisfied(condition, data)) return false;
        }
        return true;
    }

    public static boolean isHidden(SkillType type, ShipSkillData data) {
        return isLocked(type, data) && !HiddenNodeDisplayConfig.showHiddenNodesByDefault();
    }

    private static boolean isSatisfied(UnlockCondition condition, ShipSkillData data) {
        if (UnlockConditionOverrides.isDisabled(condition.getType())) return true;

        try {
            return switch (condition.getType()) {
                case BLUEPRINT -> isBlueprintKnown(condition);
                case CHARACTER_STAT -> hasCharacterStat(condition);
                case MIN_SHIP_LEVEL -> hasMinShipLevel(condition, data);
                case MEMORY_FLAG -> hasMemoryFlag(condition);
                default -> false;
            };
        } catch (RuntimeException e) {
            Logger.getLogger(SkillTypeUnlockStatus.class).error("Failed to check unlock condition " + condition, e);
            return false;
        }
    }

    private static boolean isBlueprintKnown(UnlockCondition condition) {
        String id = condition.getKey();
        if (id == null) return false;

        CharacterDataAPI playerCharacter = Global.getSector().getCharacterData();
        return playerCharacter != null && playerCharacter.knowsHullMod(id);
    }

    private static boolean hasCharacterStat(UnlockCondition condition) {
        String statId = condition.getKey();
        if (statId == null) return false;

        CharacterDataAPI playerCharacter = Global.getSector().getCharacterData();
        if (playerCharacter == null) return false;
        PersonAPI person = playerCharacter.getPerson();
        return person != null && person.getStats().getDynamic().getMod(statId).getFlatBonus() > 0f;
    }

    private static boolean hasMinShipLevel(UnlockCondition condition, ShipSkillData data) {
        return data != null && data.getLevel() >= condition.getMinLevel();
    }

    private static boolean hasMemoryFlag(UnlockCondition condition) {
        String key = condition.getKey();
        if (key == null) return false;
        return Global.getSector().getMemoryWithoutUpdate().getBoolean(key);
    }
}
