package exiledsector.ui.node;

import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;

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

    boolean matches(SkillNode node, NodeAllocator.Snapshot tree) {
        if (!isActive() || tree.isHidden(node)) {
            return false;
        }
        String needle = query.toLowerCase(Locale.ROOT);
        if (nameContains(node.resolveEffectiveType(tree.data()), needle)) {
            return true;
        }
        for (String optionId : node.getType().getOptionalOptionIds()) {
            if (nameContains(SkillTree.getType(optionId), needle)) {
                return true;
            }
        }
        return false;
    }

    float nodeAlpha(SkillNode node, NodeAllocator.Snapshot tree) {
        return !isActive() || matches(node, tree) ? 1f : DIM_ALPHA;
    }

    float connectorAlpha(SkillNode a, SkillNode b, NodeAllocator.Snapshot tree) {
        return !isActive() || (matches(a, tree) && matches(b, tree)) ? 1f : DIM_ALPHA;
    }

    private static boolean nameContains(SkillType type, String needle) {
        return type != null && (containsIgnoringCase(type.getDisplayName(), needle)
                || containsIgnoringCase(type.getSourceName(), needle) || containsIgnoringCase(type.getId(), needle));
    }

    private static boolean containsIgnoringCase(String text, String needle) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(needle);
    }
}
