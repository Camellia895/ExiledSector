package exiledsector.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.RepairTrackerAPI;
import exiledsector.skills.skilleffect.CombatSkillEffect;
import exiledsector.skills.skilleffect.DefenseSkillEffect;
import exiledsector.skills.skilleffect.FighterSkillEffect;
import exiledsector.skills.skilleffect.FluxSkillEffect;
import exiledsector.skills.skilleffect.LogisticsSkillEffect;
import exiledsector.skills.skilleffect.MiscSkillEffect;
import exiledsector.skills.skilleffect.MovementSkillEffect;
import exiledsector.skills.skilleffect.PhaseSkillEffect;
import exiledsector.skills.skilleffect.ShieldSkillEffect;
import exiledsector.skills.skilleffect.WeaponSkillEffect;
import org.junit.jupiter.api.Test;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicFakeBeamPlugin;
import org.magiclib.util.MagicFakeBeam;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SkillEffectTest {

    @Test
    void hullModifiesTheHullBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus hullBonus = mock(StatBonus.class);
        when(stats.getHullBonus()).thenReturn(hullBonus);

        DefenseSkillEffect.HULL_PERCENT.apply(stats, "mod_id", 10f);

        verify(hullBonus).modifyPercent("mod_id", 10f);
    }

    @Test
    void hullFlatModifiesTheHullBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus hullBonus = mock(StatBonus.class);
        when(stats.getHullBonus()).thenReturn(hullBonus);

        DefenseSkillEffect.HULL_FLAT.apply(stats, "mod_id", 250f);

        verify(hullBonus).modifyFlat("mod_id", 250f);
    }

    @Test
    void hullFlatDescribesAFlatChangeToHullPoints() {
        assertEquals("Increases hull points by 250.", DefenseSkillEffect.HULL_FLAT.describe(250f));
    }

    @Test
    void hullMultModifiesTheHullBonusStatMultiplicatively() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus hullBonus = mock(StatBonus.class);
        when(stats.getHullBonus()).thenReturn(hullBonus);

        DefenseSkillEffect.HULL_MULT.apply(stats, "mod_id", 2f);

        verify(hullBonus).modifyMult("mod_id", 1.02f);
    }

    @Test
    void hullMultDescribesAMultiplicativeChangeToHullPoints() {
        assertEquals("2% more hull points.", DefenseSkillEffect.HULL_MULT.describe(2f));
    }

    @Test
    void armorModifiesTheArmorBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus armorBonus = mock(StatBonus.class);
        when(stats.getArmorBonus()).thenReturn(armorBonus);

        DefenseSkillEffect.ARMOR_PERCENT.apply(stats, "mod_id", 10f);

        verify(armorBonus).modifyPercent("mod_id", 10f);
    }

    @Test
    void fluxCapacityModifiesTheFluxCapacityStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat fluxCapacity = mock(MutableStat.class);
        when(stats.getFluxCapacity()).thenReturn(fluxCapacity);

        FluxSkillEffect.FLUX_CAPACITY_PERCENT.apply(stats, "mod_id", 1f);

        verify(fluxCapacity).modifyPercent("mod_id", 1f);
    }

    @Test
    void fluxDissipationModifiesTheFluxDissipationStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat fluxDissipation = mock(MutableStat.class);
        when(stats.getFluxDissipation()).thenReturn(fluxDissipation);

        FluxSkillEffect.FLUX_DISSIPATION_PERCENT.apply(stats, "mod_id", 10f);

        verify(fluxDissipation).modifyPercent("mod_id", 10f);
    }

    @Test
    void ballisticDamageModifiesOnlyTheBallisticDamageStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat ballistic = mock(MutableStat.class);
        when(stats.getBallisticWeaponDamageMult()).thenReturn(ballistic);

        WeaponSkillEffect.BALLISTIC_DAMAGE_PERCENT.apply(stats, "mod_id", 5f);

        verify(ballistic).modifyPercent("mod_id", 5f);
    }

    @Test
    void missileDamageModifiesOnlyTheMissileDamageStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat missile = mock(MutableStat.class);
        when(stats.getMissileWeaponDamageMult()).thenReturn(missile);

        WeaponSkillEffect.MISSILE_DAMAGE_PERCENT.apply(stats, "mod_id", 5f);

        verify(missile).modifyPercent("mod_id", 5f);
    }

    @Test
    void nonBeamEnergyDamageModifiesOnlyTheEnergyDamageStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat energy = mock(MutableStat.class);
        when(stats.getEnergyWeaponDamageMult()).thenReturn(energy);

        WeaponSkillEffect.NON_BEAM_ENERGY_DAMAGE_PERCENT.apply(stats, "mod_id", 5f);

        verify(energy).modifyPercent("mod_id", 5f);
    }

    @Test
    void beamDamageModifiesOnlyTheBeamDamageStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat beam = mock(MutableStat.class);
        when(stats.getBeamWeaponDamageMult()).thenReturn(beam);

        WeaponSkillEffect.BEAM_DAMAGE_PERCENT.apply(stats, "mod_id", 5f);

        verify(beam).modifyPercent("mod_id", 5f);
    }

    @Test
    void energyDamageModifiesBothEnergyAndBeamDamageStats() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat energy = mock(MutableStat.class);
        MutableStat beam = mock(MutableStat.class);
        when(stats.getEnergyWeaponDamageMult()).thenReturn(energy);
        when(stats.getBeamWeaponDamageMult()).thenReturn(beam);

        WeaponSkillEffect.ENERGY_DAMAGE_PERCENT.apply(stats, "mod_id", 5f);

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

        WeaponSkillEffect.ALL_WEAPON_DAMAGE_PERCENT.apply(stats, "mod_id", 5f);

        verify(ballistic).modifyPercent("mod_id", 5f);
        verify(missile).modifyPercent("mod_id", 5f);
        verify(energy).modifyPercent("mod_id", 5f);
        verify(beam).modifyPercent("mod_id", 5f);
    }

    @Test
    void describeFormatsAWholeNumberMagnitudeWithoutADecimal() {
        assertEquals("Increases hull points by 10%.", DefenseSkillEffect.HULL_PERCENT.describe(10f));
    }

    @Test
    void describeFormatsAFractionalMagnitudeWithADecimal() {
        assertEquals("Increases flux capacity by 0.5%.", FluxSkillEffect.FLUX_CAPACITY_PERCENT.describe(0.5f));
    }

    @Test
    void describeMentionsBothStatsForTheAllWeaponDamageHybrid() {
        assertEquals("Increases damage of all weapon types by 5%.", WeaponSkillEffect.ALL_WEAPON_DAMAGE_PERCENT.describe(5f));
    }

    @Test
    void maneuverabilityModifiesTheMaxTurnRateStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat turnRate = mock(MutableStat.class);
        when(stats.getMaxTurnRate()).thenReturn(turnRate);

        MovementSkillEffect.MANEUVERABILITY_PERCENT.apply(stats, "mod_id", 15f);

        verify(turnRate).modifyPercent("mod_id", 15f);
    }

    @Test
    void fuelCapacityModifiesTheFuelModStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus fuelMod = mock(StatBonus.class);
        when(stats.getFuelMod()).thenReturn(fuelMod);

        LogisticsSkillEffect.FUEL_CAPACITY_PERCENT.apply(stats, "mod_id", 20f);

        verify(fuelMod).modifyPercent("mod_id", 20f);
    }

    @Test
    void cargoCapacityModifiesTheCargoModStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus cargoMod = mock(StatBonus.class);
        when(stats.getCargoMod()).thenReturn(cargoMod);

        LogisticsSkillEffect.CARGO_CAPACITY_PERCENT.apply(stats, "mod_id", 20f);

        verify(cargoMod).modifyPercent("mod_id", 20f);
    }

    @Test
    void crewCapacityModifiesTheMaxCrewModStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus maxCrewMod = mock(StatBonus.class);
        when(stats.getMaxCrewMod()).thenReturn(maxCrewMod);

        LogisticsSkillEffect.CREW_CAPACITY_PERCENT.apply(stats, "mod_id", 20f);

        verify(maxCrewMod).modifyPercent("mod_id", 20f);
    }

    @Test
    void sensorProfileModifiesTheSensorProfileStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat sensorProfile = mock(MutableStat.class);
        when(stats.getSensorProfile()).thenReturn(sensorProfile);

        LogisticsSkillEffect.SENSOR_PROFILE_PERCENT.apply(stats, "mod_id", -10f);

        verify(sensorProfile).modifyPercent("mod_id", -10f);
    }

    @Test
    void sensorStrengthModifiesTheSensorStrengthStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat sensorStrength = mock(MutableStat.class);
        when(stats.getSensorStrength()).thenReturn(sensorStrength);

        LogisticsSkillEffect.SENSOR_STRENGTH_PERCENT.apply(stats, "mod_id", 20f);

        verify(sensorStrength).modifyPercent("mod_id", 20f);
    }

    @Test
    void combatVisionModifiesTheSightRadiusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus sightRadius = mock(StatBonus.class);
        when(stats.getSightRadiusMod()).thenReturn(sightRadius);

        LogisticsSkillEffect.COMBAT_VISION.apply(stats, "mod_id", 1000f);

        verify(sightRadius).modifyFlat("mod_id", 1000f);
    }

    @Test
    void ballisticWeaponRangeModifiesTheBallisticRangeBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus rangeBonus = mock(StatBonus.class);
        when(stats.getBallisticWeaponRangeBonus()).thenReturn(rangeBonus);

        WeaponSkillEffect.BALLISTIC_WEAPON_RANGE_PERCENT.apply(stats, "mod_id", 15f);

        verify(rangeBonus).modifyPercent("mod_id", 15f);
    }

    @Test
    void energyWeaponRangeModifiesTheEnergyRangeBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus rangeBonus = mock(StatBonus.class);
        when(stats.getEnergyWeaponRangeBonus()).thenReturn(rangeBonus);

        WeaponSkillEffect.ENERGY_WEAPON_RANGE_PERCENT.apply(stats, "mod_id", 15f);

        verify(rangeBonus).modifyPercent("mod_id", 15f);
    }

    @Test
    void beamWeaponRangeModifiesTheBeamRangeBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus rangeBonus = mock(StatBonus.class);
        when(stats.getBeamWeaponRangeBonus()).thenReturn(rangeBonus);

        WeaponSkillEffect.BEAM_WEAPON_RANGE_PERCENT.apply(stats, "mod_id", 15f);

        verify(rangeBonus).modifyPercent("mod_id", 15f);
    }

    @Test
    void ballisticAmmoModifiesTheBallisticAmmoBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus ammoBonus = mock(StatBonus.class);
        when(stats.getBallisticAmmoBonus()).thenReturn(ammoBonus);

        WeaponSkillEffect.BALLISTIC_AMMO_PERCENT.apply(stats, "mod_id", 20f);

        verify(ammoBonus).modifyPercent("mod_id", 20f);
    }

    @Test
    void energyAmmoModifiesTheEnergyAmmoBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus ammoBonus = mock(StatBonus.class);
        when(stats.getEnergyAmmoBonus()).thenReturn(ammoBonus);

        WeaponSkillEffect.ENERGY_AMMO_PERCENT.apply(stats, "mod_id", 20f);

        verify(ammoBonus).modifyPercent("mod_id", 20f);
    }

    @Test
    void missileAmmoModifiesTheMissileAmmoBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus ammoBonus = mock(StatBonus.class);
        when(stats.getMissileAmmoBonus()).thenReturn(ammoBonus);

        WeaponSkillEffect.MISSILE_AMMO_PERCENT.apply(stats, "mod_id", 25f);

        verify(ammoBonus).modifyPercent("mod_id", 25f);
    }

    @Test
    void weaponTurnRateModifiesTheWeaponTurnRateBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus turnRateBonus = mock(StatBonus.class);
        when(stats.getWeaponTurnRateBonus()).thenReturn(turnRateBonus);

        WeaponSkillEffect.WEAPON_TURN_RATE_PERCENT.apply(stats, "mod_id", 20f);

        verify(turnRateBonus).modifyPercent("mod_id", 20f);
    }

    @Test
    void shieldArcModifiesTheShieldArcBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus arcBonus = mock(StatBonus.class);
        when(stats.getShieldArcBonus()).thenReturn(arcBonus);

        ShieldSkillEffect.SHIELD_ARC_PERCENT.apply(stats, "mod_id", 20f);

        verify(arcBonus).modifyPercent("mod_id", 20f);
    }

    @Test
    void shieldUpkeepModifiesTheShieldUpkeepMultStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat upkeepMult = mock(MutableStat.class);
        when(stats.getShieldUpkeepMult()).thenReturn(upkeepMult);

        ShieldSkillEffect.SHIELD_UPKEEP_PERCENT.apply(stats, "mod_id", -15f);

        verify(upkeepMult).modifyPercent("mod_id", -15f);
    }

    @Test
    void shieldAbsorptionModifiesTheShieldAbsorptionMultStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat absorptionMult = mock(MutableStat.class);
        when(stats.getShieldAbsorptionMult()).thenReturn(absorptionMult);

        DefenseSkillEffect.SHIELD_ABSORPTION_PERCENT.apply(stats, "mod_id", -10f);

        verify(absorptionMult).modifyPercent("mod_id", -10f);
    }

    @Test
    void shieldTurnRateModifiesTheShieldTurnRateMultStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat turnRateMult = mock(MutableStat.class);
        when(stats.getShieldTurnRateMult()).thenReturn(turnRateMult);

        ShieldSkillEffect.SHIELD_TURN_RATE_PERCENT.apply(stats, "mod_id", 15f);

        verify(turnRateMult).modifyPercent("mod_id", 15f);
    }

    @Test
    void shieldRaiseRateModifiesTheShieldUnfoldRateMultStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat unfoldRateMult = mock(MutableStat.class);
        when(stats.getShieldUnfoldRateMult()).thenReturn(unfoldRateMult);

        ShieldSkillEffect.SHIELD_RAISE_RATE_PERCENT.apply(stats, "mod_id", 15f);

        verify(unfoldRateMult).modifyPercent("mod_id", 15f);
    }

    @Test
    void weaponDurabilityModifiesTheWeaponHealthBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus healthBonus = mock(StatBonus.class);
        when(stats.getWeaponHealthBonus()).thenReturn(healthBonus);

        WeaponSkillEffect.WEAPON_DURABILITY_PERCENT.apply(stats, "mod_id", 20f);

        verify(healthBonus).modifyPercent("mod_id", 20f);
    }

    @Test
    void engineDurabilityModifiesTheEngineHealthBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus healthBonus = mock(StatBonus.class);
        when(stats.getEngineHealthBonus()).thenReturn(healthBonus);

        DefenseSkillEffect.ENGINE_DURABILITY_PERCENT.apply(stats, "mod_id", 15f);

        verify(healthBonus).modifyPercent("mod_id", 15f);
    }

    @Test
    void topSpeedModifiesTheMaxSpeedStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat maxSpeed = mock(MutableStat.class);
        when(stats.getMaxSpeed()).thenReturn(maxSpeed);

        MovementSkillEffect.TOP_SPEED_PERCENT.apply(stats, "mod_id", 15f);

        verify(maxSpeed).modifyPercent("mod_id", 15f);
    }

    @Test
    void peakCrDurationModifiesThePeakCRDurationStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus peakCrDuration = mock(StatBonus.class);
        when(stats.getPeakCRDuration()).thenReturn(peakCrDuration);

        MiscSkillEffect.PEAK_CR_DURATION_PERCENT.apply(stats, "mod_id", 20f);

        verify(peakCrDuration).modifyPercent("mod_id", 20f);
    }

    @Test
    void weaponRangeFalloffModifiesTheWeaponRangeMultPastThresholdStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat rangeMultPastThreshold = mock(MutableStat.class);
        when(stats.getWeaponRangeMultPastThreshold()).thenReturn(rangeMultPastThreshold);

        WeaponSkillEffect.WEAPON_RANGE_FALLOFF_PERCENT.apply(stats, "mod_id", -15f);

        verify(rangeMultPastThreshold).modifyPercent("mod_id", -15f);
    }

    @Test
    void repairTimeModifiesBothWeaponAndEngineRepairTimeStats() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat weaponRepairTime = mock(MutableStat.class);
        MutableStat engineRepairTime = mock(MutableStat.class);
        when(stats.getCombatWeaponRepairTimeMult()).thenReturn(weaponRepairTime);
        when(stats.getCombatEngineRepairTimeMult()).thenReturn(engineRepairTime);

        DefenseSkillEffect.REPAIR_TIME_PERCENT.apply(stats, "mod_id", -20f);

        verify(weaponRepairTime).modifyPercent("mod_id", -20f);
        verify(engineRepairTime).modifyPercent("mod_id", -20f);
    }

    @Test
    void missileGuidanceModifiesTheMissileGuidanceStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat missileGuidance = mock(MutableStat.class);
        when(stats.getMissileGuidance()).thenReturn(missileGuidance);

        WeaponSkillEffect.MISSILE_GUIDANCE_PERCENT.apply(stats, "mod_id", 20f);

        verify(missileGuidance).modifyPercent("mod_id", 20f);
    }

    @Test
    void crRecoveryRateModifiesTheBaseCRRecoveryRateStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat crRecoveryRate = mock(MutableStat.class);
        when(stats.getBaseCRRecoveryRatePercentPerDay()).thenReturn(crRecoveryRate);

        LogisticsSkillEffect.CR_RECOVERY_RATE_PERCENT.apply(stats, "mod_id", 15f);

        verify(crRecoveryRate).modifyPercent("mod_id", 15f);
    }

    @Test
    void crewLossModifiesTheCrewLossMultStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat crewLossMult = mock(MutableStat.class);
        when(stats.getCrewLossMult()).thenReturn(crewLossMult);

        LogisticsSkillEffect.CREW_LOSS_PERCENT.apply(stats, "mod_id", -15f);

        verify(crewLossMult).modifyPercent("mod_id", -15f);
    }

    @Test
    void energyDamageTakenModifiesTheEnergyDamageTakenMultStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat damageTakenMult = mock(MutableStat.class);
        MutableStat shieldDamageTakenMult = mock(MutableStat.class);
        when(stats.getEnergyDamageTakenMult()).thenReturn(damageTakenMult);
        when(stats.getEnergyShieldDamageTakenMult()).thenReturn(shieldDamageTakenMult);

        DefenseSkillEffect.ENERGY_DAMAGE_TAKEN_PERCENT.apply(stats, "mod_id", -10f);

        verify(damageTakenMult).modifyPercent("mod_id", -10f);
        verify(shieldDamageTakenMult).modifyPercent("mod_id", -10f);
    }

    @Test
    void describeUsesIncreasesForAPositiveBidirectionalMagnitude() {
        assertEquals("Increases peak combat readiness duration by 20%.", MiscSkillEffect.PEAK_CR_DURATION_PERCENT.describe(20f));
    }

    @Test
    void describeUsesDecreasesForANegativeBidirectionalMagnitude() {
        assertEquals("Decreases peak combat readiness duration by 20%.", MiscSkillEffect.PEAK_CR_DURATION_PERCENT.describe(-20f));
    }

    @Test
    void fighterBaysFlatModifiesTheNumFighterBaysStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat numFighterBays = mock(MutableStat.class);
        when(stats.getNumFighterBays()).thenReturn(numFighterBays);

        FighterSkillEffect.FIGHTER_BAYS_FLAT.apply(stats, "mod_id", 1f);

        verify(numFighterBays).modifyFlat("mod_id", 1f);
    }

    @Test
    void fighterBaysFlatDescribesTheBayCountOnly() {
        assertEquals("Increases number of fighter bays by 1.", FighterSkillEffect.FIGHTER_BAYS_FLAT.describe(1f));
    }

    @Test
    void fighterBaysFlatHasADeallocationWarning() {
        assertEquals("Cannot be unallocated without at least 1 empty fighter bay.",
                FighterSkillEffect.FIGHTER_BAYS_FLAT.deallocationWarning(1f));
    }

    @Test
    void mostEffectsHaveNoDeallocationWarning() {
        assertNull(DefenseSkillEffect.HULL_PERCENT.deallocationWarning(10f));
    }

    @Test
    void fighterBaysFlatBlocksDeallocationWhenNoEmptyBayWouldRemain() {
        FleetMemberAPI member = mock(FleetMemberAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(member.getVariant()).thenReturn(variant);
        when(variant.getFittedWings()).thenReturn(List.of("wing_1"));
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(member.getStats()).thenReturn(stats);
        MutableStat numFighterBays = mock(MutableStat.class);
        when(stats.getNumFighterBays()).thenReturn(numFighterBays);
        when(numFighterBays.getModifiedValue()).thenReturn(1f);

        String reason = FighterSkillEffect.FIGHTER_BAYS_FLAT.blockDeallocationReason(member, 1f);

        assertEquals("Remove a fighter wing first - not enough empty fighter bays without this skill.", reason);
    }

    @Test
    void fighterBaysFlatAllowsDeallocationWhenAnEmptyBayWouldRemain() {
        FleetMemberAPI member = mock(FleetMemberAPI.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(member.getVariant()).thenReturn(variant);
        when(variant.getFittedWings()).thenReturn(List.of("wing_1"));
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(member.getStats()).thenReturn(stats);
        MutableStat numFighterBays = mock(MutableStat.class);
        when(stats.getNumFighterBays()).thenReturn(numFighterBays);
        when(numFighterBays.getModifiedValue()).thenReturn(2f);

        String reason = FighterSkillEffect.FIGHTER_BAYS_FLAT.blockDeallocationReason(member, 1f);

        assertNull(reason);
    }

    @Test
    void mostEffectsNeverBlockDeallocation() {
        assertNull(DefenseSkillEffect.HULL_PERCENT.blockDeallocationReason(mock(FleetMemberAPI.class), 10f));
    }

    @Test
    void fighterWeaponDamageDoesNotTouchTheCarrierStatsDirectly() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);

        FighterSkillEffect.FIGHTER_WEAPON_DAMAGE_PERCENT.apply(stats, "mod_id", 15f);

        verifyNoInteractions(stats);
    }

    @Test
    void fighterWeaponDamageModifiesTheFighterSOwnWeaponDamageStats() {
        ShipAPI fighter = mock(ShipAPI.class);
        ShipAPI parentShip = mock(ShipAPI.class);
        MutableShipStatsAPI fighterStats = mock(MutableShipStatsAPI.class);
        when(fighter.getMutableStats()).thenReturn(fighterStats);
        MutableStat ballistic = mock(MutableStat.class);
        MutableStat missile = mock(MutableStat.class);
        MutableStat energy = mock(MutableStat.class);
        MutableStat beam = mock(MutableStat.class);
        when(fighterStats.getBallisticWeaponDamageMult()).thenReturn(ballistic);
        when(fighterStats.getMissileWeaponDamageMult()).thenReturn(missile);
        when(fighterStats.getEnergyWeaponDamageMult()).thenReturn(energy);
        when(fighterStats.getBeamWeaponDamageMult()).thenReturn(beam);

        FighterSkillEffect.FIGHTER_WEAPON_DAMAGE_PERCENT.applyToFighterSpawnedByShip(fighter, parentShip, "mod_id", 15f);

        verify(ballistic).modifyPercent("mod_id", 15f);
        verify(missile).modifyPercent("mod_id", 15f);
        verify(energy).modifyPercent("mod_id", 15f);
        verify(beam).modifyPercent("mod_id", 15f);
    }

    @Test
    void fighterTopSpeedDoesNotTouchTheCarrierStatsDirectly() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);

        FighterSkillEffect.FIGHTER_TOP_SPEED_PERCENT.apply(stats, "mod_id", 15f);

        verifyNoInteractions(stats);
    }

    @Test
    void fighterTopSpeedModifiesTheFighterSOwnMaxSpeedStat() {
        ShipAPI fighter = mock(ShipAPI.class);
        ShipAPI parentShip = mock(ShipAPI.class);
        MutableShipStatsAPI fighterStats = mock(MutableShipStatsAPI.class);
        when(fighter.getMutableStats()).thenReturn(fighterStats);
        MutableStat maxSpeed = mock(MutableStat.class);
        when(fighterStats.getMaxSpeed()).thenReturn(maxSpeed);

        FighterSkillEffect.FIGHTER_TOP_SPEED_PERCENT.applyToFighterSpawnedByShip(fighter, parentShip, "mod_id", 15f);

        verify(maxSpeed).modifyPercent("mod_id", 15f);
    }

    @Test
    void removeAllFighterBaysZeroesOutTheFinalBayCountRegardlessOfOtherModifiers() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat numFighterBays = mock(MutableStat.class);
        when(stats.getNumFighterBays()).thenReturn(numFighterBays);

        FighterSkillEffect.REMOVE_ALL_FIGHTER_BAYS.apply(stats, "mod_id", 0f);

        verify(numFighterBays).modifyMult("mod_id", 0f);
        verify(numFighterBays, never()).modifyFlat(any(), anyFloat());
    }

    @Test
    void cargoCapacityPerFighterBayScalesWithTheShipSOwnBayCount() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat numFighterBays = mock(MutableStat.class);
        when(stats.getNumFighterBays()).thenReturn(numFighterBays);
        when(numFighterBays.getBaseValue()).thenReturn(3f);
        StatBonus cargoMod = mock(StatBonus.class);
        when(stats.getCargoMod()).thenReturn(cargoMod);

        LogisticsSkillEffect.CARGO_CAPACITY_PER_FIGHTER_BAY.apply(stats, "mod_id", 50f);

        verify(cargoMod).modifyFlat("mod_id", 150f);
    }

    @Test
    void minCrewPercentPerFighterBayScalesWithBayCount() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat numFighterBays = mock(MutableStat.class);
        when(stats.getNumFighterBays()).thenReturn(numFighterBays);
        when(numFighterBays.getBaseValue()).thenReturn(2f);
        StatBonus minCrewMod = mock(StatBonus.class);
        when(stats.getMinCrewMod()).thenReturn(minCrewMod);

        LogisticsSkillEffect.MIN_CREW_PERCENT_PER_FIGHTER_BAY.apply(stats, "mod_id", -20f);

        verify(minCrewMod).modifyPercent("mod_id", -40f);
    }

    @Test
    void minCrewPercentPerFighterBayIsCappedAtNegativeEighty() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat numFighterBays = mock(MutableStat.class);
        when(stats.getNumFighterBays()).thenReturn(numFighterBays);
        when(numFighterBays.getBaseValue()).thenReturn(6f);
        StatBonus minCrewMod = mock(StatBonus.class);
        when(stats.getMinCrewMod()).thenReturn(minCrewMod);

        LogisticsSkillEffect.MIN_CREW_PERCENT_PER_FIGHTER_BAY.apply(stats, "mod_id", -20f);

        verify(minCrewMod).modifyPercent("mod_id", -80f);
    }

    @Test
    void removeShieldSetsShieldTypeToNoneAfterShipCreation() {
        ShipAPI ship = mock(ShipAPI.class);

        ShieldSkillEffect.REMOVE_SHIELD.applyAfterShipCreation(ship, "mod_id", 0f);

        verify(ship).setShield(com.fs.starfarer.api.combat.ShieldAPI.ShieldType.NONE, 0f, 1f, 1f);
    }

    @Test
    void createFrontShieldIfNoneInstallsAShieldWhenTheShipHasNone() {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.getShield()).thenReturn(null);

        ShieldSkillEffect.CREATE_FRONT_SHIELD_IF_NONE.applyAfterShipCreation(ship, "mod_id", 0f);

        verify(ship).setShield(com.fs.starfarer.api.combat.ShieldAPI.ShieldType.FRONT, 0.5f, 1.2f, 90f);
    }

    @Test
    void createFrontShieldIfNoneDoesNothingWhenTheShipAlreadyHasAShield() {
        ShipAPI ship = mock(ShipAPI.class);
        com.fs.starfarer.api.combat.ShieldAPI existingShield = mock(com.fs.starfarer.api.combat.ShieldAPI.class);
        when(ship.getShield()).thenReturn(existingShield);

        ShieldSkillEffect.CREATE_FRONT_SHIELD_IF_NONE.applyAfterShipCreation(ship, "mod_id", 0f);

        verify(ship, never()).setShield(any(), anyFloat(), anyFloat(), anyFloat());
    }

    @Test
    void convertShieldToFrontChangesAnExistingShieldSType() {
        ShipAPI ship = mock(ShipAPI.class);
        com.fs.starfarer.api.combat.ShieldAPI shield = mock(com.fs.starfarer.api.combat.ShieldAPI.class);
        when(ship.getShield()).thenReturn(shield);

        ShieldSkillEffect.CONVERT_SHIELD_TO_FRONT.applyAfterShipCreation(ship, "mod_id", 0f);

        verify(shield).setType(com.fs.starfarer.api.combat.ShieldAPI.ShieldType.FRONT);
    }

    @Test
    void convertShieldToFrontDoesNothingWhenTheShipHasNoShield() {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.getShield()).thenReturn(null);

        assertDoesNotThrow(() -> ShieldSkillEffect.CONVERT_SHIELD_TO_FRONT.applyAfterShipCreation(ship, "mod_id", 0f));
    }

    @Test
    void convertShieldToOmniChangesAnExistingShieldSType() {
        ShipAPI ship = mock(ShipAPI.class);
        com.fs.starfarer.api.combat.ShieldAPI shield = mock(com.fs.starfarer.api.combat.ShieldAPI.class);
        when(ship.getShield()).thenReturn(shield);

        ShieldSkillEffect.CONVERT_SHIELD_TO_OMNI.applyAfterShipCreation(ship, "mod_id", 0f);

        verify(shield).setType(com.fs.starfarer.api.combat.ShieldAPI.ShieldType.OMNI);
    }

    @Test
    void resolveDisplayShieldTypeConvertsAnExistingShieldToFront() {
        com.fs.starfarer.api.combat.ShieldAPI.ShieldType result = ShieldSkillEffect.resolveDisplayShieldType(
                com.fs.starfarer.api.combat.ShieldAPI.ShieldType.OMNI, List.of(ShieldSkillEffect.CONVERT_SHIELD_TO_FRONT));

        assertEquals(com.fs.starfarer.api.combat.ShieldAPI.ShieldType.FRONT, result);
    }

    @Test
    void resolveDisplayShieldTypeConvertsAnExistingShieldToOmni() {
        com.fs.starfarer.api.combat.ShieldAPI.ShieldType result = ShieldSkillEffect.resolveDisplayShieldType(
                com.fs.starfarer.api.combat.ShieldAPI.ShieldType.FRONT, List.of(ShieldSkillEffect.CONVERT_SHIELD_TO_OMNI));

        assertEquals(com.fs.starfarer.api.combat.ShieldAPI.ShieldType.OMNI, result);
    }

    @Test
    void resolveDisplayShieldTypeDoesNothingWhenTheHullHasNoShieldToConvert() {
        com.fs.starfarer.api.combat.ShieldAPI.ShieldType result = ShieldSkillEffect.resolveDisplayShieldType(
                com.fs.starfarer.api.combat.ShieldAPI.ShieldType.NONE, List.of(ShieldSkillEffect.CONVERT_SHIELD_TO_FRONT));

        assertEquals(com.fs.starfarer.api.combat.ShieldAPI.ShieldType.NONE, result);
    }

    @Test
    void resolveDisplayShieldTypeRemovesTheShield() {
        com.fs.starfarer.api.combat.ShieldAPI.ShieldType result = ShieldSkillEffect.resolveDisplayShieldType(
                com.fs.starfarer.api.combat.ShieldAPI.ShieldType.FRONT, List.of(ShieldSkillEffect.REMOVE_SHIELD));

        assertEquals(com.fs.starfarer.api.combat.ShieldAPI.ShieldType.NONE, result);
    }

    @Test
    void resolveDisplayShieldTypeCreatesAFrontShieldWhenTheHullHasNone() {
        com.fs.starfarer.api.combat.ShieldAPI.ShieldType result = ShieldSkillEffect.resolveDisplayShieldType(
                com.fs.starfarer.api.combat.ShieldAPI.ShieldType.NONE, List.of(ShieldSkillEffect.CREATE_FRONT_SHIELD_IF_NONE));

        assertEquals(com.fs.starfarer.api.combat.ShieldAPI.ShieldType.FRONT, result);
    }

    @Test
    void resolveDisplayShieldTypeDoesNotOverrideAnExistingShieldWithTheMakeshiftOne() {
        com.fs.starfarer.api.combat.ShieldAPI.ShieldType result = ShieldSkillEffect.resolveDisplayShieldType(
                com.fs.starfarer.api.combat.ShieldAPI.ShieldType.OMNI, List.of(ShieldSkillEffect.CREATE_FRONT_SHIELD_IF_NONE));

        assertEquals(com.fs.starfarer.api.combat.ShieldAPI.ShieldType.OMNI, result);
    }

    @Test
    void resolveDisplayShieldTypeAppliesEffectsInAllocationOrder() {
        com.fs.starfarer.api.combat.ShieldAPI.ShieldType result = ShieldSkillEffect.resolveDisplayShieldType(
                com.fs.starfarer.api.combat.ShieldAPI.ShieldType.NONE,
                List.of(ShieldSkillEffect.CREATE_FRONT_SHIELD_IF_NONE, ShieldSkillEffect.CONVERT_SHIELD_TO_OMNI));

        assertEquals(com.fs.starfarer.api.combat.ShieldAPI.ShieldType.OMNI, result);
    }

    @Test
    void resolveDisplayShieldTypeIgnoresUnrelatedEffects() {
        com.fs.starfarer.api.combat.ShieldAPI.ShieldType result = ShieldSkillEffect.resolveDisplayShieldType(
                com.fs.starfarer.api.combat.ShieldAPI.ShieldType.FRONT, List.of(ShieldSkillEffect.SHIELD_ARC_PERCENT));

        assertEquals(com.fs.starfarer.api.combat.ShieldAPI.ShieldType.FRONT, result);
    }

    private static FleetMemberAPI mockMemberWithShieldType(com.fs.starfarer.api.combat.ShieldAPI.ShieldType shieldType) {
        FleetMemberAPI member = mock(FleetMemberAPI.class);
        ShipHullSpecAPI hullSpec = mock(ShipHullSpecAPI.class);
        when(member.getHullSpec()).thenReturn(hullSpec);
        when(hullSpec.getShieldType()).thenReturn(shieldType);
        return member;
    }

    @Test
    void convertShieldToFrontBlocksAllocationWhenShipAlreadyHasFrontShields() {
        FleetMemberAPI member = mockMemberWithShieldType(com.fs.starfarer.api.combat.ShieldAPI.ShieldType.FRONT);

        String reason = ShieldSkillEffect.CONVERT_SHIELD_TO_FRONT.blockAllocationReason(member, 0f, List.of());

        assertEquals("Ship already has front shields.", reason);
    }

    @Test
    void convertShieldToFrontAllowsAllocationWhenShipHasOmniShields() {
        FleetMemberAPI member = mockMemberWithShieldType(com.fs.starfarer.api.combat.ShieldAPI.ShieldType.OMNI);

        String reason = ShieldSkillEffect.CONVERT_SHIELD_TO_FRONT.blockAllocationReason(member, 0f, List.of());

        assertNull(reason);
    }

    @Test
    void convertShieldToOmniBlocksAllocationWhenShipAlreadyHasOmniShields() {
        FleetMemberAPI member = mockMemberWithShieldType(com.fs.starfarer.api.combat.ShieldAPI.ShieldType.OMNI);

        String reason = ShieldSkillEffect.CONVERT_SHIELD_TO_OMNI.blockAllocationReason(member, 0f, List.of());

        assertEquals("Ship already has omni-directional shields.", reason);
    }

    @Test
    void convertShieldToOmniAllowsAllocationWhenShipHasFrontShields() {
        FleetMemberAPI member = mockMemberWithShieldType(com.fs.starfarer.api.combat.ShieldAPI.ShieldType.FRONT);

        String reason = ShieldSkillEffect.CONVERT_SHIELD_TO_OMNI.blockAllocationReason(member, 0f, List.of());

        assertNull(reason);
    }

    @Test
    void removeShieldBlocksAllocationWhenShipHasNoShields() {
        FleetMemberAPI member = mockMemberWithShieldType(com.fs.starfarer.api.combat.ShieldAPI.ShieldType.NONE);

        String reason = ShieldSkillEffect.REMOVE_SHIELD.blockAllocationReason(member, 0f, List.of());

        assertEquals("Ship has no shields.", reason);
    }

    @Test
    void removeShieldAllowsAllocationWhenShipHasShields() {
        FleetMemberAPI member = mockMemberWithShieldType(com.fs.starfarer.api.combat.ShieldAPI.ShieldType.FRONT);

        String reason = ShieldSkillEffect.REMOVE_SHIELD.blockAllocationReason(member, 0f, List.of());

        assertNull(reason);
    }

    @Test
    void convertShieldToFrontAccountsForAlreadyAllocatedShieldEffects() {
        FleetMemberAPI member = mockMemberWithShieldType(com.fs.starfarer.api.combat.ShieldAPI.ShieldType.NONE);

        String reason = ShieldSkillEffect.CONVERT_SHIELD_TO_FRONT.blockAllocationReason(
                member, 0f, List.of(ShieldSkillEffect.CREATE_FRONT_SHIELD_IF_NONE));

        assertEquals("Ship already has front shields.", reason);
    }

    @Test
    void minCrewPerFighterBayScalesWithTheShipSOwnBayCount() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat numFighterBays = mock(MutableStat.class);
        when(stats.getNumFighterBays()).thenReturn(numFighterBays);
        when(numFighterBays.getBaseValue()).thenReturn(3f);
        StatBonus minCrewMod = mock(StatBonus.class);
        when(stats.getMinCrewMod()).thenReturn(minCrewMod);

        LogisticsSkillEffect.MIN_CREW_PER_FIGHTER_BAY.apply(stats, "mod_id", 20f);

        verify(minCrewMod).modifyFlat("mod_id", 60f);
    }

    @Test
    void fighterReplacementDecayMultModifiesTheDecreaseMultStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        com.fs.starfarer.api.util.DynamicStatsAPI dynamic = mock(com.fs.starfarer.api.util.DynamicStatsAPI.class);
        when(stats.getDynamic()).thenReturn(dynamic);
        MutableStat decreaseMult = mock(MutableStat.class);
        when(dynamic.getStat("replacement_rate_decrease_mult")).thenReturn(decreaseMult);

        FighterSkillEffect.FIGHTER_REPLACEMENT_DECAY_PERCENT.apply(stats, "mod_id", -15f);

        verify(decreaseMult).modifyPercent("mod_id", -15f);
    }

    @Test
    void fighterReplacementRecoveryMultModifiesTheIncreaseMultStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        com.fs.starfarer.api.util.DynamicStatsAPI dynamic = mock(com.fs.starfarer.api.util.DynamicStatsAPI.class);
        when(stats.getDynamic()).thenReturn(dynamic);
        MutableStat increaseMult = mock(MutableStat.class);
        when(dynamic.getStat("replacement_rate_increase_mult")).thenReturn(increaseMult);

        FighterSkillEffect.FIGHTER_REPLACEMENT_RECOVERY_PERCENT.apply(stats, "mod_id", 25f);

        verify(increaseMult).modifyPercent("mod_id", 25f);
    }

    @Test
    void fighterPdDamageBonusDoesNotTouchTheCarrierStatsDirectly() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);

        FighterSkillEffect.FIGHTER_PD_DAMAGE_BONUS_PERCENT.apply(stats, "mod_id", 50f);

        verifyNoInteractions(stats);
    }

    @Test
    void fighterPdDamageBonusModifiesTheFighterSOwnDamageToFightersAndMissiles() {
        ShipAPI fighter = mock(ShipAPI.class);
        ShipAPI parentShip = mock(ShipAPI.class);
        MutableShipStatsAPI fighterStats = mock(MutableShipStatsAPI.class);
        when(fighter.getMutableStats()).thenReturn(fighterStats);
        MutableStat damageToFighters = mock(MutableStat.class);
        MutableStat damageToMissiles = mock(MutableStat.class);
        when(fighterStats.getDamageToFighters()).thenReturn(damageToFighters);
        when(fighterStats.getDamageToMissiles()).thenReturn(damageToMissiles);

        FighterSkillEffect.FIGHTER_PD_DAMAGE_BONUS_PERCENT.applyToFighterSpawnedByShip(fighter, parentShip, "mod_id", 50f);

        verify(damageToFighters).modifyPercent("mod_id", 50f);
        verify(damageToMissiles).modifyPercent("mod_id", 50f);
    }

    @Test
    void beamDamageHardFluxPercentModifiesTheDynamicStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        com.fs.starfarer.api.util.DynamicStatsAPI dynamic = mock(com.fs.starfarer.api.util.DynamicStatsAPI.class);
        when(stats.getDynamic()).thenReturn(dynamic);
        StatBonus hardFluxPercent = mock(StatBonus.class);
        when(dynamic.getMod("exiledSector_beamDamageHardFluxPercent")).thenReturn(hardFluxPercent);

        ShieldSkillEffect.BEAM_DAMAGE_HARD_FLUX_PERCENT.apply(stats, "mod_id", 50f);

        verify(hardFluxPercent).modifyFlat("mod_id", 50f);
    }

    @Test
    void beamDamageHardFluxPercentAddsAListenerOnce() {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.hasListenerOfClass(any())).thenReturn(false);

        ShieldSkillEffect.BEAM_DAMAGE_HARD_FLUX_PERCENT.applyAfterShipCreation(ship, "mod_id", 50f);

        verify(ship).addListener(any(DamageDealtModifier.class));
    }

    @Test
    void beamDamageHardFluxPercentDoesNotDuplicateTheListener() {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.hasListenerOfClass(any())).thenReturn(true);

        ShieldSkillEffect.BEAM_DAMAGE_HARD_FLUX_PERCENT.applyAfterShipCreation(ship, "mod_id", 50f);

        verify(ship, never()).addListener(any());
    }

    private DamageDealtModifier captureBeamHardFluxListener(float magnitude) {
        ShipAPI ship = mock(ShipAPI.class);
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        com.fs.starfarer.api.util.DynamicStatsAPI dynamic = mock(com.fs.starfarer.api.util.DynamicStatsAPI.class);
        when(ship.getMutableStats()).thenReturn(stats);
        when(stats.getDynamic()).thenReturn(dynamic);
        when(dynamic.getValue("exiledSector_beamDamageHardFluxPercent", 0f)).thenReturn(magnitude);
        when(ship.hasListenerOfClass(any())).thenReturn(false);
        ShieldSkillEffect.BEAM_DAMAGE_HARD_FLUX_PERCENT.applyAfterShipCreation(ship, "mod_id", magnitude);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(ship).addListener(captor.capture());
        return (DamageDealtModifier) captor.getValue();
    }

    @Test
    void beamHardFluxListenerConvertsAPortionOfBeamShieldDamageToHardFlux() {
        DamageDealtModifier listener = captureBeamHardFluxListener(50f);

        BeamAPI beam = mock(BeamAPI.class);
        ShipAPI target = mock(ShipAPI.class);
        DamageAPI damage = mock(DamageAPI.class);
        FluxTrackerAPI fluxTracker = mock(FluxTrackerAPI.class);
        when(target.getFluxTracker()).thenReturn(fluxTracker);
        when(damage.getDamage()).thenReturn(100f);
        when(damage.computeFluxDealt(50f)).thenReturn(50f);

        listener.modifyDamageDealt(beam, target, damage, mock(Vector2f.class), true);

        verify(damage).setDamage(50f);
        verify(fluxTracker).increaseFlux(50f, true);
    }

    @Test
    void beamHardFluxListenerIgnoresNonShieldHits() {
        DamageDealtModifier listener = captureBeamHardFluxListener(50f);

        BeamAPI beam = mock(BeamAPI.class);
        ShipAPI target = mock(ShipAPI.class);
        DamageAPI damage = mock(DamageAPI.class);

        listener.modifyDamageDealt(beam, target, damage, mock(Vector2f.class), false);

        verify(damage, never()).setDamage(anyFloat());
        verify(target, never()).getFluxTracker();
    }

    @Test
    void beamHardFluxListenerIgnoresNonBeamSources() {
        DamageDealtModifier listener = captureBeamHardFluxListener(50f);

        ShipAPI target = mock(ShipAPI.class);
        DamageAPI damage = mock(DamageAPI.class);

        listener.modifyDamageDealt(new Object(), target, damage, mock(Vector2f.class), true);

        verify(damage, never()).setDamage(anyFloat());
        verify(target, never()).getFluxTracker();
    }

    @Test
    void beamHardFluxListenerIgnoresNonShipTargets() {
        DamageDealtModifier listener = captureBeamHardFluxListener(50f);

        BeamAPI beam = mock(BeamAPI.class);
        CombatEntityAPI target = mock(CombatEntityAPI.class);
        DamageAPI damage = mock(DamageAPI.class);

        listener.modifyDamageDealt(beam, target, damage, mock(Vector2f.class), true);

        verify(damage, never()).setDamage(anyFloat());
    }

    @Test
    void phaseAnchorEmergencyDiveModifiesTheDynamicCrPenaltyStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        com.fs.starfarer.api.util.DynamicStatsAPI dynamic = mock(com.fs.starfarer.api.util.DynamicStatsAPI.class);
        when(stats.getDynamic()).thenReturn(dynamic);
        StatBonus crPenalty = mock(StatBonus.class);
        when(dynamic.getMod("exiledSector_phaseAnchorCrPenaltyPercent")).thenReturn(crPenalty);

        PhaseSkillEffect.PHASE_ANCHOR_EMERGENCY_DIVE.apply(stats, "mod_id", 100f);

        verify(crPenalty).modifyFlat("mod_id", 100f);
    }

    @Test
    void phaseAnchorEmergencyDiveAddsAListenerOnce() {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.hasListenerOfClass(any())).thenReturn(false);

        PhaseSkillEffect.PHASE_ANCHOR_EMERGENCY_DIVE.applyAfterShipCreation(ship, "mod_id", 100f);

        verify(ship).addListener(any(HullDamageAboutToBeTakenListener.class));
    }

    @Test
    void phaseAnchorEmergencyDiveDoesNotDuplicateTheListener() {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.hasListenerOfClass(any())).thenReturn(true);

        PhaseSkillEffect.PHASE_ANCHOR_EMERGENCY_DIVE.applyAfterShipCreation(ship, "mod_id", 100f);

        verify(ship, never()).addListener(any());
    }

    private Object capturePhaseAnchorDiveListener(ShipAPI ship, float magnitude) {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        com.fs.starfarer.api.util.DynamicStatsAPI dynamic = mock(com.fs.starfarer.api.util.DynamicStatsAPI.class);
        when(ship.getMutableStats()).thenReturn(stats);
        when(stats.getDynamic()).thenReturn(dynamic);
        when(dynamic.getValue("exiledSector_phaseAnchorCrPenaltyPercent", 0f)).thenReturn(magnitude);
        when(ship.hasListenerOfClass(any())).thenReturn(false);
        PhaseSkillEffect.PHASE_ANCHOR_EMERGENCY_DIVE.applyAfterShipCreation(ship, "mod_id", magnitude);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(ship).addListener(captor.capture());
        return captor.getValue();
    }

    @Test
    void phaseAnchorDiveIgnoresNonLethalDamage() {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.getHitpoints()).thenReturn(100f);
        HullDamageAboutToBeTakenListener listener = (HullDamageAboutToBeTakenListener) capturePhaseAnchorDiveListener(ship, 100f);

        boolean saved = listener.notifyAboutToTakeHullDamage(new Object(), ship, mock(Vector2f.class), 50f);

        assertFalse(saved);
        verify(ship, never()).setHitpoints(anyFloat());
    }

    @Test
    void phaseAnchorDiveSavesTheShipOnLethalDamageAndAppliesTheCrPenalty() {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.getHitpoints()).thenReturn(100f);
        when(ship.getCurrentCR()).thenReturn(20f);
        FleetMemberAPI member = mock(FleetMemberAPI.class);
        RepairTrackerAPI repairTracker = mock(RepairTrackerAPI.class);
        when(member.getDeployCost()).thenReturn(10f);
        when(member.getRepairTracker()).thenReturn(repairTracker);
        when(ship.getFleetMember()).thenReturn(member);
        HullDamageAboutToBeTakenListener listener = (HullDamageAboutToBeTakenListener) capturePhaseAnchorDiveListener(ship, 100f);

        Map<String, Object> customData = new HashMap<>();
        try (MockedStatic<Global> globalMock = Mockito.mockStatic(Global.class)) {
            CombatEngineAPI engine = mock(CombatEngineAPI.class);
            globalMock.when(Global::getCombatEngine).thenReturn(engine);
            when(engine.getCustomData()).thenReturn(customData);

            boolean saved = listener.notifyAboutToTakeHullDamage(new Object(), ship, mock(Vector2f.class), 150f);

            assertTrue(saved);
        }
        verify(ship).setHitpoints(1f);
        verify(repairTracker).applyCREvent(-10f, "Emergency phase dive");
        assertEquals(Boolean.TRUE, customData.get("phaseAnchor_canDive"));
    }

    @Test
    void phaseAnchorDiveTreatsAMissingFleetMemberAsZeroDeployCost() {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.getHitpoints()).thenReturn(100f);
        when(ship.getCurrentCR()).thenReturn(0f);
        when(ship.getFleetMember()).thenReturn(null);
        HullDamageAboutToBeTakenListener listener = (HullDamageAboutToBeTakenListener) capturePhaseAnchorDiveListener(ship, 100f);

        try (MockedStatic<Global> globalMock = Mockito.mockStatic(Global.class)) {
            CombatEngineAPI engine = mock(CombatEngineAPI.class);
            globalMock.when(Global::getCombatEngine).thenReturn(engine);
            when(engine.getCustomData()).thenReturn(new HashMap<>());

            boolean saved = listener.notifyAboutToTakeHullDamage(new Object(), ship, mock(Vector2f.class), 150f);

            assertTrue(saved);
        }
        verify(ship).setHitpoints(1f);
    }

    @Test
    void phaseAnchorDiveIsBlockedWhenAnotherShipAlreadyDoveThisBattle() {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.getHitpoints()).thenReturn(100f);
        HullDamageAboutToBeTakenListener listener = (HullDamageAboutToBeTakenListener) capturePhaseAnchorDiveListener(ship, 100f);

        Map<String, Object> customData = new HashMap<>();
        customData.put("phaseAnchor_canDive", Boolean.TRUE);
        try (MockedStatic<Global> globalMock = Mockito.mockStatic(Global.class)) {
            CombatEngineAPI engine = mock(CombatEngineAPI.class);
            globalMock.when(Global::getCombatEngine).thenReturn(engine);
            when(engine.getCustomData()).thenReturn(customData);

            boolean saved = listener.notifyAboutToTakeHullDamage(new Object(), ship, mock(Vector2f.class), 150f);

            assertFalse(saved);
        }
        verify(ship, never()).setHitpoints(anyFloat());
    }

    @Test
    void phaseAnchorDiveIsBlockedByInsufficientCombatReadiness() {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.getHitpoints()).thenReturn(100f);
        when(ship.getCurrentCR()).thenReturn(5f);
        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getDeployCost()).thenReturn(10f);
        when(ship.getFleetMember()).thenReturn(member);
        HullDamageAboutToBeTakenListener listener = (HullDamageAboutToBeTakenListener) capturePhaseAnchorDiveListener(ship, 100f);

        try (MockedStatic<Global> globalMock = Mockito.mockStatic(Global.class)) {
            CombatEngineAPI engine = mock(CombatEngineAPI.class);
            globalMock.when(Global::getCombatEngine).thenReturn(engine);
            when(engine.getCustomData()).thenReturn(new HashMap<>());

            boolean saved = listener.notifyAboutToTakeHullDamage(new Object(), ship, mock(Vector2f.class), 150f);

            assertFalse(saved);
        }
        verify(ship, never()).setHitpoints(anyFloat());
    }

    @Test
    void phaseAnchorDiveAdvanceDoesNothingWhenNotDiving() {
        ShipAPI ship = mock(ShipAPI.class);
        AdvanceableListener listener = (AdvanceableListener) capturePhaseAnchorDiveListener(ship, 100f);

        listener.advance(0.1f);

        verify(ship, never()).setRetreating(true, false);
    }

    @Test
    void beamSplitTargetsFlatModifiesTheDynamicStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        com.fs.starfarer.api.util.DynamicStatsAPI dynamic = mock(com.fs.starfarer.api.util.DynamicStatsAPI.class);
        when(stats.getDynamic()).thenReturn(dynamic);
        StatBonus splitTargets = mock(StatBonus.class);
        when(dynamic.getMod("exiledSector_beamSplitTargets")).thenReturn(splitTargets);

        CombatSkillEffect.BEAM_SPLIT_TARGETS_FLAT.apply(stats, "mod_id", 1f);

        verify(splitTargets).modifyFlat("mod_id", 1f);
    }

    @Test
    void beamSplitTargetsFlatDescribesTheSplitCount() {
        assertEquals("Beam weapon hits split their damage evenly across the target and up to 1 additional nearby enemy. "
                        + "The target acquisition range is half the beam weapon's range.",
                CombatSkillEffect.BEAM_SPLIT_TARGETS_FLAT.describe(1f));
        assertEquals("Beam weapon hits split their damage evenly across the target and up to 3 additional nearby enemies. "
                        + "The target acquisition range is half the beam weapon's range.",
                CombatSkillEffect.BEAM_SPLIT_TARGETS_FLAT.describe(3f));
    }

    @Test
    void beamSplitTargetsFlatAddsAListenerOnce() {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.hasListenerOfClass(any())).thenReturn(false);

        CombatSkillEffect.BEAM_SPLIT_TARGETS_FLAT.applyAfterShipCreation(ship, "mod_id", 1f);

        verify(ship).addListener(any(DamageDealtModifier.class));
    }

    @Test
    void beamSplitTargetsFlatDoesNotDuplicateTheListener() {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.hasListenerOfClass(any())).thenReturn(true);

        CombatSkillEffect.BEAM_SPLIT_TARGETS_FLAT.applyAfterShipCreation(ship, "mod_id", 1f);

        verify(ship, never()).addListener(any());
    }

    private DamageDealtModifier captureBeamSplitListener(ShipAPI ship) {
        when(ship.hasListenerOfClass(any())).thenReturn(false);
        CombatSkillEffect.BEAM_SPLIT_TARGETS_FLAT.applyAfterShipCreation(ship, "mod_id", 1f);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(ship).addListener(captor.capture());
        return (DamageDealtModifier) captor.getValue();
    }

    private ShipAPI mockBeamSplitEnemy(int owner, Vector2f location) {
        ShipAPI enemy = mock(ShipAPI.class);
        when(enemy.getOwner()).thenReturn(owner);
        when(enemy.isAlive()).thenReturn(true);
        when(enemy.isHulk()).thenReturn(false);
        when(enemy.getLocation()).thenReturn(location);
        return enemy;
    }

    @Test
    void beamSplitListenerIgnoresNonBeamDamageSources() {
        ShipAPI ship = mock(ShipAPI.class);
        DamageDealtModifier listener = captureBeamSplitListener(ship);

        ShipAPI target = mock(ShipAPI.class);
        DamageAPI damage = mock(DamageAPI.class);

        listener.modifyDamageDealt(new Object(), target, damage, new Vector2f(0f, 0f), false);

        verify(damage, never()).setDamage(anyFloat());
    }

    @Test
    void beamSplitListenerIgnoresNonShipTargets() {
        ShipAPI ship = mock(ShipAPI.class);
        DamageDealtModifier listener = captureBeamSplitListener(ship);

        BeamAPI beam = mock(BeamAPI.class);
        CombatEntityAPI target = mock(CombatEntityAPI.class);
        DamageAPI damage = mock(DamageAPI.class);

        listener.modifyDamageDealt(beam, target, damage, new Vector2f(0f, 0f), false);

        verify(damage, never()).setDamage(anyFloat());
    }

    @Test
    void beamSplitListenerDoesNothingWhenNoSplitTargetsGranted() {
        ShipAPI ship = mock(ShipAPI.class);
        MutableShipStatsAPI shipStats = mock(MutableShipStatsAPI.class);
        com.fs.starfarer.api.util.DynamicStatsAPI dynamic = mock(com.fs.starfarer.api.util.DynamicStatsAPI.class);
        when(ship.getMutableStats()).thenReturn(shipStats);
        when(shipStats.getDynamic()).thenReturn(dynamic);
        when(dynamic.getValue("exiledSector_beamSplitTargets", 0f)).thenReturn(0f);

        DamageDealtModifier listener = captureBeamSplitListener(ship);

        BeamAPI beam = mock(BeamAPI.class);
        ShipAPI target = mock(ShipAPI.class);
        DamageAPI damage = mock(DamageAPI.class);

        listener.modifyDamageDealt(beam, target, damage, new Vector2f(0f, 0f), false);

        verify(damage, never()).setDamage(anyFloat());
    }

    private WeaponAPI mockBeamSplitWeapon(BeamAPI beam, float range) {
        WeaponAPI weapon = mock(WeaponAPI.class);
        when(weapon.getRange()).thenReturn(range);
        when(beam.getWeapon()).thenReturn(weapon);
        mockBeamSplitDerivedStats(weapon, 100f, 0f);
        return weapon;
    }

    private void mockBeamSplitDerivedStats(WeaponAPI weapon, float dps, float empPerSecond) {
        WeaponAPI.DerivedWeaponStatsAPI derivedStats = mock(WeaponAPI.DerivedWeaponStatsAPI.class);
        when(derivedStats.getDps()).thenReturn(dps);
        when(derivedStats.getEmpPerSecond()).thenReturn(empPerSecond);
        when(weapon.getDerivedStats()).thenReturn(derivedStats);
    }

    @Test
    void beamSplitListenerSplitsDamageAcrossNearbyEnemies() {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.getOwner()).thenReturn(0);
        MutableShipStatsAPI shipStats = mock(MutableShipStatsAPI.class);
        com.fs.starfarer.api.util.DynamicStatsAPI dynamic = mock(com.fs.starfarer.api.util.DynamicStatsAPI.class);
        when(ship.getMutableStats()).thenReturn(shipStats);
        when(shipStats.getDynamic()).thenReturn(dynamic);
        when(dynamic.getValue("exiledSector_beamSplitTargets", 0f)).thenReturn(2f);

        DamageDealtModifier listener = captureBeamSplitListener(ship);

        BeamAPI beam = mock(BeamAPI.class);
        when(beam.getWidth()).thenReturn(10f);
        when(beam.getCoreColor()).thenReturn(java.awt.Color.WHITE);
        when(beam.getFringeColor()).thenReturn(java.awt.Color.RED);
        mockBeamSplitWeapon(beam, 2000f);

        ShipAPI primaryTarget = mock(ShipAPI.class);
        when(primaryTarget.getOwner()).thenReturn(1);

        Vector2f point = new Vector2f(0f, 0f);
        ShipAPI enemy1 = mockBeamSplitEnemy(1, new Vector2f(100f, 0f));
        ShipAPI enemy2 = mockBeamSplitEnemy(1, new Vector2f(0f, 100f));

        DamageAPI damage = mock(DamageAPI.class);
        when(damage.getDamage()).thenReturn(90f);
        when(damage.getType()).thenReturn(DamageType.ENERGY);

        try (MockedStatic<Global> globalMock = Mockito.mockStatic(Global.class);
             MockedStatic<MagicFakeBeam> fakeBeamMock = Mockito.mockStatic(MagicFakeBeam.class);
             MockedStatic<MagicFakeBeamPlugin> fakeBeamPluginMock = Mockito.mockStatic(MagicFakeBeamPlugin.class)) {
            CombatEngineAPI engine = mock(CombatEngineAPI.class);
            globalMock.when(Global::getCombatEngine).thenReturn(engine);
            when(engine.getShips()).thenReturn(Arrays.asList(ship, primaryTarget, enemy1, enemy2));
            fakeBeamMock.when(() -> MagicFakeBeam.getShipCollisionPoint(any(), any(), any(), anyFloat()))
                    .thenAnswer(invocation -> ((ShipAPI) invocation.getArgument(2)).getLocation());

            listener.modifyDamageDealt(beam, primaryTarget, damage, point, false);

            verify(damage).setDamage(30f);
            verify(engine).applyDamage(eq(beam), eq(enemy1), any(Vector2f.class), eq(30f), eq(DamageType.ENERGY),
                    eq(0f), eq(false), eq(true), eq(ship), eq(false));
            verify(engine).applyDamage(eq(beam), eq(enemy2), any(Vector2f.class), eq(30f), eq(DamageType.ENERGY),
                    eq(0f), eq(false), eq(true), eq(ship), eq(false));
        }
    }

    @Test
    void beamSplitListenerDoesNotReSplitItsOwnSyntheticHits() {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.getOwner()).thenReturn(0);
        MutableShipStatsAPI shipStats = mock(MutableShipStatsAPI.class);
        com.fs.starfarer.api.util.DynamicStatsAPI dynamic = mock(com.fs.starfarer.api.util.DynamicStatsAPI.class);
        when(ship.getMutableStats()).thenReturn(shipStats);
        when(shipStats.getDynamic()).thenReturn(dynamic);
        when(dynamic.getValue("exiledSector_beamSplitTargets", 0f)).thenReturn(2f);

        DamageDealtModifier listener = captureBeamSplitListener(ship);

        BeamAPI beam = mock(BeamAPI.class);
        when(beam.getWidth()).thenReturn(10f);
        when(beam.getCoreColor()).thenReturn(java.awt.Color.WHITE);
        when(beam.getFringeColor()).thenReturn(java.awt.Color.RED);
        mockBeamSplitWeapon(beam, 2000f);

        ShipAPI primaryTarget = mock(ShipAPI.class);
        when(primaryTarget.getOwner()).thenReturn(1);

        Vector2f point = new Vector2f(0f, 0f);
        ShipAPI enemy1 = mockBeamSplitEnemy(1, new Vector2f(100f, 0f));
        ShipAPI enemy2 = mockBeamSplitEnemy(1, new Vector2f(0f, 100f));

        DamageAPI damage = mock(DamageAPI.class);
        when(damage.getDamage()).thenReturn(90f);
        when(damage.getType()).thenReturn(DamageType.ENERGY);

        try (MockedStatic<Global> globalMock = Mockito.mockStatic(Global.class);
             MockedStatic<MagicFakeBeam> fakeBeamMock = Mockito.mockStatic(MagicFakeBeam.class);
             MockedStatic<MagicFakeBeamPlugin> fakeBeamPluginMock = Mockito.mockStatic(MagicFakeBeamPlugin.class)) {
            CombatEngineAPI engine = mock(CombatEngineAPI.class);
            globalMock.when(Global::getCombatEngine).thenReturn(engine);
            when(engine.getShips()).thenReturn(Arrays.asList(ship, primaryTarget, enemy1, enemy2));
            fakeBeamMock.when(() -> MagicFakeBeam.getShipCollisionPoint(any(), any(), any(), anyFloat()))
                    .thenAnswer(invocation -> ((ShipAPI) invocation.getArgument(2)).getLocation());
            // Simulate the real engine re-invoking every DamageDealtModifier (including this
            // very listener) when our own applyDamage(beam, ...) calls go through.
            Mockito.doAnswer(invocation -> {
                DamageAPI splitDamage = mock(DamageAPI.class);
                when(splitDamage.getDamage()).thenReturn(invocation.getArgument(3));
                when(splitDamage.getType()).thenReturn(invocation.getArgument(4));
                listener.modifyDamageDealt(invocation.getArgument(0), invocation.getArgument(1), splitDamage,
                        invocation.getArgument(2), false);
                return null;
            }).when(engine).applyDamage(any(), any(), any(), anyFloat(), any(), anyFloat(), anyBoolean(), anyBoolean(), any(), anyBoolean());

            listener.modifyDamageDealt(beam, primaryTarget, damage, point, false);

            verify(engine, times(2)).applyDamage(eq(beam), any(), any(), anyFloat(), any(),
                    anyFloat(), anyBoolean(), anyBoolean(), eq(ship), anyBoolean());
        }
    }

    @Test
    void beamSplitListenerOnlySplitsAcrossEnemiesActuallyInRange() {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.getOwner()).thenReturn(0);
        MutableShipStatsAPI shipStats = mock(MutableShipStatsAPI.class);
        com.fs.starfarer.api.util.DynamicStatsAPI dynamic = mock(com.fs.starfarer.api.util.DynamicStatsAPI.class);
        when(ship.getMutableStats()).thenReturn(shipStats);
        when(shipStats.getDynamic()).thenReturn(dynamic);
        when(dynamic.getValue("exiledSector_beamSplitTargets", 0f)).thenReturn(2f);

        DamageDealtModifier listener = captureBeamSplitListener(ship);

        BeamAPI beam = mock(BeamAPI.class);
        when(beam.getWidth()).thenReturn(10f);
        when(beam.getCoreColor()).thenReturn(java.awt.Color.WHITE);
        when(beam.getFringeColor()).thenReturn(java.awt.Color.RED);
        mockBeamSplitWeapon(beam, 2000f);

        ShipAPI primaryTarget = mock(ShipAPI.class);
        when(primaryTarget.getOwner()).thenReturn(1);

        Vector2f point = new Vector2f(0f, 0f);
        ShipAPI enemyInRange = mockBeamSplitEnemy(1, new Vector2f(100f, 0f));
        ShipAPI enemyOutOfRange = mockBeamSplitEnemy(1, new Vector2f(5000f, 0f));

        DamageAPI damage = mock(DamageAPI.class);
        when(damage.getDamage()).thenReturn(90f);
        when(damage.getType()).thenReturn(DamageType.ENERGY);

        try (MockedStatic<Global> globalMock = Mockito.mockStatic(Global.class);
             MockedStatic<MagicFakeBeam> fakeBeamMock = Mockito.mockStatic(MagicFakeBeam.class);
             MockedStatic<MagicFakeBeamPlugin> fakeBeamPluginMock = Mockito.mockStatic(MagicFakeBeamPlugin.class)) {
            CombatEngineAPI engine = mock(CombatEngineAPI.class);
            globalMock.when(Global::getCombatEngine).thenReturn(engine);
            when(engine.getShips()).thenReturn(Arrays.asList(ship, primaryTarget, enemyInRange, enemyOutOfRange));
            fakeBeamMock.when(() -> MagicFakeBeam.getShipCollisionPoint(any(), any(), any(), anyFloat()))
                    .thenAnswer(invocation -> ((ShipAPI) invocation.getArgument(2)).getLocation());

            listener.modifyDamageDealt(beam, primaryTarget, damage, point, false);

            verify(damage).setDamage(45f);
            verify(engine).applyDamage(eq(beam), eq(enemyInRange), any(Vector2f.class), eq(45f), eq(DamageType.ENERGY),
                    eq(0f), eq(false), eq(true), eq(ship), eq(false));
            verify(engine, never()).applyDamage(eq(beam), eq(enemyOutOfRange), any(Vector2f.class), anyFloat(), any(),
                    anyFloat(), anyBoolean(), anyBoolean(), any(), anyBoolean());
        }
    }

    @Test
    void beamSplitRadiusIsHalfTheBeamsModifiedRange() {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.getOwner()).thenReturn(0);
        MutableShipStatsAPI shipStats = mock(MutableShipStatsAPI.class);
        com.fs.starfarer.api.util.DynamicStatsAPI dynamic = mock(com.fs.starfarer.api.util.DynamicStatsAPI.class);
        when(ship.getMutableStats()).thenReturn(shipStats);
        when(shipStats.getDynamic()).thenReturn(dynamic);
        when(dynamic.getValue("exiledSector_beamSplitTargets", 0f)).thenReturn(1f);

        DamageDealtModifier listener = captureBeamSplitListener(ship);

        BeamAPI beam = mock(BeamAPI.class);
        when(beam.getWidth()).thenReturn(10f);
        when(beam.getCoreColor()).thenReturn(java.awt.Color.WHITE);
        when(beam.getFringeColor()).thenReturn(java.awt.Color.RED);
        // Range 1000 -> split radius 500: an enemy at 600 should be excluded, but included once range doubles.
        mockBeamSplitWeapon(beam, 1000f);

        ShipAPI primaryTarget = mock(ShipAPI.class);
        when(primaryTarget.getOwner()).thenReturn(1);

        Vector2f point = new Vector2f(0f, 0f);
        ShipAPI enemyJustOutOfHalfRange = mockBeamSplitEnemy(1, new Vector2f(600f, 0f));

        DamageAPI damage = mock(DamageAPI.class);
        when(damage.getDamage()).thenReturn(90f);
        when(damage.getType()).thenReturn(DamageType.ENERGY);

        try (MockedStatic<Global> globalMock = Mockito.mockStatic(Global.class);
             MockedStatic<MagicFakeBeam> fakeBeamMock = Mockito.mockStatic(MagicFakeBeam.class);
             MockedStatic<MagicFakeBeamPlugin> fakeBeamPluginMock = Mockito.mockStatic(MagicFakeBeamPlugin.class)) {
            CombatEngineAPI engine = mock(CombatEngineAPI.class);
            globalMock.when(Global::getCombatEngine).thenReturn(engine);
            when(engine.getShips()).thenReturn(Arrays.asList(ship, primaryTarget, enemyJustOutOfHalfRange));
            fakeBeamMock.when(() -> MagicFakeBeam.getShipCollisionPoint(any(), any(), any(), anyFloat()))
                    .thenAnswer(invocation -> ((ShipAPI) invocation.getArgument(2)).getLocation());

            listener.modifyDamageDealt(beam, primaryTarget, damage, point, false);
            verify(damage, never()).setDamage(anyFloat());

            when(beam.getWeapon().getRange()).thenReturn(1400f);
            listener.modifyDamageDealt(beam, primaryTarget, damage, point, false);
            verify(damage).setDamage(45f);
        }
    }

    @Test
    void beamSplitListenerSplitsEmpProportionallyAcrossTargets() {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.getOwner()).thenReturn(0);
        MutableShipStatsAPI shipStats = mock(MutableShipStatsAPI.class);
        com.fs.starfarer.api.util.DynamicStatsAPI dynamic = mock(com.fs.starfarer.api.util.DynamicStatsAPI.class);
        when(ship.getMutableStats()).thenReturn(shipStats);
        when(shipStats.getDynamic()).thenReturn(dynamic);
        when(dynamic.getValue("exiledSector_beamSplitTargets", 0f)).thenReturn(1f);

        DamageDealtModifier listener = captureBeamSplitListener(ship);

        BeamAPI beam = mock(BeamAPI.class);
        when(beam.getWidth()).thenReturn(10f);
        when(beam.getCoreColor()).thenReturn(java.awt.Color.WHITE);
        when(beam.getFringeColor()).thenReturn(java.awt.Color.RED);
        WeaponAPI weapon = mockBeamSplitWeapon(beam, 2000f);
        // dps=100, emp/sec=20 -> a hit dealing 40 damage should carry 8 emp (40 * 20/100).
        mockBeamSplitDerivedStats(weapon, 100f, 20f);

        ShipAPI primaryTarget = mock(ShipAPI.class);
        when(primaryTarget.getOwner()).thenReturn(1);

        Vector2f point = new Vector2f(0f, 0f);
        ShipAPI enemy = mockBeamSplitEnemy(1, new Vector2f(100f, 0f));

        DamageAPI damage = mock(DamageAPI.class);
        when(damage.getDamage()).thenReturn(80f);
        when(damage.getType()).thenReturn(DamageType.ENERGY);

        try (MockedStatic<Global> globalMock = Mockito.mockStatic(Global.class);
             MockedStatic<MagicFakeBeam> fakeBeamMock = Mockito.mockStatic(MagicFakeBeam.class);
             MockedStatic<MagicFakeBeamPlugin> fakeBeamPluginMock = Mockito.mockStatic(MagicFakeBeamPlugin.class)) {
            CombatEngineAPI engine = mock(CombatEngineAPI.class);
            globalMock.when(Global::getCombatEngine).thenReturn(engine);
            when(engine.getShips()).thenReturn(Arrays.asList(ship, primaryTarget, enemy));
            fakeBeamMock.when(() -> MagicFakeBeam.getShipCollisionPoint(any(), any(), any(), anyFloat()))
                    .thenAnswer(invocation -> ((ShipAPI) invocation.getArgument(2)).getLocation());

            listener.modifyDamageDealt(beam, primaryTarget, damage, point, false);

            verify(damage).setDamage(40f);
            verify(engine).applyDamage(eq(beam), eq(enemy), any(Vector2f.class), eq(40f), eq(DamageType.ENERGY),
                    eq(8f), eq(false), eq(true), eq(ship), eq(false));
        }
    }

    @Test
    void findNearbyEnemiesExcludesNeutralOwnedShipsEvenThoughTheirOwnerDiffers() {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.getOwner()).thenReturn(0);
        MutableShipStatsAPI shipStats = mock(MutableShipStatsAPI.class);
        com.fs.starfarer.api.util.DynamicStatsAPI dynamic = mock(com.fs.starfarer.api.util.DynamicStatsAPI.class);
        when(ship.getMutableStats()).thenReturn(shipStats);
        when(shipStats.getDynamic()).thenReturn(dynamic);
        when(dynamic.getValue("exiledSector_beamSplitTargets", 0f)).thenReturn(1f);

        DamageDealtModifier listener = captureBeamSplitListener(ship);

        BeamAPI beam = mock(BeamAPI.class);
        when(beam.getWidth()).thenReturn(10f);
        when(beam.getCoreColor()).thenReturn(java.awt.Color.WHITE);
        when(beam.getFringeColor()).thenReturn(java.awt.Color.RED);
        mockBeamSplitWeapon(beam, 2000f);

        ShipAPI primaryTarget = mock(ShipAPI.class);
        when(primaryTarget.getOwner()).thenReturn(1);

        Vector2f point = new Vector2f(0f, 0f);
        // Owner 100 (Misc.OWNER_NEUTRAL) differs from source's owner but is not hostile:
        // a bystander in a 3+ side battle should never be dragged into a split.
        ShipAPI neutralBystander = mockBeamSplitEnemy(100, new Vector2f(100f, 0f));

        DamageAPI damage = mock(DamageAPI.class);
        when(damage.getDamage()).thenReturn(90f);
        when(damage.getType()).thenReturn(DamageType.ENERGY);

        try (MockedStatic<Global> globalMock = Mockito.mockStatic(Global.class);
             MockedStatic<MagicFakeBeam> fakeBeamMock = Mockito.mockStatic(MagicFakeBeam.class);
             MockedStatic<MagicFakeBeamPlugin> fakeBeamPluginMock = Mockito.mockStatic(MagicFakeBeamPlugin.class)) {
            CombatEngineAPI engine = mock(CombatEngineAPI.class);
            globalMock.when(Global::getCombatEngine).thenReturn(engine);
            when(engine.getShips()).thenReturn(Arrays.asList(ship, primaryTarget, neutralBystander));

            listener.modifyDamageDealt(beam, primaryTarget, damage, point, false);

            verify(damage, never()).setDamage(anyFloat());
            verify(engine, never()).applyDamage(eq(beam), eq(neutralBystander), any(Vector2f.class), anyFloat(),
                    any(), anyFloat(), anyBoolean(), anyBoolean(), any(), anyBoolean());
        }
    }
}
