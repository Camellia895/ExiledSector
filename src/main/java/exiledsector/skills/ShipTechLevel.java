package exiledsector.skills;

import com.fs.starfarer.api.fleet.FleetMemberAPI;

import java.util.Set;

public enum ShipTechLevel {

    LOW_TECH,
    MIDLINE,
    HIGH_TECH;

    private static final Set<String> HIGH_TECH_OVERRIDE_IDS = Set.of(
            "spark", "lux", "flash", "lumen", "glimmer", "fulgent",
            "scintilla", "brilliant", "apex", "radiant", "nova");

    private static final Set<String> LOW_TECH_OVERRIDE_IDS = Set.of(
            "colossus2", "colossus3", "atlas2", "prometheus2", "onslaught_mk1",
            "warden", "defender", "picket", "sentry", "bastillon", "berserker", "rampart", "guardian");

    public static ShipTechLevel of(FleetMemberAPI member) {
        String hullId = member.getHullSpec().getHullId();
        if (HIGH_TECH_OVERRIDE_IDS.contains(hullId)) return HIGH_TECH;
        if (LOW_TECH_OVERRIDE_IDS.contains(hullId)) return LOW_TECH;

        String manufacturer = member.getHullSpec().getManufacturer();
        if (manufacturer == null) return MIDLINE;

        String trimmed = manufacturer.trim();
        if (trimmed.equalsIgnoreCase("Low Tech")) return LOW_TECH;
        if (trimmed.equalsIgnoreCase("High Tech")) return HIGH_TECH;
        return MIDLINE;
    }
}
