package exiledsector.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SkillNode extends SkillTreeObject {

    private final SkillType type;
    private final List<String> connectedNodeIds;
    private final String ringBeltPath;
    private final String ringBeltColor;
    private final Float ringBeltWidth;
    private final String wormholeColor;
    private final String pairedNodeId;

    public SkillNode(String id, SkillType type, List<String> connectedNodeIds, float offsetX, float offsetY) {
        this(id, type, connectedNodeIds, offsetX, offsetY, SkillNodeDecoration.NONE);
    }

    public SkillNode(String id, SkillType type, List<String> connectedNodeIds, float offsetX, float offsetY, SkillNodeDecoration decoration) {
        super(id, offsetX, offsetY);
        this.type = type;
        this.connectedNodeIds = connectedNodeIds == null ? Collections.emptyList() : connectedNodeIds;
        this.ringBeltPath = decoration.ringBeltPath();
        this.ringBeltColor = decoration.ringBeltColor();
        this.ringBeltWidth = decoration.ringBeltWidth();
        this.wormholeColor = decoration.wormholeColor();
        this.pairedNodeId = decoration.pairedNodeId();
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

    public String getDescription() {
        return getDescription(null);
    }

    public String getDescription(HullSize hullSize) {
        return describeType(type, hullSize);
    }

    private static void addIfPresent(List<String> lines, String line) {
        if (line != null) {
            lines.add(line);
        }
    }

    public static String describeType(SkillType type, HullSize hullSize) {
        List<String> lines = new ArrayList<>();
        addIfPresent(lines, type.getDescriptionOverride());
        List<String> effectLines = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        for (SkillTypeEffect effect : type.getEffects()) {
            addIfPresent(effectLines, effect.effect().describe(effect.magnitude()));
            addIfPresent(warnings, effect.effect().deallocationWarning(effect.magnitude()));
        }
        if (hullSize != null) {
            for (HullSizeSkillEffect effect : type.getHullSizeEffects()) {
                float magnitude = effect.valueFor(hullSize);
                addIfPresent(effectLines, effect.effect().describe(magnitude));
                addIfPresent(warnings, effect.effect().deallocationWarning(magnitude));
            }
        }
        for (String hullModId : type.getInstalledHullModIds()) {
            HullModSpecAPI spec = Global.getSettings().getHullModSpec(hullModId);
            String name = spec != null ? spec.getDisplayName() : hullModId;
            effectLines.add("Installs the " + name + " hull mod at no OP cost.");
        }
        lines.addAll(effectLines);
        addIfPresent(lines, describeTemporaryDuration(type));
        lines.addAll(warnings);
        addIfPresent(lines, describeItemCost(type));
        addIfPresent(lines, describeExclusivity(type));
        return String.join("\n\n", lines);
    }

    private static String describeTemporaryDuration(SkillType type) {
        Float seconds = type.getTemporaryAfterDeploymentSeconds();
        if (seconds == null) {
            return null;
        }
        String formatted = seconds == Math.rint(seconds) ? String.valueOf(seconds.intValue()) : String.valueOf(seconds);
        return "These effects only last for the first " + formatted + " seconds after the ship is deployed.";
    }

    private static String describeItemCost(SkillType type) {
        SkillItemCost itemCost = type.getItemCost();
        if (itemCost == null) {
            return null;
        }
        return "Costs " + itemCost.formattedQuantity() + " " + itemCost.commodityName() + " to allocate. It is retuned to you upon de-allocation";
    }

    private static String describeExclusivity(SkillType type) {
        List<String> lines = new ArrayList<>();
        Set<String> namesAlreadyShown = new LinkedHashSet<>();

        List<String> hullModNames = new ArrayList<>();
        for (String hullModId : type.getExclusiveHullModIds()) {
            if (type.getInstalledHullModIds().contains(hullModId)) {
                continue;
            }
            HullModSpecAPI spec = Global.getSettings().getHullModSpec(hullModId);
            String name = spec != null ? spec.getDisplayName() : hullModId;
            if (namesAlreadyShown.add(name)) {
                hullModNames.add(name);
            }
        }
        if (!hullModNames.isEmpty()) {
            lines.add("Mutually exclusive with: " + String.join(", ", hullModNames) + ".");
        }

        List<String> skillTypeNames = new ArrayList<>();
        for (String skillTypeId : type.getExclusiveSkillTypeIds()) {
            SkillType other = SkillTree.getType(skillTypeId);
            String name = other != null ? other.getDisplayName() : skillTypeId;
            if (namesAlreadyShown.add(name)) {
                skillTypeNames.add(name);
            }
        }
        if (!skillTypeNames.isEmpty()) {
            lines.add("Mutually exclusive with " + String.join(", ", skillTypeNames) + ".");
        }

        return lines.isEmpty() ? null : String.join("\n\n", lines);
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

    public String getPairedNodeId() {
        return pairedNodeId;
    }
}
