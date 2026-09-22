package exiledsector.ui;

import exiledsector.skills.SkillNodeOpCost;
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

        LunaSettings.SettingsCreator.addHeader(MOD_ID, "exiledSector_opCostHeader", "Skill Node Ordnance Point Cost", "");
        LunaSettings.SettingsCreator.addText(MOD_ID, "exiledSector_opCostAbout",
                "How many ordnance points each allocated skill node costs, by hull size.", "");
        LunaSettings.SettingsCreator.addInt(MOD_ID, SkillNodeOpCost.FRIGATE_FIELD_ID,
                "Frigate OP per Node", "", SkillNodeOpCost.DEFAULT_FRIGATE, 0, 100, "");
        LunaSettings.SettingsCreator.addInt(MOD_ID, SkillNodeOpCost.DESTROYER_FIELD_ID,
                "Destroyer OP per Node", "", SkillNodeOpCost.DEFAULT_DESTROYER, 0, 100, "");
        LunaSettings.SettingsCreator.addInt(MOD_ID, SkillNodeOpCost.CRUISER_FIELD_ID,
                "Cruiser OP per Node", "", SkillNodeOpCost.DEFAULT_CRUISER, 0, 100, "");
        LunaSettings.SettingsCreator.addInt(MOD_ID, SkillNodeOpCost.CAPITAL_FIELD_ID,
                "Capital OP per Node", "", SkillNodeOpCost.DEFAULT_CAPITAL, 0, 100, "");
        LunaSettings.SettingsCreator.addInt(MOD_ID, SkillNodeOpCost.UNDEFINED_FIELD_ID,
                "Other Hull Sizes OP per Node", "", SkillNodeOpCost.DEFAULT_UNDEFINED, 0, 100, "");
    }
}
