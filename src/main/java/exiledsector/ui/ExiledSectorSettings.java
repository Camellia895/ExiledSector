package exiledsector.ui;

import exiledsector.skills.PassivePointExchangeRates;
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

        LunaSettings.SettingsCreator.addHeader(MOD_ID, "exiledSector_opRatioHeader", "Passive Point Exchange Rate", "");
        LunaSettings.SettingsCreator.addText(MOD_ID, "exiledSector_opRatioAbout",
                "How many ordnance points buy one passive point, by hull size.", "");
        LunaSettings.SettingsCreator.addInt(MOD_ID, PassivePointExchangeRates.FRIGATE_FIELD_ID,
                "Frigate OP per Passive Point", "", PassivePointExchangeRates.DEFAULT_FRIGATE, 0, 100, "");
        LunaSettings.SettingsCreator.addInt(MOD_ID, PassivePointExchangeRates.DESTROYER_FIELD_ID,
                "Destroyer OP per Passive Point", "", PassivePointExchangeRates.DEFAULT_DESTROYER, 0, 100, "");
        LunaSettings.SettingsCreator.addInt(MOD_ID, PassivePointExchangeRates.CRUISER_FIELD_ID,
                "Cruiser OP per Passive Point", "", PassivePointExchangeRates.DEFAULT_CRUISER, 0, 100, "");
        LunaSettings.SettingsCreator.addInt(MOD_ID, PassivePointExchangeRates.CAPITAL_FIELD_ID,
                "Capital OP per Passive Point", "", PassivePointExchangeRates.DEFAULT_CAPITAL, 0, 100, "");
        LunaSettings.SettingsCreator.addInt(MOD_ID, PassivePointExchangeRates.UNDEFINED_FIELD_ID,
                "Other Hull Sizes OP per Passive Point", "", PassivePointExchangeRates.DEFAULT_UNDEFINED, 0, 100, "");
    }
}
