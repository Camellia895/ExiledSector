package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.StatBonus;

final class SkillEffectSupport {

    private SkillEffectSupport() {
    }

    static void applyMult(MutableStat stat, String modId, float magnitude) {
        stat.modifyMult(modId, multFrom(magnitude));
    }

    static void applyMult(StatBonus stat, String modId, float magnitude) {
        stat.modifyMult(modId, multFrom(magnitude));
    }

    static float multFrom(float magnitude) {
        return 1f + magnitude / 100f;
    }

    static void applyAllWeaponDamagePercent(MutableShipStatsAPI stats, String modId, float magnitude) {
        stats.getBallisticWeaponDamageMult().modifyPercent(modId, magnitude);
        stats.getMissileWeaponDamageMult().modifyPercent(modId, magnitude);
        stats.getEnergyWeaponDamageMult().modifyPercent(modId, magnitude);
        stats.getBeamWeaponDamageMult().modifyPercent(modId, magnitude);
    }
}
