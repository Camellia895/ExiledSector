package exiledsector.skills.loader;

import com.fs.starfarer.api.Global;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import exiledsector.ui.decoration.RingBelt;
import exiledsector.ui.decoration.Star;
import exiledsector.ui.decoration.StaticImage;
import exiledsector.ui.node.ConnectorCurve;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SkillTreeLoader {

    private static final String DATA_PATH = "data/skilltrees/ship_skill_tree.json";

    private SkillTreeLoader() {
    }

    public static JSONObject loadRoot() {
        try {
            return Global.getSettings().loadJSON(DATA_PATH);
        } catch (IOException | JSONException e) {
            Logger.getLogger(SkillTreeLoader.class).error("Failed to load " + DATA_PATH, e);
            return null;
        }
    }

    public static ParsedTree loadAll(Map<String, SkillType> skillTypes) {
        JSONObject root = loadRoot();
        if (root == null) {
            return new ParsedTree(new ArrayList<>(), new LinkedHashMap<>(), new HashSet<>(),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
        return new ParsedTree(
                safeParse("nodes", () -> parseNodes(root, skillTypes), new ArrayList<>()),
                safeParse("connector curves", () -> parseConnectorCurves(root), new LinkedHashMap<>()),
                safeParse("hidden connectors", () -> parseHiddenConnectors(root), new HashSet<>()),
                safeParse("static images", () -> parseStaticImages(root), new ArrayList<>()),
                safeParse("ring belts", () -> parseRingBelts(root), new ArrayList<>()),
                safeParse("stars", () -> parseStars(root), new ArrayList<>()));
    }

    private static <T> T safeParse(String context, JsonParser<T> parser, T fallback) {
        try {
            return parser.parse();
        } catch (JSONException e) {
            Logger.getLogger(SkillTreeLoader.class).error("Failed to parse " + context + " from " + DATA_PATH, e);
            return fallback;
        }
    }

    private interface JsonParser<T> {
        T parse() throws JSONException;
    }

    public static final class ParsedTree {
        public final List<SkillNode> nodes;
        public final Map<String, ConnectorCurve> connectorCurves;
        public final Set<String> hiddenConnectors;
        public final List<StaticImage> staticImages;
        public final List<RingBelt> ringBelts;
        public final List<Star> stars;

        ParsedTree(List<SkillNode> nodes, Map<String, ConnectorCurve> connectorCurves, Set<String> hiddenConnectors,
                   List<StaticImage> staticImages, List<RingBelt> ringBelts, List<Star> stars) {
            this.nodes = nodes;
            this.connectorCurves = connectorCurves;
            this.hiddenConnectors = hiddenConnectors;
            this.staticImages = staticImages;
            this.ringBelts = ringBelts;
            this.stars = stars;
        }
    }

    public static List<RingBelt> parseRingBelts(JSONObject root) throws JSONException {
        List<RingBelt> ringBelts = new ArrayList<>();
        JSONArray beltArray = root.optJSONArray("ringBelts");
        if (beltArray == null) return ringBelts;
        for (int i = 0; i < beltArray.length(); i++) {
            JSONObject beltJson = beltArray.getJSONObject(i);
            ringBelts.add(new RingBelt(
                    beltJson.getString("id"),
                    (float) beltJson.getDouble("x"),
                    (float) beltJson.getDouble("y"),
                    (float) beltJson.getDouble("innerRadius"),
                    (float) beltJson.getDouble("outerRadius"),
                    beltJson.getString("ringArtPath"),
                    (float) beltJson.optDouble("rotation", 0.0),
                    (float) beltJson.optDouble("rotationSpeed", 0.0)));
        }
        return ringBelts;
    }

    public static List<StaticImage> parseStaticImages(JSONObject root) throws JSONException {
        List<StaticImage> images = new ArrayList<>();
        JSONArray imageArray = root.optJSONArray("staticImages");
        if (imageArray == null) return images;
        for (int i = 0; i < imageArray.length(); i++) {
            JSONObject imageJson = imageArray.getJSONObject(i);
            images.add(new StaticImage(
                    imageJson.getString("id"),
                    (float) imageJson.getDouble("x"),
                    (float) imageJson.getDouble("y"),
                    (float) imageJson.getDouble("width"),
                    (float) imageJson.getDouble("height"),
                    imageJson.getString("imagePath"),
                    (float) imageJson.optDouble("rotation", 0.0),
                    (float) imageJson.optDouble("rotationSpeed", 0.0)));
        }
        return images;
    }

    public static List<Star> parseStars(JSONObject root) throws JSONException {
        List<Star> stars = new ArrayList<>();
        JSONArray starArray = root.optJSONArray("stars");
        if (starArray == null) return stars;
        for (int i = 0; i < starArray.length(); i++) {
            JSONObject starJson = starArray.getJSONObject(i);
            stars.add(new Star(
                    starJson.getString("id"),
                    (float) starJson.getDouble("x"),
                    (float) starJson.getDouble("y"),
                    (float) starJson.getDouble("radius"),
                    starJson.optString("starType", "star_yellow"),
                    starJson.optString("color", null)));
        }
        return stars;
    }

    public static Map<String, ConnectorCurve> parseConnectorCurves(JSONObject root) throws JSONException {
        Map<String, ConnectorCurve> curves = new LinkedHashMap<>();
        JSONArray curveArray = root.optJSONArray("connectorCurves");
        if (curveArray == null) return curves;
        for (int i = 0; i < curveArray.length(); i++) {
            JSONObject curveJson = curveArray.getJSONObject(i);
            String a = curveJson.getString("a");
            String b = curveJson.getString("b");
            float controlX = (float) curveJson.getDouble("controlX");
            float controlY = (float) curveJson.getDouble("controlY");
            curves.put(SkillTree.curveKey(a, b), new ConnectorCurve(controlX, controlY));
        }
        return curves;
    }

    public static Set<String> parseHiddenConnectors(JSONObject root) throws JSONException {
        Set<String> hiddenKeys = new HashSet<>();
        JSONArray hiddenArray = root.optJSONArray("hiddenConnectors");
        if (hiddenArray == null) return hiddenKeys;
        for (int i = 0; i < hiddenArray.length(); i++) {
            JSONObject hiddenJson = hiddenArray.getJSONObject(i);
            hiddenKeys.add(SkillTree.curveKey(hiddenJson.getString("a"), hiddenJson.getString("b")));
        }
        return hiddenKeys;
    }

    public static List<SkillNode> parseNodes(JSONObject root, Map<String, SkillType> skillTypes) throws JSONException {
        List<SkillNode> nodes = new ArrayList<>();
        JSONArray nodeArray = root.getJSONArray("nodes");
        for (int i = 0; i < nodeArray.length(); i++) {
            nodes.add(parseNode(nodeArray.getJSONObject(i), skillTypes));
        }
        return nodes;
    }

    private static SkillNode parseNode(JSONObject json, Map<String, SkillType> skillTypes) throws JSONException {
        List<String> connectedTo = new ArrayList<>();
        JSONArray connectedArray = json.optJSONArray("connectedTo");
        if (connectedArray != null) {
            for (int i = 0; i < connectedArray.length(); i++) {
                connectedTo.add(connectedArray.getString(i));
            }
        }

        String typeId = json.getString("type");
        SkillType type = skillTypes.get(typeId);
        if (type == null) {
            throw new JSONException("Unknown skill type \"" + typeId + "\" referenced by node \"" + json.optString("id") + "\"");
        }

        return new SkillNode(
                json.getString("id"),
                type,
                connectedTo,
                (float) json.optDouble("x", 0),
                (float) json.optDouble("y", 0),
                json.optString("ringBeltPath", null),
                json.optString("ringBeltColor", null),
                json.has("ringBeltWidth") ? (float) json.getDouble("ringBeltWidth") : null,
                json.optString("wormholeColor", null),
                json.optString("pairedWith", null));
    }
}
