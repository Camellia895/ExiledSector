package exiledsector.skills.skilleffect;

import exiledsector.ModSettings;

public final class MaxChainCountConfig {

    public static final String FIELD_ID = "exiledSector_maxChainCount";

    public static final int DEFAULT = 5;

    private MaxChainCountConfig() {
    }

    public static int get() {
        return ModSettings.intOr(FIELD_ID, DEFAULT);
    }
}
