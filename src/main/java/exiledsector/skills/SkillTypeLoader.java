package exiledsector.skills;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SkillTypeLoader {

    private static final String DATA_PATH = "data/skilltrees/skill_types.json";

    private SkillTypeLoader() {
    }

    public static Map<String, SkillType> loadSkillTypes() {
        try {
            return parseSkillTypes(Global.getSettings().loadJSON(DATA_PATH));
        } catch (IOException | JSONException e) {
            Logger.getLogger(SkillTypeLoader.class).error("Failed to load " + DATA_PATH, e);
            return new LinkedHashMap<>();
        }
    }

    public static Map<String, SkillType> parseSkillTypes(JSONObject root) throws JSONException {
        Map<String, SkillType> types = new LinkedHashMap<>();
        JSONArray typeArray = root.getJSONArray("skillTypes");
        for (int i = 0; i < typeArray.length(); i++) {
            SkillType type = parseSkillType(typeArray.getJSONObject(i));
            types.put(type.getId(), type);
        }
        return types;
    }

    private static SkillType parseSkillType(JSONObject json) throws JSONException {
        String effectName = json.optString("effect", null);
        SkillEffect effect = effectName == null ? null : SkillEffect.valueOf(effectName);

        return new SkillType(
                json.getString("id"),
                json.getString("name"),
                json.getString("icon"),
                json.optInt("opCost", 0),
                (float) json.optDouble("xpCost", 0),
                effect,
                (float) json.optDouble("magnitude", 0));
    }
}
