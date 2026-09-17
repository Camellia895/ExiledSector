package exiledsector.skills;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
        SkillTier tier = SkillTier.valueOf(json.optString("tier", "SMALL"));
        List<SkillTypeEffect> effects = parseEffects(json.optJSONArray("effects"));

        return new SkillType(
                json.getString("id"),
                json.getString("name"),
                json.getString("icon"),
                json.optInt("opCost", 0),
                (float) json.optDouble("xpCost", 0),
                effects,
                tier,
                json.optString("vanillaHullMod", null),
                json.optString("description", null),
                json.optString("todo", null));
    }

    private static List<SkillTypeEffect> parseEffects(JSONArray effectsArray) throws JSONException {
        List<SkillTypeEffect> effects = new ArrayList<>();
        if (effectsArray == null) {
            return effects;
        }
        for (int i = 0; i < effectsArray.length(); i++) {
            JSONObject entry = effectsArray.getJSONObject(i);
            SkillEffect effect = SkillEffect.valueOf(entry.getString("effect"));
            float magnitude = (float) entry.getDouble("magnitude");
            effects.add(new SkillTypeEffect(effect, magnitude));
        }
        return effects;
    }
}
