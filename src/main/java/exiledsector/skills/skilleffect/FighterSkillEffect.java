package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import static exiledsector.skills.skilleffect.SkillEffectText.pctChange;
import static exiledsector.skills.skilleffect.SkillEffectText.flatChange;

public enum FighterSkillEffect implements SkillEffect {

    FIGHTER_WEAPON_DAMAGE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public void applyToFighterSpawnedByShip(ShipAPI fighter, ShipAPI parentShip, String modId, float magnitude) {
            MutableShipStatsAPI fighterStats = fighter.getMutableStats();
            fighterStats.getBallisticWeaponDamageMult().modifyPercent(modId, magnitude);
            fighterStats.getMissileWeaponDamageMult().modifyPercent(modId, magnitude);
            fighterStats.getEnergyWeaponDamageMult().modifyPercent(modId, magnitude);
            fighterStats.getBeamWeaponDamageMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "weapon damage of fighters launched from this ship");
        }
    },
    FIGHTER_TOP_SPEED_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public void applyToFighterSpawnedByShip(ShipAPI fighter, ShipAPI parentShip, String modId, float magnitude) {
            fighter.getMutableStats().getMaxSpeed().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "top speed of fighters launched from this ship");
        }
    },
    FIGHTER_CREW_LOSS_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getStat("fighter_crew_loss_mult").modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "casualties suffered by fighter pilots launched from this ship");
        }
    },
    FIGHTER_REFIT_TIME_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getFighterRefitTimeMult().modifyMult(modId, 1f + magnitude / 100f);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "fighter refit time");
        }
    },
    FIGHTER_REPLACEMENT_RATE_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            float mult = 1f / (1f + magnitude / 100f);
            stats.getDynamic().getStat("replacement_rate_decrease_mult").modifyMult(modId, mult);
            stats.getDynamic().getStat("replacement_rate_increase_mult").modifyMult(modId, mult);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "fighter replacement rate (both decay and recovery)");
        }
    },
    FIGHTER_REPLACEMENT_DECAY_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getStat("replacement_rate_decrease_mult").modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "rate at which fighter replacement capability decays from losses");
        }
    },
    FIGHTER_REPLACEMENT_RECOVERY_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getStat("replacement_rate_increase_mult").modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "rate at which fighter replacement capability recovers");
        }
    },
    FIGHTER_PD_DAMAGE_BONUS_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public void applyToFighterSpawnedByShip(ShipAPI fighter, ShipAPI parentShip, String modId, float magnitude) {
            MutableShipStatsAPI fighterStats = fighter.getMutableStats();
            fighterStats.getDamageToFighters().modifyPercent(modId, magnitude);
            fighterStats.getDamageToMissiles().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "damage dealt by fighters launched from this ship to other fighters and missiles");
        }
    },
    FIGHTER_RELAUNCH_TIME_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod("fighter_rearm_time_extra_fraction_of_base_refit_time_mod").modifyFlat(modId, magnitude / 100f);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "fighter relaunch time, as a % of base refit time");
        }
    },
    FIGHTER_BAYS_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getNumFighterBays().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "number of fighter bays");
        }

        @Override
        public String blockDeallocationReason(FleetMemberAPI member, float magnitude) {
            int fittedWings = member.getVariant().getFittedWings().size();
            float baysWithoutThis = member.getStats().getNumFighterBays().getModifiedValue() - magnitude;
            if (fittedWings > baysWithoutThis) {
                return "Remove a fighter wing first - not enough empty fighter bays without this skill.";
            }
            return null;
        }

        @Override
        public String deallocationWarning(float magnitude) {
            return "Cannot be unallocated without at least 1 empty fighter bay.";
        }
    };

    @Override
    public abstract void apply(MutableShipStatsAPI stats, String modId, float magnitude);

    @Override
    public abstract String describe(float magnitude);
}
