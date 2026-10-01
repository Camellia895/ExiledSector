package exiledsector.ui.node;

import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTree;
import exiledsector.skills.template.StepVerdict;
import exiledsector.skills.template.TemplateBudget;
import exiledsector.skills.template.TemplateStep;
import exiledsector.skills.template.TemplateStepRules;
import org.apache.log4j.Logger;

import java.util.function.Consumer;
import java.util.function.Supplier;

final class TemplateStepExecutor {

    private final NodeAllocator allocator;
    private final Supplier<NodeAllocator.Snapshot> snapshot;
    private final Consumer<SkillNode> onAllocated;
    private NodeAllocator.Snapshot pointsLeftSnapshot;
    private boolean pointsLeft;

    TemplateStepExecutor(NodeAllocator allocator, Supplier<NodeAllocator.Snapshot> snapshot, Consumer<SkillNode> onAllocated) {
        this.allocator = allocator;
        this.snapshot = snapshot;
        this.onAllocated = onAllocated;
    }

    StepVerdict attempt(TemplateStep step) {
        NodeAllocator.Snapshot current = snapshot.get();
        StepVerdict verdict = TemplateStepRules.verdict(step, current.data(), current.satisfiedRootId(),
                current::canAllocate, allocator::blockAllocationReason);
        if (verdict != StepVerdict.ALLOCATE) {
            logSkip(step, verdict);
            return verdict;
        }
        SkillNode node = SkillTree.get(step.nodeId());
        if (node.getType().isOptional()) {
            allocator.allocateOption(node, TemplateStepRules.optionFor(step, node.getType()));
        } else if (!allocator.toggle(node)) {
            logSkip(step, StepVerdict.NOT_ALLOCATABLE);
            return StepVerdict.NOT_ALLOCATABLE;
        }
        onAllocated.accept(node);
        return StepVerdict.ALLOCATE;
    }

    boolean hasPointsLeft() {
        NodeAllocator.Snapshot current = snapshot.get();
        if (current != pointsLeftSnapshot) {
            pointsLeftSnapshot = current;
            ShipSkillData data = current.data();
            pointsLeft = TemplateBudget.hasPointsLeft(data.getAllocatedNodeIds().size(), current.maxAllocatedNodes(),
                    data.getBankedFreeAllocations(), data.getSpentOp(current.opCostPerNode()), current.opCostPerNode(),
                    current.totalOpBudget());
        }
        return pointsLeft;
    }

    private static void logSkip(TemplateStep step, StepVerdict verdict) {
        if (verdict != StepVerdict.ALREADY_ALLOCATED) {
            Logger.getLogger(TemplateStepExecutor.class).debug("[ExiledSector] Auto-allocate skipped " + step.nodeId() + ": " + verdict);
        }
    }
}
