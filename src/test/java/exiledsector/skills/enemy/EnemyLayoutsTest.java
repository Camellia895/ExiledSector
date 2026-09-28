package exiledsector.skills.enemy;

import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import exiledsector.skills.tags.ShipProfile;
import exiledsector.skills.tags.WeaponKind;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnemyLayoutsTest {

    @AfterEach
    void tearDown() {
        EnemyLayouts.register(Map.of());
    }

    private static EnemyLayout layout(String id, List<String> requires) {
        return new EnemyLayout(id, id, "root", requires, "", List.of(new EnemyLayoutEntry("a", null)));
    }

    @Test
    void layoutsAreKeptSortedById() {
        EnemyLayouts.register(Map.of("zeta", layout("zeta", List.of()), "alpha", layout("alpha", List.of())));

        assertEquals(List.of("alpha", "zeta"), EnemyLayouts.all().stream().map(EnemyLayout::id).toList());
    }

    @Test
    void onlyLayoutsWhoseRequirementsTheShipMeetsAreEligible() {
        EnemyLayouts.register(Map.of("carrier", layout("carrier", List.of("req_fighter_bays")),
                "bulwark", layout("bulwark", List.of())));
        ShipProfile noBays = new ShipProfile(HullSize.DESTROYER, ShieldType.FRONT, 0, Set.of(WeaponKind.BALLISTIC), false);
        ShipProfile carrier = new ShipProfile(HullSize.DESTROYER, ShieldType.FRONT, 2, Set.of(WeaponKind.BALLISTIC), false);

        assertEquals(List.of("bulwark"), EnemyLayouts.eligibleFor(noBays).stream().map(EnemyLayout::id).toList());
        assertEquals(List.of("bulwark", "carrier"), EnemyLayouts.eligibleFor(carrier).stream().map(EnemyLayout::id).toList());
    }

}
