package exiledsector.skills;

import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SkillNode extends SkillTreeObject {

    private final SkillType type;
    private final List<String> connectedNodeIds;
    private final String ringBeltPath;
    private final String ringBeltColor;
    private final Float ringBeltWidth;
    private final String wormholeColor;

    public SkillNode(String id, SkillType type, List<String> connectedNodeIds, float offsetX, float offsetY) {
        this(id, type, connectedNodeIds, offsetX, offsetY, null, null);
    }

    public SkillNode(String id, SkillType type, List<String> connectedNodeIds, float offsetX, float offsetY, String ringBeltPath) {
        this(id, type, connectedNodeIds, offsetX, offsetY, ringBeltPath, null);
    }

    public SkillNode(String id, SkillType type, List<String> connectedNodeIds, float offsetX, float offsetY, String ringBeltPath, String ringBeltColor) {
        this(id, type, connectedNodeIds, offsetX, offsetY, ringBeltPath, ringBeltColor, null);
    }

    public SkillNode(String id, SkillType type, List<String> connectedNodeIds, float offsetX, float offsetY, String ringBeltPath, String ringBeltColor, Float ringBeltWidth) {
        this(id, type, connectedNodeIds, offsetX, offsetY, ringBeltPath, ringBeltColor, ringBeltWidth, null);
    }

    public SkillNode(String id, SkillType type, List<String> connectedNodeIds, float offsetX, float offsetY, String ringBeltPath, String ringBeltColor, Float ringBeltWidth, String wormholeColor) {
        super(id, offsetX, offsetY);
        this.type = type;
        this.connectedNodeIds = connectedNodeIds == null ? Collections.emptyList() : connectedNodeIds;
        this.ringBeltPath = ringBeltPath;
        this.ringBeltColor = ringBeltColor;
        this.ringBeltWidth = ringBeltWidth;
        this.wormholeColor = wormholeColor;
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
        String selectedId = data.getOptionalSelection(getId());
        if (selectedId == null) return type;
        SkillType chosen = SkillTree.getType(selectedId);
        return chosen != null ? chosen : type;
    }

    public List<String> getConnectedNodeIds() {
        return connectedNodeIds;
    }

    public float getOffsetX() {
        return getX();
    }

    public float getOffsetY() {
        return getY();
    }

    public String getRingBeltPath() {
        return ringBeltPath;
    }

    public String getRingBeltColor() {
        return ringBeltColor;
    }

    public Float getRingBeltWidth() {
        return ringBeltWidth;
    }

    public String getWormholeColor() {
        return wormholeColor;
    }
}
