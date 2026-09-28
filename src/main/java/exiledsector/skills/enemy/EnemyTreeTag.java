package exiledsector.skills.enemy;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;

import java.util.ArrayList;
import java.util.List;

public final class EnemyTreeTag {

    public static final String PREFIX = "exiledSector_enemyTree|";
    private static final String FIELD_SEPARATOR = "|";
    private static final String NODE_SEPARATOR = ",";
    private static final String OPTION_SEPARATOR = "=";

    private EnemyTreeTag() {
    }

    public static String encode(String layoutId, ShipSkillData data) {
        List<String> nodes = new ArrayList<>();
        for (String nodeId : data.getAllocatedNodeIds()) {
            String option = data.getOptionalSelection(nodeId);
            nodes.add(option == null ? nodeId : nodeId + OPTION_SEPARATOR + option);
        }
        return PREFIX + layoutId + FIELD_SEPARATOR + data.getLevel() + FIELD_SEPARATOR + String.join(NODE_SEPARATOR, nodes);
    }

    public static String find(ShipVariantAPI variant) {
        if (variant == null || variant.getTags() == null) {
            return null;
        }
        for (String tag : variant.getTags()) {
            if (tag != null && tag.startsWith(PREFIX)) {
                return tag;
            }
        }
        return null;
    }

    public static String layoutId(String tag) {
        String[] fields = fields(tag);
        return fields == null ? null : fields[0];
    }

    public static ShipSkillData decode(String tag) {
        String[] fields = fields(tag);
        if (fields == null) {
            return null;
        }
        int level;
        try {
            level = Integer.parseInt(fields[1]);
        } catch (NumberFormatException e) {
            return null;
        }
        String[] entries = fields[2].isEmpty() ? new String[0] : fields[2].split(NODE_SEPARATOR, -1);
        ShipSkillData data = new ShipSkillData();
        data.markEnemyBuild();
        SkillNode root = entries.length == 0 ? null : SkillTree.get(entries[0]);
        if (root == null || !data.chooseStartingRoot(root)) {
            return data;
        }
        for (int i = 0; i < level; i++) {
            data.addFreeAllocationCredit();
        }
        for (int i = 1; i < entries.length; i++) {
            restore(data, entries[i]);
        }
        for (int i = 0; i < level; i++) {
            data.incrementLevel();
        }
        return data;
    }

    private static void restore(ShipSkillData data, String entry) {
        int optionAt = entry.indexOf(OPTION_SEPARATOR);
        String nodeId = optionAt < 0 ? entry : entry.substring(0, optionAt);
        SkillNode node = SkillTree.get(nodeId);
        if (node == null || data.isAllocated(nodeId) || node.getType().getTier() == SkillTier.ROOT) {
            return;
        }
        if (optionAt < 0) {
            if (!node.getType().isOptional()) {
                data.allocate(node, 1);
            }
            return;
        }
        String optionId = entry.substring(optionAt + 1);
        SkillType option = SkillTree.getType(optionId);
        if (option != null && node.getType().getOptionalOptionIds().contains(optionId)) {
            data.selectOption(node, option, 1);
        }
    }

    private static String[] fields(String tag) {
        if (tag == null || !tag.startsWith(PREFIX)) {
            return null;
        }
        String[] fields = tag.substring(PREFIX.length()).split("\\" + FIELD_SEPARATOR, -1);
        return fields.length == 3 && !fields[0].isEmpty() ? fields : null;
    }
}
