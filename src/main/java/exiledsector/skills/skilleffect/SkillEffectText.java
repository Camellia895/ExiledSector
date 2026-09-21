package exiledsector.skills.skilleffect;

final class SkillEffectText {

    private SkillEffectText() {
    }

    static String pct(float magnitude) {
        if (magnitude == Math.rint(magnitude)) {
            return String.valueOf((int) magnitude);
        }
        return String.valueOf(magnitude);
    }

    static String pctChange(float magnitude, String stat) {
        String verb = magnitude >= 0 ? "Increases " : "Decreases ";
        return verb + stat + " by " + pct(Math.abs(magnitude)) + "%.";
    }

    static String flatChange(float magnitude, String stat) {
        String verb = magnitude >= 0 ? "Increases " : "Decreases ";
        return verb + stat + " by " + pct(Math.abs(magnitude)) + ".";
    }

    static String pctMore(float magnitude, String stat) {
        String verb = magnitude >= 0 ? "more " : "less ";
        return pct(Math.abs(magnitude)) + "% " + verb + stat + ".";
    }
}
