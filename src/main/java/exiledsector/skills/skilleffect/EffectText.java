package exiledsector.skills.skilleffect;

import exiledsector.i18n.Message;
import exiledsector.i18n.StyledText;
import exiledsector.i18n.Translation;

final class EffectText {

    private EffectText() {
    }

    static StyledText pctChange(float magnitude, String statKey) {
        return StatMode.PERCENT.description(magnitude, Translation.text(statKey));
    }

    static StyledText flatChange(float magnitude, String statKey) {
        return StatMode.FLAT.description(magnitude, Translation.text(statKey));
    }

    static StyledText pctMore(float magnitude, String statKey) {
        return StatMode.MULT.description(magnitude, Translation.text(statKey));
    }

    static String key(SkillEffect effect) {
        return "effect." + effect.name();
    }

    static StyledText of(SkillEffect effect) {
        return Translation.styled(key(effect));
    }

    static Message msg(SkillEffect effect) {
        return Translation.msg(key(effect));
    }

    static Message msg(SkillEffect effect, String variant) {
        return Translation.msg(key(effect) + "." + variant);
    }

    static Message signed(SkillEffect effect, float magnitude) {
        return msg(effect, magnitude >= 0 ? "more" : "less").arg("value", Math.abs(magnitude));
    }
}
