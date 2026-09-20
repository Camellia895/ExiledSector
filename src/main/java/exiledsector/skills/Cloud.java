package exiledsector.skills;

public class Cloud {

    private final String id;
    private final float x;
    private final float y;
    private final float radius;
    private final String colorHex;

    public Cloud(String id, float x, float y, float radius, String colorHex) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.colorHex = colorHex;
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

    public float getRadius() {
        return radius;
    }

    public boolean isVanillaColor() {
        return colorHex == null;
    }

    public String getColorHex() {
        return colorHex;
    }
}
