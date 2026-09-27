package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    void applyDModEffectMultSetsTheVanillaStatAndReappliesOnlyDMods() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        DynamicStatsAPI dynamic = mock(DynamicStatsAPI.class);
        MutableStat dmodEffectMult = mock(MutableStat.class);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        SettingsAPI settings = mock(SettingsAPI.class);
        HullModSpecAPI dmodSpec = mock(HullModSpecAPI.class);
        HullModSpecAPI otherSpec = mock(HullModSpecAPI.class);
        HullModEffect dmodEffect = mock(HullModEffect.class);
        HullModEffect otherEffect = mock(HullModEffect.class);
        when(stats.getDynamic()).thenReturn(dynamic);
        when(dynamic.getStat(Stats.DMOD_EFFECT_MULT)).thenReturn(dmodEffectMult);
        when(stats.getVariant()).thenReturn(variant);
        when(variant.getHullMods()).thenReturn(List.of("degraded_engines", "heavyarmor"));
        when(variant.getHullSize()).thenReturn(HullSize.CRUISER);
        when(settings.getHullModSpec("degraded_engines")).thenReturn(dmodSpec);
        when(settings.getHullModSpec("heavyarmor")).thenReturn(otherSpec);
        when(dmodSpec.hasTag(Tags.HULLMOD_DMOD)).thenReturn(true);
        when(dmodSpec.getEffect()).thenReturn(dmodEffect);
        when(dmodSpec.getId()).thenReturn("degraded_engines");
        when(otherSpec.getEffect()).thenReturn(otherEffect);

        try (MockedStatic<Global> global = Mockito.mockStatic(Global.class)) {
            global.when(Global::getSettings).thenReturn(settings);

            SkillEffectSupport.applyDModEffectMult(stats, "mod_id", -5f);
        }

        verify(dmodEffectMult).modifyMult("mod_id", 0.95f);
        verify(dmodEffect).applyEffectsBeforeShipCreation(HullSize.CRUISER, stats, "degraded_engines");
        verifyNoInteractions(otherEffect);
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
