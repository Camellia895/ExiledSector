package exiledsector.skills;

public abstract class SkillTreeObject {

    private final String id;
    private final float x;
    private final float y;

    protected SkillTreeObject(String id, float x, float y) {
        this.id = id;
        this.x = x;
        this.y = y;
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
}
