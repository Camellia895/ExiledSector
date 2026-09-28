package exiledsector.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import exiledsector.skills.skilleffect.WeaponEffectTooltipAggregator;

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
    private final List<String> tags;

    public SkillNode(String id, SkillType type, List<String> connectedNodeIds, float offsetX, float offsetY) {
        this(id, type, connectedNodeIds, offsetX, offsetY, SkillNodeDecoration.NONE);
    }

    public SkillNode(String id, SkillType type, List<String> connectedNodeIds, float offsetX, float offsetY, SkillNodeDecoration decoration) {
        this(id, type, connectedNodeIds, offsetX, offsetY, decoration, Collections.emptyList());
    }

    public SkillNode(String id, SkillType type, List<String> connectedNodeIds, float offsetX, float offsetY,
                     SkillNodeDecoration decoration, List<String> tags) {
        super(id, offsetX, offsetY);
        this.type = type;
        this.connectedNodeIds = connectedNodeIds == null ? Collections.emptyList() : connectedNodeIds;
        this.ringBeltPath = decoration.ringBeltPath();
        this.ringBeltColor = decoration.ringBeltColor();
        this.ringBeltWidth = decoration.ringBeltWidth();
        this.wormholeColor = decoration.wormholeColor();
        this.pairedNodeId = decoration.pairedNodeId();
        this.tags = tags == null ? Collections.emptyList() : tags;
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
        List<String> texts = new ArrayList<>();
        for (DescriptionLine line : describeTypeLines(type, hullSize)) {
            texts.add(line.text());
        }
        return String.join("\n\n", texts);
    }

    public static List<DescriptionLine> describeTypeLines(SkillType type, HullSize hullSize) {
        List<DescriptionLine> lines = new ArrayList<>();
        addLine(lines, type.getDescriptionOverride(), false);
        List<SkillTypeEffect> described = hullSize == null ? type.getEffects() : type.effectsFor(hullSize);
        for (SkillTypeEffect effect : WeaponEffectTooltipAggregator.collapse(described)) {
            addLine(lines, effect.effect().describe(effect.magnitude()), effect.effect().lowerIsBetter());
        }
        for (String hullModId : type.getInstalledHullModIds()) {
            HullModSpecAPI spec = Global.getSettings().getHullModSpec(hullModId);
            String name = spec != null ? spec.getDisplayName() : hullModId;
            lines.add(new DescriptionLine("Installs the " + name + " hull mod at no OP cost.", false));
        }
        addLine(lines, describeTemporaryDuration(type), false);
        for (SkillTypeEffect effect : described) {
            addLine(lines, effect.effect().deallocationWarning(effect.magnitude()), false);
        }
        addLine(lines, describeItemCost(type), false);
        addLine(lines, describeExclusivity(type), false);
        return lines;
    }

    private static void addLine(List<DescriptionLine> lines, String text, boolean lowerIsBetter) {
        if (text != null) {
            lines.add(new DescriptionLine(text, lowerIsBetter));
        }
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
        return "Costs " + itemCost.formattedQuantity() + " " + itemCost.commodityName() + " to allocate. It is returned to you upon de-allocation.";
    }

    private static String describeExclusivity(SkillType type) {
        Set<String> hullModNames = new LinkedHashSet<>();
        for (String hullModId : type.getExclusiveHullModIds()) {
            if (!type.getInstalledHullModIds().contains(hullModId)) {
                HullModSpecAPI spec = Global.getSettings().getHullModSpec(hullModId);
                hullModNames.add(spec != null ? spec.getDisplayName() : hullModId);
            }
        }
        Set<String> nodeNames = new LinkedHashSet<>();
        for (String skillTypeId : type.getExclusiveSkillTypeIds()) {
            SkillType other = SkillTree.getType(skillTypeId);
            nodeNames.add(other != null ? other.getDisplayName() : skillTypeId);
        }

        List<String> lines = new ArrayList<>();
        addIfPresent(lines, exclusivityLine("hullmod", hullModNames));
        addIfPresent(lines, exclusivityLine("node", nodeNames));
        return lines.isEmpty() ? null : String.join("\n\n", lines);
    }

    private static String exclusivityLine(String kind, Set<String> names) {
        if (names.isEmpty()) {
            return null;
        }
        String label = names.size() == 1 ? kind : kind + "s";
        return "Mutually exclusive with " + label + ": " + String.join(", ", names) + ".";
    }

    public SkillType resolveEffectiveType(ShipSkillData data) {
        if (!type.isOptional()) return type;
        String selectedId = data.getOptionalSelection(getId());
        if (selectedId == null) return type;
        SkillType chosen = SkillTree.getType(selectedId);
        return chosen != null ? chosen : type;
    }

    public List<String> getTags() {
        return tags;
    }

    public Set<String> effectiveTags(ShipSkillData data) {
        return effectiveTags(data == null ? null : resolveEffectiveType(data));
    }

    public Set<String> effectiveTags(SkillType chosenOption) {
        Set<String> combined = new LinkedHashSet<>(tags);
        combined.addAll(type.getTags());
        if (chosenOption != null) {
            combined.addAll(chosenOption.getTags());
        }
        return Collections.unmodifiableSet(combined);
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
