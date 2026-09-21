package exiledsector.skills;

public class RingBelt {

    private final String id;
    private final float x;
    private final float y;
    private final float innerRadius;
    private final float outerRadius;
    private final String ringArtPath;

    public RingBelt(String id, float x, float y, float innerRadius, float outerRadius, String ringArtPath) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        this.ringArtPath = ringArtPath;
    }

    public String getId() {
        return id;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
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
