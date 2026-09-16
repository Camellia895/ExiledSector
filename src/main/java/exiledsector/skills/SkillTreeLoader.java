package exiledsector.skills;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads skill node definitions from data/skilltrees/skill_tree.json.
 * Parsing (parseNodes/parseNode) is kept separate from the Global-touching
 * file read (loadNodes) so it can be unit tested against a hand-built
 * JSONObject without a running game session.
 */
public final class SkillTreeLoader {

    private static final String DATA_PATH = "data/skilltrees/skill_tree.json";

    private SkillTreeLoader() {
    }

    public static List<SkillNode> loadNodes() {
        try {
            return parseNodes(Global.getSettings().loadJSON(DATA_PATH));
        } catch (IOException | JSONException e) {
            Logger.getLogger(SkillTreeLoader.class).error("Failed to load " + DATA_PATH, e);
            return new ArrayList<>();
        }
    }

    public static List<SkillNode> parseNodes(JSONObject root) throws JSONException {
        List<SkillNode> nodes = new ArrayList<>();
        JSONArray nodeArray = root.getJSONArray("nodes");
        for (int i = 0; i < nodeArray.length(); i++) {
            nodes.add(parseNode(nodeArray.getJSONObject(i)));
        }
        return nodes;
    }

    private static SkillNode parseNode(JSONObject json) throws JSONException {
        List<String> prerequisites = new ArrayList<>();
        JSONArray prereqArray = json.optJSONArray("prerequisites");
        if (prereqArray != null) {
            for (int i = 0; i < prereqArray.length(); i++) {
                prerequisites.add(prereqArray.getString(i));
            }
        }

        return new SkillNode(
                json.getString("id"),
                json.getString("name"),
                json.getString("icon"),
                json.optInt("opCost", 0),
                (float) json.optDouble("xpCost", 0),
                prerequisites,
                (float) json.optDouble("x", 0),
                (float) json.optDouble("y", 0));
    }
}
