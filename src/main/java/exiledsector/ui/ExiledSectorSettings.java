package exiledsector.ui;

import exiledsector.skills.npc.NpcLevelTable;
import exiledsector.skills.npc.NpcTreeConfig;
import exiledsector.skills.progression.ShipLevelConfig;
import exiledsector.skills.progression.SkillNodeOpCost;
import exiledsector.skills.skilleffect.MaxChainCountConfig;
import exiledsector.skills.unlock.HiddenNodeDisplayConfig;
import exiledsector.skills.unlock.UnlockConditionOverrides;
import exiledsector.ui.inspect.NpcInspectConfig;
import lunalib.lunaSettings.LunaSettings;

import static exiledsector.ExiledSectorModPlugin.MOD_ID;

public final class ExiledSectorSettings {

    static final String NPC_SCALING_TAB = "NPC Scaling";

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

        registerNpcScaling();

        LunaSettings.SettingsCreator.refresh(MOD_ID);
    }

    private static void registerNpcScaling() {
        LunaSettings.SettingsCreator.addHeader(MOD_ID, "exiledSector_npcTreesHeader", "NPC Skill Trees", NPC_SCALING_TAB);
        LunaSettings.SettingsCreator.addText(MOD_ID, "exiledSector_npcTreesAbout",
                "Some ships in NPC fleets get their own skill tree so they keep up with your fleet. This covers "
                        + "every faction, including your own faction's fleets and your allies. A chosen ship follows one "
                        + "of the NPC build layouts and gets the free root node plus a number of nodes rolled from the "
                        + "table below for your character level. If the ship strips a hullmod to take its matching "
                        + "node, the OP that hullmod used buys extra nodes at the normal per-node OP cost.", NPC_SCALING_TAB);
        LunaSettings.SettingsCreator.addBoolean(MOD_ID, NpcTreeConfig.ENABLED_FIELD_ID,
                "Enable NPC Skill Trees", "When off, no NPC ship gets a skill tree.",
                NpcTreeConfig.DEFAULT_ENABLED, NPC_SCALING_TAB);
        LunaSettings.SettingsCreator.addBoolean(MOD_ID, NpcTreeConfig.OFFICERED_SHIPS_FIELD_ID,
                "Always Level Officered Ships", "Every NPC ship captained by an officer gets a skill tree.",
                NpcTreeConfig.DEFAULT_OFFICERED_SHIPS, NPC_SCALING_TAB);
        LunaSettings.SettingsCreator.addBoolean(MOD_ID, NpcTreeConfig.FLAGSHIP_FIELD_ID,
                "Always Level the Flagship", "Every NPC fleet's flagship gets a skill tree.",
                NpcTreeConfig.DEFAULT_FLAGSHIP, NPC_SCALING_TAB);
        LunaSettings.SettingsCreator.addInt(MOD_ID, NpcTreeConfig.OTHER_SHIP_CHANCE_FIELD_ID,
                "Chance for Other Ships", "Percent chance that each remaining combat ship in an NPC fleet gets a skill tree.",
                NpcTreeConfig.DEFAULT_OTHER_SHIP_CHANCE_PERCENT, 0, 100, NPC_SCALING_TAB);
        LunaSettings.SettingsCreator.addKeybind(MOD_ID, NpcInspectConfig.KEYBIND_FIELD_ID,
                "Inspect NPC Skill Trees Key", "In a fleet encounter, or while hovering another fleet on the map, "
                        + "press this key to list its ships that have skill trees and their bonuses.",
                NpcInspectConfig.DEFAULT_KEY, NPC_SCALING_TAB);

        LunaSettings.SettingsCreator.addHeader(MOD_ID, "exiledSector_npcNodesHeader", "Nodes per Player Level", NPC_SCALING_TAB);
        LunaSettings.SettingsCreator.addText(MOD_ID, "exiledSector_npcNodesAbout",
                "Each levelled NPC ship rolls its node count between the minimum and maximum for your current "
                        + "character level, on top of its free root node. Levels above "
                        + NpcLevelTable.MAX_PLAYER_LEVEL + " use the level " + NpcLevelTable.MAX_PLAYER_LEVEL
                        + " row. If a minimum is set above its maximum, the two are swapped.", NPC_SCALING_TAB);
        for (int level = NpcLevelTable.MIN_PLAYER_LEVEL; level <= NpcLevelTable.MAX_PLAYER_LEVEL; level++) {
            LunaSettings.SettingsCreator.addHeader(MOD_ID, "exiledSector_npcLevel" + level + "Header",
                    "Player Level " + level, NPC_SCALING_TAB);
            LunaSettings.SettingsCreator.addInt(MOD_ID, NpcLevelTable.minNodesFieldId(level),
                    "Minimum Nodes", "Fewest nodes a levelled NPC ship gets at player level " + level + ".",
                    NpcLevelTable.defaultMinNodes(level), NpcLevelTable.MIN_NODES, NpcLevelTable.MAX_NODES,
                    NPC_SCALING_TAB);
            LunaSettings.SettingsCreator.addInt(MOD_ID, NpcLevelTable.maxNodesFieldId(level),
                    "Maximum Nodes", "Most nodes a levelled NPC ship gets at player level " + level + ".",
                    NpcLevelTable.defaultMaxNodes(level), NpcLevelTable.MIN_NODES, NpcLevelTable.MAX_NODES,
                    NPC_SCALING_TAB);
        }
    }
}
