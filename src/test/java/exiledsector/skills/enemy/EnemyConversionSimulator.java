package exiledsector.skills.enemy;

import exiledsector.skills.enemy.EnemyArchetypes.Archetype;
import org.json.JSONException;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class EnemyConversionSimulator {

    static final String USAGE = "Usage: EnemyConversionSimulator <layoutId> <archetype> <nodeCount> <removableHullMods,csv> [permanentHullMods,csv]";

    private EnemyConversionSimulator() {
    }

    public static void main(String[] args) {
        PrintStream out = System.out;
        if (args.length < 4 || args.length > 5) {
            out.println(USAGE);
            return;
        }
        try {
            simulate(args, out);
        } catch (IOException | JSONException | RuntimeException e) {
            out.println("ERROR: " + e);
        }
    }

    private static void simulate(String[] args, PrintStream out) throws IOException, JSONException {
        Path root = RealSkillData.projectRoot();
        RealSkillData.load(root);
        Map<String, EnemyLayout> layouts = EnemyLayoutValidator.parse(RealSkillData.readJson(root.resolve(RealSkillData.LAYOUTS_FILE)));
        EnemyLayout layout = layouts.get(args[0]);
        Archetype archetype = EnemyArchetypes.ALL.stream().filter(a -> a.name().equals(args[1])).findFirst().orElse(null);
        if (layout == null || archetype == null) {
            out.println("Unknown layout or archetype. Layouts: " + layouts.keySet() + " Archetypes: "
                    + EnemyArchetypes.ALL.stream().map(Archetype::name).toList());
            return;
        }
        EnemyHullMods hullMods = new EnemyHullMods(csv(args[3]), args.length == 5 ? csv(args[4]) : Set.of());
        EnemyTreeBuild build = EnemySkillTreeBuilder.build(layout, Integer.parseInt(args[2]), archetype.profile(), hullMods);
        out.println(layout.id() + " on " + archetype.name() + " at " + args[2] + " nodes, removable " + hullMods.removable()
                + ", permanent " + hullMods.permanent());
        int count = 0;
        for (EnemyBuildStep step : build.steps()) {
            if (step.isAllocated()) {
                count++;
                out.println(String.format("  %2d %-40s %s", count, step.nodeId(), step.outcome()));
            } else if (!EnemyBuildStep.COUNT_REACHED.equals(step.outcome()) && !EnemyBuildStep.ALREADY_ALLOCATED.equals(step.outcome())) {
                out.println(String.format("     %-40s %s", step.nodeId(), step.outcome()));
            }
        }
        out.println("Stripped hullmods: " + build.strippedHullModIds());
    }

    private static Set<String> csv(String value) {
        Set<String> ids = new TreeSet<>(Arrays.asList(value.split(",")));
        ids.remove("");
        return ids;
    }
}
