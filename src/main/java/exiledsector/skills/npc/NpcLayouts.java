package exiledsector.skills.npc;

import exiledsector.skills.tags.ShipProfile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class NpcLayouts {

    private static final List<NpcLayout> LAYOUTS = new ArrayList<>();

    private NpcLayouts() {
    }

    public static void load() {
        register(NpcLayoutLoader.load());
    }

    public static void register(Map<String, NpcLayout> layouts) {
        LAYOUTS.clear();
        LAYOUTS.addAll(layouts.values());
        LAYOUTS.sort(Comparator.comparing(NpcLayout::id));
    }

    public static NpcLayout find(String id) {
        for (NpcLayout layout : LAYOUTS) {
            if (layout.id().equals(id)) {
                return layout;
            }
        }
        return null;
    }

    public static List<NpcLayout> all() {
        return List.copyOf(LAYOUTS);
    }

    public static List<NpcLayout> eligibleFor(ShipProfile profile) {
        List<NpcLayout> eligible = new ArrayList<>();
        for (NpcLayout layout : LAYOUTS) {
            if (layout.isEligible(profile)) {
                eligible.add(layout);
            }
        }
        return eligible;
    }
}
