package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import static exiledsector.skills.skilleffect.SkillEffectText.flatChange;
import static exiledsector.skills.skilleffect.SkillEffectText.pct;
import static exiledsector.skills.skilleffect.StatMode.FLAT;
import static exiledsector.skills.skilleffect.StatMode.MULT;
import static exiledsector.skills.skilleffect.StatMode.PERCENT;
import static exiledsector.skills.skilleffect.StatTarget.bonus;
import static exiledsector.skills.skilleffect.StatTarget.dynamicMod;
import static exiledsector.skills.skilleffect.StatTarget.stat;

public enum MiscSkillEffect implements SkillEffect {

    PD_IGNORES_DECOY_FLARES {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(Stats.PD_IGNORES_FLARES).modifyFlat(modId, 1f);
        }

        @Override
        public boolean supportsTemporaryGating() {
            return false;
        }

        @Override
        public String describe(float magnitude) {
            return "Point-defense weapons can identify and ignore decoy flares.";
        }
    },
    PD_BEST_TARGET_LEADING {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(Stats.PD_BEST_TARGET_LEADING).modifyFlat(modId, 1f);
        }

        @Override
        public boolean supportsTemporaryGating() {
            return false;
        }

        @Override
        public String describe(float magnitude) {
            return "Point-defense weapons get the best possible target leading, regardless of combat readiness.";
        }
    },
    PD_DAMAGE_TO_MISSILES_PERCENT(PERCENT, stat(MutableShipStatsAPI::getDamageToMissiles),
            "damage dealt to missiles by point defence weapons", false),
    PD_RECLASSIFY_SMALL_WEAPONS {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                boolean sizeMatches = weapon.getSize() == WeaponSize.SMALL;
                if (sizeMatches && weapon.getType() != WeaponType.MISSILE && !weapon.hasAIHint(AIHints.STRIKE)) {
                    weapon.setPD(true);
                }
            }
        }

        @Override
        public boolean supportsTemporaryGating() {
            return false;
        }

        @Override
        public String describe(float magnitude) {
            return "All small non-missile, non-strike weapons are classified as point-defense, automatically target missiles, and are affected by point-defense stat modifiers.";
        }
    },
    ELECTRONIC_WARFARE_PENALTY_PERCENT(PERCENT, dynamicMod(Stats.ELECTRONIC_WARFARE_PENALTY_MOD),
            "electronic warfare penalty against this ship's weapon range", true),
    ELECTRONIC_WARFARE_PENALTY_MULT(MULT, dynamicMod(Stats.ELECTRONIC_WARFARE_PENALTY_MOD),
            "electronic warfare penalty against this ship's weapon range", true),

    ELECTRONIC_WARFARE(FLAT, dynamicMod("electronic_warfare_flat"), "ECM rating", false),
    NAV_RATING(FLAT, dynamicMod("coord_maneuvers_flat"), "fleet nav rating", false),
    OBJECTIVE_CAPTURE_RATE_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getDynamic().getStat(Stats.SHIP_OBJECTIVE_CAP_RATE_MULT), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            String verb = magnitude >= 0 ? "faster" : "slower";
            return "Captures combat objectives " + pct(Math.abs(magnitude)) + "% " + verb + ".";
        }
    },
    OBJECTIVE_CAPTURE_RANGE_FLAT(FLAT, dynamicMod(Stats.SHIP_OBJECTIVE_CAP_RANGE_MOD),
            "range from which combat objectives can be captured", false),
    PEAK_CR_DURATION_PERCENT(PERCENT, bonus(MutableShipStatsAPI::getPeakCRDuration), "peak combat readiness duration", false),
    PEAK_CR_DURATION_MULT(MULT, bonus(MutableShipStatsAPI::getPeakCRDuration), "peak combat readiness duration", false),
    COMMAND_POINT_RECOVERY_WHILE_FLAGSHIP {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public boolean isConditional() {
            return true;
        }

        @Override
        public boolean supportsTemporaryGating() {
            return false;
        }

        @Override
        public void advanceInCombat(ShipAPI ship, String modId, float magnitude) {
            boolean isFlagship = ship == Global.getCombatEngine().getPlayerShip();
            if (!isFlagship) {
                FleetMemberAPI member = ship.getMutableStats().getFleetMember();
                if (member != null) {
                    PersonAPI commander = member.getFleetCommanderForStats();
                    if (commander == null) commander = member.getFleetCommander();
                    isFlagship = commander != null && commander == ship.getCaptain();
                }
            }

            StatBonus commandPointRate = ship.getMutableStats().getDynamic().getMod("command_point_rate_flat");
            if (isFlagship) {
                commandPointRate.modifyFlat(modId, magnitude);
            } else {
                commandPointRate.unmodify(modId);
            }
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "command point recovery rate while this ship is the flagship");
        }
    };

    private final SimpleStatEffect simpleStat;

    MiscSkillEffect() {
        this.simpleStat = null;
    }

    MiscSkillEffect(StatMode mode, StatTarget target, String statName, boolean lowerIsBetter) {
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
}
