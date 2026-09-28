package exiledsector.skills.npc;

import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillTree;
import exiledsector.skills.npc.NpcArchetypes.Archetype;
import exiledsector.skills.npc.NpcLayoutValidator.ArchetypeRun;
import exiledsector.skills.npc.NpcLayoutValidator.LayoutReport;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NpcLayoutsDataTest {

    private static final int LAYOUTS_PER_ROOT = 3;

    private JSONObject layoutsJson;
    private Map<String, NpcLayout> layouts;

    @BeforeEach
    void setUp() throws Exception {
        Path root = RealSkillData.projectRoot();
        RealSkillData.load(root);
        Path layoutsFile = root.resolve(RealSkillData.LAYOUTS_FILE);
        assertTrue(Files.isRegularFile(layoutsFile), layoutsFile + " does not exist yet - designers still need to create it");
        layoutsJson = RealSkillData.readJson(layoutsFile);
        layouts = NpcLayoutValidator.parse(layoutsJson);
    }

    @AfterEach
    void tearDown() {
        RealSkillData.clear();
    }

    @Test
    void everyLayoutInTheFileParses() {
        List<String> problems = NpcLayoutValidator.fileProblems(layoutsJson);

        assertTrue(problems.isEmpty(), String.join("\n", problems));
    }

    @Test
    void everyLayoutPassesStaticValidation() {
        List<String> problems = new ArrayList<>();
        for (NpcLayout layout : layouts.values()) {
            for (String problem : NpcLayoutValidator.staticProblems(layout)) {
                problems.add(layout.id() + ": " + problem);
            }
        }

        assertTrue(problems.isEmpty(), String.join("\n", problems));
    }

    @Test
    void everyEligibleArchetypeReachesTheTargetNodeCount() {
        List<String> problems = new ArrayList<>();
        for (NpcLayout layout : layouts.values()) {
            LayoutReport report = NpcLayoutValidator.report(layout);
            for (ArchetypeRun run : report.eligibleRuns()) {
                if (!run.meetsTarget()) {
                    problems.add(layout.id() + ": " + run.archetype().name() + " allocates " + run.allocatedAtTarget()
                            + " of " + NpcLayoutValidator.TARGET_NODE_COUNT + ", skipping "
                            + run.atTarget().steps().stream().filter(NpcBuildStep::isSkipped)
                            .map(step -> step.nodeId() + " (" + step.outcome() + ")").toList());
                }
            }
        }

        assertTrue(problems.isEmpty(), String.join("\n", problems));
    }

    @Test
    void everyArchetypeHasAnEligibleLayout() {
        List<String> uncovered = new ArrayList<>();
        for (Archetype archetype : NpcArchetypes.ALL) {
            if (layouts.values().stream().noneMatch(layout -> layout.isEligible(archetype.profile()))) {
                uncovered.add(archetype.name());
            }
        }

        assertTrue(uncovered.isEmpty(), "Archetypes without an eligible layout: " + uncovered);
    }

    @Test
    void everyRootHasThreeLayouts() {
        List<String> problems = new ArrayList<>();
        for (SkillNode node : SkillTree.getAllNodes().values()) {
            if (node.getType().getTier() == SkillTier.ROOT) {
                List<String> ids = layouts.values().stream()
                        .filter(layout -> layout.rootNodeId().equals(node.getId())).map(NpcLayout::id).toList();
                if (ids.size() != LAYOUTS_PER_ROOT) {
                    problems.add(node.getId() + " has " + ids.size() + " layouts " + ids + ", expected " + LAYOUTS_PER_ROOT);
                }
            }
        }

        assertTrue(problems.isEmpty(), String.join("\n", problems));
    }

    @Test
    void everyEquivalentHullmodBelongsToExactlyOneSkillType() {
        Map<String, List<String>> typesByHullMod = new TreeMap<>();
        for (SkillNode node : SkillTree.getAllNodes().values()) {
            String hullModId = node.getType().getEquivalentHullModId();
            if (hullModId != null) {
                List<String> typeIds = typesByHullMod.computeIfAbsent(hullModId, key -> new ArrayList<>());
                if (!typeIds.contains(node.getType().getId())) {
                    typeIds.add(node.getType().getId());
                }
            }
        }
        List<String> problems = new ArrayList<>();
        typesByHullMod.forEach((hullModId, typeIds) -> {
            if (typeIds.size() > 1) {
                problems.add(hullModId + " is the equivalent hullmod of " + typeIds);
            }
        });

        assertTrue(problems.isEmpty(), String.join("\n", problems));
    }
}
