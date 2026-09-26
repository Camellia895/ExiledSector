package exiledsector.skills.loader;

import com.fs.starfarer.api.Global;
import exiledsector.skills.BlueprintCategory;
import exiledsector.skills.HullSizeSkillEffect;
import exiledsector.skills.SkillItemCost;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillType;
import exiledsector.skills.SkillTypeEffect;
import exiledsector.skills.UnlockCondition;
import exiledsector.skills.UnlockConditionType;
import exiledsector.skills.skilleffect.SkillEffect;
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
            if (types.containsKey(type.getId())) {
                Logger.getLogger(SkillTypeLoader.class).error("Duplicate skill type id \"" + type.getId()
                        + "\" in " + DATA_PATH + " - the earlier definition was overwritten.");
            }
            types.put(type.getId(), type);
        }
        return types;
    }

    private static SkillType parseSkillType(JSONObject json) throws JSONException {
        SkillTier tier = SkillTier.valueOf(json.optString("tier", "SMALL"));
        List<SkillTypeEffect> effects = parseEffects(json.optJSONArray("effects"));
        List<HullSizeSkillEffect> hullSizeEffects = parseHullSizeEffects(json.optJSONArray("hullSizeEffects"));
        List<String> optionalOptionIds = parseStringArray(json.optJSONArray("optionalOptions"));
        List<String> exclusiveHullModIds = parseStringArray(json.optJSONArray("exclusiveHullMods"));
        List<String> exclusiveSkillTypeIds = parseStringArray(json.optJSONArray("exclusiveSkillTypes"));
        List<UnlockCondition> unlockConditions = parseUnlockConditions(json.optJSONArray("unlockConditions"));
        SkillItemCost itemCost = parseItemCost(json.optJSONObject("itemCost"));

        return new SkillType.Builder(json.getString("id"), json.getString("name"), json.getString("icon"), tier)
                .effects(effects)
                .hullSizeEffects(hullSizeEffects)
                .vanillaHullModId(json.optString("vanillaHullMod", null))
                .itemCost(itemCost)
                .descriptionOverride(json.optString("description", null))
                .todo(json.optString("todo", null))
                .optionalOptionIds(optionalOptionIds)
                .exclusiveHullModIds(exclusiveHullModIds)
                .exclusiveSkillTypeIds(exclusiveSkillTypeIds)
                .unlockConditions(unlockConditions)
                .build();
    }

    private static SkillItemCost parseItemCost(JSONObject itemCostJson) throws JSONException {
        if (itemCostJson == null) {
            return null;
        }
        return new SkillItemCost(itemCostJson.getString("itemId"), (float) itemCostJson.getDouble("quantity"));
    }

    private static List<UnlockCondition> parseUnlockConditions(JSONArray conditionsArray) throws JSONException {
        List<UnlockCondition> conditions = new ArrayList<>();
        if (conditionsArray == null) {
            return conditions;
        }
        for (int i = 0; i < conditionsArray.length(); i++) {
            conditions.add(parseUnlockCondition(conditionsArray.getJSONObject(i)));
        }
        return conditions;
    }

    private static UnlockCondition parseUnlockCondition(JSONObject json) throws JSONException {
        UnlockConditionType type = UnlockConditionType.valueOf(toEnumName(json.getString("type")));
        return switch (type) {
            case BLUEPRINT -> {
                BlueprintCategory category = BlueprintCategory.valueOf(toEnumName(json.getString("category")));
                yield UnlockCondition.blueprint(category, json.getString("id"));
            }
            case CHARACTER_STAT -> UnlockCondition.characterStat(json.getString("statId"));
            case MIN_SHIP_LEVEL -> UnlockCondition.minShipLevel(json.getInt("level"));
            case MEMORY_FLAG -> UnlockCondition.memoryFlag(json.getString("key"));
            default -> throw new JSONException("Unknown unlock condition type \"" + json.getString("type") + "\"");
        };
    }

    private static String toEnumName(String jsonValue) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < jsonValue.length(); i++) {
            char c = jsonValue.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                result.append('_');
            }
            result.append(Character.toUpperCase(c));
        }
        return result.toString();
    }

    private static List<String> parseStringArray(JSONArray array) throws JSONException {
        List<String> values = new ArrayList<>();
        if (array == null) {
            return values;
        }
        for (int i = 0; i < array.length(); i++) {
            values.add(array.getString(i));
        }
        return values;
    }

    private static List<SkillTypeEffect> parseEffects(JSONArray effectsArray) throws JSONException {
        List<SkillTypeEffect> effects = new ArrayList<>();
        if (effectsArray == null) {
            return effects;
        }
        for (int i = 0; i < effectsArray.length(); i++) {
            JSONObject entry = effectsArray.getJSONObject(i);
            SkillEffect effect = SkillEffect.byName(entry.getString("effect"));
            float magnitude = (float) entry.optDouble("magnitude", 0.0);
            effects.add(new SkillTypeEffect(effect, magnitude));
        }
        return effects;
    }

    private static List<HullSizeSkillEffect> parseHullSizeEffects(JSONArray effectsArray) throws JSONException {
        List<HullSizeSkillEffect> effects = new ArrayList<>();
        if (effectsArray == null) {
            return effects;
        }
        for (int i = 0; i < effectsArray.length(); i++) {
            JSONObject entry = effectsArray.getJSONObject(i);
            SkillEffect effect = SkillEffect.byName(entry.getString("effect"));
            float frigate = (float) entry.getDouble("frigate");
            float destroyer = (float) entry.getDouble("destroyer");
            float cruiser = (float) entry.getDouble("cruiser");
            float capitalShip = (float) entry.getDouble("capitalShip");
            effects.add(new HullSizeSkillEffect(effect, frigate, destroyer, cruiser, capitalShip));
        }
        return effects;
    }
}
