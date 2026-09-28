package exiledsector.skills.enemy;

import lunalib.lunaSettings.LunaSettings;

import static exiledsector.ExiledSectorModPlugin.MOD_ID;

public final class EnemyTreeConfig {

    public static final String ENABLED_FIELD_ID = "exiledSector_enemyTreesEnabled";
    public static final String OFFICERED_SHIPS_FIELD_ID = "exiledSector_enemyTreesOfficeredShips";
    public static final String FLAGSHIP_FIELD_ID = "exiledSector_enemyTreesFlagship";
    public static final String OTHER_SHIP_CHANCE_FIELD_ID = "exiledSector_enemyTreesOtherShipChance";

    public static final boolean DEFAULT_ENABLED = true;
    public static final boolean DEFAULT_OFFICERED_SHIPS = true;
    public static final boolean DEFAULT_FLAGSHIP = true;
    public static final int DEFAULT_OTHER_SHIP_CHANCE_PERCENT = 30;

    private EnemyTreeConfig() {
    }

    public static boolean isEnabled() {
        return bool(ENABLED_FIELD_ID, DEFAULT_ENABLED);
    }

    public static boolean levelsOfficeredShips() {
        return bool(OFFICERED_SHIPS_FIELD_ID, DEFAULT_OFFICERED_SHIPS);
    }

    public static boolean levelsFlagship() {
        return bool(FLAGSHIP_FIELD_ID, DEFAULT_FLAGSHIP);
    }

    public static float otherShipChance() {
        Integer value = LunaSettings.getInt(MOD_ID, OTHER_SHIP_CHANCE_FIELD_ID);
        int percent = value != null ? value : DEFAULT_OTHER_SHIP_CHANCE_PERCENT;
        return Math.max(0, Math.min(100, percent)) / 100f;
    }

    private static boolean bool(String fieldId, boolean defaultValue) {
        Boolean value = LunaSettings.getBoolean(MOD_ID, fieldId);
        return value != null ? value : defaultValue;
    }
}
