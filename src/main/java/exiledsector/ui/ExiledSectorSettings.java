package exiledsector.ui;

import exiledsector.skills.ShipLevelConfig;
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

        LunaSettings.SettingsCreator.addHeader(MOD_ID, "exiledSector_levelHeader", "Ship Leveling", "");
        LunaSettings.SettingsCreator.addText(MOD_ID, "exiledSector_levelAbout",
                "Ships gain XP from combat and level up. Each level converts the ship's most recently " +
                        "OP-purchased node into a free one (or banks the free allocation for the next node " +
                        "allocated, if there's nothing eligible to convert).", "");
        LunaSettings.SettingsCreator.addInt(MOD_ID, ShipLevelConfig.MAX_LEVEL_FIELD_ID,
                "Max Level", "", ShipLevelConfig.DEFAULT_MAX_LEVEL, 1, 200, "");
        LunaSettings.SettingsCreator.addInt(MOD_ID, ShipLevelConfig.XP_BASE_FIELD_ID,
                "XP for Level 1", "", ShipLevelConfig.DEFAULT_XP_BASE, 1, 10000, "");
        LunaSettings.SettingsCreator.addDouble(MOD_ID, ShipLevelConfig.XP_GROWTH_FIELD_ID,
                "XP Growth per Level", "How much harder each level is to reach than the last.",
                ShipLevelConfig.DEFAULT_XP_GROWTH, 1.0, 3.0, "");
        LunaSettings.SettingsCreator.addInt(MOD_ID, ShipLevelConfig.XP_PER_COMBAT_FIELD_ID,
                "XP per Combat Won", "", ShipLevelConfig.DEFAULT_XP_PER_COMBAT, 0, 10000, "");
        LunaSettings.SettingsCreator.addDouble(MOD_ID, ShipLevelConfig.XP_LOSS_MULTIPLIER_FIELD_ID,
                "XP Multiplier on Loss", "", ShipLevelConfig.DEFAULT_XP_LOSS_MULTIPLIER, 0.0, 1.0, "");
        LunaSettings.SettingsCreator.addInt(MOD_ID, ShipLevelConfig.MAX_ALLOCATED_NODES_FIELD_ID,
                "Max Allocated Nodes", "Maximum number of nodes a ship can have allocated at once, free or not.",
                ShipLevelConfig.DEFAULT_MAX_ALLOCATED_NODES, 1, 500, "");
    }
}
