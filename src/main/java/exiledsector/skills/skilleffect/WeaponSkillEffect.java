package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;

import static exiledsector.skills.skilleffect.SkillEffectText.flatChange;
import static exiledsector.skills.skilleffect.SkillEffectText.pct;

public enum WeaponSkillEffect implements SkillEffect {

    WEAPON_DAMAGE_MULT_PER_DMOD {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            float mult = SkillEffectSupport.compoundMultPerDMod(stats, magnitude);
            WeaponStatFamily.DAMAGE.target(WeaponScope.ALL).apply(stats, modId, StatMode.MULT, (mult - 1f) * 100f);
        }

        @Override
        public String describe(float magnitude) {
            String verb = magnitude >= 0 ? "more" : "less";
            return pct(Math.abs(magnitude)) + "% " + verb + " weapon damage per D-mod";
        }
    },
    BALLISTIC_WEAPON_DAMAGE_PER_BURN_LEVEL_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            MutableShipStatsAPI stats = ship.getMutableStats();
            MutableStat burnLevel = stats.getMaxBurnLevel();
            float burnOverDefault = Math.max(0f, burnLevel.getModifiedValue() - burnLevel.getBaseValue());
            WeaponStatFamily.DAMAGE.target(WeaponScope.BALLISTIC).apply(stats, modId, StatMode.PERCENT, burnOverDefault * magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return "Grants " + pct(magnitude) + "% more ballistic weapon damage for every burn level this ship "
                    + "has above its hull's default, from any source.";
        }
    },
    ENERGY_WEAPON_RANGE_PER_SENSOR_STRENGTH_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            MutableShipStatsAPI stats = ship.getMutableStats();
            float rangeBonus = stats.getSensorStrength().getModifiedValue() * magnitude;
            WeaponStatFamily.RANGE.target(WeaponScope.ENERGY).apply(stats, modId, StatMode.FLAT, rangeBonus);
        }

        @Override
        public String describe(float magnitude) {
            return "Increases energy weapon range by " + pct(magnitude) + " for every point "
                    + "of this ship's sensor strength (after modifiers).";
        }
    },
    BALLISTIC_WEAPON_LARGE_OP_COST_FLAT {
        @Override
        public boolean lowerIsBetter() {
            return true;
        }

        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            // "large_ballistic_mod" is the same dynamic stat key vanilla's own Heavy Ballistics
            // Integration hull mod reads to reduce the ordnance point cost of large ballistic weapons
            stats.getDynamic().getMod(LARGE_BALLISTIC_OP_COST_KEY).modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "ordnance point cost of large ballistic weapons");
        }
    };

    private static final String LARGE_BALLISTIC_OP_COST_KEY = "large_ballistic_mod";
}
