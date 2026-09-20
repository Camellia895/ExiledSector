package exiledsector.skills;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SkillTree {

    private static final Map<String, SkillNode> NODES = new LinkedHashMap<>();
    private static final Map<String, SkillType> TYPES = new LinkedHashMap<>();
    private static final Map<String, ConnectorCurve> CURVES = new LinkedHashMap<>();
    private static final List<StaticImage> STATIC_IMAGES = new ArrayList<>();

    private SkillTree() {
    }

    public static void load() {
        NODES.clear();
        TYPES.clear();
        CURVES.clear();
        STATIC_IMAGES.clear();
        TYPES.putAll(SkillTypeLoader.loadSkillTypes());
        for (SkillNode node : SkillTreeLoader.loadNodes()) {
            register(node);
        }
        CURVES.putAll(SkillTreeLoader.loadConnectorCurves());
        STATIC_IMAGES.addAll(SkillTreeLoader.loadStaticImages());
    }

    public static void register(SkillNode node) {
        NODES.put(node.getId(), node);
    }

    public static SkillNode get(String nodeId) {
        return NODES.get(nodeId);
    }

    public static Map<String, SkillNode> getAllNodes() {
        return NODES;
    }

    public static SkillType getType(String typeId) {
        return TYPES.get(typeId);
    }

    public static void registerType(SkillType type) {
        TYPES.put(type.getId(), type);
    }

    public static Map<String, SkillType> getAllTypes() {
        return TYPES;
    }

    public static ConnectorCurve getCurve(String aId, String bId) {
        return CURVES.get(curveKey(aId, bId));
    }

    public static String curveKey(String aId, String bId) {
        return aId.compareTo(bId) <= 0 ? aId + "|" + bId : bId + "|" + aId;
    }

    public static List<StaticImage> getStaticImages() {
        return STATIC_IMAGES;
    }
}
