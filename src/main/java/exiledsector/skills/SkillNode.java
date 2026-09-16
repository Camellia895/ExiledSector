package exiledsector.skills;

import java.util.Collections;
import java.util.List;

/**
 * Definition of a single skill tree node. Content is authored as data (see
 * SkillTreeLoader, data/skilltrees/skill_tree.json) and loaded into SkillTree;
 * this class is just the shape that data gets parsed into.
 */
public class SkillNode {

    private final String id;
    private final String displayName;
    private final String iconPath;
    private final int opCost;
    private final float xpCost;
    private final List<String> prerequisiteNodeIds;
    // Position offset from the tree's center point, in the same panel-space
    // units as SkillTreeRefitButton's own layout - hand-placed per node
    // rather than auto-laid-out, since the JSON is meant to be hand-authored.
    private final float offsetX;
    private final float offsetY;

    public SkillNode(String id, String displayName, String iconPath, int opCost, float xpCost,
                      List<String> prerequisiteNodeIds, float offsetX, float offsetY) {
        this.id = id;
        this.displayName = displayName;
        this.iconPath = iconPath;
        this.opCost = opCost;
        this.xpCost = xpCost;
        this.prerequisiteNodeIds = prerequisiteNodeIds == null ? Collections.emptyList() : prerequisiteNodeIds;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconPath() {
        return iconPath;
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

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }
}
