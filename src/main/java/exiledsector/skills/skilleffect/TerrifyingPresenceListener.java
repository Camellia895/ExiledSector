package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.IntervalUtil;

import java.util.HashSet;
import java.util.Set;

final class TerrifyingPresenceListener implements AdvanceableListener {

    static final String ACCURACY_PENALTY_PERCENT_KEY = "exiledSector_terrifyingPresenceAccuracyPenaltyPercent";
    static final float RANGE = 1000f;
    private static final float UPDATE_SECONDS = 0.25f;
    private static final String MOD_ID_PREFIX = "exiledSector_terrifyingPresence_";

    private final ShipAPI ship;
    private final String modId;
    private final IntervalUtil interval = new IntervalUtil(UPDATE_SECONDS, UPDATE_SECONDS);
    private Set<ShipAPI> affected = new HashSet<>();

    TerrifyingPresenceListener(ShipAPI ship) {
        this.ship = ship;
        this.modId = MOD_ID_PREFIX + ship.getId();
    }

    @Override
    public void advance(float amount) {
        interval.advance(amount);
        if (!interval.intervalElapsed()) {
            return;
        }
        float penalty = isPresent() ? ship.getMutableStats().getDynamic().getValue(ACCURACY_PENALTY_PERCENT_KEY, 0f) / 100f : 0f;
        Set<ShipAPI> inRange = penalty > 0f ? new HashSet<>(CombatQueries.shipsMatching(this::isTerrified)) : new HashSet<>();
        for (ShipAPI previous : affected) {
            if (!inRange.contains(previous)) {
                previous.getMutableStats().getAutofireAimAccuracy().unmodify(modId);
            }
        }
        for (ShipAPI enemy : inRange) {
            enemy.getMutableStats().getAutofireAimAccuracy().modifyFlat(modId, -penalty);
        }
        affected = inRange;
    }

    private boolean isPresent() {
        return ship.isAlive() && !ship.isHulk() && !ship.isRetreating();
    }

    private boolean isTerrified(ShipAPI other) {
        return other.isAlive() && !other.isHulk() && CombatQueries.isHostile(ship, other)
                && CombatQueries.withinRadius(other.getLocation(), ship.getLocation(), RANGE);
    }
}
