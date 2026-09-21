package exiledsector.ui.decoration;

import exiledsector.skills.SkillTreeObject;

public class RingBelt extends SkillTreeObject {

    private final float innerRadius;
    private final float outerRadius;
    private final String ringArtPath;

    public RingBelt(String id, float x, float y, float innerRadius, float outerRadius, String ringArtPath) {
        super(id, x, y);
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        this.ringArtPath = ringArtPath;
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
}
