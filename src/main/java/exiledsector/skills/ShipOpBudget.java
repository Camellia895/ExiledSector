package exiledsector.skills;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

public final class ShipOpBudget {

    public final int total;
    public final int used;

    private ShipOpBudget(int total, int used) {
        this.total = total;
        this.used = used;
    }

    public static ShipOpBudget of(FleetMemberAPI member, ShipVariantAPI variant) {
        MutableCharacterStatsAPI captainStats = member.getCaptain() != null ? member.getCaptain().getStats() : null;
        int total = member.getHullSpec().getOrdnancePoints(captainStats);
        int used = variant.computeOPCost(captainStats);
        return new ShipOpBudget(total, used);
    }
}
