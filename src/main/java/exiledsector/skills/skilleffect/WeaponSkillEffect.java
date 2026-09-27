package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;

import static exiledsector.skills.skilleffect.SkillEffectText.pct;
import static exiledsector.skills.skilleffect.SkillEffectText.pctChange;
import static exiledsector.skills.skilleffect.SkillEffectText.pctMore;
import static exiledsector.skills.skilleffect.SkillEffectText.flatChange;

public enum WeaponSkillEffect implements SkillEffect {

    BALLISTIC_DAMAGE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getBallisticWeaponDamageMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "ballistic weapon damage");
        }
    },
    BALLISTIC_PROJECTILE_SPEED_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getBallisticProjectileSpeedMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "ballistic projectile speed");
        }
    },
    MISSILE_DAMAGE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMissileWeaponDamageMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "missile weapon damage");
        }
    },
    NON_BEAM_ENERGY_DAMAGE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getEnergyWeaponDamageMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "non-beam energy weapon damage");
        }
    },
    BEAM_DAMAGE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getBeamWeaponDamageMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "beam weapon damage");
        }
    },
    ENERGY_DAMAGE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getEnergyWeaponDamageMult().modifyPercent(modId, magnitude);
            stats.getBeamWeaponDamageMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "beam and non-beam energy weapon damage");
        }
    },
    ALL_WEAPON_DAMAGE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyAllWeaponDamagePercent(stats, modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "damage of all weapon types");
        }
    },
    ALL_WEAPON_DAMAGE_MULT_PER_DMOD {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            float mult = SkillEffectSupport.compoundMultPerDMod(stats, magnitude);
            stats.getBallisticWeaponDamageMult().modifyMult(modId, mult);
            stats.getMissileWeaponDamageMult().modifyMult(modId, mult);
            stats.getEnergyWeaponDamageMult().modifyMult(modId, mult);
            stats.getBeamWeaponDamageMult().modifyMult(modId, mult);
        }

        @Override
        public String describe(float magnitude) {
            String verb = magnitude >= 0 ? "more" : "less";
            return pct(Math.abs(magnitude)) + "% " + verb + " damage of all weapon types per D-mod";
        }
    },
    BALLISTIC_WEAPON_RANGE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getBallisticWeaponRangeBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "ballistic weapon range");
        }
    },
    BALLISTIC_WEAPON_RANGE_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getBallisticWeaponRangeBonus(), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "ballistic weapon range");
        }
    },
    ENERGY_WEAPON_RANGE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getEnergyWeaponRangeBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "energy weapon range");
        }
    },
    ENERGY_WEAPON_RANGE_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getEnergyWeaponRangeBonus(), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "energy weapon range");
        }
    },
    NON_BEAM_ENERGY_WEAPON_RANGE_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getEnergyWeaponRangeBonus().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "non-beam energy weapon range");
        }
    },
    BEAM_WEAPON_RANGE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getBeamWeaponRangeBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, STAT_BEAM_WEAPON_RANGE);
        }
    },
    BEAM_WEAPON_RANGE_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getBeamWeaponRangeBonus(), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, STAT_BEAM_WEAPON_RANGE);
        }
    },
    BALLISTIC_AMMO_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getBallisticAmmoBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "ballistic weapon ammo capacity");
        }
    },
    ENERGY_AMMO_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getEnergyAmmoBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "energy weapon ammo/charge capacity");
        }
    },
    MISSILE_AMMO_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMissileAmmoBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "missile weapon ammo capacity");
        }
    },
    BALLISTIC_AMMO_REGEN_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getBallisticAmmoRegenMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "ballistic weapon ammo regeneration rate");
        }
    },
    ENERGY_AMMO_REGEN_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getEnergyAmmoRegenMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "energy weapon ammo/charge regeneration rate");
        }
    },
    BALLISTIC_WEAPON_FIRE_RATE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getBallisticRoFMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "ballistic weapon rate of fire");
        }
    },
    ENERGY_WEAPON_FIRE_RATE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getEnergyRoFMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "energy weapon rate of fire");
        }
    },
    MISSILE_WEAPON_FIRE_RATE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMissileRoFMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "missile weapon rate of fire");
        }
    },
    WEAPON_TURN_RATE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getWeaponTurnRateBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "turret turn rate");
        }
    },
    WEAPON_TURN_RATE_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getWeaponTurnRateBonus(), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "turret turn rate");
        }
    },
    WEAPON_DURABILITY_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getWeaponHealthBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "weapon durability");
        }
    },
    WEAPON_RANGE_FALLOFF_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getWeaponRangeMultPastThreshold().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "weapon effectiveness past normal range");
        }
    },
    WEAPON_RANGE_FALLOFF_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getWeaponRangeMultPastThreshold(), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "weapon effectiveness past normal range");
        }
    },
    MISSILE_GUIDANCE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMissileGuidance().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "missile guidance");
        }
    },
    MISSILE_GUIDANCE_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMissileGuidance().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "missile guidance");
        }
    },
    BEAM_WEAPON_RANGE_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getBeamWeaponRangeBonus().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, STAT_BEAM_WEAPON_RANGE);
        }
    },
    BEAM_WEAPON_TURN_RATE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getBeamWeaponTurnRateBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "beam weapon turn rate");
        }
    },
    BEAM_WEAPON_TURN_RATE_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getBeamWeaponTurnRateBonus(), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "beam weapon turn rate");
        }
    },
    WEAPON_RECOIL_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMaxRecoilMult().modifyPercent(modId, magnitude);
            stats.getRecoilPerShotMult().modifyPercent(modId, magnitude);
            stats.getRecoilDecayMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "weapon recoil");
        }
    },
    WEAPON_RECOIL_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            float mult = SkillEffectSupport.multFrom(magnitude);
            stats.getMaxRecoilMult().modifyMult(modId, mult);
            stats.getRecoilPerShotMult().modifyMult(modId, mult);
            stats.getRecoilDecayMult().modifyMult(modId, mult);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "weapon recoil");
        }
    },
    WEAPON_RANGE_THRESHOLD {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getWeaponRangeThreshold().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "range before falloff effectiveness applies");
        }
    },
    MISSILE_SPEED_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMissileMaxSpeedBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "missile top speed");
        }
    },
    MISSILE_RANGE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMissileWeaponRangeBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "missile weapon range");
        }
    },
    MISSILE_RANGE_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getMissileWeaponRangeBonus(), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "missile weapon range");
        }
    },
    MISSILE_ACCELERATION_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMissileAccelerationBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "missile acceleration");
        }
    },
    MISSILE_TURN_RATE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMissileMaxTurnRateBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "missile turn rate");
        }
    },
    MISSILE_TURN_ACCELERATION_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getMissileTurnAccelerationBonus().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "missile turn acceleration");
        }
    },
    AUTOFIRE_AIM_ACCURACY_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getAutofireAimAccuracy().modifyFlat(modId, magnitude / 100f);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "target leading accuracy of autofiring weapons");
        }
    },
    ECCM_CHANCE {
        // magnitude is a fraction (0-1) matching getEccmChance()'s own units; describe() only scales by 100 for display
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getEccmChance().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return "Increases the chance for missiles to resist enemy ECM and flares by " + pct(magnitude * 100f) + "%.";
        }
    },
    BALLISTIC_DAMAGE_PER_BURN_LEVEL_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            MutableShipStatsAPI stats = ship.getMutableStats();
            MutableStat burnLevel = stats.getMaxBurnLevel();
            float burnOverDefault = Math.max(0f, burnLevel.getModifiedValue() - burnLevel.getBaseValue());
            stats.getBallisticWeaponDamageMult().modifyPercent(modId, burnOverDefault * magnitude);
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
            stats.getBeamWeaponRangeBonus().modifyFlat(modId, rangeBonus);
            stats.getEnergyWeaponRangeBonus().modifyFlat(modId, rangeBonus);
        }

        @Override
        public String describe(float magnitude) {
            return "Increases beam and non-beam energy weapon range by " + pct(magnitude) + " for every point "
                    + "of this ship's sensor strength (after modifiers).";
        }
    },
    LARGE_BALLISTIC_OP_COST_FLAT {
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

    private static final String STAT_BEAM_WEAPON_RANGE = "beam weapon range";
    private static final String LARGE_BALLISTIC_OP_COST_KEY = "large_ballistic_mod";
}
