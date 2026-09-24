package exiledsector.skills;

import lunalib.lunaSettings.LunaSettings;

public final class MaxChainHopsConfig {

    private static final String MOD_ID = "exiledSector";

    public static final String FIELD_ID = "exiledSector_maxChainHops";

    public static final int DEFAULT = 5;

    private MaxChainHopsConfig() {
    }

    public static int get() {
        Integer value = LunaSettings.getInt(MOD_ID, FIELD_ID);
        return value != null ? value : DEFAULT;
    }
}
