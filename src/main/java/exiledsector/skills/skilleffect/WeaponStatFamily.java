package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.StatBonus;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static exiledsector.skills.skilleffect.StatMode.FLAT;
import static exiledsector.skills.skilleffect.StatMode.MULT;
import static exiledsector.skills.skilleffect.StatMode.PERCENT;
import static exiledsector.skills.skilleffect.WeaponScope.ALL;
import static exiledsector.skills.skilleffect.WeaponScope.BEAM;
import static exiledsector.skills.skilleffect.WeaponScope.MISSILE;
import static exiledsector.skills.skilleffect.WeaponScope.NON_BEAM_ENERGY;

public enum WeaponStatFamily {

    DAMAGE("weapon damage", true, EnumSet.allOf(StatMode.class),
            Targets.typed(stat(MutableShipStatsAPI::getBallisticWeaponDamageMult),
                            stat(MutableShipStatsAPI::getMissileWeaponDamageMult),
                            stat(MutableShipStatsAPI::getEnergyWeaponDamageMult))
                    .with(BEAM, stat(MutableShipStatsAPI::getBeamWeaponDamageMult))
                    .with(NON_BEAM_ENERGY, new StatTarget.Compensated(stat(MutableShipStatsAPI::getEnergyWeaponDamageMult),
                            stat(MutableShipStatsAPI::getBeamWeaponDamageMult), EnumSet.allOf(StatMode.class)))),
    RANGE("weapon range", true, EnumSet.allOf(StatMode.class),
            Targets.typed(bonus(MutableShipStatsAPI::getBallisticWeaponRangeBonus),
                            bonus(MutableShipStatsAPI::getMissileWeaponRangeBonus),
                            bonus(MutableShipStatsAPI::getEnergyWeaponRangeBonus))
                    .with(BEAM, bonus(MutableShipStatsAPI::getBeamWeaponRangeBonus))
                    .with(NON_BEAM_ENERGY, new StatTarget.Compensated(bonus(MutableShipStatsAPI::getEnergyWeaponRangeBonus),
                            bonus(MutableShipStatsAPI::getBeamWeaponRangeBonus), EnumSet.allOf(StatMode.class)))),
    FLUX_COST("weapon flux cost", true, EnumSet.of(PERCENT, MULT),
            Targets.typed(bonus(MutableShipStatsAPI::getBallisticWeaponFluxCostMod),
                            bonus(MutableShipStatsAPI::getMissileWeaponFluxCostMod),
                            bonus(MutableShipStatsAPI::getEnergyWeaponFluxCostMod))
                    .with(BEAM, stat(MutableShipStatsAPI::getBeamWeaponFluxCostMult))
                    .with(NON_BEAM_ENERGY, new StatTarget.Compensated(bonus(MutableShipStatsAPI::getEnergyWeaponFluxCostMod),
                            stat(MutableShipStatsAPI::getBeamWeaponFluxCostMult), EnumSet.of(MULT)))),
    FIRE_RATE("weapon rate of fire", true, EnumSet.of(PERCENT, MULT),
            Targets.typed(stat(MutableShipStatsAPI::getBallisticRoFMult),
                    stat(MutableShipStatsAPI::getMissileRoFMult),
                    stat(MutableShipStatsAPI::getEnergyRoFMult))) {
        @Override
        public String describe(WeaponScope scope, StatMode mode, float magnitude) {
            String text = super.describe(scope, mode, magnitude);
            boolean reachesBeams = scope == ALL || scope == WeaponScope.ENERGY;
            return reachesBeams ? text + BURST_BEAM_FIRE_RATE_NOTE : text;
        }
    },
    AMMO("weapon ammo capacity", true, EnumSet.allOf(StatMode.class),
            Targets.typed(bonus(MutableShipStatsAPI::getBallisticAmmoBonus),
                            bonus(MutableShipStatsAPI::getMissileAmmoBonus),
                            bonus(MutableShipStatsAPI::getEnergyAmmoBonus))
                    .with(BEAM, new StatTarget.PerWeaponAmmo(BEAM, false))
                    .with(NON_BEAM_ENERGY, new StatTarget.PerWeaponAmmo(NON_BEAM_ENERGY, false))),
    AMMO_REGEN("weapon ammo regeneration rate", true, EnumSet.of(PERCENT, MULT),
            Targets.typed(stat(MutableShipStatsAPI::getBallisticAmmoRegenMult),
                            stat(MutableShipStatsAPI::getMissileAmmoRegenMult),
                            stat(MutableShipStatsAPI::getEnergyAmmoRegenMult))
                    .with(BEAM, new StatTarget.PerWeaponAmmo(BEAM, true))
                    .with(NON_BEAM_ENERGY, new StatTarget.PerWeaponAmmo(NON_BEAM_ENERGY, true))),
    PROJECTILE_SPEED("weapon projectile speed", true, EnumSet.of(PERCENT, MULT),
            Targets.typed(stat(MutableShipStatsAPI::getBallisticProjectileSpeedMult),
                            bonus(MutableShipStatsAPI::getMissileMaxSpeedBonus),
                            new StatTarget.Composite(List.of(stat(MutableShipStatsAPI::getEnergyProjectileSpeedMult),
                                    bonus(MutableShipStatsAPI::getBeamSpeedMod))))
                    .with(NON_BEAM_ENERGY, stat(MutableShipStatsAPI::getEnergyProjectileSpeedMult))
                    .with(BEAM, bonus(MutableShipStatsAPI::getBeamSpeedMod))),
    TURN_RATE("weapon turn rate", true, EnumSet.of(PERCENT, MULT),
            Targets.allOnly(new StatTarget.Composite(List.of(bonus(MutableShipStatsAPI::getWeaponTurnRateBonus),
                            bonus(MutableShipStatsAPI::getBeamWeaponTurnRateBonus))))
                    .with(BEAM, bonus(MutableShipStatsAPI::getBeamWeaponTurnRateBonus))),
    DURABILITY("weapon durability", true, EnumSet.of(PERCENT, MULT),
            Targets.allOnly(bonus(MutableShipStatsAPI::getWeaponHealthBonus))),
    RECOIL("weapon recoil", true, EnumSet.of(PERCENT, MULT),
            Targets.allOnly(new StatTarget.Composite(List.of(stat(MutableShipStatsAPI::getMaxRecoilMult),
                    stat(MutableShipStatsAPI::getRecoilPerShotMult), stat(MutableShipStatsAPI::getRecoilDecayMult))))),
    RANGE_FALLOFF("weapon effectiveness past normal range", true, EnumSet.of(PERCENT, MULT),
            Targets.allOnly(stat(MutableShipStatsAPI::getWeaponRangeMultPastThreshold))),
    RANGE_THRESHOLD("range before falloff effectiveness applies", false, EnumSet.of(FLAT),
            Targets.allOnly(stat(MutableShipStatsAPI::getWeaponRangeThreshold))),
    AUTOFIRE_ACCURACY("target leading accuracy of autofiring weapons", false, EnumSet.of(PERCENT),
            Targets.allOnly(new StatTarget.PercentagePoints(MutableShipStatsAPI::getAutofireAimAccuracy))),
    FLIGHT_ACCELERATION("missile acceleration", false, EnumSet.of(PERCENT, MULT),
            Targets.missileOnly(bonus(MutableShipStatsAPI::getMissileAccelerationBonus))),
    FLIGHT_TURN_RATE("missile turn rate", false, EnumSet.of(PERCENT, MULT),
            Targets.missileOnly(bonus(MutableShipStatsAPI::getMissileMaxTurnRateBonus))),
    FLIGHT_TURN_ACCELERATION("missile turn acceleration", false, EnumSet.of(PERCENT, MULT),
            Targets.missileOnly(bonus(MutableShipStatsAPI::getMissileTurnAccelerationBonus))),
    GUIDANCE("missile guidance", false, EnumSet.of(FLAT, PERCENT),
            Targets.missileOnly(stat(MutableShipStatsAPI::getMissileGuidance))) {
        @Override
        public String describe(WeaponScope scope, StatMode mode, float magnitude) {
            return mode == FLAT ? IMPROVED_GUIDANCE_TEXT : super.describe(scope, mode, magnitude);
        }
    },
    ECCM_CHANCE("chance for missiles to resist enemy ECM and flares", false, EnumSet.of(PERCENT),
            Targets.missileOnly(new StatTarget.PercentagePoints(MutableShipStatsAPI::getEccmChance))),
    HEALTH("missile hitpoints", false, EnumSet.of(PERCENT, MULT),
            Targets.missileOnly(bonus(MutableShipStatsAPI::getMissileHealthBonus)));

    private static final String BURST_BEAM_FIRE_RATE_NOTE = " Burst beams instead recharge faster between bursts.";
    private static final String IMPROVED_GUIDANCE_TEXT = "Significantly improved missile guidance algorithm.";

    private final String statName;
    private final boolean qualifiedByScope;
    private final Set<StatMode> modes;
    private final Map<WeaponScope, StatTarget> targets;

    WeaponStatFamily(String statName, boolean qualifiedByScope, Set<StatMode> modes, Targets targets) {
        this.statName = statName;
        this.qualifiedByScope = qualifiedByScope;
        this.modes = Collections.unmodifiableSet(modes);
        this.targets = targets.build();
    }

    StatTarget target(WeaponScope scope) {
        return targets.get(scope);
    }

    boolean supports(WeaponScope scope, StatMode mode) {
        StatTarget target = targets.get(scope);
        return target != null && modes.contains(mode) && target.supports(mode);
    }

    public String describe(WeaponScope scope, StatMode mode, float magnitude) {
        return mode.describe(magnitude, qualifiedByScope ? scope.qualify(statName) : statName);
    }

    private static StatTarget stat(Function<MutableShipStatsAPI, MutableStat> getter) {
        return new StatTarget.OfStat(getter);
    }

    private static StatTarget bonus(Function<MutableShipStatsAPI, StatBonus> getter) {
        return new StatTarget.OfBonus(getter);
    }

    private static final class Targets {

        private final Map<WeaponScope, StatTarget> byScope = new EnumMap<>(WeaponScope.class);

        static Targets typed(StatTarget ballistic, StatTarget missile, StatTarget energy) {
            return new Targets()
                    .with(WeaponScope.BALLISTIC, ballistic)
                    .with(MISSILE, missile)
                    .with(WeaponScope.ENERGY, energy)
                    .with(ALL, new StatTarget.Composite(List.of(ballistic, missile, energy)));
        }

        static Targets allOnly(StatTarget all) {
            return new Targets().with(ALL, all);
        }

        static Targets missileOnly(StatTarget missile) {
            return new Targets().with(MISSILE, missile);
        }

        Targets with(WeaponScope scope, StatTarget target) {
            byScope.put(scope, target);
            return this;
        }

        Map<WeaponScope, StatTarget> build() {
            return Collections.unmodifiableMap(new EnumMap<>(byScope));
        }
    }
}
