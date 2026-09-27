package exiledsector.skills.skilleffect;

import java.util.HashMap;
import java.util.Map;

final class SkillEffectRegistry {

    private static final Map<String, SkillEffect> BY_NAME = build();

    private SkillEffectRegistry() {
    }

    static SkillEffect byName(String name) {
        SkillEffect effect = BY_NAME.get(name);
        if (effect == null) {
            throw new IllegalArgumentException("Unknown SkillEffect: " + name);
        }
        return effect;
    }

    private static Map<String, SkillEffect> build() {
        Map<String, SkillEffect> registry = new HashMap<>();
        SkillEffect[][] groups = {
                MovementSkillEffect.values(),
                FluxSkillEffect.values(),
                WeaponSkillEffect.values(),
                ShieldSkillEffect.values(),
                LogisticsSkillEffect.values(),
                FighterSkillEffect.values(),
                PhaseSkillEffect.values(),
                MiscSkillEffect.values(),
                DefenseSkillEffect.values(),
                CombatSkillEffect.values(),
                CompatSkillEffect.values()
        };
        for (SkillEffect[] group : groups) {
            for (SkillEffect effect : group) {
                SkillEffect existing = registry.put(effect.name(), effect);
                if (existing != null) {
                    throw new IllegalStateException("Duplicate SkillEffect name: " + effect.name());
                }
            }
        }
        return registry;
    }
}
