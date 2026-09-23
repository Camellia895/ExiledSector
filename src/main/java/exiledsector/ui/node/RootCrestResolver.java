package exiledsector.ui.node;

import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.skills.ShipTechLevel;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;

public final class RootCrestResolver {

    private static final String PIRATE_CREST_PATH = "graphics/icons/non_skill_icons/used/circular/roots/crest_pirates.png";
    private static final String REMNANT_CREST_PATH = "graphics/icons/non_skill_icons/used/circular/roots/crest_ai_remnant.png";

    private RootCrestResolver() {
    }

    public static String resolve(FleetMemberAPI member) {
        if (isPirateFlagged(member)) return PIRATE_CREST_PATH;
        if (isRemnant(member)) return REMNANT_CREST_PATH;

        SkillType rootType = SkillTree.getType(ShipTechLevel.of(member).rootTypeId());
        return rootType != null ? rootType.getIconPath() : null;
    }

    static boolean isPirateFlagged(FleetMemberAPI member) {
        if (hasManufacturer(member, "Pirate")) return true;

        ShipHullSpecAPI hullSpec = member.getHullSpec();
        if (hullSpec != null && containsIgnoreCase(hullSpec.getHullId(), "pirate")) return true;

        ShipVariantAPI variant = member.getVariant();
        return variant != null && containsIgnoreCase(variant.getHullVariantId(), "pirate");
    }

    static boolean isRemnant(FleetMemberAPI member) {
        return hasManufacturer(member, "Remnant");
    }

    private static boolean hasManufacturer(FleetMemberAPI member, String expected) {
        ShipHullSpecAPI hullSpec = member.getHullSpec();
        if (hullSpec == null) return false;
        String manufacturer = hullSpec.getManufacturer();
        return manufacturer != null && manufacturer.trim().equalsIgnoreCase(expected);
    }

    private static boolean containsIgnoreCase(String value, String substring) {
        return value != null && value.toLowerCase().contains(substring);
    }
}
