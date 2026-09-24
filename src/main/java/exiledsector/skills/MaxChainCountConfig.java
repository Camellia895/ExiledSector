package exiledsector.skills;

import lunalib.lunaSettings.LunaSettings;

public final class MaxChainCountConfig {

    private static final String MOD_ID = "exiledSector";

    public static final String FIELD_ID = "exiledSector_maxChainCount";

    public static final int DEFAULT = 5;

    private MaxChainCountConfig() {
    }

    public static int get() {
        Integer value = LunaSettings.getInt(MOD_ID, FIELD_ID);
        return value != null ? value : DEFAULT;
    }
}
