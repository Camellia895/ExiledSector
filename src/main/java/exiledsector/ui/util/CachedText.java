package exiledsector.ui.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class CachedText<K, V> {

    private static final Object SINGLE_KEY = new Object();

    private final Map<Object, Entry<V>> entries = new HashMap<>();

    public V get(K key, Object signature, Function<K, V> builder) {
        return getInternal(key, signature, () -> builder.apply(key));
    }

    public V get(Object signature, Supplier<V> builder) {
        return getInternal(SINGLE_KEY, signature, builder);
    }

    public void invalidate(K key) {
        entries.remove(key);
    }

    public void clear() {
        entries.clear();
    }

    private V getInternal(Object key, Object signature, Supplier<V> builder) {
        Entry<V> entry = entries.get(key);
        if (entry != null && signature.equals(entry.signature)) {
            return entry.value;
        }
        V value = builder.get();
        entries.put(key, new Entry<>(signature, value));
        return value;
    }

    private static final class Entry<V> {
        final Object signature;
        final V value;

        Entry(Object signature, V value) {
            this.signature = signature;
            this.value = value;
        }
    }
}
