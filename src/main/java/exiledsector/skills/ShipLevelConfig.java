package exiledsector.skills;

import lunalib.lunaSettings.LunaSettings;

import static exiledsector.ExiledSectorModPlugin.MOD_ID;

public final class ShipLevelConfig {

    public static final String MAX_LEVEL_FIELD_ID = "exiledSector_levelMax";
    public static final String XP_BASE_FIELD_ID = "exiledSector_levelXpBase";
    public static final String XP_GROWTH_FIELD_ID = "exiledSector_levelXpGrowth";
    public static final String XP_GROWTH_CUTOFF_LEVEL_FIELD_ID = "exiledSector_levelXpGrowthCutoffLevel";
    public static final String XP_PER_DEPLOYMENT_POINT_FIELD_ID = "exiledSector_levelXpPerDeploymentPoint";
    public static final String XP_LOSS_MULTIPLIER_FIELD_ID = "exiledSector_levelXpLossMultiplier";
    public static final String MAX_ALLOCATED_NODES_FIELD_ID = "exiledSector_levelMaxAllocatedNodes";

    public static final int DEFAULT_MAX_LEVEL = 50;
    public static final int DEFAULT_XP_BASE = 60;
    public static final float DEFAULT_XP_GROWTH = 1.13f;
    public static final int DEFAULT_XP_GROWTH_CUTOFF_LEVEL = 25;
    public static final float DEFAULT_XP_PER_DEPLOYMENT_POINT = 1f;
    public static final float DEFAULT_XP_LOSS_MULTIPLIER = 0.5f;
    public static final int DEFAULT_MAX_ALLOCATED_NODES = 60;

    private ShipLevelConfig() {
    }

    public static int maxLevel() {
        Integer value = LunaSettings.getInt(MOD_ID, MAX_LEVEL_FIELD_ID);
        return value != null ? value : DEFAULT_MAX_LEVEL;
    }

    public static float xpBase() {
        Integer value = LunaSettings.getInt(MOD_ID, XP_BASE_FIELD_ID);
        return value != null ? value : DEFAULT_XP_BASE;
    }

    public static float xpGrowth() {
        Float value = LunaSettings.getFloat(MOD_ID, XP_GROWTH_FIELD_ID);
        return value != null ? value : DEFAULT_XP_GROWTH;
    }

    public static int xpGrowthCutoffLevel() {
        Integer value = LunaSettings.getInt(MOD_ID, XP_GROWTH_CUTOFF_LEVEL_FIELD_ID);
        return value != null ? value : DEFAULT_XP_GROWTH_CUTOFF_LEVEL;
    }

    public static float xpPerDeploymentPoint() {
        Float value = LunaSettings.getFloat(MOD_ID, XP_PER_DEPLOYMENT_POINT_FIELD_ID);
        return value != null ? value : DEFAULT_XP_PER_DEPLOYMENT_POINT;
    }

    public static float xpLossMultiplier() {
        Float value = LunaSettings.getFloat(MOD_ID, XP_LOSS_MULTIPLIER_FIELD_ID);
        return value != null ? value : DEFAULT_XP_LOSS_MULTIPLIER;
    }

    public static int maxAllocatedNodes() {
        Integer value = LunaSettings.getInt(MOD_ID, MAX_ALLOCATED_NODES_FIELD_ID);
        return value != null ? value : DEFAULT_MAX_ALLOCATED_NODES;
    }
}
