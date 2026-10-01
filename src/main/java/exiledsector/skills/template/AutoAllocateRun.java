package exiledsector.skills.template;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public final class AutoAllocateRun {

    static final float BASE_STEP_SECONDS = 0.15f;
    static final float TARGET_TOTAL_SECONDS = 2.5f;
    static final int MAX_BLOCKED_CHECKS_PER_FRAME = 8;

    public enum Status {
        RUNNING, PATH_END, OUT_OF_POINTS, CANCELLED
    }

    public record Summary(Status status, int allocated, int skipped) {
    }

    private final List<TemplateStep> steps;
    private final float stepSeconds;
    private float budget;
    private int index;
    private int allocated;
    private int skipped;
    private Status status = Status.RUNNING;

    public AutoAllocateRun(List<TemplateStep> steps, int pendingCount) {
        this.steps = List.copyOf(steps);
        this.stepSeconds = Math.min(BASE_STEP_SECONDS, TARGET_TOTAL_SECONDS / Math.max(1, pendingCount));
        this.budget = stepSeconds;
    }

    public void advance(float amount, Function<TemplateStep, StepVerdict> attempt, BooleanSupplier pointsLeft) {
        if (status != Status.RUNNING) {
            return;
        }
        budget += amount;
        int blockedChecks = 0;
        while (status == Status.RUNNING) {
            if (!pointsLeft.getAsBoolean()) {
                status = Status.OUT_OF_POINTS;
            } else if (index >= steps.size()) {
                status = Status.PATH_END;
            } else if (budget < stepSeconds || blockedChecks >= MAX_BLOCKED_CHECKS_PER_FRAME) {
                return;
            } else {
                StepVerdict verdict = attempt.apply(steps.get(index++));
                if (verdict == StepVerdict.ALLOCATE) {
                    allocated++;
                    budget -= stepSeconds;
                } else if (verdict != StepVerdict.ALREADY_ALLOCATED) {
                    skipped++;
                    if (verdict == StepVerdict.BLOCKED) {
                        blockedChecks++;
                    }
                }
            }
        }
    }

    public void cancel() {
        if (status == Status.RUNNING) {
            status = Status.CANCELLED;
        }
    }

    public boolean isFinished() {
        return status != Status.RUNNING;
    }

    public Summary summary() {
        return new Summary(status, allocated, skipped);
    }

    float stepSeconds() {
        return stepSeconds;
    }
}
