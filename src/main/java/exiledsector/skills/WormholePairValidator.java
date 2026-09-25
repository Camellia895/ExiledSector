package exiledsector.skills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WormholePairValidator {

    private static final String WORMHOLE_NODE_PREFIX = "Wormhole node \"";
    private static final String PAIRED_WITH = "\" is paired with \"";

    private WormholePairValidator() {
    }

    public static List<String> findIssues(Iterable<SkillNode> nodes) {
        Map<String, SkillNode> byId = new HashMap<>();
        for (SkillNode node : nodes) {
            byId.put(node.getId(), node);
        }

        List<String> issues = new ArrayList<>();
        for (SkillNode node : byId.values()) {
            if (node.getType().getTier() != SkillTier.WORMHOLE) continue;

            String pairedId = node.getPairedNodeId();
            if (pairedId == null) {
                issues.add(WORMHOLE_NODE_PREFIX + node.getId() + "\" has no pairedWith id set.");
                continue;
            }
            if (pairedId.equals(node.getId())) {
                issues.add(WORMHOLE_NODE_PREFIX + node.getId() + "\" is paired with itself.");
                continue;
            }

            SkillNode paired = byId.get(pairedId);
            if (paired == null) {
                issues.add(WORMHOLE_NODE_PREFIX + node.getId() + PAIRED_WITH + pairedId + "\", which does not exist.");
                continue;
            }
            if (paired.getType().getTier() != SkillTier.WORMHOLE) {
                issues.add(WORMHOLE_NODE_PREFIX + node.getId() + PAIRED_WITH + pairedId + "\", which is not a wormhole-tier node.");
                continue;
            }
            if (!node.getId().equals(paired.getPairedNodeId())) {
                issues.add(WORMHOLE_NODE_PREFIX + node.getId() + PAIRED_WITH + pairedId
                        + "\", but that node's pairedWith is \"" + paired.getPairedNodeId() + "\" instead.");
            }
        }
        return issues;
    }
}
