package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import static exiledsector.skills.skilleffect.SkillEffectText.pct;
import static exiledsector.skills.skilleffect.SkillEffectText.pctMore;
import static exiledsector.skills.skilleffect.StatMode.FLAT;
import static exiledsector.skills.skilleffect.StatMode.MULT;
import static exiledsector.skills.skilleffect.StatMode.PERCENT;
import static exiledsector.skills.skilleffect.StatTarget.all;
import static exiledsector.skills.skilleffect.StatTarget.bonus;
import static exiledsector.skills.skilleffect.StatTarget.stat;

public enum DefenseSkillEffect implements SkillEffect {

    HULL_PERCENT(PERCENT, bonus(MutableShipStatsAPI::getHullBonus), StatNames.HULL_POINTS, false),
    HULL_FLAT(FLAT, bonus(MutableShipStatsAPI::getHullBonus), StatNames.HULL_POINTS, false),
    HULL_MULT(MULT, bonus(MutableShipStatsAPI::getHullBonus), StatNames.HULL_POINTS, false),
    ARMOR_FLAT(FLAT, bonus(MutableShipStatsAPI::getArmorBonus), "armor", false),
    SHIP_RECOVERY_CHANCE_BONUS {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(modId, magnitude);
        }

        @Override
        public boolean appliesToNpcShips() {
            return false;
        }

        @Override
        public String describe(float magnitude) {
            return "If disabled, this ship is almost always recoverable after the battle.";
        }
    },
    BREAK_PROBABILITY_PERCENT(PERCENT, stat(MutableShipStatsAPI::getBreakProb), "chance of this ship breaking apart when destroyed", true),
    ARMOR_PERCENT(PERCENT, bonus(MutableShipStatsAPI::getArmorBonus), "armor", false),
    ARMOR_DAMAGE_TAKEN_MULT_PER_DMOD {
        @Override
        public boolean lowerIsBetter() {
            return true;
        }

        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getArmorDamageTakenMult().modifyMult(modId, SkillEffectSupport.compoundMultPerDMod(stats, magnitude));
        }

        @Override
        public String describe(float magnitude) {
            String verb = magnitude >= 0 ? "more" : "less";
            return pct(Math.abs(magnitude)) + "% " + verb + " armor damage taken per D-mod ";
        }
    },
    DMOD_EFFECT_MULT {
        @Override
        public boolean lowerIsBetter() {
            return true;
        }

        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyDModEffectMult(stats, modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "negative effects from D-mods");
        }
    },
    SHIELD_ABSORPTION_PERCENT(PERCENT, stat(MutableShipStatsAPI::getShieldAbsorptionMult), "damage taken by shields", true),
    SHIELD_DAMAGE_TAKEN_MULT(MULT, stat(MutableShipStatsAPI::getShieldDamageTakenMult), "damage taken by shields", true),
    ENGINE_DURABILITY_PERCENT(PERCENT, bonus(MutableShipStatsAPI::getEngineHealthBonus), "engine durability", false),
    REPAIR_TIME_PERCENT(PERCENT, all(stat(MutableShipStatsAPI::getCombatWeaponRepairTimeMult),
            stat(MutableShipStatsAPI::getCombatEngineRepairTimeMult)),
            "weapon and engine repair time", true),
    REPAIR_TIME_MULT(MULT, all(stat(MutableShipStatsAPI::getCombatWeaponRepairTimeMult),
            stat(MutableShipStatsAPI::getCombatEngineRepairTimeMult)),
            "weapon and engine repair time", true),
    EMP_DAMAGE_TAKEN_PERCENT(PERCENT, stat(MutableShipStatsAPI::getEmpDamageTakenMult), "EMP damage taken", true),
    EMP_DAMAGE_TAKEN_MULT(MULT, stat(MutableShipStatsAPI::getEmpDamageTakenMult), "EMP damage taken", true),
    ENERGY_DAMAGE_TAKEN_PERCENT(PERCENT, all(stat(MutableShipStatsAPI::getEnergyDamageTakenMult),
            stat(MutableShipStatsAPI::getEnergyShieldDamageTakenMult)),
            "energy damage taken, including hits on shields, armor, and hull", true);

    private final SimpleStatEffect simpleStat;

    DefenseSkillEffect() {
        this.simpleStat = null;
    }

    DefenseSkillEffect(StatMode mode, StatTarget target, String statName, boolean lowerIsBetter) {
        this.simpleStat = new SimpleStatEffect(mode, target, statName, lowerIsBetter);
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        simpleStat.apply(stats, modId, magnitude);
    }

    @Override
    public String describe(float magnitude) {
        return simpleStat.describe(magnitude);
    }

    @Override
    public boolean lowerIsBetter() {
        return simpleStat != null && simpleStat.lowerIsBetter();
    }

    private static final class StatNames {
        static final String HULL_POINTS = "hull points";

        private StatNames() {
        }
    }
}
