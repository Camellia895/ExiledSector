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
}
