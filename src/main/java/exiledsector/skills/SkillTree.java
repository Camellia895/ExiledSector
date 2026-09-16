package exiledsector.skills;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry of all defined SkillNodes. Node content is not authored yet -
 * this holds the lookup structure for future tree definitions to populate.
 */
public class SkillTree {

    private static final Map<String, SkillNode> NODES = new LinkedHashMap<>();

    static {
        // TODO: define real nodes here, e.g.:
        // register(new SkillNode("armor_1", "Reinforced Plating", 2, 500f, List.of()));
    }

    private SkillTree() {
    }

    public static void register(SkillNode node) {
        NODES.put(node.getId(), node);
    }

    public static SkillNode get(String nodeId) {
        return NODES.get(nodeId);
    }

    public static Map<String, SkillNode> getAllNodes() {
        return NODES;
    }
}
