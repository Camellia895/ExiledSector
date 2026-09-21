package exiledsector.ui.decoration;

import exiledsector.skills.SkillTreeObject;

public class StaticImage extends SkillTreeObject {

    private final float width;
    private final float height;
    private final String imagePath;
    private final float rotation;
    private final float rotationSpeed;

    public StaticImage(String id, float x, float y, float width, float height, String imagePath, float rotation, float rotationSpeed) {
        super(id, x, y);
        this.width = width;
        this.height = height;
        this.imagePath = imagePath;
        this.rotation = rotation;
        this.rotationSpeed = rotationSpeed;
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
        return rotation;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }
}
