package exiledsector.skills.enemy;

import exiledsector.skills.tags.NodeRequirements;
import exiledsector.skills.tags.ShipProfile;

import java.util.List;

public record EnemyLayout(String id, String name, String rootNodeId, List<String> requires, String description,
                          List<EnemyLayoutEntry> entries) {

    public EnemyLayout {
        requires = requires == null ? List.of() : List.copyOf(requires);
        entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public boolean isEligible(ShipProfile profile) {
        return NodeRequirements.isSatisfiedBy(requires, profile);
    }
}
