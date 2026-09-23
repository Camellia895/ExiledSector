package exiledsector.skills;

import lunalib.lunaSettings.LunaSettings;

public final class HiddenNodeDisplayConfig {

    private static final String MOD_ID = "exiledSector";

    public static final String SHOW_HIDDEN_NODES_FIELD_ID = "exiledSector_showHiddenNodesByDefault";

    public static final boolean DEFAULT_SHOW_HIDDEN_NODES = false;

    private HiddenNodeDisplayConfig() {
    }

    public static boolean showHiddenNodesByDefault() {
        Boolean value = LunaSettings.getBoolean(MOD_ID, SHOW_HIDDEN_NODES_FIELD_ID);
        return value != null ? value : DEFAULT_SHOW_HIDDEN_NODES;
    }
}
