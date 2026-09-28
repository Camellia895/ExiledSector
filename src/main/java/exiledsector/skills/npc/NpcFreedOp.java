package exiledsector.skills.npc;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import exiledsector.skills.progression.ShipLevelConfig;
import exiledsector.skills.progression.SkillNodeOpCost;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public record NpcFreedOp(int opCostPerNode, Map<String, Integer> hullModOpCosts, int maxNodeCount) {

    public static final NpcFreedOp NONE = new NpcFreedOp(1, Map.of(), NpcSkillTreeBuilder.MAX_NODE_COUNT);

    public NpcFreedOp {
        opCostPerNode = Math.max(1, opCostPerNode);
        hullModOpCosts = hullModOpCosts == null ? Map.of() : Map.copyOf(hullModOpCosts);
    }

    public static NpcFreedOp of(FleetMemberAPI member, NpcHullMods hullMods) {
        HullSize hullSize = member.getHullSpec() == null ? null : member.getHullSpec().getHullSize();
        SettingsAPI settings = Global.getSettings();
        Map<String, Integer> costs = new HashMap<>();
        for (String hullModId : hullMods.removable()) {
            HullModSpecAPI spec = settings == null || hullSize == null ? null : settings.getHullModSpec(hullModId);
            if (spec != null) {
                costs.put(hullModId, spec.getCostFor(hullSize));
            }
        }
        return new NpcFreedOp(SkillNodeOpCost.perNode(member.getHullSpec()), costs, ShipLevelConfig.maxAllocatedNodes());
    }

    int extraNodes(Collection<String> strippedHullModIds, int rolledNodes) {
        int freedOp = 0;
        for (String hullModId : strippedHullModIds) {
            freedOp += Math.max(0, hullModOpCosts.getOrDefault(hullModId, 0));
        }
        int room = Math.min(maxNodeCount, NpcSkillTreeBuilder.MAX_NODE_COUNT) - rolledNodes;
        return Math.max(0, Math.min(freedOp / opCostPerNode, room));
    }
}
