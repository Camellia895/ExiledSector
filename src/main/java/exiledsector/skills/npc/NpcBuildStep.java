package exiledsector.skills.npc;

public record NpcBuildStep(String nodeId, String outcome) {

    public static final String ALLOCATED = "allocated";
    public static final String ALLOCATED_WITH_FREED_OP = "allocated with OP freed by stripped hullmods";
    public static final String UNKNOWN_NODE = "unknown node";
    public static final String ALREADY_ALLOCATED = "already allocated";
    public static final String WORMHOLE = "wormhole";
    public static final String ROOT_NODE = "root node";
    public static final String MISSING_OPTION = "missing option";
    public static final String INVALID_OPTION = "invalid option: ";
    public static final String UNEXPECTED_OPTION = "unexpected option: ";
    public static final String UNMET_REQUIREMENT = "unmet requirement: ";
    public static final String INSTALLED_HULLMOD_CONFLICT = "conflicts with installed hullmod: ";
    public static final String EXCLUSIVE_TYPE_CONFLICT = "exclusive with allocated type: ";
    public static final String BLOCKED_BY_SHIP_STATE = "blocked by ship state: ";
    public static final String NOT_CONNECTED = "not connected";
    public static final String COUNT_REACHED = "count reached";
    public static final String INVALID_ROOT = "invalid root: ";
    public static final String PATH_TO_CONVERTED_HULLMOD = "allocated: path to converted hullmod ";
    public static final String CONVERTED_HULLMOD = "allocated: converts hullmod ";
    public static final String HULLMOD_KEPT = "hullmod kept, equivalent node out of reach: ";

    public boolean isAllocated() {
        return outcome != null && outcome.startsWith(ALLOCATED);
    }

    public boolean isSkipped() {
        return !isAllocated() && !COUNT_REACHED.equals(outcome);
    }
}
