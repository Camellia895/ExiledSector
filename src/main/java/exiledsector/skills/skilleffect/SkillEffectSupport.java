package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import java.util.ArrayList;
import java.util.List;

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

    // the per-D-mod multiplier compounds (e.g. -2% per D-mod means 0.98^dmodCount, not a flat
    // -2%-times-count reduction), matching the "(multiplicative)" design of these two effects
    static float compoundMultPerDMod(MutableShipStatsAPI stats, float magnitudePerDMod) {
        return (float) Math.pow(multFrom(magnitudePerDMod), countDMods(stats));
    }

    private static int countDMods(MutableShipStatsAPI stats) {
        return dModSpecs(stats.getVariant()).size();
    }

    static void applyDModEffectMult(MutableShipStatsAPI stats, String modId, float magnitude) {
        applyMult(stats.getDynamic().getStat(Stats.DMOD_EFFECT_MULT), modId, magnitude);
        ShipVariantAPI variant = stats.getVariant();
        for (HullModSpecAPI spec : dModSpecs(variant)) {
            if (spec.getEffect() != null) {
                spec.getEffect().applyEffectsBeforeShipCreation(variant.getHullSize(), stats, spec.getId());
            }
        }
    }

    private static List<HullModSpecAPI> dModSpecs(ShipVariantAPI variant) {
        List<HullModSpecAPI> specs = new ArrayList<>();
        if (variant == null) {
            return specs;
        }
        for (String hullModId : variant.getHullMods()) {
            HullModSpecAPI spec = Global.getSettings().getHullModSpec(hullModId);
            if (spec != null && spec.hasTag(Tags.HULLMOD_DMOD)) {
                specs.add(spec);
            }
        }
        return specs;
    }

    static void applyAllWeaponDamagePercent(MutableShipStatsAPI stats, String modId, float magnitude) {
        stats.getBallisticWeaponDamageMult().modifyPercent(modId, magnitude);
        stats.getMissileWeaponDamageMult().modifyPercent(modId, magnitude);
        stats.getEnergyWeaponDamageMult().modifyPercent(modId, magnitude);
        stats.getBeamWeaponDamageMult().modifyPercent(modId, magnitude);
    }
}
