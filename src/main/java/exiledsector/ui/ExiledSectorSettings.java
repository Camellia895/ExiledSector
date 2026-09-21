package exiledsector.ui;

import lunalib.lunaSettings.LunaSettings;

public final class ExiledSectorSettings {

    private static final String MOD_ID = "exiledSector";

    private ExiledSectorSettings() {
    }

    public static void register() {
        LunaSettings.SettingsCreator.addHeader(MOD_ID, "exiledSector_header", "Exiled Sector", "");
        LunaSettings.SettingsCreator.addText(MOD_ID, "exiledSector_about",
                "Gives each ship its own skill tree. Open a ship's refit screen and use the Skill Tree button to allocate nodes.",
                "");
    }
}
