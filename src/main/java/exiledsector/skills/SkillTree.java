package exiledsector.skills;

import exiledsector.skills.layout.ConnectorCurve;
import exiledsector.skills.layout.RingBelt;
import exiledsector.skills.layout.Star;
import exiledsector.skills.layout.StaticImage;
import exiledsector.skills.loader.SkillTreeLoader;
import exiledsector.skills.loader.SkillTypeLoader;
import exiledsector.skills.loader.WormholePairValidator;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SkillTree {

    private static final Map<String, SkillNode> NODES = new LinkedHashMap<>();
    private static final Map<String, SkillType> TYPES = new LinkedHashMap<>();
    private static final Map<String, ConnectorCurve> CURVES = new LinkedHashMap<>();
    private static final Set<String> HIDDEN_CONNECTOR_KEYS = new HashSet<>();
    private static final List<StaticImage> STATIC_IMAGES = new ArrayList<>();
    private static final List<RingBelt> RING_BELTS = new ArrayList<>();
    private static final List<Star> STARS = new ArrayList<>();

    private SkillTree() {
    }

    public static void load() {
        NODES.clear();
        TYPES.clear();
        CURVES.clear();
        HIDDEN_CONNECTOR_KEYS.clear();
        STATIC_IMAGES.clear();
        RING_BELTS.clear();
        STARS.clear();

        Map<String, SkillType> types = SkillTypeLoader.loadSkillTypes();
        TYPES.putAll(types);

        SkillTreeLoader.ParsedTree parsed = SkillTreeLoader.loadAll(types);
        for (SkillNode node : parsed.nodes) {
            register(node);
        }
        for (String issue : WormholePairValidator.findIssues(NODES.values())) {
            Logger.getLogger(SkillTree.class).error(issue);
        }
        CURVES.putAll(parsed.connectorCurves);
        HIDDEN_CONNECTOR_KEYS.addAll(parsed.hiddenConnectors);
        STATIC_IMAGES.addAll(parsed.staticImages);
        RING_BELTS.addAll(parsed.ringBelts);
        STARS.addAll(parsed.stars);
    }

    public static void register(SkillNode node) {
        if (NODES.containsKey(node.getId())) {
            Logger.getLogger(SkillTree.class).error("Duplicate skill node id \"" + node.getId() + "\" - the earlier definition was overwritten.");
        }
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
        if (TYPES.containsKey(type.getId())) {
            Logger.getLogger(SkillTree.class).error("Duplicate skill type id \"" + type.getId() + "\" - the earlier definition was overwritten.");
        }
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

    public static boolean isConnectorVisible(String aId, String bId) {
        return !HIDDEN_CONNECTOR_KEYS.contains(curveKey(aId, bId));
    }

    public static List<StaticImage> getStaticImages() {
        return STATIC_IMAGES;
    }

    public static List<RingBelt> getRingBelts() {
        return RING_BELTS;
    }

    public static List<Star> getStars() {
        return STARS;
    }
}
