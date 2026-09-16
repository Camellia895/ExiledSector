package exiledsector.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves which symbol a ship's skill tree should show: the crest of the
 * one faction that "owns" its hull design, or a generic tech-tier icon if
 * the hull is shared/ambiguous (e.g. a civilian hull many factions use) or
 * unrecognized.
 */
public class ShipSymbolResolver {

    private static final String LOW_TECH_CREST = "graphics/factions/crest_lowtech.png";
    private static final String MIDLINE_CREST = "graphics/factions/crest_midline.png";
    private static final String HIGH_TECH_CREST = "graphics/factions/crest_hightech.png";

    private ShipSymbolResolver() {
    }

    public static String resolveSymbolPath(FleetMemberAPI member, ShipVariantAPI variant) {
        String hullId = member.getHullId();

        List<FactionAPI> owners = new ArrayList<>();
        for (FactionAPI faction : Global.getSector().getAllFactions()) {
            if (faction.knowsShip(hullId)) {
                owners.add(faction);
            }
        }

        if (owners.size() == 1) {
            return owners.get(0).getCrest();
        }

        return resolveTechTierCrest(variant);
    }

    private static String resolveTechTierCrest(ShipVariantAPI variant) {
        String manufacturer = variant.getHullSpec().getManufacturer();

        if ("Low Tech".equals(manufacturer)) {
            return LOW_TECH_CREST;
        }
        if ("High Tech".equals(manufacturer)) {
            return HIGH_TECH_CREST;
        }
        // "Midline" and anything unrecognized
        return MIDLINE_CREST;
    }
}
