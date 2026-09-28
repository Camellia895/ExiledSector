package exiledsector.ui.node;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import exiledsector.effects.SkillTreeHullMod;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillItemCost;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillNodeOpCost;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import lunalib.lunaSettings.LunaSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NodeAllocatorTest {

    private MockedStatic<LunaSettings> lunaSettingsMock;
    private MockedStatic<Global> globalMock;
    private MockedConstruction<SkillTreeHullMod> hullModConstruction;
    private SettingsAPI settings;
    private CargoAPI cargo;
    private FleetMemberAPI member;
    private ShipVariantAPI variant;

    private SkillNode root;
    private SkillNode frontShield;
    private SkillType omniShieldType;
    private SkillType armorType;
    private SkillNode coreSlot;
    private SkillNode unreachable;

    @BeforeEach
    void setUp() {
        lunaSettingsMock = Mockito.mockStatic(LunaSettings.class, invocation -> null);
        globalMock = Mockito.mockStatic(Global.class);
        SectorAPI sector = mock(SectorAPI.class);
        Map<String, Object> persistentData = new HashMap<>();
        when(sector.getPersistentData()).thenReturn(persistentData);
        CampaignFleetAPI playerFleet = mock(CampaignFleetAPI.class);
        cargo = mock(CargoAPI.class);
        when(playerFleet.getCargo()).thenReturn(cargo);
        when(sector.getPlayerFleet()).thenReturn(playerFleet);
        settings = mock(SettingsAPI.class);
        globalMock.when(Global::getSector).thenReturn(sector);
        globalMock.when(Global::getSettings).thenReturn(settings);
        hullModConstruction = Mockito.mockConstruction(SkillTreeHullMod.class);

        SkillTree.getAllNodes().clear();
        SkillTree.getAllTypes().clear();
        root = register("root_1", type("root", "Low Tech", SkillTier.ROOT).build());
        frontShield = register("frontshield_1", type("frontshield", "Front Shield", SkillTier.NOTABLE).build(), "root_1");
        omniShieldType = type("omnishield", "Omni Shield", SkillTier.NOTABLE).exclusiveSkillTypeIds(List.of("frontshield")).build();
        armorType = type("heavyarmor", "Heavy Armor", SkillTier.NOTABLE).exclusiveHullModIds(List.of("heavyarmor")).build();
        coreSlot = register("core_1", type("core", "Core Slot", SkillTier.NOTABLE)
                .itemCost(new SkillItemCost("alpha_core", 1f)).build(), "root_1");
        SkillNode gate = register("gate_1", type("gate", "Gate", SkillTier.SMALL).build(), "frontshield_1");
        unreachable = register("beyond_1", type("beyond", "Beyond", SkillTier.SMALL).build(), gate.getId());

        member = mock(FleetMemberAPI.class);
        when(member.getId()).thenReturn("ship-1");
        ShipHullSpecAPI hullSpec = mock(ShipHullSpecAPI.class);
        when(hullSpec.getHullSize()).thenReturn(HullSize.CRUISER);
        when(hullSpec.getOrdnancePoints(any())).thenReturn(100);
        when(member.getHullSpec()).thenReturn(hullSpec);
        variant = mock(ShipVariantAPI.class);
        when(variant.computeOPCost(any())).thenReturn(80);
    }

    @AfterEach
    void tearDown() {
        hullModConstruction.close();
        globalMock.close();
        lunaSettingsMock.close();
        SkillTree.getAllNodes().clear();
        SkillTree.getAllTypes().clear();
    }

    private static SkillType.Builder type(String id, String name, SkillTier tier) {
        return new SkillType.Builder(id, name, "a.png", tier);
    }

    private static SkillNode register(String id, SkillType type, String... connectedTo) {
        SkillNode node = new SkillNode(id, type, List.of(connectedTo), 0f, 0f);
        SkillTree.register(node);
        return node;
    }

    private NodeAllocator allocatorStartingAt(SkillNode startingRoot) {
        return new NodeAllocator(member, variant, () -> startingRoot);
    }

    private static ShipSkillData data() {
        return ShipSkillDataManager.get("ship-1");
    }

    @Test
    void theStartingRootIsFreeAndEveryOtherNodeCostsThePerNodeOp() {
        NodeAllocator.Snapshot snapshot = allocatorStartingAt(root).snapshot();

        assertEquals(0, snapshot.opCostFor(root));
        assertEquals(SkillNodeOpCost.perNode(HullSize.CRUISER), snapshot.opCostFor(frontShield));
    }

    @Test
    void theBudgetIsTheShipsFreeOpPlusWhatTheTreeHasAlreadySpent() {
        data().chooseStartingRoot(root);
        data().allocate(frontShield, 3);

        assertEquals(100 - 80 + 3, allocatorStartingAt(root).snapshot().totalOpBudget());
    }

    @Test
    void theStartingRootCanNeverBeDeallocated() {
        data().chooseStartingRoot(root);

        assertFalse(allocatorStartingAt(root).canDeallocate(root));
    }

    @Test
    void anAllocatedTypeThatIsExclusiveBlocksAllocation() {
        data().chooseStartingRoot(root);
        data().allocate(frontShield, 0);

        assertEquals("Already have Front Shield allocated.", allocatorStartingAt(root).blockAllocationReason(omniShieldType));
    }

    @Test
    void anInstalledExclusiveHullmodBlocksAllocation() {
        HullModSpecAPI spec = mock(HullModSpecAPI.class);
        when(spec.getDisplayName()).thenReturn("Heavy Armor");
        when(settings.getHullModSpec("heavyarmor")).thenReturn(spec);
        when(variant.hasHullMod("heavyarmor")).thenReturn(true);

        assertEquals("Ship already has Heavy Armor installed.", allocatorStartingAt(root).blockAllocationReason(armorType));
    }

    @Test
    void aNodeWhoseItemIsNotInCargoIsBlockedUntilItIs() {
        CommoditySpecAPI spec = mock(CommoditySpecAPI.class);
        when(spec.getName()).thenReturn("Alpha Core");
        when(settings.getCommoditySpec("alpha_core")).thenReturn(spec);
        NodeAllocator allocator = allocatorStartingAt(root);

        assertEquals("Requires 1 Alpha Core (have 0).", allocator.blockAllocationReason(coreSlot.getType()));
        when(cargo.getCommodityQuantity("alpha_core")).thenReturn(1f);
        assertNull(allocator.blockAllocationReason(coreSlot.getType()));
    }

    @Test
    void togglingANodeOnTakesItsItemAndTogglingItOffRefundsItRefreshingTheShipEachTime() {
        data().chooseStartingRoot(root);
        NodeAllocator allocator = allocatorStartingAt(root);

        assertTrue(allocator.toggle(coreSlot));
        verify(cargo).removeCommodity("alpha_core", 1f);
        assertTrue(allocator.toggle(coreSlot));
        verify(cargo).addCommodity("alpha_core", 1f);
        assertEquals(2, hullModConstruction.constructed().size());
        verify(member, Mockito.times(2)).updateStats();
    }

    @Test
    void aToggleThatChangesNothingTakesNoItemAndDoesNotRefreshTheShip() {
        data().chooseStartingRoot(root);

        assertFalse(allocatorStartingAt(root).toggle(unreachable));

        verifyNoInteractions(cargo);
        assertTrue(hullModConstruction.constructed().isEmpty());
    }

    @Test
    void choosingTheStartingRootOnlyWorksOnce() {
        NodeAllocator allocator = allocatorStartingAt(null);

        assertTrue(allocator.chooseStartingRoot(root));
        assertFalse(allocator.chooseStartingRoot(root));
        assertTrue(data().isAllocated("root_1"));
    }
}
