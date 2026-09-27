package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.BeamWeaponSpecAPI;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class SplitBeamEffects {

    private static final float SPLIT_TIMEOUT_SECONDS = 0.3f;

    private static final Set<Class<?>> DISABLED_PLUGIN_CLASSES = ConcurrentHashMap.newKeySet();

    private final Map<SplitKey, ActiveSplit> activeSplits = new HashMap<>();

    void refresh(BeamAPI sourceBeam, ShipAPI splitTarget, Vector2f from, Vector2f to) {
        SplitKey key = new SplitKey(sourceBeam, splitTarget);
        ActiveSplit split = activeSplits.get(key);
        if (split == null) {
            BeamEffectPlugin plugin = newPluginFor(sourceBeam.getWeapon());
            if (plugin == null) {
                return;
            }
            split = new ActiveSplit(plugin, sourceBeam, splitTarget);
            activeSplits.put(key, split);
        }
        split.from.set(from);
        split.to.set(to);
        split.secondsSinceRefresh = 0f;
    }

    void advance(float amount) {
        if (activeSplits.isEmpty()) {
            return;
        }
        CombatEngineAPI engine = Global.getCombatEngine();
        Iterator<ActiveSplit> iterator = activeSplits.values().iterator();
        while (iterator.hasNext()) {
            ActiveSplit split = iterator.next();
            split.secondsSinceRefresh += amount;
            boolean keep = !split.isExpired() && split.advancePlugin(amount, engine);
            if (!keep) {
                iterator.remove();
            }
        }
    }

    static BeamEffectPlugin newPluginFor(WeaponAPI weapon) {
        if (weapon == null || !(weapon.getSpec() instanceof BeamWeaponSpecAPI spec)) {
            return null;
        }
        BeamEffectPlugin prototype = spec.getBeamEffect();
        if (prototype == null || DISABLED_PLUGIN_CLASSES.contains(prototype.getClass())) {
            return null;
        }
        try {
            return prototype.getClass().getConstructor().newInstance();
        } catch (ReflectiveOperationException | RuntimeException e) {
            disable(prototype.getClass(), "can't be instantiated without constructor arguments", e);
            return null;
        }
    }

    static BeamAPI splitBeam(BeamAPI sourceBeam, ShipAPI splitTarget, Vector2f from, Vector2f to) {
        return (BeamAPI) Proxy.newProxyInstance(BeamAPI.class.getClassLoader(), new Class<?>[]{BeamAPI.class},
                new SplitBeamHandler(sourceBeam, splitTarget, from, to));
    }

    private static void disable(Class<?> pluginClass, String reason, Throwable cause) {
        if (DISABLED_PLUGIN_CLASSES.add(pluginClass)) {
            Logger.getLogger(SplitBeamEffects.class).warn("Beam effect " + pluginClass.getName() + " " + reason
                    + "; split beams won't apply it", cause);
        }
    }

    private record SplitKey(BeamAPI sourceBeam, ShipAPI splitTarget) {
    }

    private static final class ActiveSplit {

        private final BeamEffectPlugin plugin;
        private final ShipAPI splitTarget;
        private final Vector2f from = new Vector2f();
        private final Vector2f to = new Vector2f();
        private final BeamAPI splitBeam;
        private float secondsSinceRefresh;

        private ActiveSplit(BeamEffectPlugin plugin, BeamAPI sourceBeam, ShipAPI splitTarget) {
            this.plugin = plugin;
            this.splitTarget = splitTarget;
            this.splitBeam = splitBeam(sourceBeam, splitTarget, from, to);
        }

        private boolean isExpired() {
            return secondsSinceRefresh > SPLIT_TIMEOUT_SECONDS || !splitTarget.isAlive();
        }

        private boolean advancePlugin(float amount, CombatEngineAPI engine) {
            try {
                plugin.advance(amount, engine, splitBeam);
                return true;
            } catch (RuntimeException e) {
                disable(plugin.getClass(), "failed on a split beam", e);
                return false;
            }
        }
    }

    private static final class SplitBeamHandler implements InvocationHandler {

        private final BeamAPI sourceBeam;
        private final ShipAPI splitTarget;
        private final Vector2f from;
        private final Vector2f to;

        private SplitBeamHandler(BeamAPI sourceBeam, ShipAPI splitTarget, Vector2f from, Vector2f to) {
            this.sourceBeam = sourceBeam;
            this.splitTarget = splitTarget;
            this.from = from;
            this.to = to;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            return switch (name) {
                case "getDamageTarget" -> splitTarget;
                case "getFrom" -> new Vector2f(from);
                case "getTo", "getRayEndPrevFrame" -> new Vector2f(to);
                case "getLength", "getLengthPrevFrame" -> MathUtils.getDistance(from, to);
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> "SplitBeam[" + sourceBeam + " -> " + splitTarget + "]";
                default -> name.startsWith("set") ? null : delegate(method, args);
            };
        }

        private Object delegate(Method method, Object[] args) {
            try {
                return method.invoke(sourceBeam, args);
            } catch (InvocationTargetException e) {
                throw unchecked(e.getCause());
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("BeamAPI." + method.getName() + " is not accessible", e);
            }
        }

        private static RuntimeException unchecked(Throwable cause) {
            if (cause instanceof Error error) {
                throw error;
            }
            return cause instanceof RuntimeException runtime ? runtime : new UndeclaredThrowableException(cause);
        }
    }
}
