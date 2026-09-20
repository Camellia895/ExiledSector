package exiledsector.skills;

public class StaticImage {

    private final String id;
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final String imagePath;
    private final float rotation;

    public StaticImage(String id, float x, float y, float width, float height, String imagePath, float rotation) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.imagePath = imagePath;
        this.rotation = rotation;
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
}
