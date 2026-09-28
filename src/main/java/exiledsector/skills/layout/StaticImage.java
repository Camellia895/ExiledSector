package exiledsector.skills.layout;

public class StaticImage extends SkillTreeObject {

    private final float width;
    private final float height;
    private final String imagePath;
    private final Rotation rotation;

    public StaticImage(String id, float x, float y, float width, float height, String imagePath, Rotation rotation) {
        super(id, x, y);
        this.width = width;
        this.height = height;
        this.imagePath = imagePath;
        this.rotation = rotation;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public String getImagePath() {
        return imagePath;
    }

    public float getRotation() {
        return rotation.degrees();
    }

    public float getRotationSpeed() {
        return rotation.speed();
    }
}
