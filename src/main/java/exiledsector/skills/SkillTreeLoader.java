package exiledsector.skills;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SkillTreeLoader {

    private static final String DATA_PATH = "data/skilltrees/ship_skill_tree.json";

    private SkillTreeLoader() {
    }

    public static List<SkillNode> loadNodes() {
        try {
            Map<String, SkillType> skillTypes = SkillTypeLoader.loadSkillTypes();
            return parseNodes(Global.getSettings().loadJSON(DATA_PATH), skillTypes);
        } catch (IOException | JSONException e) {
            Logger.getLogger(SkillTreeLoader.class).error("Failed to load " + DATA_PATH, e);
            return new ArrayList<>();
        }
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
                (float) json.optDouble("y", 0));
    }
}
