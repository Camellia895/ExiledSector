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
        return describeType(type, hullSize);
    }

    public static String describeType(SkillType type, HullSize hullSize) {
        List<String> lines = new ArrayList<>();
        if (type.getDescriptionOverride() != null) {
            lines.add(type.getDescriptionOverride());
        }
        List<String> effectLines = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        for (SkillTypeEffect effect : type.getEffects()) {
            effectLines.add(effect.effect().describe(effect.magnitude()));
            String warning = effect.effect().deallocationWarning(effect.magnitude());
            if (warning != null) warnings.add(warning);
        }
        if (hullSize != null) {
            for (HullSizeSkillEffect effect : type.getHullSizeEffects()) {
                float magnitude = effect.valueFor(hullSize);
                effectLines.add(effect.effect().describe(magnitude));
                String warning = effect.effect().deallocationWarning(magnitude);
                if (warning != null) warnings.add(warning);
            }
        }
        lines.addAll(effectLines);
        lines.addAll(warnings);
        return String.join("\n\n", lines);
    }

    public SkillType resolveEffectiveType(ShipSkillData data) {
        if (!type.isOptional()) return type;
        String selectedId = data.getOptionalSelection(id);
        if (selectedId == null) return type;
        SkillType chosen = SkillTree.getType(selectedId);
        return chosen != null ? chosen : type;
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
