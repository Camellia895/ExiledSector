package exiledsector.effects;

import com.fs.starfarer.api.combat.ShipVariantAPI;

import java.util.HashMap;
import java.util.Map;

final class SkillConflictWarnings {

    private static final Map<ShipVariantAPI, Removal> REMOVALS = new HashMap<>();

    private SkillConflictWarnings() {
    }

    static void record(ShipVariantAPI variant, String removedHullModId, String causeSkillDisplayName) {
        REMOVALS.clear();
        REMOVALS.put(variant, new Removal(removedHullModId, causeSkillDisplayName));
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
