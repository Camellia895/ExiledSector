package exiledsector.skills.npc;

import exiledsector.skills.ShipSkillData;

import java.util.List;

public record NpcTreeBuild(ShipSkillData data, List<NpcBuildStep> steps, List<String> strippedHullModIds) {

    public NpcTreeBuild {
        steps = List.copyOf(steps);
        strippedHullModIds = strippedHullModIds == null ? List.of() : List.copyOf(strippedHullModIds);
    }

    public List<String> allocatedNodeIds() {
        return steps.stream().filter(NpcBuildStep::isAllocated).map(NpcBuildStep::nodeId).toList();
    }

    public int allocatedCount() {
        return (int) steps.stream().filter(NpcBuildStep::isAllocated).count();
    }
}
