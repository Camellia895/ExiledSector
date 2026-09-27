package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.StatBonus;

import java.util.List;
import java.util.function.Function;

final class SplitBeamDroneStats {

    private static final List<Function<MutableShipStatsAPI, MutableStat>> MUTABLE_STATS = List.of(
            MutableShipStatsAPI::getBallisticWeaponDamageMult,
            MutableShipStatsAPI::getEnergyWeaponDamageMult,
            MutableShipStatsAPI::getMissileWeaponDamageMult,
            MutableShipStatsAPI::getBeamWeaponDamageMult,
            MutableShipStatsAPI::getDamageToTargetShieldsMult,
            MutableShipStatsAPI::getDamageToTargetHullMult,
            MutableShipStatsAPI::getDamageToFighters,
            MutableShipStatsAPI::getDamageToMissiles,
            MutableShipStatsAPI::getDamageToFrigates,
            MutableShipStatsAPI::getDamageToDestroyers,
            MutableShipStatsAPI::getDamageToCruisers,
            MutableShipStatsAPI::getDamageToCapital,
            MutableShipStatsAPI::getWeaponRangeThreshold,
            MutableShipStatsAPI::getWeaponRangeMultPastThreshold);

    private static final List<Function<MutableShipStatsAPI, StatBonus>> STAT_BONUSES = List.of(
            MutableShipStatsAPI::getBallisticWeaponRangeBonus,
            MutableShipStatsAPI::getEnergyWeaponRangeBonus,
            MutableShipStatsAPI::getMissileWeaponRangeBonus,
            MutableShipStatsAPI::getBeamWeaponRangeBonus,
            MutableShipStatsAPI::getBeamPDWeaponRangeBonus,
            MutableShipStatsAPI::getHitStrengthBonus);

    private SplitBeamDroneStats() {
    }

    static void mirror(MutableShipStatsAPI source, MutableShipStatsAPI drone) {
        for (Function<MutableShipStatsAPI, MutableStat> stat : MUTABLE_STATS) {
            MutableStat target = stat.apply(drone);
            target.getFlatMods().clear();
            target.getPercentMods().clear();
            target.getMultMods().clear();
            target.applyMods(stat.apply(source));
        }
        for (Function<MutableShipStatsAPI, StatBonus> bonus : STAT_BONUSES) {
            StatBonus target = bonus.apply(drone);
            target.getFlatBonuses().clear();
            target.getPercentBonuses().clear();
            target.getMultBonuses().clear();
            target.applyMods(bonus.apply(source));
        }
    }
}
