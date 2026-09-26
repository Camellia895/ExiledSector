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
        return switch (type) {
            case BLUEPRINT -> DISABLE_BLUEPRINT_FIELD_ID;
            case CHARACTER_STAT -> DISABLE_CHARACTER_STAT_FIELD_ID;
            case MIN_SHIP_LEVEL -> DISABLE_MIN_SHIP_LEVEL_FIELD_ID;
            case MEMORY_FLAG -> DISABLE_MEMORY_FLAG_FIELD_ID;
            default -> null;
        };
    }
}
