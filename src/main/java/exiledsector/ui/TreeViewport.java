package exiledsector.ui;

public record TreeViewport(float centerX, float centerY, float zoom) {

    public float screenX(float worldX) {
        return centerX + worldX * zoom;
    }

    public float screenY(float worldY) {
        return centerY - worldY * zoom;
    }
}
