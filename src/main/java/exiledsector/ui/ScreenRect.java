package exiledsector.ui;

record ScreenRect(float left, float bottom, float width, float height) {

    static final ScreenRect NONE = new ScreenRect(0f, 0f, 0f, 0f);

    boolean contains(float x, float y) {
        return width > 0f && height > 0f && x >= left && x <= left + width && y >= bottom && y <= bottom + height;
    }
}
