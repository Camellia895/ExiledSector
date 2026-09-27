package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.StatBonus;

import static exiledsector.skills.skilleffect.SkillEffectText.flatChange;
import static exiledsector.skills.skilleffect.SkillEffectText.pctChange;
import static exiledsector.skills.skilleffect.SkillEffectText.pctMore;

public enum StatMode {

    FLAT {
        @Override
        void apply(MutableStat stat, String modId, float magnitude) {
            stat.modifyFlat(modId, magnitude);
        }

        @Override
        void apply(StatBonus stat, String modId, float magnitude) {
            stat.modifyFlat(modId, magnitude);
        }

        @Override
        String describe(float magnitude, String statName) {
            return flatChange(magnitude, statName);
        }

        @Override
        float inverse(float magnitude) {
            return -magnitude;
        }
    },
    PERCENT {
        @Override
        void apply(MutableStat stat, String modId, float magnitude) {
            stat.modifyPercent(modId, magnitude);
        }

        @Override
        void apply(StatBonus stat, String modId, float magnitude) {
            stat.modifyPercent(modId, magnitude);
        }

        @Override
        String describe(float magnitude, String statName) {
            return pctChange(magnitude, statName);
        }

        @Override
        float inverse(float magnitude) {
            return -magnitude;
        }
    },
    MULT {
        @Override
        void apply(MutableStat stat, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stat, modId, magnitude);
        }

        @Override
        void apply(StatBonus stat, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stat, modId, magnitude);
        }

        @Override
        String describe(float magnitude, String statName) {
            return pctMore(magnitude, statName);
        }

        @Override
        float inverse(float magnitude) {
            return (1f / SkillEffectSupport.multFrom(magnitude) - 1f) * 100f;
        }
    };

    abstract void apply(MutableStat stat, String modId, float magnitude);

    abstract void apply(StatBonus stat, String modId, float magnitude);

    abstract String describe(float magnitude, String statName);

    abstract float inverse(float magnitude);
}
