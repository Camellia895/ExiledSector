package exiledsector.skills;

import java.util.Collections;
import java.util.List;

/**
 * A single placed instance of a SkillType within the tree: its id,
 * prerequisites, and position. Display name/icon/cost are not duplicated
 * here - they're delegated to the shared SkillType so authoring many copies
 * of the same skill (e.g. four "Capacitors" nodes) doesn't repeat that data.
 * Content is authored as data (see SkillTreeLoader,
 * data/skilltrees/ship_skill_tree.json) and loaded into SkillTree.
 */
public class SkillNode {

    private final String id;
    private final SkillType type;
    private final List<String> prerequisiteNodeIds;
    // Position offset from the tree's center point, in the same panel-space
    // units as SkillTreeRefitButton's own layout - hand-placed per node
    // rather than auto-laid-out, since the JSON is meant to be hand-authored.
    private final float offsetX;
    private final float offsetY;

    public SkillNode(String id, SkillType type, List<String> prerequisiteNodeIds, float offsetX, float offsetY) {
        this.id = id;
        this.type = type;
        this.prerequisiteNodeIds = prerequisiteNodeIds == null ? Collections.emptyList() : prerequisiteNodeIds;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public String getId() {
        return id;
    }

    public SkillType getType() {
        return type;
    }

    public String getDisplayName() {
        return type.getDisplayName();
    }

    public String getIconPath() {
        return type.getIconPath();
    }

    public int getOpCost() {
        return type.getOpCost();
    }

    public float getXpCost() {
        return type.getXpCost();
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
