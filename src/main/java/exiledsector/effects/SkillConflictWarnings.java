package exiledsector.effects;

import com.fs.starfarer.api.combat.ShipVariantAPI;

import java.util.Map;
import java.util.WeakHashMap;

final class SkillConflictWarnings {

    private static final Map<ShipVariantAPI, Removal> REMOVALS = new WeakHashMap<>();

    private SkillConflictWarnings() {
    }

    static void record(ShipVariantAPI variant, String removedHullModId, String causeSkillDisplayName) {
        REMOVALS.put(variant, new Removal(removedHullModId, causeSkillDisplayName));
    }

    static void clear(ShipVariantAPI variant) {
        REMOVALS.remove(variant);
    }

    static Removal get(ShipVariantAPI variant) {
        return REMOVALS.get(variant);
    }

    static final class Removal {
        final String removedHullModId;
        final String causeSkillDisplayName;

        private Removal(String removedHullModId, String causeSkillDisplayName) {
            this.removedHullModId = removedHullModId;
            this.causeSkillDisplayName = causeSkillDisplayName;
        }
    }
}
