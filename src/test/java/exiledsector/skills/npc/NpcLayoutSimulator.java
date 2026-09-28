package exiledsector.skills.npc;

import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import exiledsector.skills.npc.NpcArchetypes.Archetype;
import exiledsector.skills.npc.NpcLayoutValidator.ArchetypeRun;
import exiledsector.skills.npc.NpcLayoutValidator.Ineligible;
import exiledsector.skills.npc.NpcLayoutValidator.LayoutReport;
import exiledsector.skills.npc.NpcLayoutValidator.Milestone;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class NpcLayoutSimulator {

    static final String USAGE = "Usage: NpcLayoutSimulator <layoutsJsonFile> [--markdown <outFile>]";
    static final List<Integer> MARKDOWN_COUNTS = List.of(5, 10, 20, 30, NpcLayoutValidator.TARGET_NODE_COUNT);
    private static final String RULE = "=".repeat(78);

    private record Arguments(Path layoutsFile, Path markdownFile) {
    }

    private NpcLayoutSimulator() {
    }

    public static void main(String[] args) {
        PrintStream out = System.out;
        Arguments arguments = parseArguments(args);
        if (arguments == null) {
            out.println(USAGE);
            return;
        }
        try {
            simulate(arguments, out);
        } catch (IOException | JSONException | RuntimeException e) {
            out.println("ERROR: " + e);
        }
    }

    private static Arguments parseArguments(String[] args) {
        if (args.length == 1) {
            return new Arguments(Path.of(args[0]), null);
        }
        if (args.length == 3 && "--markdown".equals(args[1])) {
            return new Arguments(Path.of(args[0]), Path.of(args[2]));
        }
        return null;
    }

    private static void simulate(Arguments arguments, PrintStream out) throws IOException, JSONException {
        Path projectRoot = RealSkillData.projectRoot();
        RealSkillData.load(projectRoot);
        JSONObject root = RealSkillData.readJson(arguments.layoutsFile());
        List<String> fileProblems = NpcLayoutValidator.fileProblems(root);
        Map<String, NpcLayout> layouts = NpcLayoutValidator.parse(root);
        List<LayoutReport> reports = layouts.values().stream().map(NpcLayoutValidator::report).toList();

        out.println("NPC layout simulation");
        out.println("  layouts file: " + arguments.layoutsFile().toAbsolutePath());
        out.println("  skill data:   " + projectRoot + " (" + SkillTree.getAllNodes().size() + " nodes, "
                + SkillTree.getAllTypes().size() + " types)");
        out.println("  layouts:      " + layouts.size() + " parsed, " + fileProblems.size() + " file problem(s)");
        for (String problem : fileProblems) {
            out.println("FILE PROBLEM: " + problem);
        }
        for (LayoutReport report : reports) {
            printReport(report, out);
        }
        List<String> malformedIds = malformedLayoutIds(root, layouts);
        if (!malformedIds.isEmpty()) {
            out.println();
            out.println(RULE);
        }
        for (String id : malformedIds) {
            out.println("RESULT " + id + ": FAIL - malformed, skipped by the loader (see FILE PROBLEM above)");
        }
        printSummary(reports, malformedIds, fileProblems, out);

        if (arguments.markdownFile() != null) {
            Path markdownFile = arguments.markdownFile().toAbsolutePath();
            if (markdownFile.getParent() != null) {
                Files.createDirectories(markdownFile.getParent());
            }
            Files.writeString(markdownFile, markdown(arguments.layoutsFile(), reports, fileProblems), StandardCharsets.UTF_8);
            out.println("Markdown written to " + markdownFile);
        }
    }

    private static void printReport(LayoutReport report, PrintStream out) {
        NpcLayout layout = report.layout();
        out.println();
        out.println(RULE);
        out.println("Layout " + layout.id() + " - \"" + layout.name() + "\"");
        out.println("  root: " + layout.rootNodeId() + "   requires: " + layout.requires() + "   entries: " + layout.entries().size());
        if (!layout.description().isBlank()) {
            out.println("  description: " + layout.description());
        }
        if (report.staticProblems().isEmpty()) {
            out.println("  static problems: none");
        } else {
            out.println("  static problems (" + report.staticProblems().size() + "):");
            report.staticProblems().forEach(problem -> out.println("    - " + problem));
        }
        out.println("  ineligible archetypes: " + (report.ineligible().isEmpty() ? "none" : report.ineligible().stream()
                .map(ineligible -> ineligible.archetype().name() + " (" + ineligible.unmetRequirement() + ")")
                .collect(Collectors.joining(", "))));
        out.println("  eligible archetypes (" + report.eligibleRuns().size() + "):");
        if (report.eligibleRuns().isEmpty()) {
            out.println("    WARNING: no archetype is eligible for this layout");
        }
        for (ArchetypeRun run : report.eligibleRuns()) {
            printRun(run, out);
        }
        out.println("RESULT " + layout.id() + ": " + (report.passed() ? "PASS" : "FAIL - " + String.join("; ", failureSummary(report))));
    }

    private static List<String> failureSummary(LayoutReport report) {
        List<String> summary = new ArrayList<>();
        if (!report.staticProblems().isEmpty()) {
            summary.add(report.staticProblems().size() + " static problem(s)");
        }
        summary.addAll(report.runProblems());
        return summary;
    }

    private static void printRun(ArchetypeRun run, PrintStream out) {
        out.println("    " + run.archetype().name() + ": " + run.allocatedAtTarget() + "/" + NpcLayoutValidator.TARGET_NODE_COUNT
                + " at nodeCount " + NpcLayoutValidator.TARGET_NODE_COUNT + ", " + run.allocatedAtMax() + "/"
                + NpcLayoutValidator.MAX_NODE_COUNT + " at nodeCount " + NpcLayoutValidator.MAX_NODE_COUNT
                + (run.meetsTarget() ? "" : "   <-- below " + NpcLayoutValidator.TARGET_NODE_COUNT));
        List<NpcBuildStep> steps = run.atMax().steps();
        List<String> skipped = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            NpcBuildStep step = steps.get(i);
            if (step.isSkipped()) {
                skipped.add("#" + (i + 1) + " " + step.nodeId() + ": " + step.outcome());
            }
        }
        out.println("      skipped: " + (skipped.isEmpty() ? "none" : skipped.size()));
        skipped.forEach(line -> out.println("        - " + line));
        if (run.unreachedAtMax() > 0) {
            out.println("      entries not reached at nodeCount " + NpcLayoutValidator.MAX_NODE_COUNT + ": " + run.unreachedAtMax());
        }
        List<Milestone> milestones = run.milestones();
        out.println("      notables/keystones: " + (milestones.isEmpty() ? "none" : milestones.stream()
                .map(milestone -> "@" + milestone.nodeCount() + " " + milestone.displayName() + " [" + milestone.tier() + ", "
                        + milestone.nodeId() + "]")
                .collect(Collectors.joining(", "))));
    }

    private static List<String> malformedLayoutIds(JSONObject root, Map<String, NpcLayout> layouts) {
        JSONObject layoutsJson = root.optJSONObject("layouts");
        if (layoutsJson == null) {
            return List.of();
        }
        return NpcLayoutLoader.layoutIds(layoutsJson).stream().filter(id -> !layouts.containsKey(id)).toList();
    }

    private static void printSummary(List<LayoutReport> reports, List<String> malformedIds, List<String> fileProblems,
                                     PrintStream out) {
        out.println();
        out.println(RULE);
        out.println("Eligible layouts per archetype:");
        for (Archetype archetype : NpcArchetypes.ALL) {
            out.println(String.format("  %-26s %d", archetype.name(), eligibleCount(reports, archetype)));
        }
        long passed = reports.stream().filter(LayoutReport::passed).count();
        out.println("Summary: " + passed + " PASS, " + (reports.size() - passed + malformedIds.size()) + " FAIL ("
                + malformedIds.size() + " malformed), " + fileProblems.size() + " file problem(s)");
    }

    private static long eligibleCount(List<LayoutReport> reports, Archetype archetype) {
        return reports.stream().filter(report -> report.layout().isEligible(archetype.profile())).count();
    }

    static String markdown(Path layoutsFile, List<LayoutReport> reports, List<String> fileProblems) {
        StringBuilder md = new StringBuilder();
        md.append("# NPC layouts\n\n");
        md.append("Generated by `NpcLayoutSimulator` from `").append(layoutsFile.getFileName()).append("`. ")
                .append(reports.size()).append(" layout(s). Target: every eligible archetype allocates ")
                .append(NpcLayoutValidator.TARGET_NODE_COUNT).append(" nodes at nodeCount ")
                .append(NpcLayoutValidator.TARGET_NODE_COUNT).append(".\n\n");
        if (!fileProblems.isEmpty()) {
            md.append("## File problems\n\n");
            fileProblems.forEach(problem -> md.append("- ").append(cell(problem)).append('\n'));
            md.append('\n');
        }
        appendOverview(md, reports);
        for (LayoutReport report : reports) {
            appendLayout(md, report);
        }
        return md.toString();
    }

    private static void appendOverview(StringBuilder md, List<LayoutReport> reports) {
        md.append("## Overview\n\n| Layout | Id | Root | Requires | Status |\n|---|---|---|---|---|\n");
        for (LayoutReport report : reports) {
            NpcLayout layout = report.layout();
            md.append("| ").append(cell(layout.name())).append(" | `").append(layout.id()).append("` | `")
                    .append(layout.rootNodeId()).append("` | ").append(requiresText(layout)).append(" | ")
                    .append(report.passed() ? "PASS" : "FAIL").append(" |\n");
        }
        md.append("\n| Archetype | Eligible layouts |\n|---|---|\n");
        for (Archetype archetype : NpcArchetypes.ALL) {
            md.append("| ").append(archetype.name()).append(" | ").append(eligibleCount(reports, archetype)).append(" |\n");
        }
        md.append('\n');
    }

    private static void appendLayout(StringBuilder md, LayoutReport report) {
        NpcLayout layout = report.layout();
        md.append("## ").append(cell(layout.name())).append("\n\n");
        md.append("- **Id:** `").append(layout.id()).append("`\n");
        md.append("- **Root:** `").append(layout.rootNodeId()).append("`").append(rootName(layout)).append('\n');
        md.append("- **Requires:** ").append(requiresText(layout)).append('\n');
        md.append("- **Status:** ").append(report.passed() ? "PASS" : "FAIL").append('\n');
        md.append("- **Eligible archetypes:** ").append(report.eligibleRuns().isEmpty() ? "none" : report.eligibleRuns().stream()
                .map(run -> run.archetype().name()).collect(Collectors.joining(", "))).append('\n');
        md.append("- **Ineligible archetypes:** ").append(report.ineligible().isEmpty() ? "none" : report.ineligible().stream()
                .map(NpcLayoutSimulator::ineligibleText).collect(Collectors.joining(", "))).append("\n\n");
        if (!layout.description().isBlank()) {
            md.append(layout.description()).append("\n\n");
        }
        if (!report.problems().isEmpty()) {
            md.append("### Problems\n\n");
            report.problems().forEach(problem -> md.append("- ").append(cell(problem)).append('\n'));
            md.append('\n');
        }
        appendMilestoneTable(md, report);
        appendNodeList(md, layout);
    }

    private static String ineligibleText(Ineligible ineligible) {
        return ineligible.archetype().name() + " (" + ineligible.unmetRequirement() + ")";
    }

    private static void appendMilestoneTable(StringBuilder md, LayoutReport report) {
        if (report.eligibleRuns().isEmpty()) {
            return;
        }
        md.append("### Notables and keystones by node count\n\n");
        md.append("Each column lists the notables/keystones first reached in that node-count range; the number in ")
                .append("brackets is the node count at which the node arrives. Everything to the left is also owned.\n\n");
        md.append("| Archetype | Nodes at ").append(NpcLayoutValidator.TARGET_NODE_COUNT);
        int previous = 0;
        for (int count : MARKDOWN_COUNTS) {
            md.append(" | ").append(previous + 1).append('-').append(count);
            previous = count;
        }
        md.append(" |\n|---|---").append("|---".repeat(MARKDOWN_COUNTS.size())).append("|\n");
        for (ArchetypeRun run : report.eligibleRuns()) {
            md.append("| ").append(run.archetype().name()).append(" | ").append(run.allocatedAtTarget());
            List<Milestone> milestones = run.milestones();
            previous = 0;
            for (int count : MARKDOWN_COUNTS) {
                int lower = previous;
                String names = milestones.stream()
                        .filter(milestone -> milestone.nodeCount() > lower && milestone.nodeCount() <= count)
                        .map(milestone -> cell(milestone.displayName()) + " (" + milestone.nodeCount() + ")")
                        .collect(Collectors.joining(", "));
                md.append(" | ").append(names.isEmpty() ? "-" : names);
                previous = count;
            }
            md.append(" |\n");
        }
        md.append('\n');
    }

    private static void appendNodeList(StringBuilder md, NpcLayout layout) {
        md.append("### Build path\n\n| # | Node | Type | Option | Tier |\n|---|---|---|---|---|\n");
        for (int i = 0; i < layout.entries().size(); i++) {
            NpcLayoutEntry entry = layout.entries().get(i);
            SkillNode node = SkillTree.get(entry.nodeId());
            SkillType option = entry.optionTypeId() == null ? null : SkillTree.getType(entry.optionTypeId());
            md.append("| ").append(i + 1).append(" | `").append(entry.nodeId()).append("` | ")
                    .append(node == null ? "(unknown node)" : cell(node.getDisplayName())).append(" | ")
                    .append(optionText(entry, option)).append(" | ")
                    .append(node == null ? "-" : node.getType().getTier()).append(" |\n");
        }
        md.append('\n');
    }

    private static String optionText(NpcLayoutEntry entry, SkillType option) {
        if (entry.optionTypeId() == null) {
            return "-";
        }
        return option == null ? "(unknown option `" + entry.optionTypeId() + "`)" : cell(option.getDisplayName());
    }

    private static String rootName(NpcLayout layout) {
        SkillNode root = SkillTree.get(layout.rootNodeId());
        return root == null ? " (unknown node)" : " (" + cell(root.getDisplayName()) + ")";
    }

    private static String requiresText(NpcLayout layout) {
        return layout.requires().isEmpty() ? "none" : layout.requires().stream()
                .map(tag -> "`" + tag + "`").collect(Collectors.joining(", "));
    }

    private static String cell(String text) {
        return text == null ? "" : text.replace("|", "\\|").replace("\r", " ").replace("\n", " ");
    }
}
