package exiledsector.ui;

import exiledsector.skills.HiddenNodeDisplayConfig;
import exiledsector.skills.MaxChainCountConfig;
import exiledsector.skills.ShipLevelConfig;
import exiledsector.skills.SkillNodeOpCost;
import exiledsector.skills.UnlockConditionOverrides;
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
                "Ships gain XP from combat, scaled by the deployment points of enemy ships destroyed in that " +
                        "battle, and level up. Each level converts the ship's most recently OP-purchased node " +
                        "into a free one (or banks the free allocation for the next node allocated, if there's " +
                        "nothing eligible to convert).", "");
        LunaSettings.SettingsCreator.addInt(MOD_ID, ShipLevelConfig.MAX_LEVEL_FIELD_ID,
                "Max Level", "", ShipLevelConfig.DEFAULT_MAX_LEVEL, 1, 200, "");
        LunaSettings.SettingsCreator.addInt(MOD_ID, ShipLevelConfig.XP_BASE_FIELD_ID,
                "XP for Level 1", "", ShipLevelConfig.DEFAULT_XP_BASE, 1, 10000, "");
        LunaSettings.SettingsCreator.addDouble(MOD_ID, ShipLevelConfig.XP_GROWTH_FIELD_ID,
                "XP Growth per Level", "How much harder each level is to reach than the last.",
                ShipLevelConfig.DEFAULT_XP_GROWTH, 1.0, 3.0, "");
        LunaSettings.SettingsCreator.addInt(MOD_ID, ShipLevelConfig.XP_GROWTH_CUTOFF_LEVEL_FIELD_ID,
                "XP Growth Cutoff Level", "The level at which the exponential XP curve stops compounding. " +
                        "From this level onward, every level costs the same XP as the level just before the " +
                        "cutoff did (e.g. a cutoff of 30 makes every level from 30 on cost the same as level 29 to 30).",
                ShipLevelConfig.DEFAULT_XP_GROWTH_CUTOFF_LEVEL, 1, 200, "");
        LunaSettings.SettingsCreator.addDouble(MOD_ID, ShipLevelConfig.XP_PER_DEPLOYMENT_POINT_FIELD_ID,
                "XP per Enemy Deployment Point Destroyed", "", ShipLevelConfig.DEFAULT_XP_PER_DEPLOYMENT_POINT, 0.0, 100.0, "");
        LunaSettings.SettingsCreator.addDouble(MOD_ID, ShipLevelConfig.XP_LOSS_MULTIPLIER_FIELD_ID,
                "XP Multiplier on Loss", "", ShipLevelConfig.DEFAULT_XP_LOSS_MULTIPLIER, 0.0, 1.0, "");
        LunaSettings.SettingsCreator.addInt(MOD_ID, ShipLevelConfig.MAX_ALLOCATED_NODES_FIELD_ID,
                "Max Allocated Nodes", "Maximum number of nodes a ship can have allocated at once, free or not.",
                ShipLevelConfig.DEFAULT_MAX_ALLOCATED_NODES, 1, 500, "");

        LunaSettings.SettingsCreator.addHeader(MOD_ID, "exiledSector_hiddenNodesHeader", "Hidden Nodes", "");
        LunaSettings.SettingsCreator.addText(MOD_ID, "exiledSector_hiddenNodesAbout",
                "Nodes with an unmet unlock condition normally render as unidentified, blinking sensor ghosts.",
                "");
        LunaSettings.SettingsCreator.addBoolean(MOD_ID, HiddenNodeDisplayConfig.SHOW_HIDDEN_NODES_FIELD_ID,
                "Show Hidden Nodes by Default", "Reveals the true appearance and tooltip of locked nodes instead " +
                        "of hiding them as sensor ghosts. Purely visual - locked nodes still cannot be allocated.",
                HiddenNodeDisplayConfig.DEFAULT_SHOW_HIDDEN_NODES, "");

        LunaSettings.SettingsCreator.addHeader(MOD_ID, "exiledSector_unlockConditionsHeader", "Unlock Conditions", "");
        LunaSettings.SettingsCreator.addText(MOD_ID, "exiledSector_unlockConditionsAbout",
                "Each toggle treats every unlock condition of that type as already met, letting nodes gated only " +
                        "by it be allocated. This does not grant hullmods, change game state, or affect anything " +
                        "else - allocation still requires the node's connected-node prerequisites to be satisfied.",
                "");
        LunaSettings.SettingsCreator.addBoolean(MOD_ID, UnlockConditionOverrides.DISABLE_BLUEPRINT_FIELD_ID,
                "Disable Blueprint Unlock Conditions", "", UnlockConditionOverrides.DEFAULT_DISABLED, "");
        LunaSettings.SettingsCreator.addBoolean(MOD_ID, UnlockConditionOverrides.DISABLE_CHARACTER_STAT_FIELD_ID,
                "Disable Character Stat Unlock Conditions", "", UnlockConditionOverrides.DEFAULT_DISABLED, "");
        LunaSettings.SettingsCreator.addBoolean(MOD_ID, UnlockConditionOverrides.DISABLE_MIN_SHIP_LEVEL_FIELD_ID,
                "Disable Minimum Ship Level Unlock Conditions", "", UnlockConditionOverrides.DEFAULT_DISABLED, "");
        LunaSettings.SettingsCreator.addBoolean(MOD_ID, UnlockConditionOverrides.DISABLE_MEMORY_FLAG_FIELD_ID,
                "Disable Game State Unlock Conditions", "", UnlockConditionOverrides.DEFAULT_DISABLED, "");

        LunaSettings.SettingsCreator.addHeader(MOD_ID, "exiledSector_energyChainHeader", "Non-Beam Energy Chain", "");
        LunaSettings.SettingsCreator.addInt(MOD_ID, MaxChainCountConfig.FIELD_ID,
                "Max Chain Count", "Safety limit on how many links a single non-beam energy chain can have, "
                        + "independent of chance rolls succeeding. Not intended as a balance knob.",
                MaxChainCountConfig.DEFAULT, 1, 20, "");
    }
}
