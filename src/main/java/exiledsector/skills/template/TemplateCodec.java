package exiledsector.skills.template;

import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class TemplateCodec {

    static final int VERSION = 1;
    private static final String OPTION_SEPARATOR = "=";

    private TemplateCodec() {
    }

    public static String encode(SkillTreeTemplate template) {
        JSONArray steps = new JSONArray();
        for (TemplateStep step : template.steps()) {
            steps.put(step.optionTypeId() == null ? step.nodeId() : step.nodeId() + OPTION_SEPARATOR + step.optionTypeId());
        }
        try {
            JSONObject json = new JSONObject();
            json.put("v", VERSION);
            json.put("id", template.id());
            json.put("name", template.name());
            json.put("root", template.rootNodeId());
            if (template.hullSize() != null) {
                json.put("hull", template.hullSize().name());
            }
            json.put("steps", steps);
            return json.toString();
        } catch (JSONException e) {
            throw new IllegalArgumentException("Template " + template.id() + " could not be encoded", e);
        }
    }

    public static SkillTreeTemplate decode(String text) {
        if (text == null) {
            return null;
        }
        JSONObject json;
        try {
            json = new JSONObject(text);
        } catch (JSONException e) {
            return null;
        }
        String id = json.optString("id", null);
        String name = json.optString("name", null);
        String root = json.optString("root", null);
        if (isBlank(id) || isBlank(name) || isBlank(root)) {
            return null;
        }
        return new SkillTreeTemplate(id, name, root, parseHullSize(json.optString("hull", null)), parseSteps(json.optJSONArray("steps")));
    }

    private static HullSize parseHullSize(String name) {
        if (name == null) {
            return null;
        }
        try {
            return HullSize.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static List<TemplateStep> parseSteps(JSONArray array) {
        List<TemplateStep> steps = new ArrayList<>();
        if (array == null) {
            return steps;
        }
        for (int i = 0; i < array.length(); i++) {
            String entry = array.optString(i, "").trim();
            if (entry.isEmpty()) {
                continue;
            }
            int separator = entry.indexOf(OPTION_SEPARATOR);
            if (separator < 0) {
                steps.add(new TemplateStep(entry, null));
            } else if (separator > 0) {
                String option = entry.substring(separator + 1);
                steps.add(new TemplateStep(entry.substring(0, separator), option.isEmpty() ? null : option));
            }
        }
        return steps;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
