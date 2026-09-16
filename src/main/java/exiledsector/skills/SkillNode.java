package exiledsector.skills;

import java.util.Collections;
import java.util.List;

/**
 * Definition of a single skill tree node. Content (real nodes, costs, effects)
 * is authored via SkillTree; this class is just the data shape.
 */
public class SkillNode {

    private final String id;
    private final String displayName;
    private final int opCost;
    private final float xpCost;
    private final List<String> prerequisiteNodeIds;

    public SkillNode(String id, String displayName, int opCost, float xpCost, List<String> prerequisiteNodeIds) {
        this.id = id;
        this.displayName = displayName;
        this.opCost = opCost;
        this.xpCost = xpCost;
        this.prerequisiteNodeIds = prerequisiteNodeIds == null ? Collections.emptyList() : prerequisiteNodeIds;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getOpCost() {
        return opCost;
    }

    public float getXpCost() {
        return xpCost;
    }

    public List<String> getPrerequisiteNodeIds() {
        return prerequisiteNodeIds;
    }
}
