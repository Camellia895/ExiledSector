package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

final class CombatQueries {

    private CombatQueries() {
    }

    static boolean isHostile(ShipAPI source, ShipAPI other) {
        return other.getOwner() != source.getOwner() && other.getOwner() != Misc.OWNER_NEUTRAL;
    }

    static boolean withinRadius(Vector2f a, Vector2f b, float radius) {
        return Vector2f.sub(a, b, null).lengthSquared() <= radius * radius;
    }

    static List<ShipAPI> shipsMatching(Predicate<ShipAPI> filter) {
        List<ShipAPI> result = new ArrayList<>();
        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (filter.test(ship)) {
                result.add(ship);
            }
        }
        return result;
    }
}
