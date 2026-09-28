package exiledsector.skills.enemy;

import exiledsector.skills.ShipSkillData;

import java.util.List;

public record EnemyTreeBuild(ShipSkillData data, List<EnemyBuildStep> steps, List<String> strippedHullModIds) {

    public EnemyTreeBuild {
        steps = List.copyOf(steps);
        strippedHullModIds = strippedHullModIds == null ? List.of() : List.copyOf(strippedHullModIds);
    }

    public List<String> allocatedNodeIds() {
        return steps.stream().filter(EnemyBuildStep::isAllocated).map(EnemyBuildStep::nodeId).toList();
    }

    public int allocatedCount() {
        return (int) steps.stream().filter(EnemyBuildStep::isAllocated).count();
    }
}
