package exiledsector.skills;

import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShipTechLevelTest {

    private static FleetMemberAPI mockMember(String hullId, String manufacturer) {
        FleetMemberAPI member = mock(FleetMemberAPI.class);
        ShipHullSpecAPI hullSpec = mock(ShipHullSpecAPI.class);
        when(member.getHullSpec()).thenReturn(hullSpec);
        when(hullSpec.getHullId()).thenReturn(hullId);
        when(hullSpec.getManufacturer()).thenReturn(manufacturer);
        return member;
    }

    @Test
    void resolvesLowTechFromManufacturerString() {
        assertEquals(ShipTechLevel.LOW_TECH, ShipTechLevel.of(mockMember("onslaught", "Low Tech")));
    }

    @Test
    void resolvesHighTechFromManufacturerString() {
        assertEquals(ShipTechLevel.HIGH_TECH, ShipTechLevel.of(mockMember("paragon", "High Tech")));
    }

    @Test
    void resolvesMidlineFromManufacturerString() {
        assertEquals(ShipTechLevel.MIDLINE, ShipTechLevel.of(mockMember("eagle", "Midline")));
    }

    @Test
    void fallsBackToMidlineForNullManufacturer() {
        assertEquals(ShipTechLevel.MIDLINE, ShipTechLevel.of(mockMember("drone_pd", null)));
    }

    @Test
    void fallsBackToMidlineForAnUnrecognizedModdedManufacturer() {
        assertEquals(ShipTechLevel.MIDLINE, ShipTechLevel.of(mockMember("modded_cruiser", "Interstellar Federation")));
    }

    @Test
    void manufacturerMatchIsCaseAndWhitespaceInsensitive() {
        assertEquals(ShipTechLevel.LOW_TECH, ShipTechLevel.of(mockMember("onslaught", "  low tech  ")));
        assertEquals(ShipTechLevel.HIGH_TECH, ShipTechLevel.of(mockMember("paragon", "HIGH TECH")));
    }

    @Test
    void remnantHullIdOverridesToHighTechEvenThoughItsOwnManufacturerStringIsRemnant() {
        assertEquals(ShipTechLevel.HIGH_TECH, ShipTechLevel.of(mockMember("radiant", "Remnant")));
    }

    @Test
    void explorariumHullIdOverridesToLowTechEvenThoughItsOwnManufacturerStringIsExplorarium() {
        assertEquals(ShipTechLevel.LOW_TECH, ShipTechLevel.of(mockMember("warden", "Explorarium")));
    }

    @Test
    void pirateVariantHullIdOverridesToLowTech() {
        assertEquals(ShipTechLevel.LOW_TECH, ShipTechLevel.of(mockMember("colossus3", "Pirate")));
    }

    @Test
    void domainRestrictedHullIdOverridesToLowTech() {
        assertEquals(ShipTechLevel.LOW_TECH, ShipTechLevel.of(mockMember("onslaught_mk1", "Domain Restricted")));
    }

    @Test
    void rootTypeIdMapsEachTechLevelToItsRootType() {
        assertEquals("root_low_tech", ShipTechLevel.LOW_TECH.rootTypeId());
        assertEquals("root_high_tech", ShipTechLevel.HIGH_TECH.rootTypeId());
        assertEquals("root_midline", ShipTechLevel.MIDLINE.rootTypeId());
    }
}
