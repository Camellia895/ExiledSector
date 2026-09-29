package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import exiledsector.i18n.StyledText;
import exiledsector.i18n.Translation;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static exiledsector.skills.skilleffect.StatMode.FLAT;
import static exiledsector.skills.skilleffect.StatMode.MULT;
import static exiledsector.skills.skilleffect.StatMode.PERCENT;
import static exiledsector.skills.skilleffect.StatTarget.bonus;
import static exiledsector.skills.skilleffect.StatTarget.stat;
import static exiledsector.skills.skilleffect.WeaponScope.ALL;
import static exiledsector.skills.skilleffect.WeaponScope.BEAM;
import static exiledsector.skills.skilleffect.WeaponScope.MISSILE;
import static exiledsector.skills.skilleffect.WeaponScope.NON_BEAM_ENERGY;

public enum WeaponStatFamily {

    DAMAGE(true, EnumSet.allOf(StatMode.class),
            Targets.typed(stat(MutableShipStatsAPI::getBallisticWeaponDamageMult),
                            stat(MutableShipStatsAPI::getMissileWeaponDamageMult),
                            stat(MutableShipStatsAPI::getEnergyWeaponDamageMult))
                    .with(BEAM, stat(MutableShipStatsAPI::getBeamWeaponDamageMult))
                    .with(NON_BEAM_ENERGY, new StatTarget.Compensated(stat(MutableShipStatsAPI::getEnergyWeaponDamageMult),
                            stat(MutableShipStatsAPI::getBeamWeaponDamageMult), EnumSet.allOf(StatMode.class)))),
    RANGE(true, EnumSet.allOf(StatMode.class),
            Targets.typed(bonus(MutableShipStatsAPI::getBallisticWeaponRangeBonus),
                            bonus(MutableShipStatsAPI::getMissileWeaponRangeBonus),
                            bonus(MutableShipStatsAPI::getEnergyWeaponRangeBonus))
                    .with(BEAM, bonus(MutableShipStatsAPI::getBeamWeaponRangeBonus))
                    .with(NON_BEAM_ENERGY, new StatTarget.Compensated(bonus(MutableShipStatsAPI::getEnergyWeaponRangeBonus),
                            bonus(MutableShipStatsAPI::getBeamWeaponRangeBonus), EnumSet.allOf(StatMode.class)))),
    FLUX_COST(true, EnumSet.of(PERCENT, MULT),
            Targets.typed(bonus(MutableShipStatsAPI::getBallisticWeaponFluxCostMod),
                            bonus(MutableShipStatsAPI::getMissileWeaponFluxCostMod),
                            bonus(MutableShipStatsAPI::getEnergyWeaponFluxCostMod))
                    .with(BEAM, stat(MutableShipStatsAPI::getBeamWeaponFluxCostMult))
                    .with(NON_BEAM_ENERGY, new StatTarget.Compensated(bonus(MutableShipStatsAPI::getEnergyWeaponFluxCostMod),
                            stat(MutableShipStatsAPI::getBeamWeaponFluxCostMult), EnumSet.of(MULT)))),
    FIRE_RATE(true, EnumSet.of(PERCENT, MULT),
            Targets.typed(stat(MutableShipStatsAPI::getBallisticRoFMult),
                    stat(MutableShipStatsAPI::getMissileRoFMult),
                    stat(MutableShipStatsAPI::getEnergyRoFMult))) {
        @Override
        public StyledText description(WeaponScope scope, StatMode mode, float magnitude) {
            StyledText text = super.description(scope, mode, magnitude);
            boolean reachesBeams = scope == ALL || scope == WeaponScope.ENERGY;
            return reachesBeams ? withNote(text, "weapon.note.burstBeam") : text;
        }
    },
    AMMO(true, EnumSet.allOf(StatMode.class),
            Targets.typed(bonus(MutableShipStatsAPI::getBallisticAmmoBonus),
                            bonus(MutableShipStatsAPI::getMissileAmmoBonus),
                            bonus(MutableShipStatsAPI::getEnergyAmmoBonus))
                    .with(BEAM, new StatTarget.PerWeaponAmmo(BEAM, false))
                    .with(NON_BEAM_ENERGY, new StatTarget.PerWeaponAmmo(NON_BEAM_ENERGY, false))),
    AMMO_REGEN(true, EnumSet.of(PERCENT, MULT),
            Targets.typed(stat(MutableShipStatsAPI::getBallisticAmmoRegenMult),
                            stat(MutableShipStatsAPI::getMissileAmmoRegenMult),
                            stat(MutableShipStatsAPI::getEnergyAmmoRegenMult))
                    .with(BEAM, new StatTarget.PerWeaponAmmo(BEAM, true))
                    .with(NON_BEAM_ENERGY, new StatTarget.PerWeaponAmmo(NON_BEAM_ENERGY, true))),
    PROJECTILE_SPEED(true, EnumSet.of(PERCENT, MULT),
            Targets.typed(stat(MutableShipStatsAPI::getBallisticProjectileSpeedMult),
                            bonus(MutableShipStatsAPI::getMissileMaxSpeedBonus),
                            new StatTarget.Composite(List.of(stat(MutableShipStatsAPI::getEnergyProjectileSpeedMult),
                                    bonus(MutableShipStatsAPI::getBeamSpeedMod))))
                    .with(NON_BEAM_ENERGY, stat(MutableShipStatsAPI::getEnergyProjectileSpeedMult))
                    .with(BEAM, bonus(MutableShipStatsAPI::getBeamSpeedMod))),
    TURN_RATE(true, EnumSet.of(PERCENT, MULT),
            Targets.allOnly(new StatTarget.Composite(List.of(bonus(MutableShipStatsAPI::getWeaponTurnRateBonus),
                            bonus(MutableShipStatsAPI::getBeamWeaponTurnRateBonus))))
                    .with(BEAM, bonus(MutableShipStatsAPI::getBeamWeaponTurnRateBonus))),
    DURABILITY(true, EnumSet.of(PERCENT, MULT),
            Targets.allOnly(bonus(MutableShipStatsAPI::getWeaponHealthBonus))),
    RECOIL(true, EnumSet.of(PERCENT, MULT),
            Targets.allOnly(new StatTarget.Composite(List.of(stat(MutableShipStatsAPI::getMaxRecoilMult),
                    stat(MutableShipStatsAPI::getRecoilPerShotMult), stat(MutableShipStatsAPI::getRecoilDecayMult))))),
    RANGE_FALLOFF(true, EnumSet.of(PERCENT, MULT),
            Targets.allOnly(stat(MutableShipStatsAPI::getWeaponRangeMultPastThreshold))),
    RANGE_THRESHOLD(false, EnumSet.of(FLAT),
            Targets.allOnly(stat(MutableShipStatsAPI::getWeaponRangeThreshold))),
    AUTOFIRE_ACCURACY(false, EnumSet.of(PERCENT),
            Targets.allOnly(new StatTarget.PercentagePoints(MutableShipStatsAPI::getAutofireAimAccuracy))),
    FLIGHT_ACCELERATION(false, EnumSet.of(PERCENT, MULT),
            Targets.missileOnly(bonus(MutableShipStatsAPI::getMissileAccelerationBonus))),
    FLIGHT_TURN_RATE(false, EnumSet.of(PERCENT, MULT),
            Targets.missileOnly(bonus(MutableShipStatsAPI::getMissileMaxTurnRateBonus))),
    FLIGHT_TURN_ACCELERATION(false, EnumSet.of(PERCENT, MULT),
            Targets.missileOnly(bonus(MutableShipStatsAPI::getMissileTurnAccelerationBonus))),
    GUIDANCE(false, EnumSet.of(FLAT, PERCENT),
            Targets.missileOnly(stat(MutableShipStatsAPI::getMissileGuidance))) {
        @Override
        public StyledText description(WeaponScope scope, StatMode mode, float magnitude) {
            return mode == FLAT ? Translation.styled("weapon.guidanceImproved") : super.description(scope, mode, magnitude);
        }
    },
    ECCM_CHANCE(false, EnumSet.of(PERCENT),
            Targets.missileOnly(new StatTarget.PercentagePoints(MutableShipStatsAPI::getEccmChance))),
    HEALTH(false, EnumSet.of(PERCENT, MULT),
            Targets.missileOnly(bonus(MutableShipStatsAPI::getMissileHealthBonus)));

    private final String statKey;
    private final boolean qualifiedByScope;
    private final Set<StatMode> modes;
    private final Map<WeaponScope, StatTarget> targets;

    WeaponStatFamily(boolean qualifiedByScope, Set<StatMode> modes, Targets targets) {
        this.statKey = "weapon.stat." + name();
        this.qualifiedByScope = qualifiedByScope;
        this.modes = Collections.unmodifiableSet(modes);
        this.targets = targets.build();
    }

    public boolean lowerIsBetter() {
        return this == FLUX_COST || this == RECOIL;
    }

    StatTarget target(WeaponScope scope) {
        return targets.get(scope);
    }

    boolean supports(WeaponScope scope, StatMode mode) {
        StatTarget target = targets.get(scope);
        return target != null && modes.contains(mode) && target.supports(mode);
    }

    public StyledText description(WeaponScope scope, StatMode mode, float magnitude) {
        String stat = qualifiedByScope ? scope.qualify(statKey) : Translation.text(statKey);
        StyledText text = mode.description(magnitude, stat);
        return targets.get(scope) instanceof StatTarget.PerWeaponAmmo ? withNote(text, "weapon.note.perWeapon") : text;
    }

    private static StyledText withNote(StyledText text, String noteKey) {
        return Translation.msg("format.sentences").arg("a", text).arg("b", Translation.styled(noteKey)).styled();
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
