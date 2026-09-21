package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.StatBonus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SkillEffectSupportTest {

    @Test
    void multFromConvertsAPercentMagnitudeIntoAMultiplier() {
        assertEquals(1.1f, SkillEffectSupport.multFrom(10f), 0.0001f);
        assertEquals(0.9f, SkillEffectSupport.multFrom(-10f), 0.0001f);
        assertEquals(1f, SkillEffectSupport.multFrom(0f), 0.0001f);
    }

    @Test
    void applyMultOnAMutableStatUsesTheConvertedMultiplier() {
        MutableStat stat = mock(MutableStat.class);

        SkillEffectSupport.applyMult(stat, "mod_id", 25f);

        verify(stat).modifyMult("mod_id", 1.25f);
    }

    @Test
    void applyMultOnAStatBonusUsesTheConvertedMultiplier() {
        StatBonus stat = mock(StatBonus.class);

        SkillEffectSupport.applyMult(stat, "mod_id", -25f);

        verify(stat).modifyMult("mod_id", 0.75f);
    }

    @Test
    void applyAllWeaponDamagePercentModifiesAllFourWeaponDamageStats() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        MutableStat ballistic = mock(MutableStat.class);
        MutableStat missile = mock(MutableStat.class);
        MutableStat energy = mock(MutableStat.class);
        MutableStat beam = mock(MutableStat.class);
        when(stats.getBallisticWeaponDamageMult()).thenReturn(ballistic);
        when(stats.getMissileWeaponDamageMult()).thenReturn(missile);
        when(stats.getEnergyWeaponDamageMult()).thenReturn(energy);
        when(stats.getBeamWeaponDamageMult()).thenReturn(beam);

        SkillEffectSupport.applyAllWeaponDamagePercent(stats, "mod_id", 15f);

        verify(ballistic).modifyPercent("mod_id", 15f);
        verify(missile).modifyPercent("mod_id", 15f);
        verify(energy).modifyPercent("mod_id", 15f);
        verify(beam).modifyPercent("mod_id", 15f);
    }

    @Test
    void pctMoreDescribesAPositiveMagnitudeAsMore() {
        assertEquals("30% more beam weapon damage.", SkillEffectText.pctMore(30f, "beam weapon damage"));
    }

    @Test
    void pctMoreDescribesANegativeMagnitudeAsLess() {
        assertEquals("20% less flux dissipation.", SkillEffectText.pctMore(-20f, "flux dissipation"));
    }

    @Test
    void multAndPercentEffectsOnTheSameStatDescribeThemselvesDifferently() {
        String percentText = FluxSkillEffect.FLUX_DISSIPATION_PERCENT.describe(30f);
        String multText = FluxSkillEffect.FLUX_DISSIPATION_MULT.describe(30f);

        assertEquals("Increases flux dissipation by 30%.", percentText);
        assertEquals("30% more flux dissipation.", multText);
    }
}
