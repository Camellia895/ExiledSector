package exiledsector.effects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.skilleffect.DefenseSkillEffect;
import exiledsector.skills.skilleffect.FluxSkillEffect;
import exiledsector.skills.skilleffect.MiscSkillEffect;
import exiledsector.skills.skilleffect.PhaseSkillEffect;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import exiledsector.skills.SkillTypeEffect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SkillTreeHullModTest {

    private MockedStatic<Global> globalMock;

    @BeforeEach
    void setUp() {
        Map<String, Object> persistentData = new HashMap<>();
        SectorAPI sector = mock(SectorAPI.class);
        when(sector.getPersistentData()).thenReturn(persistentData);

        globalMock = Mockito.mockStatic(Global.class);
        globalMock.when(Global::getSector).thenReturn(sector);

        SkillTree.getAllNodes().clear();
    }

    @AfterEach
    void tearDown() {
        globalMock.close();
        SkillTree.getAllNodes().clear();
    }

    @Test
    void appliesTheEffectOfEachAllocatedNodeWithOneRegisteredOnTheTree() {
        SkillType hullType = new SkillType("hull", "Reinforced Hull", "graphics/hullmods/reinforced_bulkheads.png", 2, 500,
                List.of(new SkillTypeEffect(DefenseSkillEffect.HULL, 10f)), SkillTier.SMALL, null, null, null);
        SkillNode hullNode = new SkillNode("hull_1", hullType, List.of(), 0f, 0f);
        SkillTree.register(hullNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(hullNode);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(stats.getFleetMember()).thenReturn(member);
        com.fs.starfarer.api.combat.StatBonus hullStatBonus = mock(com.fs.starfarer.api.combat.StatBonus.class);
        when(stats.getHullBonus()).thenReturn(hullStatBonus);

        new SkillTreeHullMod().applyEffectsBeforeShipCreation(HullSize.FRIGATE, stats, "exiledSector_core");

        verify(hullStatBonus).modifyPercent("exiledSector_skill_hull_1", 10f);
    }

    @Test
    void appliesEachEffectOnANodeWithMultipleEffectsIndependently() {
        SkillType multiType = new SkillType("heavyarmor", "Heavy Armor", "graphics/icons/notable_hullmods/heavy_armor.png", 4, 2000,
                List.of(new SkillTypeEffect(DefenseSkillEffect.ARMOR, 15f), new SkillTypeEffect(DefenseSkillEffect.HULL, 5f)),
                SkillTier.NOTABLE, null, null, null);
        SkillNode multiNode = new SkillNode("heavyarmor_1", multiType, List.of(), 0f, 0f);
        SkillTree.register(multiNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(multiNode);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(stats.getFleetMember()).thenReturn(member);
        com.fs.starfarer.api.combat.StatBonus armorBonus = mock(com.fs.starfarer.api.combat.StatBonus.class);
        com.fs.starfarer.api.combat.StatBonus hullBonus = mock(com.fs.starfarer.api.combat.StatBonus.class);
        when(stats.getArmorBonus()).thenReturn(armorBonus);
        when(stats.getHullBonus()).thenReturn(hullBonus);

        new SkillTreeHullMod().applyEffectsBeforeShipCreation(HullSize.FRIGATE, stats, "exiledSector_core");

        verify(armorBonus).modifyPercent("exiledSector_skill_heavyarmor_1", 15f);
        verify(hullBonus).modifyPercent("exiledSector_skill_heavyarmor_1", 5f);
    }

    @Test
    void skipsNodesWithNoEffectDefinedYet() {
        SkillType cosmeticType = new SkillType("capacitors", "Capacitors", "graphics/hullmods/flux_coil_adjunct.png", 2, 500,
                List.of(), SkillTier.SMALL, null, null, null);
        SkillNode cosmeticNode = new SkillNode("capacitors_1", cosmeticType, List.of(), 0f, 0f);
        SkillTree.register(cosmeticNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(cosmeticNode);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(stats.getFleetMember()).thenReturn(member);

        new SkillTreeHullMod().applyEffectsBeforeShipCreation(HullSize.FRIGATE, stats, "exiledSector_core");

        verify(stats, never()).getHullBonus();
    }

    @Test
    void delegatesToTheRealVanillaHullModEffectWhenTypeSpecifiesOne() {
        SkillType keystoneType = new SkillType("safety_overrides", "Safety Overrides", "graphics/icons/skills/helmsmanship.png", 4, 2000,
                List.of(), SkillTier.KEYSTONE, "safetyoverrides", null, null);
        SkillNode keystoneNode = new SkillNode("safety_overrides_1", keystoneType, List.of(), 0f, 0f);
        SkillTree.register(keystoneNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(keystoneNode);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(stats.getFleetMember()).thenReturn(member);

        SettingsAPI settings = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settings);
        HullModSpecAPI spec = mock(HullModSpecAPI.class);
        when(settings.getHullModSpec("safetyoverrides")).thenReturn(spec);
        HullModEffect vanillaEffect = mock(HullModEffect.class);
        when(spec.getEffect()).thenReturn(vanillaEffect);

        new SkillTreeHullMod().applyEffectsBeforeShipCreation(HullSize.FRIGATE, stats, "exiledSector_core");

        verify(vanillaEffect).applyEffectsBeforeShipCreation(HullSize.FRIGATE, stats, "safetyoverrides");
    }

    @Test
    void doesNothingWhenTheShipHasNoFleetMember() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(stats.getFleetMember()).thenReturn(null);

        new SkillTreeHullMod().applyEffectsBeforeShipCreation(HullSize.FRIGATE, stats, "exiledSector_core");

        verify(stats, never()).getHullBonus();
    }

    private ShipAPI mockShip(FleetMemberAPI member, MutableShipStatsAPI stats) {
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.getMutableStats()).thenReturn(stats);
        when(ship.getHullSize()).thenReturn(HullSize.FRIGATE);
        when(stats.getFleetMember()).thenReturn(member);
        return ship;
    }

    @Test
    void advanceInCombatAppliesConditionalEffectWhileVenting() {
        SkillType ventType = new SkillType("fluxbreakers", "Resistant Flux Conduits", "graphics/icons/notable_hullmods/resistant_flux_conduits.png", 4, 2000,
                List.of(new SkillTypeEffect(FluxSkillEffect.FLUX_DISSIPATION_WHILE_VENTING, 25f)), SkillTier.NOTABLE, null, null, null);
        SkillNode ventNode = new SkillNode("fluxbreakers_1", ventType, List.of(), 0f, 0f);
        SkillTree.register(ventNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(ventNode);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        ShipAPI ship = mockShip(member, stats);
        FluxTrackerAPI fluxTracker = mock(FluxTrackerAPI.class);
        when(ship.getFluxTracker()).thenReturn(fluxTracker);
        when(fluxTracker.isVenting()).thenReturn(true);
        MutableStat dissipation = mock(MutableStat.class);
        when(stats.getFluxDissipation()).thenReturn(dissipation);

        new SkillTreeHullMod().advanceInCombat(ship, 0.1f);

        verify(dissipation).modifyPercent("exiledSector_skill_fluxbreakers_1", 25f);
        verify(dissipation, never()).unmodify(anyString());
    }

    @Test
    void advanceInCombatRemovesConditionalEffectWhenNotVenting() {
        SkillType ventType = new SkillType("fluxbreakers", "Resistant Flux Conduits", "graphics/icons/notable_hullmods/resistant_flux_conduits.png", 4, 2000,
                List.of(new SkillTypeEffect(FluxSkillEffect.FLUX_DISSIPATION_WHILE_VENTING, 25f)), SkillTier.NOTABLE, null, null, null);
        SkillNode ventNode = new SkillNode("fluxbreakers_1", ventType, List.of(), 0f, 0f);
        SkillTree.register(ventNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(ventNode);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        ShipAPI ship = mockShip(member, stats);
        FluxTrackerAPI fluxTracker = mock(FluxTrackerAPI.class);
        when(ship.getFluxTracker()).thenReturn(fluxTracker);
        when(fluxTracker.isVenting()).thenReturn(false);
        MutableStat dissipation = mock(MutableStat.class);
        when(stats.getFluxDissipation()).thenReturn(dissipation);

        new SkillTreeHullMod().advanceInCombat(ship, 0.1f);

        verify(dissipation).unmodify("exiledSector_skill_fluxbreakers_1");
        verify(dissipation, never()).modifyPercent(anyString(), anyFloat());
    }

    @Test
    void advanceInCombatDoesNotTouchNonConditionalEffects() {
        SkillType hullType = new SkillType("hull", "Reinforced Hull", "graphics/hullmods/reinforced_bulkheads.png", 2, 500,
                List.of(new SkillTypeEffect(DefenseSkillEffect.HULL, 10f)), SkillTier.SMALL, null, null, null);
        SkillNode hullNode = new SkillNode("hull_1", hullType, List.of(), 0f, 0f);
        SkillTree.register(hullNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(hullNode);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        ShipAPI ship = mockShip(member, stats);

        new SkillTreeHullMod().advanceInCombat(ship, 0.1f);

        verify(ship, never()).getFluxTracker();
        verify(stats, never()).getHullBonus();
    }

    @Test
    void advanceInCombatDoesNothingWhenTheShipHasNoFleetMember() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(stats.getFleetMember()).thenReturn(null);
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.getMutableStats()).thenReturn(stats);

        new SkillTreeHullMod().advanceInCombat(ship, 0.1f);

        verify(ship, never()).getFluxTracker();
    }

    @Test
    void advanceInCombatDoublesStatsWhilePhased() {
        SkillType phaseType = new SkillType("phase_anchor", "Phase Anchor", "graphics/icons/notable_hullmods/phase_anchor.png", 4, 2000,
                List.of(new SkillTypeEffect(PhaseSkillEffect.COMBAT_BOOST_WHILE_PHASED, 100f)), SkillTier.NOTABLE, null, null, null);
        SkillNode phaseNode = new SkillNode("phase_anchor_1", phaseType, List.of(), 0f, 0f);
        SkillTree.register(phaseNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(phaseNode);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        ShipAPI ship = mockShip(member, stats);
        when(ship.isPhased()).thenReturn(true);
        when(ship.getPhaseCloak()).thenReturn(null);
        MutableStat dissipation = mock(MutableStat.class);
        when(stats.getFluxDissipation()).thenReturn(dissipation);
        when(stats.getBallisticRoFMult()).thenReturn(mock(MutableStat.class));
        when(stats.getEnergyRoFMult()).thenReturn(mock(MutableStat.class));
        when(stats.getMissileRoFMult()).thenReturn(mock(MutableStat.class));
        when(stats.getBallisticAmmoRegenMult()).thenReturn(mock(MutableStat.class));
        when(stats.getEnergyAmmoRegenMult()).thenReturn(mock(MutableStat.class));
        when(stats.getMissileAmmoRegenMult()).thenReturn(mock(MutableStat.class));

        new SkillTreeHullMod().advanceInCombat(ship, 0.1f);

        verify(dissipation).modifyMult("exiledSector_skill_phase_anchor_1", 2f);
        verify(dissipation, never()).unmodifyMult(anyString());
    }

    @Test
    void advanceInCombatUndoesTheBoostWhenNotPhased() {
        SkillType phaseType = new SkillType("phase_anchor", "Phase Anchor", "graphics/icons/notable_hullmods/phase_anchor.png", 4, 2000,
                List.of(new SkillTypeEffect(PhaseSkillEffect.COMBAT_BOOST_WHILE_PHASED, 100f)), SkillTier.NOTABLE, null, null, null);
        SkillNode phaseNode = new SkillNode("phase_anchor_1", phaseType, List.of(), 0f, 0f);
        SkillTree.register(phaseNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(phaseNode);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        ShipAPI ship = mockShip(member, stats);
        when(ship.isPhased()).thenReturn(false);
        MutableStat dissipation = mock(MutableStat.class);
        when(stats.getFluxDissipation()).thenReturn(dissipation);
        when(stats.getBallisticRoFMult()).thenReturn(mock(MutableStat.class));
        when(stats.getEnergyRoFMult()).thenReturn(mock(MutableStat.class));
        when(stats.getMissileRoFMult()).thenReturn(mock(MutableStat.class));
        when(stats.getBallisticAmmoRegenMult()).thenReturn(mock(MutableStat.class));
        when(stats.getEnergyAmmoRegenMult()).thenReturn(mock(MutableStat.class));
        when(stats.getMissileAmmoRegenMult()).thenReturn(mock(MutableStat.class));

        new SkillTreeHullMod().advanceInCombat(ship, 0.1f);

        verify(dissipation).unmodifyMult("exiledSector_skill_phase_anchor_1");
        verify(dissipation, never()).modifyMult(anyString(), anyFloat());
    }

    @Test
    void advanceInCombatGrantsCommandPointRecoveryWhenFlagship() {
        SkillType opsType = new SkillType("operations_center", "Operations Center", "graphics/icons/notable_hullmods/operations_center.png", 4, 2000,
                List.of(new SkillTypeEffect(MiscSkillEffect.COMMAND_POINT_RECOVERY_WHILE_FLAGSHIP, 2.5f)), SkillTier.NOTABLE, null, null, null);
        SkillNode opsNode = new SkillNode("operations_center_1", opsType, List.of(), 0f, 0f);
        SkillTree.register(opsNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(opsNode);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        ShipAPI ship = mockShip(member, stats);
        CombatEngineAPI engine = mock(CombatEngineAPI.class);
        globalMock.when(Global::getCombatEngine).thenReturn(engine);
        when(engine.getPlayerShip()).thenReturn(ship);
        DynamicStatsAPI dynamic = mock(DynamicStatsAPI.class);
        when(stats.getDynamic()).thenReturn(dynamic);
        StatBonus commandPointRate = mock(StatBonus.class);
        when(dynamic.getMod("command_point_rate_flat")).thenReturn(commandPointRate);

        new SkillTreeHullMod().advanceInCombat(ship, 0.1f);

        verify(commandPointRate).modifyFlat("exiledSector_skill_operations_center_1", 2.5f);
        verify(commandPointRate, never()).unmodify(anyString());
    }

    @Test
    void advanceInCombatWithholdsCommandPointRecoveryWhenNotFlagship() {
        SkillType opsType = new SkillType("operations_center", "Operations Center", "graphics/icons/notable_hullmods/operations_center.png", 4, 2000,
                List.of(new SkillTypeEffect(MiscSkillEffect.COMMAND_POINT_RECOVERY_WHILE_FLAGSHIP, 2.5f)), SkillTier.NOTABLE, null, null, null);
        SkillNode opsNode = new SkillNode("operations_center_1", opsType, List.of(), 0f, 0f);
        SkillTree.register(opsNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(opsNode);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        ShipAPI ship = mockShip(member, stats);
        ShipAPI otherShip = mock(ShipAPI.class);
        CombatEngineAPI engine = mock(CombatEngineAPI.class);
        globalMock.when(Global::getCombatEngine).thenReturn(engine);
        when(engine.getPlayerShip()).thenReturn(otherShip);
        PersonAPI captain = mock(PersonAPI.class);
        when(ship.getCaptain()).thenReturn(captain);
        when(member.getFleetCommander()).thenReturn(null);
        when(member.getFleetCommanderForStats()).thenReturn(null);
        DynamicStatsAPI dynamic = mock(DynamicStatsAPI.class);
        when(stats.getDynamic()).thenReturn(dynamic);
        StatBonus commandPointRate = mock(StatBonus.class);
        when(dynamic.getMod("command_point_rate_flat")).thenReturn(commandPointRate);

        new SkillTreeHullMod().advanceInCombat(ship, 0.1f);

        verify(commandPointRate).unmodify("exiledSector_skill_operations_center_1");
        verify(commandPointRate, never()).modifyFlat(anyString(), anyFloat());
    }
}
