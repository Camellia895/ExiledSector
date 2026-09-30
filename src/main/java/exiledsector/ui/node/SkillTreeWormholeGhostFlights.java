package exiledsector.ui.node;

import com.fs.starfarer.api.graphics.SpriteAPI;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTier;
import exiledsector.skills.SkillTree;
import exiledsector.ui.TreeViewport;
import exiledsector.ui.util.SpriteCache;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

final class SkillTreeWormholeGhostFlights {

    static final int GHOSTS_PER_FLIGHT = 3;
    static final float MIN_SECONDS_BETWEEN_FLIGHTS = 10f;
    static final float MAX_SECONDS_BETWEEN_FLIGHTS = 25f;
    private static final float FLIGHT_SPEED = 1400f;
    private static final float MIN_FLIGHT_SECONDS = 2.5f;
    private static final float FADE_SECONDS = 0.6f;
    private static final float MAX_ARC_RATIO = 0.2f;
    private static final float GHOST_SIZE_RATIO = 0.35f;
    private static final float WING_BACK_SPACING = 0.9f;
    private static final float WING_SIDE_SPACING = 0.75f;
    private static final float WOBBLE_RATIO = 0.15f;
    private static final float WOBBLE_FREQUENCY = 3f;
    private static final float FLICKER_FREQUENCY = 5f;
    private static final float MIN_ALPHA = 0.45f;
    private static final float MAX_ALPHA = 0.9f;

    private final SkillTreeNodeGhostRenderer ghostRenderer;
    private final SpriteCache spriteCache = new SpriteCache(SkillTreeWormholeGhostFlights.class);
    private final Random random;
    private final Map<String, Float> secondsUntilNextFlight = new HashMap<>();
    private final List<Flight> flights = new ArrayList<>();
    private List<WormholePair> pairs;

    SkillTreeWormholeGhostFlights(SkillTreeNodeGhostRenderer ghostRenderer, Random random) {
        this.ghostRenderer = ghostRenderer;
        this.random = random;
    }

    void advance(float amount, ShipSkillData data) {
        Iterator<Flight> iterator = flights.iterator();
        while (iterator.hasNext()) {
            Flight flight = iterator.next();
            flight.elapsed += amount;
            if (flight.elapsed >= flight.duration) {
                iterator.remove();
            }
        }
        for (WormholePair pair : wormholePairs()) {
            if (data.isAllocated(pair.a().getId()) && data.isAllocated(pair.b().getId())) {
                scheduleFlights(pair, amount);
            }
        }
    }

    int activeFlightCount() {
        return flights.size();
    }

    private void scheduleFlights(WormholePair pair, float amount) {
        String key = pair.a().getId();
        float remaining = secondsUntilNextFlight.computeIfAbsent(key,
                id -> random.nextFloat() * MAX_SECONDS_BETWEEN_FLIGHTS) - amount;
        if (remaining <= 0f) {
            flights.add(launch(pair));
            remaining = MIN_SECONDS_BETWEEN_FLIGHTS
                    + random.nextFloat() * (MAX_SECONDS_BETWEEN_FLIGHTS - MIN_SECONDS_BETWEEN_FLIGHTS);
        }
        secondsUntilNextFlight.put(key, remaining);
    }

    void launchFrom(SkillNode from, SkillNode to) {
        flights.add(launch(from, to));
    }

    private Flight launch(WormholePair pair) {
        boolean forwards = random.nextBoolean();
        return forwards ? launch(pair.a(), pair.b()) : launch(pair.b(), pair.a());
    }

    private Flight launch(SkillNode from, SkillNode to) {
        Flight flight = new Flight();
        flight.from = new Vector2f(from.getOffsetX(), from.getOffsetY());
        flight.to = new Vector2f(to.getOffsetX(), to.getOffsetY());
        float distance = Vector2f.sub(flight.to, flight.from, null).length();
        flight.duration = Math.max(MIN_FLIGHT_SECONDS, distance / FLIGHT_SPEED);
        flight.arc = (random.nextFloat() * 2f - 1f) * MAX_ARC_RATIO * distance;
        flight.phase = random.nextFloat() * 10f;
        return flight;
    }

    private List<WormholePair> wormholePairs() {
        if (pairs == null) {
            pairs = new ArrayList<>();
            for (SkillNode node : SkillTree.getAllNodes().values()) {
                SkillNode paired = node.getPairedNodeId() == null ? null : SkillTree.get(node.getPairedNodeId());
                boolean isFirstOfPair = paired != null && node.getId().compareTo(paired.getId()) < 0;
                if (node.getType().getTier() == SkillTier.WORMHOLE && isFirstOfPair) {
                    pairs.add(new WormholePair(node, paired));
                }
            }
        }
        return pairs;
    }

    void draw(TreeViewport viewport, float alphaMult) {
        SpriteAPI sprite = flights.isEmpty() ? null : spriteCache.sprite(SkillTreeNodeGhostRenderer.GHOST_TEXTURE_PATH);
        if (sprite == null) {
            return;
        }
        Color color = ghostRenderer.ghostColor();
        float size = SkillTreeNodeGeometry.NODE_SIZE * GHOST_SIZE_RATIO * viewport.zoom();


        for (Flight flight : flights) {
            drawFlight(flight, sprite, color, size, viewport, alphaMult);
        }
    }

    private void drawFlight(Flight flight, SpriteAPI sprite, Color color, float size, TreeViewport viewport, float alphaMult) {
        float t = flight.elapsed / flight.duration;
        Vector2f position = flight.pointAt(t);
        Vector2f heading = flight.headingAt(t);
        float fade = Math.min(1f, Math.min(flight.elapsed, flight.duration - flight.elapsed) / FADE_SECONDS);
        float screenAngle = (float) Math.toDegrees(Math.atan2(-heading.y, heading.x));

        for (int i = 0; i < GHOSTS_PER_FLIGHT; i++) {
            Vector2f offset = formationOffset(i, flight, heading, size / viewport.zoom());
            float flicker = (float) (0.5 + 0.5 * Math.sin(FLICKER_FREQUENCY * flight.elapsed + flight.phase + i * 2.1));
            sprite.setSize(size, size);
            sprite.setAngle(screenAngle - 90f);
            sprite.setColor(color);
            sprite.setAlphaMult((MIN_ALPHA + (MAX_ALPHA - MIN_ALPHA) * flicker) * fade * alphaMult);
            sprite.renderAtCenter(viewport.screenX(position.x + offset.x), viewport.screenY(position.y + offset.y));
        }
    }

    private static Vector2f formationOffset(int index, Flight flight, Vector2f heading, float spacing) {
        float back = index == 0 ? 0f : -WING_BACK_SPACING * spacing;
        float side = 0f;
        if (index > 0) {
            side = (index == 1 ? 1f : -1f) * WING_SIDE_SPACING * spacing;
        }
        side += (float) Math.sin(WOBBLE_FREQUENCY * flight.elapsed + flight.phase + index) * WOBBLE_RATIO * spacing;
        return new Vector2f(heading.x * back - heading.y * side, heading.y * back + heading.x * side);
    }

    private record WormholePair(SkillNode a, SkillNode b) {
    }

    private static final class Flight {
        private Vector2f from;
        private Vector2f to;
        private float duration;
        private float arc;
        private float phase;
        private float elapsed;

        private Vector2f control() {
            Vector2f delta = Vector2f.sub(to, from, null);
            float length = delta.length();
            Vector2f mid = new Vector2f((from.x + to.x) / 2f, (from.y + to.y) / 2f);
            if (length <= 0f) {
                return mid;
            }
            return new Vector2f(mid.x - delta.y / length * arc, mid.y + delta.x / length * arc);
        }

        private Vector2f pointAt(float t) {
            Vector2f control = control();
            float u = 1f - t;
            return new Vector2f(u * u * from.x + 2f * u * t * control.x + t * t * to.x,
                    u * u * from.y + 2f * u * t * control.y + t * t * to.y);
        }

        private Vector2f headingAt(float t) {
            Vector2f control = control();
            float u = 1f - t;
            Vector2f heading = new Vector2f(2f * u * (control.x - from.x) + 2f * t * (to.x - control.x),
                    2f * u * (control.y - from.y) + 2f * t * (to.y - control.y));
            if (heading.lengthSquared() <= 0f) {
                return new Vector2f(1f, 0f);
            }
            heading.normalise();
            return heading;
        }
    }
}
