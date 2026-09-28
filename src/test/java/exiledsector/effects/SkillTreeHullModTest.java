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
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
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
import static org.junit.jupiter.api.Assertions.assertNull;
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
        SkillType hullType = new SkillType.Builder("hull", "Reinforced Hull", "graphics/hullmods/reinforced_bulkheads.png", SkillTier.SMALL)
                .effects(List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)))
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
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
        SkillType placeholder = new SkillType.Builder("slot", "Optional Skill", "a.png", SkillTier.SMALL)
                .effects(List.of())
                .hullSizeEffects(List.of())
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .optionalOptionIds(List.of("hull"))
                .build();
        SkillType hullOption = new SkillType.Builder("hull", "Reinforced Hull", "graphics/hullmods/reinforced_bulkheads.png", SkillTier.SMALL)
                .effects(List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)))
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
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
        SkillType multiType = new SkillType.Builder("heavyarmor", "Heavy Armor", "graphics/icons/notable_hullmods/heavy_armor.png", SkillTier.NOTABLE)
                .effects(List.of(new SkillTypeEffect(DefenseSkillEffect.ARMOR_PERCENT, 15f), new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 5f)))
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
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
        SkillType cosmeticType = new SkillType.Builder("capacitors", "Capacitors", "graphics/hullmods/flux_coil_adjunct.png", SkillTier.SMALL)
                .effects(List.of())
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
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
        SkillType keystoneType = new SkillType.Builder("safety_overrides", "Safety Overrides", "graphics/icons/skills/helmsmanship.png", SkillTier.KEYSTONE)
                .effects(List.of())
                .vanillaHullModId("safetyoverrides")
                .descriptionOverride(null)
                .todo(null)
                .build();
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
        SkillType frontType = new SkillType.Builder("frontemitter", "Shield Conversion - Front", "a.png", SkillTier.NOTABLE)
                .effects(List.of())
                .hullSizeEffects(List.of())
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .optionalOptionIds(List.of())
                .exclusiveHullModIds(List.of("adaptiveshields"))
                .build();
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
        SkillType hullType = new SkillType.Builder("hull", "Reinforced Hull", "graphics/hullmods/reinforced_bulkheads.png", SkillTier.SMALL)
                .effects(List.of())
                .hullSizeEffects(List.of())
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .optionalOptionIds(List.of())
                .exclusiveHullModIds(List.of("armoredcladding"))
                .build();
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
    void removeHullModsConflictingWithAllocatedSkillsRemovesTheWarningHullModOnceTheConflictIsGone() {
        SkillType frontType = new SkillType.Builder("frontemitter", "Shield Conversion - Front", "a.png", SkillTier.NOTABLE)
                .effects(List.of())
                .hullSizeEffects(List.of())
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .optionalOptionIds(List.of())
                .exclusiveHullModIds(List.of("adaptiveshields"))
                .build();
        SkillNode frontNode = new SkillNode("frontemitter_1", frontType, List.of(), 0f, 0f);
        SkillTree.register(frontNode);

        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");
        ShipSkillData data = ShipSkillDataManager.get("ship-a");
        data.allocate(frontNode, 1);

        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(variant.hasHullMod("adaptiveshields")).thenReturn(true);
        when(variant.getHullMods()).thenReturn(new LinkedHashSet<>(List.of("adaptiveshields")));
        when(variant.getSMods()).thenReturn(new LinkedHashSet<>());

        SettingsAPI settings = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settings);

        SkillTreeHullMod.removeHullModsConflictingWithAllocatedSkills(member, variant);
        verify(variant).addMod("exiledSector_conflictWarning");

        data.deallocate(frontNode, 1);
        when(variant.hasHullMod("adaptiveshields")).thenReturn(false);
        when(variant.hasHullMod("exiledSector_conflictWarning")).thenReturn(true);

        SkillTreeHullMod.removeHullModsConflictingWithAllocatedSkills(member, variant);

        verify(variant).removeMod("exiledSector_conflictWarning");
        assertNull(SkillConflictWarnings.get(variant));
    }

    private static SkillNode registerMilitarizedNode() {
        SkillType militarizedType = new SkillType.Builder("militarized_subsystems", "Militarized Subsystems", "a.png", SkillTier.NOTABLE)
                .installedHullModIds(List.of("militarized_subsystems"))
                .build();
        SkillNode militarizedNode = new SkillNode("militarized_subsystems_1", militarizedType, List.of(), 0f, 0f);
        SkillTree.register(militarizedNode);
        return militarizedNode;
    }

    private static FleetMemberAPI memberWithId(String id) {
        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn(id);
        return member;
    }

    @Test
    void syncInstalledHullModsInstallsTheHullModAsATaggedPermaModWhileTheNodeIsAllocated() {
        ShipSkillDataManager.get("ship-a").allocate(registerMilitarizedNode(), 1);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(variant.getTags()).thenReturn(List.of());

        SkillTreeHullMod.syncInstalledHullMods(memberWithId("ship-a"), variant);

        verify(variant).addPermaMod("militarized_subsystems");
        verify(variant).addTag("exiledSector_installed_militarized_subsystems");
    }

    @Test
    void syncInstalledHullModsRemovesOnlyHullModsItInstalledOnceTheNodeIsNoLongerAllocated() {
        registerMilitarizedNode();
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(variant.getTags()).thenReturn(List.of("exiledSector_installed_militarized_subsystems", "some_other_tag"));

        SkillTreeHullMod.syncInstalledHullMods(memberWithId("ship-a"), variant);

        verify(variant).removePermaMod("militarized_subsystems");
        verify(variant).removeTag("exiledSector_installed_militarized_subsystems");
        verify(variant, never()).removeTag("some_other_tag");
    }

    @Test
    void removeHullModsConflictingWithAllocatedSkillsLeavesAHullModTheSkillTreeInstalledItself() {
        ShipSkillDataManager.get("ship-a").allocate(registerMilitarizedNode(), 1);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(variant.hasHullMod("militarized_subsystems")).thenReturn(true);
        when(variant.hasTag("exiledSector_installed_militarized_subsystems")).thenReturn(true);

        SkillTreeHullMod.removeHullModsConflictingWithAllocatedSkills(memberWithId("ship-a"), variant);

        verify(variant, never()).removeMod("militarized_subsystems");
        verify(variant, never()).addMod("exiledSector_conflictWarning");
    }

    @Test
    void removeHullModsConflictingWithAllocatedSkillsNeverStripsABuiltInHullMod() {
        SkillType targetingType = new SkillType.Builder("targetingunit", "Integrated Targeting Unit", "a.png", SkillTier.NOTABLE)
                .exclusiveHullModIds(List.of("advancedcore"))
                .build();
        SkillNode targetingNode = new SkillNode("targetingunit_1", targetingType, List.of(), 0f, 0f);
        SkillTree.register(targetingNode);
        ShipSkillDataManager.get("ship-a").allocate(targetingNode, 1);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        ShipHullSpecAPI hullSpec = mock(ShipHullSpecAPI.class);
        when(variant.getHullSpec()).thenReturn(hullSpec);
        when(hullSpec.isBuiltInMod("advancedcore")).thenReturn(true);
        when(variant.hasHullMod("advancedcore")).thenReturn(true);

        SkillTreeHullMod.removeHullModsConflictingWithAllocatedSkills(memberWithId("ship-a"), variant);

        verify(variant, never()).removeMod("advancedcore");
        verify(variant, never()).addMod("exiledSector_conflictWarning");
    }

    @Test
    void removeHullModsConflictingWithAllocatedSkillsDoesNothingWhenNoConflictWasEverPresent() {
        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");

        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(variant.hasHullMod("exiledSector_conflictWarning")).thenReturn(false);

        SkillTreeHullMod.removeHullModsConflictingWithAllocatedSkills(member, variant);

        verify(variant, never()).removeMod("exiledSector_conflictWarning");
    }

    @Test
    void beforeShipCreationDoesNotRemoveAnythingWhenNoConflictingHullModIsInstalled() {
        SkillType frontType = new SkillType.Builder("frontemitter", "Shield Conversion - Front", "a.png", SkillTier.NOTABLE)
                .effects(List.of())
                .hullSizeEffects(List.of())
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .optionalOptionIds(List.of())
                .exclusiveHullModIds(List.of("adaptiveshields"))
                .build();
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
        SkillType type = new SkillType.Builder("t", "t", "a.png", SkillTier.SMALL)
                .effects(List.of())
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
        SkillNode node = new SkillNode("armor_1", type, List.of(), 0f, 0f);
        ShipSkillDataManager.get("ship-a").allocate(node, 4);

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(stats.getFleetMember()).thenReturn(member);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(stats.getVariant()).thenReturn(variant);
        when(variant.hasHullMod("exiledSector_opSpent_0")).thenReturn(false);

        SettingsAPI settings = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settings);
        HullModSpecAPI opSpentSpec = mock(HullModSpecAPI.class);
        when(settings.getHullModSpec("exiledSector_opSpent_0")).thenReturn(opSpentSpec);

        new SkillTreeHullMod().applyEffectsBeforeShipCreation(HullSize.FRIGATE, stats, "exiledSector_core");

        verify(opSpentSpec).setFrigateCost(4);
        verify(opSpentSpec).setDestroyerCost(4);
        verify(opSpentSpec).setCruiserCost(4);
        verify(opSpentSpec).setCapitalCost(4);
        verify(variant).addMod("exiledSector_opSpent_0");
    }

    @Test
    void beforeShipCreationRemovesTheOpSpentHullModWhenNoOpHasBeenSpentOnAllocatedNodes() {
        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-a");

        MutableShipStatsAPI stats = mock(MutableShipStatsAPI.class);
        when(stats.getFleetMember()).thenReturn(member);
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        when(stats.getVariant()).thenReturn(variant);
        when(variant.hasHullMod("exiledSector_opSpent_0")).thenReturn(true);

        SettingsAPI settings = mock(SettingsAPI.class);
        globalMock.when(Global::getSettings).thenReturn(settings);
        HullModSpecAPI opSpentSpec = mock(HullModSpecAPI.class);
        when(settings.getHullModSpec("exiledSector_opSpent_0")).thenReturn(opSpentSpec);

        new SkillTreeHullMod().applyEffectsBeforeShipCreation(HullSize.FRIGATE, stats, "exiledSector_core");

        verify(variant).removeMod("exiledSector_opSpent_0");
        verify(variant, never()).addMod(anyString());
    }

    @Test
    void delegatesToTheRealVanillaHullModEffectAfterShipCreationWhenTypeSpecifiesOne() {
        SkillType keystoneType = new SkillType.Builder("frontshield", "Makeshift Shield Generator", "graphics/icons/skills/front_shield_generator.png", SkillTier.KEYSTONE)
                .effects(List.of())
                .vanillaHullModId("frontshield")
                .descriptionOverride(null)
                .todo(null)
                .build();
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
        SkillType hullType = new SkillType.Builder("hull", "Reinforced Hull", "graphics/hullmods/reinforced_bulkheads.png", SkillTier.SMALL)
                .effects(List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)))
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
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
        SkillType passthroughType = new SkillType.Builder("defensive_targeting_array", "Defensive Targeting Array", "graphics/icons/skills/defensive_targeting_array.png", SkillTier.NOTABLE)
                .effects(List.of())
                .vanillaHullModId("defensive_targeting_array")
                .descriptionOverride(null)
                .todo(null)
                .build();
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
        SkillType fighterType = new SkillType.Builder("fighter_weapon_damage", "Fighter Weapon Damage", "graphics/hullmods/fighter_uplink2.png", SkillTier.SMALL)
                .effects(List.of(new SkillTypeEffect(fighterEffect, 15f)))
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
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
        SkillType listenerType = new SkillType.Builder("high_scatter_amp", "High Scatter Amplifier", "graphics/hullmods/high_scatter_amp.png", SkillTier.NOTABLE)
                .effects(List.of(new SkillTypeEffect(listenerEffect, 50f)))
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
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
        SkillType ventType = new SkillType.Builder("fluxbreakers", "Resistant Flux Conduits", "graphics/icons/notable_hullmods/resistant_flux_conduits.png", SkillTier.NOTABLE)
                .effects(List.of(new SkillTypeEffect(FluxSkillEffect.FLUX_DISSIPATION_WHILE_VENTING_PERCENT, 25f)))
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
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
        SkillType ventType = new SkillType.Builder("fluxbreakers", "Resistant Flux Conduits", "graphics/icons/notable_hullmods/resistant_flux_conduits.png", SkillTier.NOTABLE)
                .effects(List.of(new SkillTypeEffect(FluxSkillEffect.FLUX_DISSIPATION_WHILE_VENTING_PERCENT, 25f)))
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
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
        SkillType hullType = new SkillType.Builder("hull", "Reinforced Hull", "graphics/hullmods/reinforced_bulkheads.png", SkillTier.SMALL)
                .effects(List.of(new SkillTypeEffect(DefenseSkillEffect.HULL_PERCENT, 10f)))
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
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
        SkillType phaseType = new SkillType.Builder("phase_anchor", "Phase Anchor", "graphics/icons/notable_hullmods/phase_anchor.png", SkillTier.NOTABLE)
                .effects(List.of(new SkillTypeEffect(PhaseSkillEffect.COMBAT_BOOST_WHILE_PHASED, 100f)))
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
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
        MutableStat ballisticRoF = mock(MutableStat.class);
        MutableStat energyRoF = mock(MutableStat.class);
        MutableStat missileRoF = mock(MutableStat.class);
        MutableStat ballisticAmmoRegen = mock(MutableStat.class);
        MutableStat energyAmmoRegen = mock(MutableStat.class);
        MutableStat missileAmmoRegen = mock(MutableStat.class);
        when(stats.getBallisticRoFMult()).thenReturn(ballisticRoF);
        when(stats.getEnergyRoFMult()).thenReturn(energyRoF);
        when(stats.getMissileRoFMult()).thenReturn(missileRoF);
        when(stats.getBallisticAmmoRegenMult()).thenReturn(ballisticAmmoRegen);
        when(stats.getEnergyAmmoRegenMult()).thenReturn(energyAmmoRegen);
        when(stats.getMissileAmmoRegenMult()).thenReturn(missileAmmoRegen);

        new SkillTreeHullMod().advanceInCombat(ship, 0.1f);

        verify(dissipation).modifyMult("exiledSector_skill_phase_anchor_1", 2f);
        verify(dissipation, never()).unmodifyMult(anyString());
    }

    @Test
    void advanceInCombatUndoesTheBoostWhenNotPhased() {
        SkillType phaseType = new SkillType.Builder("phase_anchor", "Phase Anchor", "graphics/icons/notable_hullmods/phase_anchor.png", SkillTier.NOTABLE)
                .effects(List.of(new SkillTypeEffect(PhaseSkillEffect.COMBAT_BOOST_WHILE_PHASED, 100f)))
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
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
        MutableStat ballisticRoF = mock(MutableStat.class);
        MutableStat energyRoF = mock(MutableStat.class);
        MutableStat missileRoF = mock(MutableStat.class);
        MutableStat ballisticAmmoRegen = mock(MutableStat.class);
        MutableStat energyAmmoRegen = mock(MutableStat.class);
        MutableStat missileAmmoRegen = mock(MutableStat.class);
        when(stats.getBallisticRoFMult()).thenReturn(ballisticRoF);
        when(stats.getEnergyRoFMult()).thenReturn(energyRoF);
        when(stats.getMissileRoFMult()).thenReturn(missileRoF);
        when(stats.getBallisticAmmoRegenMult()).thenReturn(ballisticAmmoRegen);
        when(stats.getEnergyAmmoRegenMult()).thenReturn(energyAmmoRegen);
        when(stats.getMissileAmmoRegenMult()).thenReturn(missileAmmoRegen);

        new SkillTreeHullMod().advanceInCombat(ship, 0.1f);

        verify(dissipation).unmodifyMult("exiledSector_skill_phase_anchor_1");
        verify(dissipation, never()).modifyMult(anyString(), anyFloat());
    }

    @Test
    void advanceInCombatGrantsCommandPointRecoveryWhenFlagship() {
        SkillType opsType = new SkillType.Builder("operations_center", "Operations Center", "graphics/icons/notable_hullmods/operations_center.png", SkillTier.NOTABLE)
                .effects(List.of(new SkillTypeEffect(MiscSkillEffect.COMMAND_POINT_RECOVERY_WHILE_FLAGSHIP, 2.5f)))
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
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
        SkillType opsType = new SkillType.Builder("operations_center", "Operations Center", "graphics/icons/notable_hullmods/operations_center.png", SkillTier.NOTABLE)
                .effects(List.of(new SkillTypeEffect(MiscSkillEffect.COMMAND_POINT_RECOVERY_WHILE_FLAGSHIP, 2.5f)))
                .vanillaHullModId(null)
                .descriptionOverride(null)
                .todo(null)
                .build();
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
