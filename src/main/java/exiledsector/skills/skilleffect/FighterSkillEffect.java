package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.WingRole;
import exiledsector.i18n.StyledText;
import exiledsector.i18n.Translation;

import static exiledsector.skills.skilleffect.StatMode.MULT;
import static exiledsector.skills.skilleffect.StatMode.PERCENT;
import static exiledsector.skills.skilleffect.StatTarget.dynamicStat;
import static exiledsector.skills.skilleffect.StatTarget.stat;

public enum FighterSkillEffect implements SkillEffect {

    FIGHTER_WEAPON_DAMAGE_PERCENT {
        @Override
        public void applyToFighterSpawnedByShip(ShipAPI fighter, ShipAPI parentShip, String modId, float magnitude) {
            applyRoleDamage(fighter, modId, magnitude);
        }

        @Override
        public StyledText description(float magnitude) {
            return StatMode.PERCENT.describeStat(magnitude, "stat.weaponDamageOfFightersLaunchedFromThisShip");
        }
    },
    FIGHTER_TOP_SPEED_PERCENT {
        @Override
        public void applyToFighterSpawnedByShip(ShipAPI fighter, ShipAPI parentShip, String modId, float magnitude) {
            applyRoleTopSpeed(fighter, modId, magnitude);
        }

        @Override
        public StyledText description(float magnitude) {
            return StatMode.PERCENT.describeStat(magnitude, "stat.topSpeedOfFightersLaunchedFromThisShip");
        }
    },
    FIGHTER_CREW_LOSS_PERCENT(PERCENT, dynamicStat("fighter_crew_loss_mult"),
            "stat.casualtiesSufferedByFighterPilotsLaunchedFromThisShip", true),
    FIGHTER_CREW_LOSS_MULT(MULT, dynamicStat("fighter_crew_loss_mult"),
            "stat.casualtiesSufferedByFighterPilotsLaunchedFromThisShip", true),
    FIGHTER_REFIT_TIME_MULT(MULT, stat(MutableShipStatsAPI::getFighterRefitTimeMult), "stat.fighterRefitTime", true),
    FIGHTER_REFIT_TIME_PERCENT(PERCENT, stat(MutableShipStatsAPI::getFighterRefitTimeMult), "stat.fighterRefitTime", true),
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
        public StyledText description(float magnitude) {
            return EffectText.msg(this, magnitude >= 0 ? "slower" : "faster").arg("value", Math.abs(magnitude)).styled();
        }
    },
    FIGHTER_REPLACEMENT_DECAY_PERCENT(PERCENT, dynamicStat("replacement_rate_decrease_mult"),
            "stat.rateAtWhichFighterReplacementCapabilityDecaysFromLosses", true),
    FIGHTER_REPLACEMENT_RECOVERY_PERCENT(PERCENT, dynamicStat("replacement_rate_increase_mult"),
            "stat.rateAtWhichFighterReplacementCapabilityRecovers", false),
    FIGHTER_PD_DAMAGE_BONUS_PERCENT {
        @Override
        public void applyToFighterSpawnedByShip(ShipAPI fighter, ShipAPI parentShip, String modId, float magnitude) {
            MutableShipStatsAPI fighterStats = fighter.getMutableStats();
            fighterStats.getDamageToFighters().modifyPercent(modId, magnitude);
            fighterStats.getDamageToMissiles().modifyPercent(modId, magnitude);
        }

        @Override
        public StyledText description(float magnitude) {
            return StatMode.PERCENT.describeStat(magnitude, "stat.damageDealtByFightersLaunchedFromThisShipToOtherFightersAndMissiles");
        }
    },
    FIGHTER_RELAUNCH_TIME_FLAT {
        @Override
        public boolean lowerIsBetter() {
            return true;
        }

        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod("fighter_rearm_time_extra_fraction_of_base_refit_time_mod").modifyFlat(modId, magnitude / 100f);
        }

        @Override
        public boolean supportsTemporaryGating() {
            return true;
        }

        @Override
        public StyledText description(float magnitude) {
            return StatMode.FLAT.describeStat(magnitude, "stat.fighterRelaunchTimeAsAOfBaseRefitTime");
        }
    },
    FIGHTER_ARMOR_PERCENT {
        @Override
        public void applyToFighterSpawnedByShip(ShipAPI fighter, ShipAPI parentShip, String modId, float magnitude) {
            applyRoleArmor(fighter, modId, magnitude);
        }

        @Override
        public StyledText description(float magnitude) {
            return StatMode.PERCENT.describeStat(magnitude, "stat.armorOfFightersLaunchedFromThisShip");
        }
    },
    FIGHTER_SHIELD_DAMAGE_TAKEN_PERCENT {
        @Override
        public boolean lowerIsBetter() {
            return true;
        }

        @Override
        public void applyToFighterSpawnedByShip(ShipAPI fighter, ShipAPI parentShip, String modId, float magnitude) {
            applyRoleShieldDamageTaken(fighter, modId, magnitude);
        }

        @Override
        public StyledText description(float magnitude) {
            return StatMode.PERCENT.describeStat(magnitude, "stat.damageTakenByShieldsOfFightersLaunchedFromThisShip");
        }
    },
    FIGHTER_RATE_OF_FIRE_PERCENT {
        @Override
        public void applyToFighterSpawnedByShip(ShipAPI fighter, ShipAPI parentShip, String modId, float magnitude) {
            applyRoleRateOfFire(fighter, modId, magnitude);
        }

        @Override
        public StyledText description(float magnitude) {
            return StatMode.PERCENT.describeStat(magnitude, "stat.weaponRateOfFireOfFightersLaunchedFromThisShip");
        }
    },
    FIGHTER_ENGAGEMENT_RANGE_PERCENT {
        @Override
        public void applyToFighterSpawnedByShip(ShipAPI fighter, ShipAPI parentShip, String modId, float magnitude) {
            applyRoleEngagementRange(fighter, modId, magnitude);
        }

        @Override
        public StyledText description(float magnitude) {
            return StatMode.PERCENT.describeStat(magnitude, "stat.engagementRangeOfFightersLaunchedFromThisShip");
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
        public StyledText description(float magnitude) {
            return StatMode.FLAT.describeStat(magnitude, "stat.weaponRangeOfFightersLaunchedFromThisShip");
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
        public StyledText description(float magnitude) {
            return EffectText.of(this);
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
        public StyledText description(float magnitude) {
            return StatMode.FLAT.describeStat(magnitude, "stat.numberOfFighterBays");
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
        public StyledText deallocationWarning(float magnitude) {
            return EffectText.msg(this, "warning").styled();
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
        WeaponStatFamily.DAMAGE.target(WeaponScope.ALL).apply(fighter.getMutableStats(), modId, StatMode.PERCENT, magnitude);
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

    private static String roleKey(WingRole role) {
        return switch (role) {
            case INTERCEPTOR, BOMBER, SUPPORT -> "fighter.role." + role.name();
            default -> "fighter.role." + WingRole.FIGHTER.name();
        };
    }

    private final WingRole role;
    private final RoleStat roleStat;
    private final SimpleStatEffect simpleStat;

    FighterSkillEffect() {
        this(null, null, null);
    }

    FighterSkillEffect(WingRole role, RoleStat roleStat) {
        this(role, roleStat, null);
    }

    FighterSkillEffect(StatMode mode, StatTarget target, String statKey, boolean lowerIsBetter) {
        this(null, null, new SimpleStatEffect(mode, target, statKey, lowerIsBetter));
    }

    FighterSkillEffect(WingRole role, RoleStat roleStat, SimpleStatEffect simpleStat) {
        this.role = role;
        this.roleStat = roleStat;
        this.simpleStat = simpleStat;
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        if (simpleStat != null) {
            simpleStat.apply(stats, modId, magnitude);
        }
    }

    @Override
    public boolean supportsTemporaryGating() {
        return simpleStat != null && simpleStat.supportsTemporaryGating();
    }

    @Override
    public boolean lowerIsBetter() {
        if (simpleStat != null) {
            return simpleStat.lowerIsBetter();
        }
        return roleStat != null && roleStat.lowerIsBetter;
    }

    @Override
    public void applyToFighterSpawnedByShip(ShipAPI fighter, ShipAPI parentShip, String modId, float magnitude) {
        if (roleStat != null && matchesRole(fighter, role)) {
            roleStat.applier.apply(fighter, modId, magnitude);
        }
    }

    @Override
    public StyledText description(float magnitude) {
        if (simpleStat != null) {
            return simpleStat.description(magnitude);
        }
        String stat = Translation.msg("fighter.launched").arg("stat", Translation.text(roleStat.key())).arg("role", Translation.text(roleKey(role))).text();
        return StatMode.PERCENT.description(magnitude, stat);
    }

    @FunctionalInterface
    private interface RoleStatApplier {
        void apply(ShipAPI fighter, String modId, float magnitude);
    }

    private enum RoleStat {
        WEAPON_DAMAGE(FighterSkillEffect::applyRoleDamage),
        TOP_SPEED(FighterSkillEffect::applyRoleTopSpeed),
        ARMOR(FighterSkillEffect::applyRoleArmor),
        SHIELD_DAMAGE_TAKEN(FighterSkillEffect::applyRoleShieldDamageTaken, true),
        RATE_OF_FIRE(FighterSkillEffect::applyRoleRateOfFire),
        ENGAGEMENT_RANGE(FighterSkillEffect::applyRoleEngagementRange);

        private final RoleStatApplier applier;
        private final boolean lowerIsBetter;

        RoleStat(RoleStatApplier applier) {
            this(applier, false);
        }

        RoleStat(RoleStatApplier applier, boolean lowerIsBetter) {
            this.applier = applier;
            this.lowerIsBetter = lowerIsBetter;
        }

        String key() {
            return "fighter.stat." + name();
        }
    }
}
