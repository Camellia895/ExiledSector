package exiledsector.skills;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        if (type.getDescriptionOverride() != null) {
            return type.getDescriptionOverride();
        }
        if (!type.getEffects().isEmpty()) {
            return type.getEffects().stream()
                    .map(e -> e.effect().describe(e.magnitude()))
                    .collect(Collectors.joining(" "));
        }
        return "";
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
