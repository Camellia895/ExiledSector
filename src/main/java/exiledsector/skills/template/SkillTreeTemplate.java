package exiledsector.skills.template;

import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record SkillTreeTemplate(String id, String name, String rootNodeId, HullSize hullSize, List<TemplateStep> steps) {

    public SkillTreeTemplate {
        steps = List.copyOf(steps);
    }

    public Set<String> nodeIds() {
        Set<String> ids = new LinkedHashSet<>();
        ids.add(rootNodeId);
        for (TemplateStep step : steps) {
            ids.add(step.nodeId());
        }
        return Set.copyOf(ids);
    }
}
