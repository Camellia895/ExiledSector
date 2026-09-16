package exiledsector.skills;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry of all defined SkillNodes, populated by load() from
 * data/skilltrees/ship_skill_tree.json (see SkillTreeLoader) rather than
 * hardcoded here, so nodes can be added/tweaked without recompiling.
 */
public class SkillTree {

    private static final Map<String, SkillNode> NODES = new LinkedHashMap<>();

    private SkillTree() {
    }

    public static void load() {
        NODES.clear();
        for (SkillNode node : SkillTreeLoader.loadNodes()) {
            register(node);
        }
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
