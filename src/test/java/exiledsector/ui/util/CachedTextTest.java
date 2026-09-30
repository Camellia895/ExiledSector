package exiledsector.ui.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class CachedTextTest {

    private final CachedText<String, StringBuilder> cache = new CachedText<>();
    private final List<String> builds = new ArrayList<>();

    private StringBuilder build(String key) {
        builds.add(key);
        return new StringBuilder(key);
    }

    @Test
    void anEqualSignatureReusesTheCachedValueWithoutRebuilding() {
        StringBuilder first = cache.get("node_a", List.of(1, "x"), this::build);
        StringBuilder second = cache.get("node_a", List.of(1, "x"), this::build);

        assertSame(first, second);
        assertEquals(List.of("node_a"), builds);
    }

    @Test
    void aChangedSignatureRebuildsAndReplacesTheCachedValue() {
        StringBuilder first = cache.get("node_a", 1, this::build);
        StringBuilder second = cache.get("node_a", 2, this::build);
        StringBuilder third = cache.get("node_a", 2, this::build);

        assertNotSame(first, second);
        assertSame(second, third);
        assertEquals(List.of("node_a", "node_a"), builds);
    }

    @Test
    void eachKeyIsCachedIndependently() {
        cache.get("node_a", 1, this::build);
        cache.get("node_b", 1, this::build);
        cache.get("node_a", 1, this::build);
        cache.get("node_b", 1, this::build);

        assertEquals(List.of("node_a", "node_b"), builds);
    }

    @Test
    void theKeylessFormCachesASingleValueBySignature() {
        CachedText<Void, StringBuilder> single = new CachedText<>();

        StringBuilder first = single.get("sig", () -> build("title"));
        StringBuilder second = single.get("sig", () -> build("title"));
        StringBuilder third = single.get("other", () -> build("title"));

        assertSame(first, second);
        assertNotSame(first, third);
        assertEquals(List.of("title", "title"), builds);
    }
}
