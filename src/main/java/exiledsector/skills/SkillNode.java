package exiledsector.skills;

import java.util.Collections;
import java.util.List;

public class SkillNode {

    private final String id;
    private final SkillType type;
    private final List<String> prerequisiteNodeIds;
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

    public String getDescription() {
        return type.getEffect() == null ? "" : type.getEffect().describe(type.getMagnitude());
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
