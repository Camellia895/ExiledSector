package exiledsector.skills;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.skills.skilleffect.DefenseSkillEffect;
import exiledsector.skills.skilleffect.FighterSkillEffect;
import exiledsector.skills.skilleffect.FluxSkillEffect;
import exiledsector.skills.skilleffect.LogisticsSkillEffect;
import exiledsector.skills.skilleffect.MiscSkillEffect;
import exiledsector.skills.skilleffect.MovementSkillEffect;
import exiledsector.skills.skilleffect.ShieldSkillEffect;
import exiledsector.skills.skilleffect.WeaponSkillEffect;
import org.junit.jupiter.api.Test;
import org.lwjgl.util.vector.Vector2f;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SkillEffectTest {

    @Test
    void hullModifiesTheHullBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus hullBonus = mock(StatBonus.class);
        when(stats.getHullBonus()).thenReturn(hullBonus);

        DefenseSkillEffect.HULL.apply(stats, "mod_id", 10f);

        verify(hullBonus).modifyPercent("mod_id", 10f);
    }

    @Test
    void armorModifiesTheArmorBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus armorBonus = mock(StatBonus.class);
        when(stats.getArmorBonus()).thenReturn(armorBonus);

        DefenseSkillEffect.ARMOR.apply(stats, "mod_id", 10f);

        verify(armorBonus).modifyPercent("mod_id", 10f);
    }

    @Test
    void fluxCapacityModifiesTheFluxCapacityStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat fluxCapacity = mock(MutableStat.class);
        when(stats.getFluxCapacity()).thenReturn(fluxCapacity);

        FluxSkillEffect.FLUX_CAPACITY.apply(stats, "mod_id", 1f);

        verify(fluxCapacity).modifyPercent("mod_id", 1f);
    }

    @Test
    void fluxDissipationModifiesTheFluxDissipationStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat fluxDissipation = mock(MutableStat.class);
        when(stats.getFluxDissipation()).thenReturn(fluxDissipation);

        FluxSkillEffect.FLUX_DISSIPATION.apply(stats, "mod_id", 10f);

        verify(fluxDissipation).modifyPercent("mod_id", 10f);
    }

    @Test
    void fluxRegulationModifiesBothFluxCapacityAndDissipationStats() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat fluxCapacity = mock(MutableStat.class);
        MutableStat fluxDissipation = mock(MutableStat.class);
        when(stats.getFluxCapacity()).thenReturn(fluxCapacity);
        when(stats.getFluxDissipation()).thenReturn(fluxDissipation);

        FluxSkillEffect.HYBRID_FLUX.apply(stats, "mod_id", 0.5f);

        verify(fluxCapacity).modifyPercent("mod_id", 0.5f);
        verify(fluxDissipation).modifyPercent("mod_id", 0.5f);
    }

    @Test
    void ballisticDamageModifiesOnlyTheBallisticDamageStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat ballistic = mock(MutableStat.class);
        when(stats.getBallisticWeaponDamageMult()).thenReturn(ballistic);

        WeaponSkillEffect.BALLISTIC_DAMAGE.apply(stats, "mod_id", 5f);

        verify(ballistic).modifyPercent("mod_id", 5f);
    }

    @Test
    void missileDamageModifiesOnlyTheMissileDamageStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat missile = mock(MutableStat.class);
        when(stats.getMissileWeaponDamageMult()).thenReturn(missile);

        WeaponSkillEffect.MISSILE_DAMAGE.apply(stats, "mod_id", 5f);

        verify(missile).modifyPercent("mod_id", 5f);
    }

    @Test
    void nonBeamEnergyDamageModifiesOnlyTheEnergyDamageStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat energy = mock(MutableStat.class);
        when(stats.getEnergyWeaponDamageMult()).thenReturn(energy);

        WeaponSkillEffect.NON_BEAM_ENERGY_DAMAGE.apply(stats, "mod_id", 5f);

        verify(energy).modifyPercent("mod_id", 5f);
    }

    @Test
    void beamDamageModifiesOnlyTheBeamDamageStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat beam = mock(MutableStat.class);
        when(stats.getBeamWeaponDamageMult()).thenReturn(beam);

        WeaponSkillEffect.BEAM_DAMAGE.apply(stats, "mod_id", 5f);

        verify(beam).modifyPercent("mod_id", 5f);
    }

    @Test
    void energyDamageModifiesBothEnergyAndBeamDamageStats() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat energy = mock(MutableStat.class);
        MutableStat beam = mock(MutableStat.class);
        when(stats.getEnergyWeaponDamageMult()).thenReturn(energy);
        when(stats.getBeamWeaponDamageMult()).thenReturn(beam);

        WeaponSkillEffect.ENERGY_DAMAGE.apply(stats, "mod_id", 5f);

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

        WeaponSkillEffect.ALL_WEAPON_DAMAGE.apply(stats, "mod_id", 5f);

        verify(ballistic).modifyPercent("mod_id", 5f);
        verify(missile).modifyPercent("mod_id", 5f);
        verify(energy).modifyPercent("mod_id", 5f);
        verify(beam).modifyPercent("mod_id", 5f);
    }

    @Test
    void describeFormatsAWholeNumberMagnitudeWithoutADecimal() {
        assertEquals("Increases hull points by 10%.", DefenseSkillEffect.HULL.describe(10f));
    }

    @Test
    void describeFormatsAFractionalMagnitudeWithADecimal() {
        assertEquals("Increases flux capacity and dissipation by 0.5% each.", FluxSkillEffect.HYBRID_FLUX.describe(0.5f));
    }

    @Test
    void describeMentionsBothStatsForTheAllWeaponDamageHybrid() {
        assertEquals("Increases damage of all weapon types by 5%.", WeaponSkillEffect.ALL_WEAPON_DAMAGE.describe(5f));
    }

    @Test
    void maneuverabilityModifiesTheMaxTurnRateStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat turnRate = mock(MutableStat.class);
        when(stats.getMaxTurnRate()).thenReturn(turnRate);

        MovementSkillEffect.MANEUVERABILITY.apply(stats, "mod_id", 15f);

        verify(turnRate).modifyPercent("mod_id", 15f);
    }

    @Test
    void fuelCapacityModifiesTheFuelModStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus fuelMod = mock(StatBonus.class);
        when(stats.getFuelMod()).thenReturn(fuelMod);

        LogisticsSkillEffect.FUEL_CAPACITY.apply(stats, "mod_id", 20f);

        verify(fuelMod).modifyPercent("mod_id", 20f);
    }

    @Test
    void cargoCapacityModifiesTheCargoModStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus cargoMod = mock(StatBonus.class);
        when(stats.getCargoMod()).thenReturn(cargoMod);

        LogisticsSkillEffect.CARGO_CAPACITY.apply(stats, "mod_id", 20f);

        verify(cargoMod).modifyPercent("mod_id", 20f);
    }

    @Test
    void crewCapacityModifiesTheMaxCrewModStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus maxCrewMod = mock(StatBonus.class);
        when(stats.getMaxCrewMod()).thenReturn(maxCrewMod);

        LogisticsSkillEffect.CREW_CAPACITY.apply(stats, "mod_id", 20f);

        verify(maxCrewMod).modifyPercent("mod_id", 20f);
    }

    @Test
    void sensorProfileModifiesTheSensorProfileStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat sensorProfile = mock(MutableStat.class);
        when(stats.getSensorProfile()).thenReturn(sensorProfile);

        LogisticsSkillEffect.SENSOR_PROFILE.apply(stats, "mod_id", -10f);

        verify(sensorProfile).modifyPercent("mod_id", -10f);
    }

    @Test
    void sensorStrengthModifiesTheSensorStrengthStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat sensorStrength = mock(MutableStat.class);
        when(stats.getSensorStrength()).thenReturn(sensorStrength);

        LogisticsSkillEffect.SENSOR_STRENGTH.apply(stats, "mod_id", 20f);

        verify(sensorStrength).modifyPercent("mod_id", 20f);
    }

    @Test
    void ballisticWeaponRangeModifiesTheBallisticRangeBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus rangeBonus = mock(StatBonus.class);
        when(stats.getBallisticWeaponRangeBonus()).thenReturn(rangeBonus);

        WeaponSkillEffect.BALLISTIC_WEAPON_RANGE.apply(stats, "mod_id", 15f);

        verify(rangeBonus).modifyPercent("mod_id", 15f);
    }

    @Test
    void energyWeaponRangeModifiesTheEnergyRangeBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus rangeBonus = mock(StatBonus.class);
        when(stats.getEnergyWeaponRangeBonus()).thenReturn(rangeBonus);

        WeaponSkillEffect.ENERGY_WEAPON_RANGE.apply(stats, "mod_id", 15f);

        verify(rangeBonus).modifyPercent("mod_id", 15f);
    }

    @Test
    void beamWeaponRangeModifiesTheBeamRangeBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus rangeBonus = mock(StatBonus.class);
        when(stats.getBeamWeaponRangeBonus()).thenReturn(rangeBonus);

        WeaponSkillEffect.BEAM_WEAPON_RANGE.apply(stats, "mod_id", 15f);

        verify(rangeBonus).modifyPercent("mod_id", 15f);
    }

    @Test
    void ballisticAmmoModifiesTheBallisticAmmoBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus ammoBonus = mock(StatBonus.class);
        when(stats.getBallisticAmmoBonus()).thenReturn(ammoBonus);

        WeaponSkillEffect.BALLISTIC_AMMO.apply(stats, "mod_id", 20f);

        verify(ammoBonus).modifyPercent("mod_id", 20f);
    }

    @Test
    void energyAmmoModifiesTheEnergyAmmoBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus ammoBonus = mock(StatBonus.class);
        when(stats.getEnergyAmmoBonus()).thenReturn(ammoBonus);

        WeaponSkillEffect.ENERGY_AMMO.apply(stats, "mod_id", 20f);

        verify(ammoBonus).modifyPercent("mod_id", 20f);
    }

    @Test
    void missileAmmoModifiesTheMissileAmmoBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus ammoBonus = mock(StatBonus.class);
        when(stats.getMissileAmmoBonus()).thenReturn(ammoBonus);

        WeaponSkillEffect.MISSILE_AMMO.apply(stats, "mod_id", 25f);

        verify(ammoBonus).modifyPercent("mod_id", 25f);
    }

    @Test
    void weaponTurnRateModifiesTheWeaponTurnRateBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus turnRateBonus = mock(StatBonus.class);
        when(stats.getWeaponTurnRateBonus()).thenReturn(turnRateBonus);

        WeaponSkillEffect.WEAPON_TURN_RATE.apply(stats, "mod_id", 20f);

        verify(turnRateBonus).modifyPercent("mod_id", 20f);
    }

    @Test
    void shieldArcModifiesTheShieldArcBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus arcBonus = mock(StatBonus.class);
        when(stats.getShieldArcBonus()).thenReturn(arcBonus);

        ShieldSkillEffect.SHIELD_ARC.apply(stats, "mod_id", 20f);

        verify(arcBonus).modifyPercent("mod_id", 20f);
    }

    @Test
    void shieldUpkeepModifiesTheShieldUpkeepMultStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat upkeepMult = mock(MutableStat.class);
        when(stats.getShieldUpkeepMult()).thenReturn(upkeepMult);

        ShieldSkillEffect.SHIELD_UPKEEP.apply(stats, "mod_id", -15f);

        verify(upkeepMult).modifyPercent("mod_id", -15f);
    }

    @Test
    void shieldAbsorptionModifiesTheShieldAbsorptionMultStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat absorptionMult = mock(MutableStat.class);
        when(stats.getShieldAbsorptionMult()).thenReturn(absorptionMult);

        DefenseSkillEffect.SHIELD_ABSORPTION.apply(stats, "mod_id", -10f);

        verify(absorptionMult).modifyPercent("mod_id", -10f);
    }

    @Test
    void shieldTurnRateModifiesTheShieldTurnRateMultStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat turnRateMult = mock(MutableStat.class);
        when(stats.getShieldTurnRateMult()).thenReturn(turnRateMult);

        ShieldSkillEffect.SHIELD_TURN_RATE.apply(stats, "mod_id", 15f);

        verify(turnRateMult).modifyPercent("mod_id", 15f);
    }

    @Test
    void shieldRaiseRateModifiesTheShieldUnfoldRateMultStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat unfoldRateMult = mock(MutableStat.class);
        when(stats.getShieldUnfoldRateMult()).thenReturn(unfoldRateMult);

        ShieldSkillEffect.SHIELD_RAISE_RATE.apply(stats, "mod_id", 15f);

        verify(unfoldRateMult).modifyPercent("mod_id", 15f);
    }

    @Test
    void weaponDurabilityModifiesTheWeaponHealthBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus healthBonus = mock(StatBonus.class);
        when(stats.getWeaponHealthBonus()).thenReturn(healthBonus);

        WeaponSkillEffect.WEAPON_DURABILITY.apply(stats, "mod_id", 20f);

        verify(healthBonus).modifyPercent("mod_id", 20f);
    }

    @Test
    void engineDurabilityModifiesTheEngineHealthBonusStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus healthBonus = mock(StatBonus.class);
        when(stats.getEngineHealthBonus()).thenReturn(healthBonus);

        DefenseSkillEffect.ENGINE_DURABILITY.apply(stats, "mod_id", 15f);

        verify(healthBonus).modifyPercent("mod_id", 15f);
    }

    @Test
    void topSpeedModifiesTheMaxSpeedStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat maxSpeed = mock(MutableStat.class);
        when(stats.getMaxSpeed()).thenReturn(maxSpeed);

        MovementSkillEffect.TOP_SPEED.apply(stats, "mod_id", 15f);

        verify(maxSpeed).modifyPercent("mod_id", 15f);
    }

    @Test
    void peakCrDurationModifiesThePeakCRDurationStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        StatBonus peakCrDuration = mock(StatBonus.class);
        when(stats.getPeakCRDuration()).thenReturn(peakCrDuration);

        MiscSkillEffect.PEAK_CR_DURATION.apply(stats, "mod_id", 20f);

        verify(peakCrDuration).modifyPercent("mod_id", 20f);
    }

    @Test
    void weaponRangeFalloffModifiesTheWeaponRangeMultPastThresholdStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat rangeMultPastThreshold = mock(MutableStat.class);
        when(stats.getWeaponRangeMultPastThreshold()).thenReturn(rangeMultPastThreshold);

        WeaponSkillEffect.WEAPON_RANGE_FALLOFF.apply(stats, "mod_id", -15f);

        verify(rangeMultPastThreshold).modifyPercent("mod_id", -15f);
    }

    @Test
    void repairTimeModifiesBothWeaponAndEngineRepairTimeStats() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat weaponRepairTime = mock(MutableStat.class);
        MutableStat engineRepairTime = mock(MutableStat.class);
        when(stats.getCombatWeaponRepairTimeMult()).thenReturn(weaponRepairTime);
        when(stats.getCombatEngineRepairTimeMult()).thenReturn(engineRepairTime);

        DefenseSkillEffect.REPAIR_TIME.apply(stats, "mod_id", -20f);

        verify(weaponRepairTime).modifyPercent("mod_id", -20f);
        verify(engineRepairTime).modifyPercent("mod_id", -20f);
    }

    @Test
    void missileGuidanceModifiesTheMissileGuidanceStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat missileGuidance = mock(MutableStat.class);
        when(stats.getMissileGuidance()).thenReturn(missileGuidance);

        WeaponSkillEffect.MISSILE_GUIDANCE.apply(stats, "mod_id", 20f);

        verify(missileGuidance).modifyPercent("mod_id", 20f);
    }

    @Test
    void crRecoveryRateModifiesTheBaseCRRecoveryRateStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat crRecoveryRate = mock(MutableStat.class);
        when(stats.getBaseCRRecoveryRatePercentPerDay()).thenReturn(crRecoveryRate);

        LogisticsSkillEffect.CR_RECOVERY_RATE.apply(stats, "mod_id", 15f);

        verify(crRecoveryRate).modifyPercent("mod_id", 15f);
    }

    @Test
    void crewLossModifiesTheCrewLossMultStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat crewLossMult = mock(MutableStat.class);
        when(stats.getCrewLossMult()).thenReturn(crewLossMult);

        LogisticsSkillEffect.CREW_LOSS.apply(stats, "mod_id", -15f);

        verify(crewLossMult).modifyPercent("mod_id", -15f);
    }

    @Test
    void energyDamageTakenModifiesTheEnergyDamageTakenMultStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat damageTakenMult = mock(MutableStat.class);
        when(stats.getEnergyDamageTakenMult()).thenReturn(damageTakenMult);

        DefenseSkillEffect.ENERGY_DAMAGE_TAKEN.apply(stats, "mod_id", -10f);

        verify(damageTakenMult).modifyPercent("mod_id", -10f);
    }

    @Test
    void describeUsesIncreasesForAPositiveBidirectionalMagnitude() {
        assertEquals("Increases peak combat readiness duration by 20%.", MiscSkillEffect.PEAK_CR_DURATION.describe(20f));
    }

    @Test
    void describeUsesDecreasesForANegativeBidirectionalMagnitude() {
        assertEquals("Decreases peak combat readiness duration by 20%.", MiscSkillEffect.PEAK_CR_DURATION.describe(-20f));
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
        assertNull(DefenseSkillEffect.HULL.deallocationWarning(10f));
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
        assertNull(DefenseSkillEffect.HULL.blockDeallocationReason(mock(FleetMemberAPI.class), 10f));
    }

    @Test
    void fighterWeaponDamageDoesNotTouchTheCarrierStatsDirectly() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);

        FighterSkillEffect.FIGHTER_WEAPON_DAMAGE.apply(stats, "mod_id", 15f);

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

        FighterSkillEffect.FIGHTER_WEAPON_DAMAGE.applyToFighterSpawnedByShip(fighter, parentShip, "mod_id", 15f);

        verify(ballistic).modifyPercent("mod_id", 15f);
        verify(missile).modifyPercent("mod_id", 15f);
        verify(energy).modifyPercent("mod_id", 15f);
        verify(beam).modifyPercent("mod_id", 15f);
    }

    @Test
    void fighterTopSpeedDoesNotTouchTheCarrierStatsDirectly() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);

        FighterSkillEffect.FIGHTER_TOP_SPEED.apply(stats, "mod_id", 15f);

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

        FighterSkillEffect.FIGHTER_TOP_SPEED.applyToFighterSpawnedByShip(fighter, parentShip, "mod_id", 15f);

        verify(maxSpeed).modifyPercent("mod_id", 15f);
    }

    @Test
    void removeAllFighterBaysZeroesOutTheShipSOwnBaseBayCount() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat numFighterBays = mock(MutableStat.class);
        when(stats.getNumFighterBays()).thenReturn(numFighterBays);
        when(numFighterBays.getBaseValue()).thenReturn(3f);

        FighterSkillEffect.REMOVE_ALL_FIGHTER_BAYS.apply(stats, "mod_id", 0f);

        verify(numFighterBays).modifyFlat("mod_id", -3f);
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

        FighterSkillEffect.FIGHTER_REPLACEMENT_DECAY_MULT.apply(stats, "mod_id", -15f);

        verify(decreaseMult).modifyPercent("mod_id", -15f);
    }

    @Test
    void fighterReplacementRecoveryMultModifiesTheIncreaseMultStat() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        com.fs.starfarer.api.util.DynamicStatsAPI dynamic = mock(com.fs.starfarer.api.util.DynamicStatsAPI.class);
        when(stats.getDynamic()).thenReturn(dynamic);
        MutableStat increaseMult = mock(MutableStat.class);
        when(dynamic.getStat("replacement_rate_increase_mult")).thenReturn(increaseMult);

        FighterSkillEffect.FIGHTER_REPLACEMENT_RECOVERY_MULT.apply(stats, "mod_id", 25f);

        verify(increaseMult).modifyPercent("mod_id", 25f);
    }

    @Test
    void fighterPdDamageBonusDoesNotTouchTheCarrierStatsDirectly() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);

        FighterSkillEffect.FIGHTER_PD_DAMAGE_BONUS.apply(stats, "mod_id", 50f);

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

        FighterSkillEffect.FIGHTER_PD_DAMAGE_BONUS.applyToFighterSpawnedByShip(fighter, parentShip, "mod_id", 50f);

        verify(damageToFighters).modifyPercent("mod_id", 50f);
        verify(damageToMissiles).modifyPercent("mod_id", 50f);
    }

    @Test
    void beamDamageHardFluxPercentDoesNotTouchStatsDirectly() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);

        ShieldSkillEffect.BEAM_DAMAGE_HARD_FLUX_PERCENT.apply(stats, "mod_id", 50f);

        verifyNoInteractions(stats);
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
}
