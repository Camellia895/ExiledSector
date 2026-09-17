package exiledsector.skills;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.StatBonus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SkillEffectTest {

    @Test
    void hullModifiesTheHullBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus hullBonus = mock(StatBonus.class);
        when(stats.getHullBonus()).thenReturn(hullBonus);

        SkillEffect.HULL.apply(stats, "mod_id", 10f);

        verify(hullBonus).modifyPercent("mod_id", 10f);
    }

    @Test
    void armorModifiesTheArmorBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus armorBonus = mock(StatBonus.class);
        when(stats.getArmorBonus()).thenReturn(armorBonus);

        SkillEffect.ARMOR.apply(stats, "mod_id", 10f);

        verify(armorBonus).modifyPercent("mod_id", 10f);
    }

    @Test
    void fluxCapacityModifiesTheFluxCapacityStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat fluxCapacity = mock(MutableStat.class);
        when(stats.getFluxCapacity()).thenReturn(fluxCapacity);

        SkillEffect.FLUX_CAPACITY.apply(stats, "mod_id", 1f);

        verify(fluxCapacity).modifyPercent("mod_id", 1f);
    }

    @Test
    void fluxDissipationModifiesTheFluxDissipationStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat fluxDissipation = mock(MutableStat.class);
        when(stats.getFluxDissipation()).thenReturn(fluxDissipation);

        SkillEffect.FLUX_DISSIPATION.apply(stats, "mod_id", 10f);

        verify(fluxDissipation).modifyPercent("mod_id", 10f);
    }

    @Test
    void fluxRegulationModifiesBothFluxCapacityAndDissipationStats() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat fluxCapacity = mock(MutableStat.class);
        MutableStat fluxDissipation = mock(MutableStat.class);
        when(stats.getFluxCapacity()).thenReturn(fluxCapacity);
        when(stats.getFluxDissipation()).thenReturn(fluxDissipation);

        SkillEffect.HYBRID_FLUX.apply(stats, "mod_id", 0.5f);

        verify(fluxCapacity).modifyPercent("mod_id", 0.5f);
        verify(fluxDissipation).modifyPercent("mod_id", 0.5f);
    }

    @Test
    void ballisticDamageModifiesOnlyTheBallisticDamageStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat ballistic = mock(MutableStat.class);
        when(stats.getBallisticWeaponDamageMult()).thenReturn(ballistic);

        SkillEffect.BALLISTIC_DAMAGE.apply(stats, "mod_id", 5f);

        verify(ballistic).modifyPercent("mod_id", 5f);
    }

    @Test
    void missileDamageModifiesOnlyTheMissileDamageStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat missile = mock(MutableStat.class);
        when(stats.getMissileWeaponDamageMult()).thenReturn(missile);

        SkillEffect.MISSILE_DAMAGE.apply(stats, "mod_id", 5f);

        verify(missile).modifyPercent("mod_id", 5f);
    }

    @Test
    void nonBeamEnergyDamageModifiesOnlyTheEnergyDamageStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat energy = mock(MutableStat.class);
        when(stats.getEnergyWeaponDamageMult()).thenReturn(energy);

        SkillEffect.NON_BEAM_ENERGY_DAMAGE.apply(stats, "mod_id", 5f);

        verify(energy).modifyPercent("mod_id", 5f);
    }

    @Test
    void beamDamageModifiesOnlyTheBeamDamageStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat beam = mock(MutableStat.class);
        when(stats.getBeamWeaponDamageMult()).thenReturn(beam);

        SkillEffect.BEAM_DAMAGE.apply(stats, "mod_id", 5f);

        verify(beam).modifyPercent("mod_id", 5f);
    }

    @Test
    void energyDamageModifiesBothEnergyAndBeamDamageStats() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat energy = mock(MutableStat.class);
        MutableStat beam = mock(MutableStat.class);
        when(stats.getEnergyWeaponDamageMult()).thenReturn(energy);
        when(stats.getBeamWeaponDamageMult()).thenReturn(beam);

        SkillEffect.ENERGY_DAMAGE.apply(stats, "mod_id", 5f);

        verify(energy).modifyPercent("mod_id", 5f);
        verify(beam).modifyPercent("mod_id", 5f);
    }

    @Test
    void allWeaponDamageModifiesEveryWeaponDamageStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat ballistic = mock(MutableStat.class);
        MutableStat missile = mock(MutableStat.class);
        MutableStat energy = mock(MutableStat.class);
        MutableStat beam = mock(MutableStat.class);
        when(stats.getBallisticWeaponDamageMult()).thenReturn(ballistic);
        when(stats.getMissileWeaponDamageMult()).thenReturn(missile);
        when(stats.getEnergyWeaponDamageMult()).thenReturn(energy);
        when(stats.getBeamWeaponDamageMult()).thenReturn(beam);

        SkillEffect.ALL_WEAPON_DAMAGE.apply(stats, "mod_id", 5f);

        verify(ballistic).modifyPercent("mod_id", 5f);
        verify(missile).modifyPercent("mod_id", 5f);
        verify(energy).modifyPercent("mod_id", 5f);
        verify(beam).modifyPercent("mod_id", 5f);
    }

    @Test
    void describeFormatsAWholeNumberMagnitudeWithoutADecimal() {
        assertEquals("Increases hull points by 10%.", SkillEffect.HULL.describe(10f));
    }

    @Test
    void describeFormatsAFractionalMagnitudeWithADecimal() {
        assertEquals("Increases flux capacity and dissipation by 0.5% each.", SkillEffect.HYBRID_FLUX.describe(0.5f));
    }

    @Test
    void describeMentionsBothStatsForTheAllWeaponDamageHybrid() {
        assertEquals("Increases damage of all weapon types by 5%.", SkillEffect.ALL_WEAPON_DAMAGE.describe(5f));
    }

    @Test
    void maneuverabilityModifiesTheMaxTurnRateStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat turnRate = mock(MutableStat.class);
        when(stats.getMaxTurnRate()).thenReturn(turnRate);

        SkillEffect.MANEUVERABILITY.apply(stats, "mod_id", 15f);

        verify(turnRate).modifyPercent("mod_id", 15f);
    }

    @Test
    void fuelCapacityModifiesTheFuelModStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus fuelMod = mock(StatBonus.class);
        when(stats.getFuelMod()).thenReturn(fuelMod);

        SkillEffect.FUEL_CAPACITY.apply(stats, "mod_id", 20f);

        verify(fuelMod).modifyPercent("mod_id", 20f);
    }

    @Test
    void cargoCapacityModifiesTheCargoModStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus cargoMod = mock(StatBonus.class);
        when(stats.getCargoMod()).thenReturn(cargoMod);

        SkillEffect.CARGO_CAPACITY.apply(stats, "mod_id", 20f);

        verify(cargoMod).modifyPercent("mod_id", 20f);
    }

    @Test
    void crewCapacityModifiesTheMaxCrewModStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus maxCrewMod = mock(StatBonus.class);
        when(stats.getMaxCrewMod()).thenReturn(maxCrewMod);

        SkillEffect.CREW_CAPACITY.apply(stats, "mod_id", 20f);

        verify(maxCrewMod).modifyPercent("mod_id", 20f);
    }

    @Test
    void burnLevelModifiesTheMaxBurnLevelStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat maxBurnLevel = mock(MutableStat.class);
        when(stats.getMaxBurnLevel()).thenReturn(maxBurnLevel);

        SkillEffect.BURN_LEVEL.apply(stats, "mod_id", 15f);

        verify(maxBurnLevel).modifyPercent("mod_id", 15f);
    }

    @Test
    void sensorProfileModifiesTheSensorProfileStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat sensorProfile = mock(MutableStat.class);
        when(stats.getSensorProfile()).thenReturn(sensorProfile);

        SkillEffect.SENSOR_PROFILE.apply(stats, "mod_id", -10f);

        verify(sensorProfile).modifyPercent("mod_id", -10f);
    }

    @Test
    void sensorStrengthModifiesTheSensorStrengthStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat sensorStrength = mock(MutableStat.class);
        when(stats.getSensorStrength()).thenReturn(sensorStrength);

        SkillEffect.SENSOR_STRENGTH.apply(stats, "mod_id", 20f);

        verify(sensorStrength).modifyPercent("mod_id", 20f);
    }

    @Test
    void ballisticWeaponRangeModifiesTheBallisticRangeBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus rangeBonus = mock(StatBonus.class);
        when(stats.getBallisticWeaponRangeBonus()).thenReturn(rangeBonus);

        SkillEffect.BALLISTIC_WEAPON_RANGE.apply(stats, "mod_id", 15f);

        verify(rangeBonus).modifyPercent("mod_id", 15f);
    }

    @Test
    void energyWeaponRangeModifiesTheEnergyRangeBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus rangeBonus = mock(StatBonus.class);
        when(stats.getEnergyWeaponRangeBonus()).thenReturn(rangeBonus);

        SkillEffect.ENERGY_WEAPON_RANGE.apply(stats, "mod_id", 15f);

        verify(rangeBonus).modifyPercent("mod_id", 15f);
    }

    @Test
    void beamWeaponRangeModifiesTheBeamRangeBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus rangeBonus = mock(StatBonus.class);
        when(stats.getBeamWeaponRangeBonus()).thenReturn(rangeBonus);

        SkillEffect.BEAM_WEAPON_RANGE.apply(stats, "mod_id", 15f);

        verify(rangeBonus).modifyPercent("mod_id", 15f);
    }

    @Test
    void ballisticAmmoModifiesTheBallisticAmmoBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus ammoBonus = mock(StatBonus.class);
        when(stats.getBallisticAmmoBonus()).thenReturn(ammoBonus);

        SkillEffect.BALLISTIC_AMMO.apply(stats, "mod_id", 20f);

        verify(ammoBonus).modifyPercent("mod_id", 20f);
    }

    @Test
    void energyAmmoModifiesTheEnergyAmmoBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus ammoBonus = mock(StatBonus.class);
        when(stats.getEnergyAmmoBonus()).thenReturn(ammoBonus);

        SkillEffect.ENERGY_AMMO.apply(stats, "mod_id", 20f);

        verify(ammoBonus).modifyPercent("mod_id", 20f);
    }

    @Test
    void missileAmmoModifiesTheMissileAmmoBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus ammoBonus = mock(StatBonus.class);
        when(stats.getMissileAmmoBonus()).thenReturn(ammoBonus);

        SkillEffect.MISSILE_AMMO.apply(stats, "mod_id", 25f);

        verify(ammoBonus).modifyPercent("mod_id", 25f);
    }

    @Test
    void weaponTurnRateModifiesTheWeaponTurnRateBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus turnRateBonus = mock(StatBonus.class);
        when(stats.getWeaponTurnRateBonus()).thenReturn(turnRateBonus);

        SkillEffect.WEAPON_TURN_RATE.apply(stats, "mod_id", 20f);

        verify(turnRateBonus).modifyPercent("mod_id", 20f);
    }

    @Test
    void shieldArcModifiesTheShieldArcBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus arcBonus = mock(StatBonus.class);
        when(stats.getShieldArcBonus()).thenReturn(arcBonus);

        SkillEffect.SHIELD_ARC.apply(stats, "mod_id", 20f);

        verify(arcBonus).modifyPercent("mod_id", 20f);
    }

    @Test
    void shieldUpkeepModifiesTheShieldUpkeepMultStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat upkeepMult = mock(MutableStat.class);
        when(stats.getShieldUpkeepMult()).thenReturn(upkeepMult);

        SkillEffect.SHIELD_UPKEEP.apply(stats, "mod_id", -15f);

        verify(upkeepMult).modifyPercent("mod_id", -15f);
    }

    @Test
    void shieldAbsorptionModifiesTheShieldAbsorptionMultStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat absorptionMult = mock(MutableStat.class);
        when(stats.getShieldAbsorptionMult()).thenReturn(absorptionMult);

        SkillEffect.SHIELD_ABSORPTION.apply(stats, "mod_id", -10f);

        verify(absorptionMult).modifyPercent("mod_id", -10f);
    }

    @Test
    void shieldTurnRateModifiesTheShieldTurnRateMultStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat turnRateMult = mock(MutableStat.class);
        when(stats.getShieldTurnRateMult()).thenReturn(turnRateMult);

        SkillEffect.SHIELD_TURN_RATE.apply(stats, "mod_id", 15f);

        verify(turnRateMult).modifyPercent("mod_id", 15f);
    }

    @Test
    void shieldRaiseRateModifiesTheShieldUnfoldRateMultStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat unfoldRateMult = mock(MutableStat.class);
        when(stats.getShieldUnfoldRateMult()).thenReturn(unfoldRateMult);

        SkillEffect.SHIELD_RAISE_RATE.apply(stats, "mod_id", 15f);

        verify(unfoldRateMult).modifyPercent("mod_id", 15f);
    }

    @Test
    void weaponDurabilityModifiesTheWeaponHealthBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus healthBonus = mock(StatBonus.class);
        when(stats.getWeaponHealthBonus()).thenReturn(healthBonus);

        SkillEffect.WEAPON_DURABILITY.apply(stats, "mod_id", 20f);

        verify(healthBonus).modifyPercent("mod_id", 20f);
    }

    @Test
    void engineDurabilityModifiesTheEngineHealthBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus healthBonus = mock(StatBonus.class);
        when(stats.getEngineHealthBonus()).thenReturn(healthBonus);

        SkillEffect.ENGINE_DURABILITY.apply(stats, "mod_id", 15f);

        verify(healthBonus).modifyPercent("mod_id", 15f);
    }

    @Test
    void topSpeedModifiesTheMaxSpeedStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat maxSpeed = mock(MutableStat.class);
        when(stats.getMaxSpeed()).thenReturn(maxSpeed);

        SkillEffect.TOP_SPEED.apply(stats, "mod_id", 15f);

        verify(maxSpeed).modifyPercent("mod_id", 15f);
    }

    @Test
    void peakCrDurationModifiesThePeakCRDurationStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus peakCrDuration = mock(StatBonus.class);
        when(stats.getPeakCRDuration()).thenReturn(peakCrDuration);

        SkillEffect.PEAK_CR_DURATION.apply(stats, "mod_id", 20f);

        verify(peakCrDuration).modifyPercent("mod_id", 20f);
    }

    @Test
    void weaponRangeFalloffModifiesTheWeaponRangeMultPastThresholdStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat rangeMultPastThreshold = mock(MutableStat.class);
        when(stats.getWeaponRangeMultPastThreshold()).thenReturn(rangeMultPastThreshold);

        SkillEffect.WEAPON_RANGE_FALLOFF.apply(stats, "mod_id", -15f);

        verify(rangeMultPastThreshold).modifyPercent("mod_id", -15f);
    }

    @Test
    void repairTimeModifiesBothWeaponAndEngineRepairTimeStats() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat weaponRepairTime = mock(MutableStat.class);
        MutableStat engineRepairTime = mock(MutableStat.class);
        when(stats.getCombatWeaponRepairTimeMult()).thenReturn(weaponRepairTime);
        when(stats.getCombatEngineRepairTimeMult()).thenReturn(engineRepairTime);

        SkillEffect.REPAIR_TIME.apply(stats, "mod_id", -20f);

        verify(weaponRepairTime).modifyPercent("mod_id", -20f);
        verify(engineRepairTime).modifyPercent("mod_id", -20f);
    }

    @Test
    void missileGuidanceModifiesTheMissileGuidanceStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat missileGuidance = mock(MutableStat.class);
        when(stats.getMissileGuidance()).thenReturn(missileGuidance);

        SkillEffect.MISSILE_GUIDANCE.apply(stats, "mod_id", 20f);

        verify(missileGuidance).modifyPercent("mod_id", 20f);
    }

    @Test
    void crRecoveryRateModifiesTheBaseCRRecoveryRateStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat crRecoveryRate = mock(MutableStat.class);
        when(stats.getBaseCRRecoveryRatePercentPerDay()).thenReturn(crRecoveryRate);

        SkillEffect.CR_RECOVERY_RATE.apply(stats, "mod_id", 15f);

        verify(crRecoveryRate).modifyPercent("mod_id", 15f);
    }

    @Test
    void crewLossModifiesTheCrewLossMultStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat crewLossMult = mock(MutableStat.class);
        when(stats.getCrewLossMult()).thenReturn(crewLossMult);

        SkillEffect.CREW_LOSS.apply(stats, "mod_id", -15f);

        verify(crewLossMult).modifyPercent("mod_id", -15f);
    }

    @Test
    void energyDamageTakenModifiesTheEnergyDamageTakenMultStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat damageTakenMult = mock(MutableStat.class);
        when(stats.getEnergyDamageTakenMult()).thenReturn(damageTakenMult);

        SkillEffect.ENERGY_DAMAGE_TAKEN.apply(stats, "mod_id", -10f);

        verify(damageTakenMult).modifyPercent("mod_id", -10f);
    }

    @Test
    void describeUsesIncreasesForAPositiveBidirectionalMagnitude() {
        assertEquals("Increases peak combat readiness duration by 20%.", SkillEffect.PEAK_CR_DURATION.describe(20f));
    }

    @Test
    void describeUsesDecreasesForANegativeBidirectionalMagnitude() {
        assertEquals("Decreases peak combat readiness duration by 20%.", SkillEffect.PEAK_CR_DURATION.describe(-20f));
    }
}
