package exiledsector.skills;

import lunalib.lunaSettings.LunaSettings;

public final class UnlockConditionOverrides {

    private static final String MOD_ID = "exiledSector";

    public static final String DISABLE_BLUEPRINT_FIELD_ID = "exiledSector_disableBlueprintUnlock";
    public static final String DISABLE_CHARACTER_STAT_FIELD_ID = "exiledSector_disableCharacterStatUnlock";
    public static final String DISABLE_MIN_SHIP_LEVEL_FIELD_ID = "exiledSector_disableMinShipLevelUnlock";
    public static final String DISABLE_MEMORY_FLAG_FIELD_ID = "exiledSector_disableMemoryFlagUnlock";

    public static final boolean DEFAULT_DISABLED = false;

    private UnlockConditionOverrides() {
    }

    public static boolean isDisabled(UnlockConditionType type) {
        String fieldId = fieldIdFor(type);
        if (fieldId == null) return false;

        Boolean value = LunaSettings.getBoolean(MOD_ID, fieldId);
        return value != null ? value : DEFAULT_DISABLED;
    }

    private static String fieldIdFor(UnlockConditionType type) {
        switch (type) {
            case BLUEPRINT:
                return DISABLE_BLUEPRINT_FIELD_ID;
            case CHARACTER_STAT:
                return DISABLE_CHARACTER_STAT_FIELD_ID;
            case MIN_SHIP_LEVEL:
                return DISABLE_MIN_SHIP_LEVEL_FIELD_ID;
            case MEMORY_FLAG:
                return DISABLE_MEMORY_FLAG_FIELD_ID;
            default:
                return null;
        }
    }
}
