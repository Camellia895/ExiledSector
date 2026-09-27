package exiledsector.ui;

final class CameraPanAnimation {

    static final float DURATION_SECONDS = 1f;

    private final float startX;
    private final float startY;
    private final float targetX;
    private final float targetY;
    private float elapsed;

    CameraPanAnimation(float startX, float startY, float targetX, float targetY) {
        this.startX = startX;
        this.startY = startY;
        this.targetX = targetX;
        this.targetY = targetY;
    }

    void advance(float amount) {
        elapsed = Math.min(DURATION_SECONDS, elapsed + amount);
    }

    boolean isFinished() {
        return elapsed >= DURATION_SECONDS;
    }

    float x() {
        return startX + (targetX - startX) * eased();
    }

    float y() {
        return startY + (targetY - startY) * eased();
    }

    private float eased() {
        float t = elapsed / DURATION_SECONDS;
        return t * t * (3f - 2f * t);
    }
}
