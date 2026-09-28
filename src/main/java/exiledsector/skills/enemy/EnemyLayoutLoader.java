package exiledsector.skills.enemy;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EnemyLayoutLoader {

    public static final String DATA_PATH = "data/config/exiledSector/enemy_layouts.json";
    static final String CLEAR_ARRAY_MARKER = "core_clearArray";

    private EnemyLayoutLoader() {
    }

    public static Map<String, EnemyLayout> load() {
        try {
            return parseLayouts(Global.getSettings().getMergedJSON(DATA_PATH));
        } catch (IOException | JSONException | RuntimeException e) {
            Logger.getLogger(EnemyLayoutLoader.class).error("Failed to load " + DATA_PATH, e);
            return new LinkedHashMap<>();
        }
    }

    public static Map<String, EnemyLayout> parseLayouts(JSONObject root) {
        Map<String, EnemyLayout> layouts = new LinkedHashMap<>();
        JSONObject layoutsJson = root == null ? null : root.optJSONObject("layouts");
        if (layoutsJson == null) {
            Logger.getLogger(EnemyLayoutLoader.class).error(DATA_PATH + " has no \"layouts\" object - no enemy layouts loaded.");
            return layouts;
        }
        for (String id : layoutIds(layoutsJson)) {
            try {
                layouts.put(id, parseLayout(id, layoutsJson.opt(id)));
            } catch (JSONException e) {
                Logger.getLogger(EnemyLayoutLoader.class).error("Skipping malformed enemy layout \"" + id + "\" in "
                        + DATA_PATH + ": " + e.getMessage());
            }
        }
        return layouts;
    }

    static List<String> layoutIds(JSONObject layoutsJson) {
        List<String> ids = new ArrayList<>();
        Iterator<?> keys = layoutsJson.sortedKeys();
        while (keys.hasNext()) {
            ids.add(String.valueOf(keys.next()));
        }
        return ids;
    }

    static EnemyLayout parseLayout(String id, Object json) throws JSONException {
        if (!(json instanceof JSONObject layoutJson)) {
            throw new JSONException("expected an object with \"root\" and \"nodes\"");
        }
        String rootNodeId = layoutJson.optString("root", "").trim();
        if (rootNodeId.isEmpty()) {
            throw new JSONException("missing \"root\"");
        }
        JSONArray nodes = layoutJson.optJSONArray("nodes");
        if (nodes == null) {
            throw new JSONException("missing \"nodes\" array");
        }
        return new EnemyLayout(id, layoutJson.optString("name", id), rootNodeId, parseRequires(layoutJson),
                layoutJson.optString("description", ""), parseEntries(nodes));
    }

    private static List<String> parseRequires(JSONObject layoutJson) throws JSONException {
        List<String> requires = new ArrayList<>();
        if (!layoutJson.has("requires")) {
            return requires;
        }
        JSONArray array = layoutJson.optJSONArray("requires");
        if (array == null) {
            throw new JSONException("\"requires\" must be an array of tags");
        }
        for (int i = 0; i < array.length(); i++) {
            String tag = array.getString(i).trim();
            if (!tag.isEmpty() && !CLEAR_ARRAY_MARKER.equals(tag)) {
                requires.add(tag);
            }
        }
        return requires;
    }

    private static List<EnemyLayoutEntry> parseEntries(JSONArray nodes) throws JSONException {
        List<EnemyLayoutEntry> entries = new ArrayList<>();
        for (int i = 0; i < nodes.length(); i++) {
            Object value = nodes.get(i);
            if (!CLEAR_ARRAY_MARKER.equals(value)) {
                entries.add(parseEntry(value, i));
            }
        }
        return entries;
    }

    private static EnemyLayoutEntry parseEntry(Object value, int index) throws JSONException {
        String nodeId = "";
        String optionTypeId = null;
        if (value instanceof String plainId) {
            nodeId = plainId.trim();
        } else if (value instanceof JSONObject entryJson) {
            nodeId = entryJson.optString("id", "").trim();
            String option = entryJson.optString("option", "").trim();
            optionTypeId = option.isEmpty() ? null : option;
        }
        if (nodeId.isEmpty()) {
            throw new JSONException("nodes[" + index + "] must be a node id or an {\"id\": ..., \"option\": ...} object");
        }
        return new EnemyLayoutEntry(nodeId, optionTypeId);
    }
}
