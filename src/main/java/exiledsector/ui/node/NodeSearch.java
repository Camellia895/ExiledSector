package exiledsector.ui.node;

import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import exiledsector.skills.unlock.SkillTypeUnlockStatus;

import java.util.Locale;

public final class NodeSearch {

    static final float DIM_ALPHA = 0.2f;

    private String query = "";

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query == null ? "" : query;
    }

    public boolean isActive() {
        return !query.isEmpty();
    }

    public float backgroundAlpha() {
        return isActive() ? DIM_ALPHA : 1f;
    }

    boolean matches(SkillNode node, ShipSkillData data) {
        if (!isActive() || SkillTypeUnlockStatus.isHidden(node.getType(), data)) {
            return false;
        }
        String needle = query.toLowerCase(Locale.ROOT);
        if (nameContains(node.resolveEffectiveType(data), needle)) {
            return true;
        }
        for (String optionId : node.getType().getOptionalOptionIds()) {
            if (nameContains(SkillTree.getType(optionId), needle)) {
                return true;
            }
        }
        return false;
    }

    float nodeAlpha(SkillNode node, ShipSkillData data) {
        return !isActive() || matches(node, data) ? 1f : DIM_ALPHA;
    }

    float connectorAlpha(SkillNode a, SkillNode b, ShipSkillData data) {
        return !isActive() || (matches(a, data) && matches(b, data)) ? 1f : DIM_ALPHA;
    }

    private static boolean nameContains(SkillType type, String needle) {
        return type != null && type.getDisplayName() != null
                && type.getDisplayName().toLowerCase(Locale.ROOT).contains(needle);
    }
}
