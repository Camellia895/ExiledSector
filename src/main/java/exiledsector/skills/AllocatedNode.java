package exiledsector.skills;

import java.util.ArrayList;
import java.util.List;

public record AllocatedNode(SkillNode node, SkillType effectiveType) {

    public static List<AllocatedNode> of(ShipSkillData data) {
        List<AllocatedNode> allocated = new ArrayList<>();
        for (String nodeId : data.getAllocatedNodeIds()) {
            SkillNode node = SkillTree.get(nodeId);
            if (node != null) {
                allocated.add(new AllocatedNode(node, node.resolveEffectiveType(data)));
            }
        }
        return allocated;
    }

    public static AllocatedNode planned(SkillNode node, SkillType option) {
        return new AllocatedNode(node, option != null ? option : node.getType());
    }

    public boolean isExclusiveWith(SkillType other) {
        return node.getType().isExclusiveWith(other) || effectiveType.isExclusiveWith(other);
    }

    public boolean isExclusiveWith(AllocatedNode other) {
        return isExclusiveWith(other.node().getType()) || isExclusiveWith(other.effectiveType());
    }
}
