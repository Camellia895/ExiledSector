package exiledsector.i18n;

public final class NumberText {

    private NumberText() {
    }

    public static String format(float value) {
        if (value == Math.rint(value)) {
            return String.valueOf((int) value);
        }
        return String.valueOf(value);
    }
}
