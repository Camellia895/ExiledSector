package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.WingRole;

import static exiledsector.skills.skilleffect.SkillEffectText.pct;
import static exiledsector.skills.skilleffect.SkillEffectText.pctChange;
import static exiledsector.skills.skilleffect.SkillEffectText.pctMore;
import static exiledsector.skills.skilleffect.SkillEffectText.flatChange;

public enum FighterSkillEffect implements SkillEffect {

    FIGHTER_WEAPON_DAMAGE_PERCENT {
        @Override
        public void applyToFighterSpawnedByShip(ShipAPI fighter, ShipAPI parentShip, String modId, float magnitude) {
            applyRoleDamage(fighter, modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "weapon damage of fighters launched from this ship");
        }
    },
    FIGHTER_TOP_SPEED_PERCENT {
        @Override
        public void applyToFighterSpawnedByShip(ShipAPI fighter, ShipAPI parentShip, String modId, float magnitude) {
            applyRoleTopSpeed(fighter, modId, magnitude);
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
        public boolean supportsTemporaryGating() {
            return true;
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "casualties suffered by fighter pilots launched from this ship");
        }
    },
    FIGHTER_CREW_LOSS_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getDynamic().getStat("fighter_crew_loss_mult"), modId, magnitude);
        }

        @Override
        public boolean supportsTemporaryGating() {
            return true;
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "casualties suffered by fighter pilots launched from this ship");
        }
    },
    FIGHTER_REFIT_TIME_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getFighterRefitTimeMult(), modId, magnitude);
        }

        @Override
        public boolean supportsTemporaryGating() {
            return true;
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "fighter refit time");
        }
    },
    FIGHTER_REFIT_TIME_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getFighterRefitTimeMult().modifyPercent(modId, magnitude);
        }

        @Override
        public boolean supportsTemporaryGating() {
            return true;
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "fighter refit time");
        }
    },
    FIGHTER_REPLACEMENT_RATE_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            float mult = 1f / SkillEffectSupport.multFrom(magnitude);
            stats.getDynamic().getStat("replacement_rate_decrease_mult").modifyMult(modId, mult);
            stats.getDynamic().getStat("replacement_rate_increase_mult").modifyMult(modId, mult);
        }

        @Override
        public boolean supportsTemporaryGating() {
            return true;
        }

        @Override
        public String describe(float magnitude) {
            String verb = magnitude >= 0 ? "slower" : "faster";
            return pct(Math.abs(magnitude)) + "% " + verb + " fighter replacement rate decay and recovery.";
        }
    },
    FIGHTER_REPLACEMENT_DECAY_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getStat("replacement_rate_decrease_mult").modifyPercent(modId, magnitude);
        }

        @Override
        public boolean supportsTemporaryGating() {
            return true;
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
        public boolean supportsTemporaryGating() {
            return true;
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "rate at which fighter replacement capability recovers");
        }
    },
    FIGHTER_PD_DAMAGE_BONUS_PERCENT {
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
        public boolean supportsTemporaryGating() {
            return true;
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "fighter relaunch time, as a % of base refit time");
        }
    },
    FIGHTER_ARMOR_PERCENT {
        @Override
        public void applyToFighterSpawnedByShip(ShipAPI fighter, ShipAPI parentShip, String modId, float magnitude) {
            applyRoleArmor(fighter, modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "armor of fighters launched from this ship");
        }
    },
    FIGHTER_SHIELD_DAMAGE_TAKEN_PERCENT {
        @Override
        public void applyToFighterSpawnedByShip(ShipAPI fighter, ShipAPI parentShip, String modId, float magnitude) {
            applyRoleShieldDamageTaken(fighter, modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "damage taken by shields of fighters launched from this ship");
        }
    },
    FIGHTER_RATE_OF_FIRE_PERCENT {
        @Override
        public void applyToFighterSpawnedByShip(ShipAPI fighter, ShipAPI parentShip, String modId, float magnitude) {
            applyRoleRateOfFire(fighter, modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "weapon rate of fire of fighters launched from this ship");
        }
    },
    FIGHTER_ENGAGEMENT_RANGE_PERCENT {
        @Override
        public void applyToFighterSpawnedByShip(ShipAPI fighter, ShipAPI parentShip, String modId, float magnitude) {
            applyRoleEngagementRange(fighter, modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "engagement range of fighters launched from this ship");
        }
    },
    FIGHTER_WEAPON_RANGE_FLAT {
        @Override
        public void applyToFighterSpawnedByShip(ShipAPI fighter, ShipAPI parentShip, String modId, float magnitude) {
            MutableShipStatsAPI fighterStats = fighter.getMutableStats();
            fighterStats.getBallisticWeaponRangeBonus().modifyFlat(modId, magnitude);
            fighterStats.getEnergyWeaponRangeBonus().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "weapon range of fighters launched from this ship");
        }
    },
    FIGHTER_ROLE_DAMAGE_PERCENT(WingRole.FIGHTER, RoleStat.WEAPON_DAMAGE),
    FIGHTER_ROLE_TOP_SPEED_PERCENT(WingRole.FIGHTER, RoleStat.TOP_SPEED),
    FIGHTER_ROLE_ARMOR_PERCENT(WingRole.FIGHTER, RoleStat.ARMOR),
    FIGHTER_ROLE_SHIELD_DAMAGE_TAKEN_PERCENT(WingRole.FIGHTER, RoleStat.SHIELD_DAMAGE_TAKEN),
    FIGHTER_ROLE_RATE_OF_FIRE_PERCENT(WingRole.FIGHTER, RoleStat.RATE_OF_FIRE),
    FIGHTER_ROLE_ENGAGEMENT_RANGE_PERCENT(WingRole.FIGHTER, RoleStat.ENGAGEMENT_RANGE),

    INTERCEPTOR_ROLE_DAMAGE_PERCENT(WingRole.INTERCEPTOR, RoleStat.WEAPON_DAMAGE),
    INTERCEPTOR_ROLE_TOP_SPEED_PERCENT(WingRole.INTERCEPTOR, RoleStat.TOP_SPEED),
    INTERCEPTOR_ROLE_ARMOR_PERCENT(WingRole.INTERCEPTOR, RoleStat.ARMOR),
    INTERCEPTOR_ROLE_SHIELD_DAMAGE_TAKEN_PERCENT(WingRole.INTERCEPTOR, RoleStat.SHIELD_DAMAGE_TAKEN),
    INTERCEPTOR_ROLE_RATE_OF_FIRE_PERCENT(WingRole.INTERCEPTOR, RoleStat.RATE_OF_FIRE),
    INTERCEPTOR_ROLE_ENGAGEMENT_RANGE_PERCENT(WingRole.INTERCEPTOR, RoleStat.ENGAGEMENT_RANGE),

    BOMBER_ROLE_DAMAGE_PERCENT(WingRole.BOMBER, RoleStat.WEAPON_DAMAGE),
    BOMBER_ROLE_TOP_SPEED_PERCENT(WingRole.BOMBER, RoleStat.TOP_SPEED),
    BOMBER_ROLE_ARMOR_PERCENT(WingRole.BOMBER, RoleStat.ARMOR),
    BOMBER_ROLE_SHIELD_DAMAGE_TAKEN_PERCENT(WingRole.BOMBER, RoleStat.SHIELD_DAMAGE_TAKEN),
    BOMBER_ROLE_RATE_OF_FIRE_PERCENT(WingRole.BOMBER, RoleStat.RATE_OF_FIRE),
    BOMBER_ROLE_ENGAGEMENT_RANGE_PERCENT(WingRole.BOMBER, RoleStat.ENGAGEMENT_RANGE),

    SUPPORT_ROLE_DAMAGE_PERCENT(WingRole.SUPPORT, RoleStat.WEAPON_DAMAGE),
    SUPPORT_ROLE_TOP_SPEED_PERCENT(WingRole.SUPPORT, RoleStat.TOP_SPEED),
    SUPPORT_ROLE_ARMOR_PERCENT(WingRole.SUPPORT, RoleStat.ARMOR),
    SUPPORT_ROLE_SHIELD_DAMAGE_TAKEN_PERCENT(WingRole.SUPPORT, RoleStat.SHIELD_DAMAGE_TAKEN),
    SUPPORT_ROLE_RATE_OF_FIRE_PERCENT(WingRole.SUPPORT, RoleStat.RATE_OF_FIRE),
    SUPPORT_ROLE_ENGAGEMENT_RANGE_PERCENT(WingRole.SUPPORT, RoleStat.ENGAGEMENT_RANGE),

    REMOVE_ALL_FIGHTER_BAYS {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getNumFighterBays().modifyMult(modId, 0f);
        }

        @Override
        public String describe(float magnitude) {
            return "Removes all of this ship's fighter bays.";
        }
    },
    FIGHTER_BAYS_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getNumFighterBays().modifyFlat(modId, magnitude);
        }

        @Override
        public boolean supportsTemporaryGating() {
            return true;
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

    private static WingRole effectiveRole(ShipAPI fighter) {
        FighterWingAPI wing = fighter.getWing();
        WingRole role = wing == null ? null : wing.getRole();
        if (role == WingRole.FIGHTER || role == WingRole.INTERCEPTOR || role == WingRole.BOMBER || role == WingRole.SUPPORT) {
            return role;
        }
        return WingRole.FIGHTER;
    }

    private static boolean matchesRole(ShipAPI fighter, WingRole role) {
        return effectiveRole(fighter) == role;
    }

    private static void applyRoleDamage(ShipAPI fighter, String modId, float magnitude) {
        SkillEffectSupport.applyAllWeaponDamagePercent(fighter.getMutableStats(), modId, magnitude);
    }

    private static void applyRoleTopSpeed(ShipAPI fighter, String modId, float magnitude) {
        fighter.getMutableStats().getMaxSpeed().modifyPercent(modId, magnitude);
    }

    private static void applyRoleArmor(ShipAPI fighter, String modId, float magnitude) {
        fighter.getMutableStats().getArmorBonus().modifyPercent(modId, magnitude);
    }

    private static void applyRoleShieldDamageTaken(ShipAPI fighter, String modId, float magnitude) {
        fighter.getMutableStats().getShieldDamageTakenMult().modifyPercent(modId, magnitude);
    }

    private static void applyRoleRateOfFire(ShipAPI fighter, String modId, float magnitude) {
        MutableShipStatsAPI fighterStats = fighter.getMutableStats();
        fighterStats.getBallisticRoFMult().modifyPercent(modId, magnitude);
        fighterStats.getEnergyRoFMult().modifyPercent(modId, magnitude);
        fighterStats.getMissileRoFMult().modifyPercent(modId, magnitude);
    }

    private static void applyRoleEngagementRange(ShipAPI fighter, String modId, float magnitude) {
        fighter.getMutableStats().getFighterWingRange().modifyPercent(modId, magnitude);
    }

    private static String roleDisplayName(WingRole role) {
        return switch (role) {
            case INTERCEPTOR -> "Interceptors";
            case BOMBER -> "Bombers";
            case SUPPORT -> "Support fighters";
            default -> "Fighters";
        };
    }

    private final WingRole role;
    private final RoleStat roleStat;

    FighterSkillEffect() {
        this(null, null);
    }

    FighterSkillEffect(WingRole role, RoleStat roleStat) {
        this.role = role;
        this.roleStat = roleStat;
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
    }

    @Override
    public boolean supportsTemporaryGating() {
        return false;
    }

    @Override
    public void applyToFighterSpawnedByShip(ShipAPI fighter, ShipAPI parentShip, String modId, float magnitude) {
        if (roleStat != null && matchesRole(fighter, role)) {
            roleStat.applier.apply(fighter, modId, magnitude);
        }
    }

    @Override
    public String describe(float magnitude) {
        return pctChange(magnitude, roleStat.description + " of " + roleDisplayName(role) + " launched from this ship");
    }

    @FunctionalInterface
    private interface RoleStatApplier {
        void apply(ShipAPI fighter, String modId, float magnitude);
    }

    private enum RoleStat {
        WEAPON_DAMAGE("weapon damage", FighterSkillEffect::applyRoleDamage),
        TOP_SPEED("top speed", FighterSkillEffect::applyRoleTopSpeed),
        ARMOR("armor", FighterSkillEffect::applyRoleArmor),
        SHIELD_DAMAGE_TAKEN("damage taken by shields", FighterSkillEffect::applyRoleShieldDamageTaken),
        RATE_OF_FIRE("weapon rate of fire", FighterSkillEffect::applyRoleRateOfFire),
        ENGAGEMENT_RANGE("engagement range", FighterSkillEffect::applyRoleEngagementRange);

        private final String description;
        private final RoleStatApplier applier;

        RoleStat(String description, RoleStatApplier applier) {
            this.description = description;
            this.applier = applier;
        }
    }
}
