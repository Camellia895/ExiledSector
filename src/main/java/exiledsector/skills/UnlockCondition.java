package exiledsector.skills;

public final class UnlockCondition {

    private final UnlockConditionType type;
    private final BlueprintCategory blueprintCategory;
    private final String key;
    private final int minLevel;

    private UnlockCondition(UnlockConditionType type, BlueprintCategory blueprintCategory, String key, int minLevel) {
        this.type = type;
        this.blueprintCategory = blueprintCategory;
        this.key = key;
        this.minLevel = minLevel;
    }

    public static UnlockCondition blueprint(BlueprintCategory category, String id) {
        return new UnlockCondition(UnlockConditionType.BLUEPRINT, category, id, 0);
    }

    public static UnlockCondition characterStat(String statId) {
        return new UnlockCondition(UnlockConditionType.CHARACTER_STAT, null, statId, 0);
    }

    public static UnlockCondition minShipLevel(int level) {
        return new UnlockCondition(UnlockConditionType.MIN_SHIP_LEVEL, null, null, level);
    }

    public static UnlockCondition memoryFlag(String key) {
        return new UnlockCondition(UnlockConditionType.MEMORY_FLAG, null, key, 0);
    }

    public UnlockConditionType getType() {
        return type;
    }

    public BlueprintCategory getBlueprintCategory() {
        return blueprintCategory;
    }

    public String getKey() {
        return key;
    }

    public int getMinLevel() {
        return minLevel;
    }

    @Override
    public String toString() {
        return "UnlockCondition{type=" + type + ", blueprintCategory=" + blueprintCategory
                + ", key=" + key + ", minLevel=" + minLevel + "}";
    }
}
