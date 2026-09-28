package exiledsector.effects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.VariantSource;
import exiledsector.skills.SkillDataResolver;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import exiledsector.skills.enemy.EnemyLayout;
import exiledsector.skills.enemy.EnemyLayoutEntry;
import exiledsector.skills.enemy.EnemyLayouts;
import exiledsector.skills.enemy.EnemyTreeConfig;
import exiledsector.skills.enemy.EnemyTreeRecords;
import exiledsector.skills.enemy.EnemyTreeTag;
import lunalib.lunaSettings.LunaSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.util.vector.Vector2f;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnemyFleetLevellerTest {

    private MockedStatic<Global> globalMock;
    private MockedStatic<LunaSettings> lunaSettingsMock;
    private MutableCharacterStatsAPI playerStats;
    private CampaignFleetAPI playerFleet;
    private LocationAPI location;

    @BeforeEach
    void setUp() {
        SkillDataResolver.clearCache();
        SkillTree.getAllNodes().clear();
        SkillTree.getAllTypes().clear();
        SkillTree.register(new SkillNode("root_1", type("root", SkillTier.ROOT).build(), List.of(), 0f, 0f));
        SkillTree.register(new SkillNode("a_1", type("a", SkillTier.SMALL).build(), List.of("root_1"), 0f, 0f));
        SkillTree.register(new SkillNode("armor_1", type("heavyarmor", SkillTier.NOTABLE)
                .exclusiveHullModIds(List.of("heavyarmor")).build(), List.of("a_1"), 0f, 0f));
        EnemyLayouts.register(Map.of("bulwark", new EnemyLayout("bulwark", "Bulwark", "root_1", List.of(), "",
                List.of(new EnemyLayoutEntry("a_1", null), new EnemyLayoutEntry("armor_1", null)))));

        playerStats = mock(MutableCharacterStatsAPI.class);
        when(playerStats.getLevel()).thenReturn(15);
        location = mock(LocationAPI.class);
        playerFleet = mock(CampaignFleetAPI.class);
        when(playerFleet.getContainingLocation()).thenReturn(location);
        when(playerFleet.getLocation()).thenReturn(new Vector2f(0f, 0f));
        SectorAPI sector = mock(SectorAPI.class);
        when(sector.getPlayerStats()).thenReturn(playerStats);
        when(sector.getSeedString()).thenReturn("SEED-1");
        when(sector.getPlayerFleet()).thenReturn(playerFleet);
        lunaSettingsMock = Mockito.mockStatic(LunaSettings.class, invocation -> null);
        globalMock = Mockito.mockStatic(Global.class);
        globalMock.when(Global::getSector).thenReturn(sector);
    }

    @AfterEach
    void tearDown() {
        lunaSettingsMock.close();
        globalMock.close();
        EnemyLayouts.register(Map.of());
        SkillDataResolver.clearCache();
        SkillTree.getAllNodes().clear();
        SkillTree.getAllTypes().clear();
    }

    private static SkillType.Builder type(String id, SkillTier tier) {
        return new SkillType.Builder(id, id, "a.png", tier);
    }

    private static ShipVariantAPI statefulVariant(Set<String> hullMods, List<String> tags) {
        ShipVariantAPI variant = mock(ShipVariantAPI.class);
        ShipHullSpecAPI hullSpec = mock(ShipHullSpecAPI.class);
        when(hullSpec.getHullSize()).thenReturn(HullSize.DESTROYER);
        when(hullSpec.getShieldType()).thenReturn(ShieldType.FRONT);
        when(variant.getHullSpec()).thenReturn(hullSpec);
        when(variant.getSource()).thenReturn(VariantSource.REFIT);
        when(variant.getHullMods()).thenAnswer(invocation -> new LinkedHashSet<>(hullMods));
        when(variant.hasHullMod(anyString())).thenAnswer(invocation -> hullMods.contains((String) invocation.getArgument(0)));
        doAnswer(invocation -> hullMods.add(invocation.getArgument(0))).when(variant).addMod(anyString());
        doAnswer(invocation -> hullMods.remove((String) invocation.getArgument(0))).when(variant).removeMod(anyString());
        when(variant.getTags()).thenAnswer(invocation -> new ArrayList<>(tags));
        doAnswer(invocation -> tags.add(invocation.getArgument(0))).when(variant).addTag(anyString());
        doAnswer(invocation -> tags.remove((String) invocation.getArgument(0))).when(variant).removeTag(anyString());
        return variant;
    }

    private static FleetMemberAPI member(String id, ShipVariantAPI variant, boolean officered) {
        FleetMemberAPI member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn(id);
        when(member.getVariant()).thenReturn(variant);
        ShipHullSpecAPI hullSpec = variant.getHullSpec();
        when(member.getHullSpec()).thenReturn(hullSpec);
        PersonAPI captain = mock(PersonAPI.class);
        when(captain.isDefault()).thenReturn(!officered);
        when(member.getCaptain()).thenReturn(captain);
        return member;
    }

    private static CampaignFleetAPI fleet(String id, List<FleetMemberAPI> members, LocationAPI location, Vector2f position) {
        CampaignFleetAPI fleet = mock(CampaignFleetAPI.class);
        when(fleet.getId()).thenReturn(id);
        when(fleet.getContainingLocation()).thenReturn(location);
        when(fleet.getLocation()).thenReturn(position);
        FleetDataAPI fleetData = mock(FleetDataAPI.class);
        when(fleetData.getMembersListCopy()).thenReturn(members);
        when(fleet.getFleetData()).thenReturn(fleetData);
        FactionAPI faction = mock(FactionAPI.class);
        when(fleet.getFaction()).thenReturn(faction);
        Map<String, Object> memoryStore = new HashMap<>();
        MemoryAPI memory = mock(MemoryAPI.class);
        when(memory.get(anyString())).thenAnswer(invocation -> memoryStore.get((String) invocation.getArgument(0)));
        doAnswer(invocation -> memoryStore.put(invocation.getArgument(0), invocation.getArgument(1)))
                .when(memory).set(anyString(), org.mockito.ArgumentMatchers.any());
        when(fleet.getMemoryWithoutUpdate()).thenReturn(memory);
        return fleet;
    }

    private CampaignFleetAPI fleetOf(FleetMemberAPI... members) {
        return fleet("fleet-1", List.of(members), location, new Vector2f(100f, 0f));
    }

    private static Map<String, String> records(CampaignFleetAPI fleet) {
        return EnemyTreeRecords.of(fleet.getMemoryWithoutUpdate());
    }

    private void setOtherShipChance(int percent) {
        lunaSettingsMock.when(() -> LunaSettings.getInt("exiledSector", EnemyTreeConfig.OTHER_SHIP_CHANCE_FIELD_ID)).thenReturn(percent);
    }

    @Test
    void anOfficeredEnemyShipGetsItsTreeTaggedOnItsVariantAndItsConvertedHullmodStripped() {
        Set<String> hullMods = new LinkedHashSet<>(List.of("heavyarmor", "hardenedshieldemitter"));
        List<String> tags = new ArrayList<>();
        FleetMemberAPI officered = member("m1", statefulVariant(hullMods, tags), true);

        EnemyFleetLeveller.ensure(fleetOf(officered));

        String tag = EnemyTreeTag.find(officered.getVariant());
        assertNotNull(tag);
        assertEquals("bulwark", EnemyTreeTag.layoutId(tag));
        assertTrue(tag.endsWith("root_1,a_1,armor_1"));
        assertTrue(hullMods.contains(SkillTreeHullMod.ID));
        assertFalse(hullMods.contains("heavyarmor"));
        assertTrue(hullMods.contains("hardenedshieldemitter"));
    }

    @Test
    void theDecisionIsStoredInFleetMemoryAndNeverRerolled() {
        FleetMemberAPI officered = member("m1", statefulVariant(new LinkedHashSet<>(), new ArrayList<>()), true);
        CampaignFleetAPI fleet = fleetOf(officered);

        EnemyFleetLeveller.ensure(fleet);
        String firstRecord = records(fleet).get("m1");
        EnemyLayouts.register(Map.of());
        when(playerStats.getLevel()).thenReturn(1);
        EnemyFleetLeveller.ensure(fleet);

        assertTrue(EnemyTreeRecords.isLevelled(firstRecord));
        assertEquals(firstRecord, records(fleet).get("m1"));
        assertEquals(firstRecord, EnemyTreeTag.find(officered.getVariant()));
    }

    @Test
    void aVariantRebuiltByReinflationGetsTheSameTreeReapplied() {
        Set<String> hullMods = new LinkedHashSet<>(List.of("heavyarmor"));
        List<String> tags = new ArrayList<>();
        FleetMemberAPI officered = member("m1", statefulVariant(hullMods, tags), true);
        CampaignFleetAPI fleet = fleetOf(officered);
        EnemyFleetLeveller.ensure(fleet);
        String tag = EnemyTreeTag.find(officered.getVariant());

        hullMods.clear();
        hullMods.add("heavyarmor");
        tags.clear();
        EnemyFleetLeveller.ensure(fleet);

        assertEquals(tag, EnemyTreeTag.find(officered.getVariant()));
        assertTrue(hullMods.contains(SkillTreeHullMod.ID));
        assertFalse(hullMods.contains("heavyarmor"));
    }

    @Test
    void anUnchosenShipIsRecordedAsNotLevelledAndLeftUntouched() {
        setOtherShipChance(0);
        Set<String> hullMods = new LinkedHashSet<>(List.of("heavyarmor"));
        List<String> tags = new ArrayList<>();
        FleetMemberAPI regular = member("m1", statefulVariant(hullMods, tags), false);
        CampaignFleetAPI fleet = fleetOf(regular);

        EnemyFleetLeveller.ensure(fleet);

        assertEquals(EnemyTreeRecords.NOT_LEVELLED, records(fleet).get("m1"));
        assertTrue(tags.isEmpty());
        assertEquals(Set.of("heavyarmor"), hullMods);
    }

    @Test
    void everyOtherShipIsLevelledAtAHundredPercentChance() {
        setOtherShipChance(100);
        FleetMemberAPI regular = member("m1", statefulVariant(new LinkedHashSet<>(), new ArrayList<>()), false);

        EnemyFleetLeveller.ensure(fleetOf(regular));

        assertNotNull(EnemyTreeTag.find(regular.getVariant()));
    }

    @Test
    void nothingHappensWhileEnemySkillTreesAreDisabled() {
        lunaSettingsMock.when(() -> LunaSettings.getBoolean("exiledSector", EnemyTreeConfig.ENABLED_FIELD_ID)).thenReturn(false);
        FleetMemberAPI officered = member("m1", statefulVariant(new LinkedHashSet<>(), new ArrayList<>()), true);
        CampaignFleetAPI fleet = fleetOf(officered);

        EnemyFleetLeveller.ensure(fleet);

        assertNull(fleet.getMemoryWithoutUpdate().get(EnemyTreeRecords.MEMORY_KEY));
        assertNull(EnemyTreeTag.find(officered.getVariant()));
    }

    @Test
    void identicalFleetsGetIdenticalTrees() {
        setOtherShipChance(100);
        FleetMemberAPI first = member("m1", statefulVariant(new LinkedHashSet<>(), new ArrayList<>()), false);
        FleetMemberAPI second = member("m1", statefulVariant(new LinkedHashSet<>(), new ArrayList<>()), false);

        EnemyFleetLeveller.ensure(fleetOf(first));
        EnemyFleetLeveller.ensure(fleetOf(second));

        assertEquals(EnemyTreeTag.find(first.getVariant()), EnemyTreeTag.find(second.getVariant()));
    }

    @Test
    void playerStationAndLocationlessFleetsAreNeverLevelled() {
        CampaignFleetAPI ordinary = fleetOf();
        CampaignFleetAPI player = fleetOf();
        when(player.isPlayerFleet()).thenReturn(true);
        CampaignFleetAPI station = fleetOf();
        when(station.isStationMode()).thenReturn(true);
        CampaignFleetAPI playerFaction = fleetOf();
        when(playerFaction.getFaction().isPlayerFaction()).thenReturn(true);
        CampaignFleetAPI temporary = fleet("temp", List.of(), null, new Vector2f());

        assertTrue(EnemyFleetLeveller.isLevellable(ordinary));
        assertFalse(EnemyFleetLeveller.isLevellable(player));
        assertFalse(EnemyFleetLeveller.isLevellable(station));
        assertFalse(EnemyFleetLeveller.isLevellable(playerFaction));
        assertFalse(EnemyFleetLeveller.isLevellable(temporary));
        assertFalse(EnemyFleetLeveller.isLevellable(null));
    }

    @Test
    void theSweepOnlyLevelsFleetsWithinRangeOfThePlayer() {
        FleetMemberAPI near = member("near", statefulVariant(new LinkedHashSet<>(), new ArrayList<>()), true);
        FleetMemberAPI far = member("far", statefulVariant(new LinkedHashSet<>(), new ArrayList<>()), true);
        CampaignFleetAPI nearFleet = fleet("near-fleet", List.of(near), location, new Vector2f(3000f, 0f));
        CampaignFleetAPI farFleet = fleet("far-fleet", List.of(far), location, new Vector2f(5000f, 0f));
        when(location.getFleets()).thenReturn(List.of(playerFleet, nearFleet, farFleet));

        EnemyFleetSweepScript.sweepAround(playerFleet, EnemyFleetSweepScript.SWEEP_RANGE);

        assertNotNull(EnemyTreeTag.find(near.getVariant()));
        assertNull(EnemyTreeTag.find(far.getVariant()));
    }

    @Test
    void theSweepChecksStraightAwayAndKeepsRunningWithTheGamePaused() {
        FleetMemberAPI near = member("near", statefulVariant(new LinkedHashSet<>(), new ArrayList<>()), true);
        CampaignFleetAPI nearFleet = fleet("near-fleet", List.of(near), location, new Vector2f(100f, 0f));
        when(location.getFleets()).thenReturn(List.of(nearFleet));
        EnemyFleetSweepScript script = new EnemyFleetSweepScript();

        script.advance(0f);
        assertNotNull(EnemyTreeTag.find(near.getVariant()));
        assertFalse(script.isDone());
        assertFalse(script.runWhilePaused());
    }

    @Test
    void openingAnEncounterDialogWithAFleetLevelsTheFleetsAroundThePlayer() {
        FleetMemberAPI near = member("near", statefulVariant(new LinkedHashSet<>(), new ArrayList<>()), true);
        CampaignFleetAPI target = fleet("target", List.of(near), location, new Vector2f(50f, 0f));
        when(location.getFleets()).thenReturn(List.of(target));
        InteractionDialogAPI dialog = mock(InteractionDialogAPI.class);
        when(dialog.getInteractionTarget()).thenReturn(target);

        new EnemyFleetDialogListener().reportShownInteractionDialog(dialog);

        assertNotNull(EnemyTreeTag.find(near.getVariant()));
    }

    @Test
    void openingADialogWithSomethingOtherThanAFleetDoesNothing() {
        FleetMemberAPI near = member("near", statefulVariant(new LinkedHashSet<>(), new ArrayList<>()), true);
        CampaignFleetAPI nearby = fleet("nearby", List.of(near), location, new Vector2f(50f, 0f));
        when(location.getFleets()).thenReturn(List.of(nearby));
        InteractionDialogAPI dialog = mock(InteractionDialogAPI.class);
        when(dialog.getInteractionTarget()).thenReturn(mock(SectorEntityToken.class));

        new EnemyFleetDialogListener().reportShownInteractionDialog(dialog);

        assertNull(EnemyTreeTag.find(near.getVariant()));
    }

    @Test
    void theInflationListenerLevelsTheInflatedFleet() {
        FleetMemberAPI officered = member("m1", statefulVariant(new LinkedHashSet<>(), new ArrayList<>()), true);
        CampaignFleetAPI fleet = fleetOf(officered);

        new EnemyFleetInflationListener().reportFleetInflated(fleet, null);

        assertNotNull(EnemyTreeTag.find(officered.getVariant()));
    }
}
