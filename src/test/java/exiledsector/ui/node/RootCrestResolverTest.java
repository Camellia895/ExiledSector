package exiledsector.ui.node;

import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RootCrestResolverTest {

    @AfterEach
    void tearDown() {
        SkillTree.getAllTypes().clear();
    }

    private static FleetMemberAPI mockMember(String hullId, String manufacturer, String hullVariantId) {
        FleetMemberAPI member = mock(FleetMemberAPI.class);
        ShipHullSpecAPI hullSpec = mock(ShipHullSpecAPI.class);
        when(member.getHullSpec()).thenReturn(hullSpec);
        when(hullSpec.getHullId()).thenReturn(hullId);
        when(hullSpec.getManufacturer()).thenReturn(manufacturer);

        if (hullVariantId != null) {
            ShipVariantAPI variant = mock(ShipVariantAPI.class);
            when(variant.getHullVariantId()).thenReturn(hullVariantId);
            when(member.getVariant()).thenReturn(variant);
        }
        return member;
    }

    @Test
    void manufacturerPirateIsPirateFlagged() {
        assertTrue(RootCrestResolver.isPirateFlagged(mockMember("colossus3", "Pirate", null)));
    }

    @Test
    void manufacturerMatchIsCaseAndWhitespaceInsensitive() {
        assertTrue(RootCrestResolver.isPirateFlagged(mockMember("colossus3", "  pirate  ", null)));
    }

    @Test
    void hullIdContainingPirateIsPirateFlagged() {
        assertTrue(RootCrestResolver.isPirateFlagged(mockMember("afflictor_d_pirates", "Low Tech", null)));
    }

    @Test
    void variantIdContainingPirateIsPirateFlagged() {
        assertTrue(RootCrestResolver.isPirateFlagged(mockMember("hammerhead", "Low Tech", "hammerhead_pirates_Strike")));
    }

    @Test
    void ordinaryShipIsNotPirateFlagged() {
        assertFalse(RootCrestResolver.isPirateFlagged(mockMember("hammerhead", "Low Tech", "hammerhead_Standard")));
    }

    @Test
    void manufacturerRemnantIsRemnant() {
        assertTrue(RootCrestResolver.isRemnant(mockMember("radiant", "Remnant", null)));
    }

    @Test
    void ordinaryShipIsNotRemnant() {
        assertFalse(RootCrestResolver.isRemnant(mockMember("paragon", "High Tech", null)));
    }

    @Test
    void resolveReturnsPirateCrestRegardlessOfTechLevel() {
        FleetMemberAPI member = mockMember("atlas2", "Pirate", null);
        assertEquals("graphics/icons/circular/roots/crest_pirates.png", RootCrestResolver.resolve(member));
    }

    @Test
    void resolveReturnsRemnantCrestRegardlessOfTechLevel() {
        FleetMemberAPI member = mockMember("radiant", "Remnant", null);
        assertEquals("graphics/icons/circular/roots/crest_ai_remnant.png", RootCrestResolver.resolve(member));
    }

    @Test
    void resolveFallsBackToTheShipsTechLevelRootIconWhenNoFlagApplies() {
        SkillTree.registerType(new SkillType("root_high_tech", "High Tech Root", "graphics/icons/circular/roots/crest_hightech.png",
                0, List.of(), SkillTier.ROOT, null, null, null));

        FleetMemberAPI member = mockMember("paragon", "High Tech", null);
        assertEquals("graphics/icons/circular/roots/crest_hightech.png", RootCrestResolver.resolve(member));
    }
}
