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
import com.fs.starfarer.api.combat.ShipVariantAPI;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        SkillTree.getAllTypes().clear();
    }

    @AfterEach
    void tearDown() {
        globalMock.close();
        SkillTree.getAllNodes().clear();
        SkillTree.getAllTypes().clear();
    }

    @Test
    void appliesTheEffectOfEachAllocatedNodeWithOneRegisteredOnTheTree() {
        SkillType hullType = new SkillType("hull", "Reinforced Hull", "graphics/hullmods/reinforced_bulkheads.png", List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)), SkillTier.SMALL, null, null, null);
        SkillNode hullNode = new SkillNode("hull_1", hullType, List.of(), 0f, 0f);
        SkillTree.register(hullNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(hullNode, 1);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(stats.getFleetMember()).thenReturn(member);
        com.fs.starfarer.api.combat.StatBonus hullStatBonus = mock(com.fs.starfarer.api.combat.StatBonus.class);
        when(stats.getHullBonus()).thenReturn(hullStatBonus);

        new SkillTreeHullMod().applyEffectsBeforeShipCreation(HullSize.FRIGATE, stats, "exiledSector_core");

        verify(hullStatBonus).modifyPercent("exiledSector_skill_hull_1", 10f);
    }

    @Test
    void appliesTheSelectedOptionsEffectForAnOptionalNodeNotThePlaceholders() {
        SkillType placeholder = new SkillType("slot", "Optional Skill", "a.png", List.of(), List.of(), SkillTier.SMALL, null, null, null, List.of("hull"));
        SkillType hullOption = new SkillType("hull", "Reinforced Hull", "graphics/hullmods/reinforced_bulkheads.png", List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)), SkillTier.SMALL, null, null, null);
        SkillTree.registerType(hullOption);
        SkillNode slotNode = new SkillNode("slot_1", placeholder, List.of(), 0f, 0f);
        SkillTree.register(slotNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").selectOption(slotNode, hullOption, 1);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(stats.getFleetMember()).thenReturn(member);
        com.fs.starfarer.api.combat.StatBonus hullStatBonus = mock(com.fs.starfarer.api.combat.StatBonus.class);
        when(stats.getHullBonus()).thenReturn(hullStatBonus);

        new SkillTreeHullMod().applyEffectsBeforeShipCreation(HullSize.FRIGATE, stats, "exiledSector_core");

        verify(hullStatBonus).modifyPercent("exiledSector_skill_slot_1", 10f);
    }

    @Test
    void appliesEachEffectOnANodeWithMultipleEffectsIndependently() {
        SkillType multiType = new SkillType("heavyarmor", "Heavy Armor", "graphics/icons/notable_hullmods/heavy_armor.png", List.of(new SkillTypeEffect(DefenseSkillEffect.ARMOR_PERCENT, 15f), new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 5f)),
                SkillTier.NOTABLE, null, null, null);
        SkillNode multiNode = new SkillNode("heavyarmor_1", multiType, List.of(), 0f, 0f);
        SkillTree.register(multiNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(multiNode, 1);

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
        SkillType cosmeticType = new SkillType("capacitors", "Capacitors", "graphics/hullmods/flux_coil_adjunct.png", List.of(), SkillTier.SMALL, null, null, null);
        SkillNode cosmeticNode = new SkillNode("capacitors_1", cosmeticType, List.of(), 0f, 0f);
        SkillTree.register(cosmeticNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(cosmeticNode, 1);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(stats.getFleetMember()).thenReturn(member);

        new SkillTreeHullMod().applyEffectsBeforeShipCreation(HullSize.FRIGATE, stats, "exiledSector_core");

        verify(stats, never()).getHullBonus();
    }

    @Test
    void delegatesToTheRealVanillaHullModEffectWhenTypeSpecifiesOne() {
        SkillType keystoneType = new SkillType("safety_overrides", "Safety Overrides", "graphics/icons/skills/helmsmanship.png", List.of(), SkillTier.KEYSTONE, "safetyoverrides", null, null);
        SkillNode keystoneNode = new SkillNode("safety_overrides_1", keystoneType, List.of(), 0f, 0f);
        SkillTree.register(keystoneNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(keystoneNode, 1);

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

    @Test
    void beforeShipCreationRemovesARealHullModThatConflictsWithAnAllocatedSkill() {
        SkillType frontType = new SkillType("frontemitter", "Shield Conversion - Front", "a.png", List.of(), List.of(), SkillTier.NOTABLE, null, null, null,
                List.of(), List.of("adaptiveshields"));
        SkillNode frontNode = new SkillNode("frontemitter_1", frontType, List.of(), 0f, 0f);
        SkillTree.register(frontNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(frontNode, 1);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(stats.getFleetMember()).thenReturn(member);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(stats.getVariant()).thenReturn(variant);
        when(variant.hasHullMod("adaptiveshields")).thenReturn(true);
        when(variant.getHullMods()).thenReturn(new LinkedHashSet<>(List.of("adaptiveshields")));
        when(variant.getSMods()).thenReturn(new LinkedHashSet<>());

        SettingsAPI settings = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settings);

        new SkillTreeHullMod().applyEffectsBeforeShipCreation(HullSize.FRIGATE, stats, "exiledSector_core");

        verify(variant).removeMod("adaptiveshields");
        verify(variant).removeMod("ML_incompatibleHullmodWarning");
        verify(variant).addMod("exiledSector_conflictWarning");
        verify(variant, never()).removeMod(SkillTreeHullMod.ID);

        SkillConflictWarnings.Removal removal = SkillConflictWarnings.get(variant);
        assertEquals("adaptiveshields", removal.removedHullModId);
        assertEquals("Shield Conversion - Front", removal.causeSkillDisplayName);
    }

    @Test
    void beforeShipCreationNeverRemovesTheUmbrellaHullModEvenWhenTheConflictingHullModIsSModded() {
        SkillType hullType = new SkillType("hull", "Reinforced Hull", "graphics/hullmods/reinforced_bulkheads.png", List.of(), List.of(), SkillTier.SMALL, null, null, null,
                List.of(), List.of("armoredcladding"));
        SkillNode hullNode = new SkillNode("hull_1", hullType, List.of(), 0f, 0f);
        SkillTree.register(hullNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(hullNode, 1);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(stats.getFleetMember()).thenReturn(member);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(stats.getVariant()).thenReturn(variant);
        when(variant.hasHullMod("armoredcladding")).thenReturn(true);
        when(variant.getHullMods()).thenReturn(new LinkedHashSet<>(List.of("armoredcladding")));
        when(variant.getSMods()).thenReturn(new LinkedHashSet<>(List.of("armoredcladding")));

        SettingsAPI settings = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settings);

        new SkillTreeHullMod().applyEffectsBeforeShipCreation(HullSize.FRIGATE, stats, "exiledSector_core");

        verify(variant, never()).removeMod(SkillTreeHullMod.ID);
        verify(variant, never()).removeMod("armoredcladding");
        verify(variant).removeMod("exiledSector_conflictWarning");
    }

    @Test
    void beforeShipCreationDoesNotRemoveAnythingWhenNoConflictingHullModIsInstalled() {
        SkillType frontType = new SkillType("frontemitter", "Shield Conversion - Front", "a.png", List.of(), List.of(), SkillTier.NOTABLE, null, null, null,
                List.of(), List.of("adaptiveshields"));
        SkillNode frontNode = new SkillNode("frontemitter_1", frontType, List.of(), 0f, 0f);
        SkillTree.register(frontNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(frontNode, 1);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(stats.getFleetMember()).thenReturn(member);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(stats.getVariant()).thenReturn(variant);
        when(variant.hasHullMod("adaptiveshields")).thenReturn(false);

        SettingsAPI settings = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settings);

        new SkillTreeHullMod().applyEffectsBeforeShipCreation(HullSize.FRIGATE, stats, "exiledSector_core");

        verify(variant, never()).removeMod(anyString());
    }

    @Test
    void beforeShipCreationInstallsAndCostsTheOpSpentHullModWhenOpHasBeenSpentOnAllocatedNodes() {
        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        SkillType type = new SkillType("t", "t", "a.png", List.of(), SkillTier.SMALL, null, null, null);
        SkillNode node = new SkillNode("armor_1", type, List.of(), 0f, 0f);
        ShipSkillDataManager.get("ship-a").allocate(node, 4);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(stats.getFleetMember()).thenReturn(member);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(stats.getVariant()).thenReturn(variant);
        when(variant.hasHullMod("exiledSector_opSpent")).thenReturn(false);

        SettingsAPI settings = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settings);
        HullModSpecAPI opSpentSpec = mock(HullModSpecAPI.class);
        when(settings.getHullModSpec("exiledSector_opSpent")).thenReturn(opSpentSpec);

        new SkillTreeHullMod().applyEffectsBeforeShipCreation(HullSize.FRIGATE, stats, "exiledSector_core");

        verify(opSpentSpec).setFrigateCost(4);
        verify(opSpentSpec).setDestroyerCost(4);
        verify(opSpentSpec).setCruiserCost(4);
        verify(opSpentSpec).setCapitalCost(4);
        verify(variant).addMod("exiledSector_opSpent");
    }

    @Test
    void beforeShipCreationRemovesTheOpSpentHullModWhenNoOpHasBeenSpentOnAllocatedNodes() {
        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(stats.getFleetMember()).thenReturn(member);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(stats.getVariant()).thenReturn(variant);
        when(variant.hasHullMod("exiledSector_opSpent")).thenReturn(true);

        SettingsAPI settings = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settings);
        HullModSpecAPI opSpentSpec = mock(HullModSpecAPI.class);
        when(settings.getHullModSpec("exiledSector_opSpent")).thenReturn(opSpentSpec);

        new SkillTreeHullMod().applyEffectsBeforeShipCreation(HullSize.FRIGATE, stats, "exiledSector_core");

        verify(variant).removeMod("exiledSector_opSpent");
        verify(variant, never()).addMod(anyString());
    }

    @Test
    void delegatesToTheRealVanillaHullModEffectAfterShipCreationWhenTypeSpecifiesOne() {
        SkillType keystoneType = new SkillType("frontshield", "Makeshift Shield Generator", "graphics/icons/skills/front_shield_generator.png", List.of(), SkillTier.KEYSTONE, "frontshield", null, null);
        SkillNode keystoneNode = new SkillNode("frontshield_1", keystoneType, List.of(), 0f, 0f);
        SkillTree.register(keystoneNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(keystoneNode, 1);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        ShipAPI ship = mockShip(member, stats);

        SettingsAPI settings = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settings);
        HullModSpecAPI spec = mock(HullModSpecAPI.class);
        when(settings.getHullModSpec("frontshield")).thenReturn(spec);
        HullModEffect vanillaEffect = mock(HullModEffect.class);
        when(spec.getEffect()).thenReturn(vanillaEffect);

        new SkillTreeHullMod().applyEffectsAfterShipCreation(ship, "exiledSector_core");

        verify(vanillaEffect).applyEffectsAfterShipCreation(ship, "frontshield");
    }

    @Test
    void afterShipCreationSkipsNodesWithoutAVanillaHullMod() {
        SkillType hullType = new SkillType("hull", "Reinforced Hull", "graphics/hullmods/reinforced_bulkheads.png", List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)), SkillTier.SMALL, null, null, null);
        SkillNode hullNode = new SkillNode("hull_1", hullType, List.of(), 0f, 0f);
        SkillTree.register(hullNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(hullNode, 1);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        ShipAPI ship = mockShip(member, stats);

        SettingsAPI settings = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settings);

        new SkillTreeHullMod().applyEffectsAfterShipCreation(ship, "exiledSector_core");

        verify(settings, never()).getHullModSpec(anyString());
    }

    @Test
    void fighterSpawnDelegatesToTheRealVanillaHullModEffectWhenTypeSpecifiesOne() {
        SkillType passthroughType = new SkillType("defensive_targeting_array", "Defensive Targeting Array", "graphics/icons/skills/defensive_targeting_array.png", List.of(), SkillTier.NOTABLE, "defensive_targeting_array", null, null);
        SkillNode passthroughNode = new SkillNode("defensive_targeting_array_1", passthroughType, List.of(), 0f, 0f);
        SkillTree.register(passthroughNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(passthroughNode, 1);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        ShipAPI ship = mockShip(member, stats);
        ShipAPI fighter = mock(ShipAPI.class);

        SettingsAPI settings = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settings);
        HullModSpecAPI spec = mock(HullModSpecAPI.class);
        when(settings.getHullModSpec("defensive_targeting_array")).thenReturn(spec);
        HullModEffect vanillaEffect = mock(HullModEffect.class);
        when(spec.getEffect()).thenReturn(vanillaEffect);

        new SkillTreeHullMod().applyEffectsToFighterSpawnedByShip(fighter, ship, "exiledSector_core");

        verify(vanillaEffect).applyEffectsToFighterSpawnedByShip(fighter, ship, "defensive_targeting_array");
    }

    @Test
    void fighterSpawnCallsApplyToFighterSpawnedByShipOnNonPassthroughEffects() {
        exiledsector.skills.skilleffect.SkillEffect fighterEffect = mock(exiledsector.skills.skilleffect.SkillEffect.class);
        SkillType fighterType = new SkillType("fighter_weapon_damage", "Fighter Weapon Damage", "graphics/hullmods/fighter_uplink2.png", List.of(new SkillTypeEffect(fighterEffect, 15f)), SkillTier.SMALL, null, null, null);
        SkillNode fighterNode = new SkillNode("fighter_weapon_damage_1", fighterType, List.of(), 0f, 0f);
        SkillTree.register(fighterNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(fighterNode, 1);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        ShipAPI ship = mockShip(member, stats);
        ShipAPI fighter = mock(ShipAPI.class);

        new SkillTreeHullMod().applyEffectsToFighterSpawnedByShip(fighter, ship, "exiledSector_core");

        verify(fighterEffect).applyToFighterSpawnedByShip(fighter, ship, "exiledSector_skill_fighter_weapon_damage_1", 15f);
    }

    @Test
    void fighterSpawnDoesNothingWhenTheShipHasNoFleetMember() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(stats.getFleetMember()).thenReturn(null);
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.getMutableStats()).thenReturn(stats);
        ShipAPI fighter = mock(ShipAPI.class);

        new SkillTreeHullMod().applyEffectsToFighterSpawnedByShip(fighter, ship, "exiledSector_core");

        globalMock.verify(Global::getSettings, never());
    }

    @Test
    void afterShipCreationCallsApplyAfterShipCreationOnNonPassthroughEffects() {
        exiledsector.skills.skilleffect.SkillEffect listenerEffect = mock(exiledsector.skills.skilleffect.SkillEffect.class);
        SkillType listenerType = new SkillType("high_scatter_amp", "High Scatter Amplifier", "graphics/hullmods/high_scatter_amp.png", List.of(new SkillTypeEffect(listenerEffect, 50f)), SkillTier.NOTABLE, null, null, null);
        SkillNode listenerNode = new SkillNode("high_scatter_amp_1", listenerType, List.of(), 0f, 0f);
        SkillTree.register(listenerNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(listenerNode, 1);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        ShipAPI ship = mockShip(member, stats);

        new SkillTreeHullMod().applyEffectsAfterShipCreation(ship, "exiledSector_core");

        verify(listenerEffect).applyAfterShipCreation(ship, "exiledSector_skill_high_scatter_amp_1", 50f);
    }

    @Test
    void afterShipCreationDoesNothingWhenTheShipHasNoFleetMember() {
        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(stats.getFleetMember()).thenReturn(null);
        ShipAPI ship = mock(ShipAPI.class);
        when(ship.getMutableStats()).thenReturn(stats);

        new SkillTreeHullMod().applyEffectsAfterShipCreation(ship, "exiledSector_core");

        globalMock.verify(Global::getSettings, never());
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
        SkillType ventType = new SkillType("fluxbreakers", "Resistant Flux Conduits", "graphics/icons/notable_hullmods/resistant_flux_conduits.png", List.of(new SkillTypeEffect(FluxSkillEffect.FLUX_DISSIPATION_WHILE_VENTING_PERCENT, 25f)), SkillTier.NOTABLE, null, null, null);
        SkillNode ventNode = new SkillNode("fluxbreakers_1", ventType, List.of(), 0f, 0f);
        SkillTree.register(ventNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(ventNode, 1);

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
        SkillType ventType = new SkillType("fluxbreakers", "Resistant Flux Conduits", "graphics/icons/notable_hullmods/resistant_flux_conduits.png", List.of(new SkillTypeEffect(FluxSkillEffect.FLUX_DISSIPATION_WHILE_VENTING_PERCENT, 25f)), SkillTier.NOTABLE, null, null, null);
        SkillNode ventNode = new SkillNode("fluxbreakers_1", ventType, List.of(), 0f, 0f);
        SkillTree.register(ventNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(ventNode, 1);

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
        SkillType hullType = new SkillType("hull", "Reinforced Hull", "graphics/hullmods/reinforced_bulkheads.png", List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)), SkillTier.SMALL, null, null, null);
        SkillNode hullNode = new SkillNode("hull_1", hullType, List.of(), 0f, 0f);
        SkillTree.register(hullNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(hullNode, 1);

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
        SkillType phaseType = new SkillType("phase_anchor", "Phase Anchor", "graphics/icons/notable_hullmods/phase_anchor.png", List.of(new SkillTypeEffect(PhaseSkillEffect.COMBAT_BOOST_WHILE_PHASED, 100f)), SkillTier.NOTABLE, null, null, null);
        SkillNode phaseNode = new SkillNode("phase_anchor_1", phaseType, List.of(), 0f, 0f);
        SkillTree.register(phaseNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(phaseNode, 1);

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
        SkillType phaseType = new SkillType("phase_anchor", "Phase Anchor", "graphics/icons/notable_hullmods/phase_anchor.png", List.of(new SkillTypeEffect(PhaseSkillEffect.COMBAT_BOOST_WHILE_PHASED, 100f)), SkillTier.NOTABLE, null, null, null);
        SkillNode phaseNode = new SkillNode("phase_anchor_1", phaseType, List.of(), 0f, 0f);
        SkillTree.register(phaseNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(phaseNode, 1);

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
        SkillType opsType = new SkillType("operations_center", "Operations Center", "graphics/icons/notable_hullmods/operations_center.png", List.of(new SkillTypeEffect(MiscSkillEffect.COMMAND_POINT_RECOVERY_WHILE_FLAGSHIP, 2.5f)), SkillTier.NOTABLE, null, null, null);
        SkillNode opsNode = new SkillNode("operations_center_1", opsType, List.of(), 0f, 0f);
        SkillTree.register(opsNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(opsNode, 1);

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
        SkillType opsType = new SkillType("operations_center", "Operations Center", "graphics/icons/notable_hullmods/operations_center.png", List.of(new SkillTypeEffect(MiscSkillEffect.COMMAND_POINT_RECOVERY_WHILE_FLAGSHIP, 2.5f)), SkillTier.NOTABLE, null, null, null);
        SkillNode opsNode = new SkillNode("operations_center_1", opsType, List.of(), 0f, 0f);
        SkillTree.register(opsNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillDataManager.get("ship-a").allocate(opsNode, 1);

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
