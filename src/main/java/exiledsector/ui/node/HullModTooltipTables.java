package exiledsector.ui.node;

import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.hullmods.BallisticRangefinder;
import com.fs.starfarer.api.impl.hullmods.MissileAutoloader;
import com.fs.starfarer.api.impl.hullmods.MissileAutoloader.ReloadCapacityData;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import exiledsector.skills.SkillType;
import exiledsector.ui.TooltipTable;
import exiledsector.ui.TooltipTable.Row;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class HullModTooltipTables {

    static final String BALLISTIC_RANGEFINDER = "ballistic_rangefinder";
    static final String MISSILE_AUTOLOADER = "missile_autoloader";
    private static final String NOT_APPLICABLE = "---";
    private static final String SMALL_OR_MEDIUM = "Small / Medium";
    private static final String LARGE = "Large";
    private static final String LARGEST_SLOT = "Largest b. slot";
    private static final String RANGE_CAP = "Range cap";

    private HullModTooltipTables() {
    }

    static List<TooltipTable> forType(SkillType type, ShipHullSpecAPI hull) {
        String hullModId = type == null ? null : type.getVanillaHullModId();
        if (BALLISTIC_RANGEFINDER.equals(hullModId)) {
            return rangefinderTables(largestBallisticSlot(hull));
        }
        if (MISSILE_AUTOLOADER.equals(hullModId)) {
            return List.of(autoloaderTable(hull));
        }
        return List.of();
    }

    static List<TooltipTable> rangefinderTables(WeaponSize largest) {
        boolean smallOrMediumLargest = largest == WeaponSize.SMALL || largest == WeaponSize.MEDIUM;
        boolean largeLargest = largest == WeaponSize.LARGE;

        TooltipTable ballistic = new TooltipTable("Ballistic weapon range",
                List.of(LARGEST_SLOT, "Small wpn", "Medium wpn", RANGE_CAP),
                List.of(Row.of(smallOrMediumLargest, SMALL_OR_MEDIUM, bonus(BallisticRangefinder.BONUS_SMALL_1),
                                NOT_APPLICABLE, whole(BallisticRangefinder.BONUS_MAX_1)),
                        Row.of(largeLargest, LARGE, bonus(BallisticRangefinder.BONUS_SMALL_3),
                                bonus(BallisticRangefinder.BONUS_MEDIUM_3), whole(BallisticRangefinder.BONUS_MAX_3))));

        float hybridMult = BallisticRangefinder.HYBRID_MULT;
        float hybridMin = BallisticRangefinder.HYBRID_BONUS_MIN;
        TooltipTable hybrid = new TooltipTable("Hybrid weapon range",
                List.of(LARGEST_SLOT, "Small", "Medium", LARGE, RANGE_CAP),
                List.of(Row.of(smallOrMediumLargest, SMALL_OR_MEDIUM, bonus(BallisticRangefinder.BONUS_SMALL_1 * hybridMult),
                                bonus(hybridMin), bonus(hybridMin), whole(BallisticRangefinder.BONUS_MAX_1)),
                        Row.of(largeLargest, LARGE, bonus(BallisticRangefinder.BONUS_SMALL_3 * hybridMult),
                                bonus(BallisticRangefinder.BONUS_MEDIUM_3 * hybridMult), bonus(hybridMin),
                                whole(BallisticRangefinder.BONUS_MAX_3))));
        return List.of(ballistic, hybrid);
    }

    static TooltipTable autoloaderTable(ShipHullSpecAPI hull) {
        HullSize hullSize = hull.getHullSize();
        ReloadCapacityData current = capacityFor(hullSize, smallMissileSlotCount(hull));
        List<ReloadCapacityData> sizeRows = new ArrayList<>();
        for (ReloadCapacityData data : MissileAutoloader.CAPACITY_DATA) {
            if (data.size == hullSize) {
                sizeRows.add(data);
            }
        }
        sizeRows.sort(Comparator.comparingInt(data -> data.capacity));

        List<Row> rows = new ArrayList<>();
        for (ReloadCapacityData data : sizeRows) {
            rows.add(Row.of(data == current, hullSizeName(data.size), data.getWeaponsString(), String.valueOf(data.capacity)));
        }
        return new TooltipTable("Reload capacity", List.of("Ship size", "Small missiles", "Reload capacity"), rows);
    }

    static WeaponSize largestBallisticSlot(ShipHullSpecAPI hull) {
        WeaponSize largest = null;
        for (WeaponSlotAPI slot : hull.getAllWeaponSlotsCopy()) {
            boolean ballistic = !slot.isDecorative() && slot.getWeaponType() == WeaponType.BALLISTIC;
            if (ballistic && (largest == null || largest.ordinal() < slot.getSlotSize().ordinal())) {
                largest = slot.getSlotSize();
            }
        }
        return largest;
    }

    private static int smallMissileSlotCount(ShipHullSpecAPI hull) {
        int count = 0;
        for (WeaponSlotAPI slot : hull.getAllWeaponSlotsCopy()) {
            if (slot.getSlotSize() == WeaponSize.SMALL && slot.getWeaponType() == WeaponType.MISSILE) {
                count++;
            }
        }
        return count;
    }

    private static ReloadCapacityData capacityFor(HullSize hullSize, int smallMissileSlots) {
        for (ReloadCapacityData data : MissileAutoloader.CAPACITY_DATA) {
            boolean withinRange = smallMissileSlots >= data.minW && (data.maxW < 0 || smallMissileSlots <= data.maxW);
            if (data.size == hullSize && withinRange) {
                return data;
            }
        }
        return null;
    }

    private static String hullSizeName(HullSize size) {
        return switch (size) {
            case FRIGATE -> "Frigate";
            case DESTROYER -> "Destroyer";
            case CRUISER -> "Cruiser";
            case CAPITAL_SHIP -> "Capital";
            default -> size.name();
        };
    }

    private static String bonus(float value) {
        return "+" + (int) value;
    }

    private static String whole(float value) {
        return String.valueOf((int) value);
    }
}
