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
import exiledsector.i18n.StyledText;

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
        public StyledText description(float magnitude) {
            return EffectText.of(this);
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
        public StyledText description(float magnitude) {
            return EffectText.of(this);
        }
    },
    PD_DAMAGE_TO_MISSILES_PERCENT(PERCENT, stat(MutableShipStatsAPI::getDamageToMissiles),
            "stat.damageDealtToMissilesByPointDefenceWeapons", false),
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
        public StyledText description(float magnitude) {
            return EffectText.of(this);
        }
    },
    ELECTRONIC_WARFARE_PENALTY_PERCENT(PERCENT, dynamicMod(Stats.ELECTRONIC_WARFARE_PENALTY_MOD),
            "stat.electronicWarfarePenaltyAgainstThisShipSWeaponRange", true),
    ELECTRONIC_WARFARE_PENALTY_MULT(MULT, dynamicMod(Stats.ELECTRONIC_WARFARE_PENALTY_MOD),
            "stat.electronicWarfarePenaltyAgainstThisShipSWeaponRange", true),

    ELECTRONIC_WARFARE(FLAT, dynamicMod("electronic_warfare_flat"), "stat.ecmRating", false),
    NAV_RATING(FLAT, dynamicMod("coord_maneuvers_flat"), "stat.fleetNavRating", false),
    OBJECTIVE_CAPTURE_RATE_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getDynamic().getStat(Stats.SHIP_OBJECTIVE_CAP_RATE_MULT), modId, magnitude);
        }

        @Override
        public StyledText description(float magnitude) {
            return EffectText.signed(this, magnitude).styled();
        }
    },
    OBJECTIVE_CAPTURE_RANGE_FLAT(FLAT, dynamicMod(Stats.SHIP_OBJECTIVE_CAP_RANGE_MOD),
            "stat.rangeFromWhichCombatObjectivesCanBeCaptured", false),
    PEAK_CR_DURATION_PERCENT(PERCENT, bonus(MutableShipStatsAPI::getPeakCRDuration), "stat.peakCombatReadinessDuration", false),
    PEAK_CR_DURATION_MULT(MULT, bonus(MutableShipStatsAPI::getPeakCRDuration), "stat.peakCombatReadinessDuration", false),
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
        public StyledText description(float magnitude) {
            return EffectText.flatChange(magnitude, "stat.commandPointRecoveryWhileFlagship");
        }
    };

    private final SimpleStatEffect simpleStat;

    MiscSkillEffect() {
        this.simpleStat = null;
    }

    MiscSkillEffect(StatMode mode, StatTarget target, String statKey, boolean lowerIsBetter) {
        this.simpleStat = new SimpleStatEffect(mode, target, statKey, lowerIsBetter);
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        simpleStat.apply(stats, modId, magnitude);
    }

    @Override
    public StyledText description(float magnitude) {
        return simpleStat.description(magnitude);
    }

    @Override
    public boolean lowerIsBetter() {
        return simpleStat != null && simpleStat.lowerIsBetter();
    }
}
