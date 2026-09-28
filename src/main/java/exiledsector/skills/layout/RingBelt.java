package exiledsector.skills.layout;

public class RingBelt extends SkillTreeObject {

    private final float innerRadius;
    private final float outerRadius;
    private final String ringArtPath;
    private final Rotation rotation;

    public RingBelt(String id, float x, float y, float innerRadius, float outerRadius, String ringArtPath,
                     Rotation rotation) {
        super(id, x, y);
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        this.ringArtPath = ringArtPath;
        this.rotation = rotation;
    }

    public float getInnerRadius() {
        return innerRadius;
    }

    public float getOuterRadius() {
        return outerRadius;
    }

    public String getRingArtPath() {
        return ringArtPath;
    }

    public float getRotation() {
        return rotation.degrees();
    }

    public float getRotationSpeed() {
        return rotation.speed();
    }
}
