package exiledsector.skills.template;

import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TemplateCapture {

    private TemplateCapture() {
    }

    public static List<TemplateStep> capture(ShipSkillData data, String startingRootId, Map<String, SkillNode> tree) {
        List<String> remaining = new ArrayList<>();
        for (String nodeId : data.getAllocatedNodeIds()) {
            if (!nodeId.equals(startingRootId) && tree.containsKey(nodeId)) {
                remaining.add(nodeId);
            }
        }
        Set<String> placed = new HashSet<>();
        if (startingRootId != null) {
            placed.add(startingRootId);
        }
        List<TemplateStep> steps = new ArrayList<>();
        while (!remaining.isEmpty()) {
            SkillNode next = firstPlaceable(remaining, placed, tree);
            if (next == null) {
                break;
            }
            remaining.remove(next.getId());
            place(next, data, placed, steps);
            String partnerId = next.getPairedNodeId();
            if (partnerId != null && remaining.remove(partnerId)) {
                place(tree.get(partnerId), data, placed, steps);
            }
        }
        for (String nodeId : remaining) {
            place(tree.get(nodeId), data, placed, steps);
        }
        return steps;
    }

    private static SkillNode firstPlaceable(List<String> remaining, Set<String> placed, Map<String, SkillNode> tree) {
        for (String nodeId : remaining) {
            SkillNode node = tree.get(nodeId);
            if (isPlaceable(node, placed)) {
                return node;
            }
        }
        return null;
    }

    private static boolean isPlaceable(SkillNode node, Set<String> placed) {
        List<String> connected = node.getConnectedNodeIds();
        if (connected.isEmpty()) {
            return true;
        }
        for (String connectedId : connected) {
            if (placed.contains(connectedId)) {
                return true;
            }
        }
        return false;
    }

    private static void place(SkillNode node, ShipSkillData data, Set<String> placed, List<TemplateStep> steps) {
        placed.add(node.getId());
        String option = node.getType().isOptional() ? data.getOptionalSelection(node.getId()) : null;
        steps.add(new TemplateStep(node.getId(), option));
    }
}
