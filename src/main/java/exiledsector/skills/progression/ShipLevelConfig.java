package exiledsector.skills.progression;

import exiledsector.ModSettings;

public final class ShipLevelConfig {

    public static final String MAX_LEVEL_FIELD_ID = "exiledSector_levelMax";
    public static final String XP_BASE_FIELD_ID = "exiledSector_levelXpBase";
    public static final String XP_GROWTH_FIELD_ID = "exiledSector_levelXpGrowth";
    public static final String XP_GROWTH_CUTOFF_LEVEL_FIELD_ID = "exiledSector_levelXpGrowthCutoffLevel";
    public static final String XP_PER_DEPLOYMENT_POINT_FIELD_ID = "exiledSector_levelXpPerDeploymentPoint";
    public static final String XP_LOSS_MULTIPLIER_FIELD_ID = "exiledSector_levelXpLossMultiplier";
    public static final String XP_DIFFICULTY_STRENGTH_FIELD_ID = "exiledSector_levelXpDifficultyStrength";
    public static final String XP_DIFFICULTY_MAX_MULTIPLIER_FIELD_ID = "exiledSector_levelXpDifficultyMaxMultiplier";
    public static final String MAX_ALLOCATED_NODES_FIELD_ID = "exiledSector_levelMaxAllocatedNodes";

    public static final int DEFAULT_MAX_LEVEL = 50;
    public static final int DEFAULT_XP_BASE = 60;
    public static final float DEFAULT_XP_GROWTH = 1.13f;
    public static final int DEFAULT_XP_GROWTH_CUTOFF_LEVEL = 25;
    public static final float DEFAULT_XP_PER_DEPLOYMENT_POINT = 1f;
    public static final float DEFAULT_XP_LOSS_MULTIPLIER = 0.5f;
    public static final float DEFAULT_XP_DIFFICULTY_STRENGTH = 1f;
    public static final float DEFAULT_XP_DIFFICULTY_MAX_MULTIPLIER = 6f;
    public static final int DEFAULT_MAX_ALLOCATED_NODES = 60;

    private ShipLevelConfig() {
    }

    public static int maxLevel() {
        return ModSettings.intOr(MAX_LEVEL_FIELD_ID, DEFAULT_MAX_LEVEL);
    }

    public static float xpBase() {
        return ModSettings.intOr(XP_BASE_FIELD_ID, DEFAULT_XP_BASE);
    }

    public static float xpGrowth() {
        return ModSettings.floatOr(XP_GROWTH_FIELD_ID, DEFAULT_XP_GROWTH);
    }

    public static int xpGrowthCutoffLevel() {
        return ModSettings.intOr(XP_GROWTH_CUTOFF_LEVEL_FIELD_ID, DEFAULT_XP_GROWTH_CUTOFF_LEVEL);
    }

    public static float xpPerDeploymentPoint() {
        return ModSettings.floatOr(XP_PER_DEPLOYMENT_POINT_FIELD_ID, DEFAULT_XP_PER_DEPLOYMENT_POINT);
    }

    public static float xpLossMultiplier() {
        return ModSettings.floatOr(XP_LOSS_MULTIPLIER_FIELD_ID, DEFAULT_XP_LOSS_MULTIPLIER);
    }

    public static float xpDifficultyStrength() {
        return ModSettings.floatOr(XP_DIFFICULTY_STRENGTH_FIELD_ID, DEFAULT_XP_DIFFICULTY_STRENGTH);
    }

    public static float xpDifficultyMaxMultiplier() {
        return ModSettings.floatOr(XP_DIFFICULTY_MAX_MULTIPLIER_FIELD_ID, DEFAULT_XP_DIFFICULTY_MAX_MULTIPLIER);
    }

    public static int maxAllocatedNodes() {
        return ModSettings.intOr(MAX_ALLOCATED_NODES_FIELD_ID, DEFAULT_MAX_ALLOCATED_NODES);
    }

    public static int maxAllocatedNodesBesidesRoot() {
        return Math.max(0, maxAllocatedNodes() - 1);
    }
}
