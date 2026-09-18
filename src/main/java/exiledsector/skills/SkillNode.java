package exiledsector.skills;

import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SkillNode {

    private final String id;
    private final SkillType type;
    private final List<String> connectedNodeIds;
    private final float offsetX;
    private final float offsetY;

    public SkillNode(String id, SkillType type, List<String> connectedNodeIds, float offsetX, float offsetY) {
        this.id = id;
        this.type = type;
        this.connectedNodeIds = connectedNodeIds == null ? Collections.emptyList() : connectedNodeIds;
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
        return getDescription(null);
    }

    public String getDescription(HullSize hullSize) {
        if (type.getDescriptionOverride() != null) {
            return type.getDescriptionOverride();
        }
        List<String> lines = new ArrayList<>();
        for (SkillTypeEffect effect : type.getEffects()) {
            lines.add(effect.effect().describe(effect.magnitude()));
        }
        if (hullSize != null) {
            for (HullSizeSkillEffect effect : type.getHullSizeEffects()) {
                lines.add(effect.effect().describe(effect.valueFor(hullSize)));
            }
        }
        return String.join(" ", lines);
    }

    public List<String> getConnectedNodeIds() {
        return connectedNodeIds;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }
}
