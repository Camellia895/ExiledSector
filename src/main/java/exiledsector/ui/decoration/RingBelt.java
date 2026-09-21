package exiledsector.ui.decoration;

import exiledsector.skills.SkillTreeObject;

public class RingBelt extends SkillTreeObject {

    private final float innerRadius;
    private final float outerRadius;
    private final String ringArtPath;
    private final float rotation;
    private final float rotationSpeed;

    public RingBelt(String id, float x, float y, float innerRadius, float outerRadius, String ringArtPath,
                     float rotation, float rotationSpeed) {
        super(id, x, y);
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        this.ringArtPath = ringArtPath;
        this.rotation = rotation;
        this.rotationSpeed = rotationSpeed;
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
        return rotation;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }
}
