package exiledsector.skills.enemy;

import exiledsector.skills.tags.ShipProfile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class EnemyLayouts {

    private static final List<EnemyLayout> LAYOUTS = new ArrayList<>();

    private EnemyLayouts() {
    }

    public static void load() {
        register(EnemyLayoutLoader.load());
    }

    public static void register(Map<String, EnemyLayout> layouts) {
        LAYOUTS.clear();
        LAYOUTS.addAll(layouts.values());
        LAYOUTS.sort(Comparator.comparing(EnemyLayout::id));
    }

    public static List<EnemyLayout> all() {
        return List.copyOf(LAYOUTS);
    }

    public static List<EnemyLayout> eligibleFor(ShipProfile profile) {
        List<EnemyLayout> eligible = new ArrayList<>();
        for (EnemyLayout layout : LAYOUTS) {
            if (layout.isEligible(profile)) {
                eligible.add(layout);
            }
        }
        return eligible;
    }
}
