package exiledsector.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShipSymbolResolverTest {

    private MockedStatic<Global> globalMock;
    private SectorAPI sector;
    private FleetMemberAPI member;
    private ShipVariantAPI variant;
    private ShipHullSpecAPI hullSpec;

    @BeforeEach
    void setUp() {
        sector = mock(SectorAPI.class);
        globalMock = Mockito.mockStatic(Global.class);
        globalMock.when(Global::getSector).thenReturn(sector);

        member = mock(FleetMemberAPI.class);
        when(member.getHullId()).thenReturn("brawler_tritachyon");

        hullSpec = mock(ShipHullSpecAPI.class);
        variant = mock(ShipVariantAPI.class);
        when(variant.getHullSpec()).thenReturn(hullSpec);
    }

    @AfterEach
    void tearDown() {
        globalMock.close();
    }

    private FactionAPI factionKnowing(String crest, boolean knowsHull) {
        FactionAPI faction = mock(FactionAPI.class);
        when(faction.knowsShip("brawler_tritachyon")).thenReturn(knowsHull);
        when(faction.getCrest()).thenReturn(crest);
        return faction;
    }

    @Test
    void singleFactionMatchUsesThatFactionsCrest() {
        FactionAPI tritachyon = factionKnowing("graphics/factions/crest_tritachyon.png", true);
        FactionAPI hegemony = factionKnowing("graphics/factions/crest_hegemony.png", false);
        when(sector.getAllFactions()).thenReturn(List.of(hegemony, tritachyon));

        String result = ShipSymbolResolver.resolveSymbolPath(member, variant);

        assertEquals("graphics/factions/crest_tritachyon.png", result);
    }

    @Test
    void noFactionMatchFallsBackToTechTier() {
        FactionAPI nonMatch = factionKnowing("irrelevant.png", false);
        when(sector.getAllFactions()).thenReturn(List.of(nonMatch));
        when(hullSpec.getManufacturer()).thenReturn("High Tech");

        String result = ShipSymbolResolver.resolveSymbolPath(member, variant);

        assertEquals("graphics/factions/crest_hightech.png", result);
    }

    @Test
    void multipleFactionMatchesFallsBackToTechTier() {
        FactionAPI a = factionKnowing("a.png", true);
        FactionAPI b = factionKnowing("b.png", true);
        when(sector.getAllFactions()).thenReturn(List.of(a, b));
        when(hullSpec.getManufacturer()).thenReturn("Low Tech");

        String result = ShipSymbolResolver.resolveSymbolPath(member, variant);

        assertEquals("graphics/factions/crest_lowtech.png", result);
    }

    @Test
    void midlineManufacturerMapsToMidlineCrest() {
        when(sector.getAllFactions()).thenReturn(List.of());
        when(hullSpec.getManufacturer()).thenReturn("Midline");

        assertEquals("graphics/factions/crest_midline.png", ShipSymbolResolver.resolveSymbolPath(member, variant));
    }

    @Test
    void unrecognizedManufacturerDefaultsToMidline() {
        when(sector.getAllFactions()).thenReturn(List.of());
        when(hullSpec.getManufacturer()).thenReturn("Some Modded Design Type");

        assertEquals("graphics/factions/crest_midline.png", ShipSymbolResolver.resolveSymbolPath(member, variant));
    }

    @Test
    void blankManufacturerDefaultsToMidline() {
        when(sector.getAllFactions()).thenReturn(List.of());
        when(hullSpec.getManufacturer()).thenReturn("");

        assertEquals("graphics/factions/crest_midline.png", ShipSymbolResolver.resolveSymbolPath(member, variant));
    }
}
