package exiledsector.skills;

public final class BlueprintNodeReveals {

    private BlueprintNodeReveals() {
    }

    public static boolean revealsAnyNode(String hullModId) {
        if (hullModId == null) {
            return false;
        }
        for (SkillNode node : SkillTree.getAllNodes().values()) {
            if (nodeRequiresBlueprint(node.getType(), hullModId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean nodeRequiresBlueprint(SkillType type, String hullModId) {
        if (requiresBlueprint(type, hullModId)) {
            return true;
        }
        for (String optionId : type.getOptionalOptionIds()) {
            if (requiresBlueprint(SkillTree.getType(optionId), hullModId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean requiresBlueprint(SkillType type, String hullModId) {
        if (type == null) {
            return false;
        }
        for (UnlockCondition condition : type.getUnlockConditions()) {
            if (condition.getType() == UnlockConditionType.BLUEPRINT && hullModId.equals(condition.getKey())) {
                return true;
            }
        }
        return false;
    }
}
