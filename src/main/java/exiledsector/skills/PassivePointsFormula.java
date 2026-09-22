package exiledsector.skills;

import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public final class PassivePointsFormula {

    private static final int BASE = 20;
    private static final float SLOPE = 5f;
    private static final float MAX_ABS_Z = 2f;

    private static final int FRIGATE_FLOOR = 10;
    private static final int DESTROYER_FLOOR = 15;
    private static final int CRUISER_FLOOR = 20;
    private static final int CAPITAL_FLOOR = 25;
    private static final int UNDEFINED_FLOOR = 10;

    private PassivePointsFormula() {
    }

    public static int compute(HullSize hullSize, float op, float mean, float stdDev) {
        float z = stdDev > 0f ? (op - mean) / stdDev : 0f;
        float clamped = Math.max(-MAX_ABS_Z, Math.min(MAX_ABS_Z, z));
        int points = Math.round(BASE + SLOPE * clamped);
        return Math.max(points, floorFor(hullSize));
    }

    private static int floorFor(HullSize hullSize) {
        if (hullSize == null) return UNDEFINED_FLOOR;
        switch (hullSize) {
            case FRIGATE: return FRIGATE_FLOOR;
            case DESTROYER: return DESTROYER_FLOOR;
            case CRUISER: return CRUISER_FLOOR;
            case CAPITAL_SHIP: return CAPITAL_FLOOR;
            default: return UNDEFINED_FLOOR;
        }
    }
}
