package exiledsector.skills;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.StatBonus;
import org.junit.jupiter.api.Test;

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

        SkillEffect.FLUX_REGULATION.apply(stats, "mod_id", 0.5f);

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
}
