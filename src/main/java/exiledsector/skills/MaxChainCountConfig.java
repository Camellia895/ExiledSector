package exiledsector.skills;

import lunalib.lunaSettings.LunaSettings;

import static exiledsector.ExiledSectorModPlugin.MOD_ID;

public final class MaxChainCountConfig {

    public static final String FIELD_ID = "exiledSector_maxChainCount";

    public static final int DEFAULT = 5;

    private MaxChainCountConfig() {
    }

    public static int get() {
        Integer value = LunaSettings.getInt(MOD_ID, FIELD_ID);
        return value != null ? value : DEFAULT;
    }
}
