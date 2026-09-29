package exiledsector.i18n;

import java.util.HashMap;
import java.util.Map;

public final class Message {

    private final String key;
    private final Map<String, StyledText> args = new HashMap<>();
    private Integer count;

    Message(String key) {
        this.key = key;
    }

    public Message arg(String name, String literal) {
        args.put(name, StyledText.of(literal));
        return this;
    }

    public Message arg(String name, float number) {
        return arg(name, NumberText.format(number));
    }

    public Message arg(String name, int number) {
        return arg(name, String.valueOf(number));
    }

    public Message arg(String name, StyledText value) {
        args.put(name, value);
        return this;
    }

    public Message count(int count) {
        this.count = count;
        return arg("count", count);
    }

    public StyledText styled() {
        Catalogue catalogue = I18n.catalogue();
        return catalogue.template(resolvedKey(catalogue)).render(args);
    }

    public String text() {
        return styled().plain();
    }

    private String resolvedKey(Catalogue catalogue) {
        if (count == null) {
            return key;
        }
        String categoryKey = key + "." + Plurals.category(catalogue.locale(), count);
        if (catalogue.has(categoryKey)) {
            return categoryKey;
        }
        String otherKey = key + "." + Plurals.OTHER;
        return catalogue.has(otherKey) ? otherKey : key;
    }
}
