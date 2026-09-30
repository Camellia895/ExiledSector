package exiledsector.skills.progression;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
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
        MutableCharacterStatsAPI commanderStats = commanderStats(member);
        int total = member.getHullSpec().getOrdnancePoints(commanderStats);
        int used = variant.computeOPCost(commanderStats);
        return new ShipOpBudget(total, used);
    }

    private static MutableCharacterStatsAPI commanderStats(FleetMemberAPI member) {
        PersonAPI commander = member.getFleetCommanderForStats();
        if (commander == null) commander = member.getFleetCommander();
        if (commander == null) commander = member.getCaptain();
        return commander != null ? commander.getStats() : null;
    }
}
