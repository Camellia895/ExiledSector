package exiledsector.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public final class VanillaHullBaselines {

    private static final Map<HullSize, Baseline> BASELINES = new EnumMap<>(HullSize.class);

    private VanillaHullBaselines() {
    }

    public static void compute() {
        Map<HullSize, List<Float>> opBySize = new EnumMap<>(HullSize.class);
        for (ShipHullSpecAPI hull : Global.getSettings().getAllShipHullSpecs()) {
            if (!isVanillaBaselineEligible(hull)) continue;
            opBySize.computeIfAbsent(hull.getHullSize(), size -> new ArrayList<>())
                    .add((float) hull.getOrdnancePoints(null));
        }

        BASELINES.clear();
        for (Map.Entry<HullSize, List<Float>> entry : opBySize.entrySet()) {
            BASELINES.put(entry.getKey(), Baseline.of(entry.getValue()));
        }

        Logger.getLogger(VanillaHullBaselines.class).info("Computed vanilla hull OP baselines: " + BASELINES);
    }

    private static boolean isVanillaBaselineEligible(ShipHullSpecAPI hull) {
        if (hull.getSourceMod() != null) return false;
        if (hull.isDHull()) return false;
        if (!hull.hasHullName()) return false;
        if (hull.getOrdnancePoints(null) <= 0) return false;

        HullSize size = hull.getHullSize();
        if (size != HullSize.FRIGATE && size != HullSize.DESTROYER
                && size != HullSize.CRUISER && size != HullSize.CAPITAL_SHIP) {
            return false;
        }

        EnumSet<ShipTypeHints> hints = hull.getHints();
        return !hints.contains(ShipTypeHints.MODULE) && !hints.contains(ShipTypeHints.UNDER_PARENT)
                && !hints.contains(ShipTypeHints.STATION) && !hints.contains(ShipTypeHints.HIDE_IN_CODEX);
    }

    public static int passivePointsFor(ShipHullSpecAPI hull) {
        HullSize size = hull.getHullSize();
        Baseline baseline = BASELINES.get(size);
        float mean = baseline != null ? baseline.mean : 0f;
        float stdDev = baseline != null ? baseline.stdDev : 0f;
        return PassivePointsFormula.compute(size, hull.getOrdnancePoints(null), mean, stdDev);
    }

    private static final class Baseline {
        final float mean;
        final float stdDev;

        private Baseline(float mean, float stdDev) {
            this.mean = mean;
            this.stdDev = stdDev;
        }

        static Baseline of(List<Float> values) {
            float sum = 0f;
            for (float value : values) sum += value;
            float mean = sum / values.size();

            float variance = 0f;
            for (float value : values) variance += (value - mean) * (value - mean);
            variance /= values.size();

            return new Baseline(mean, (float) Math.sqrt(variance));
        }

        @Override
        public String toString() {
            return "mean=" + mean + " stdDev=" + stdDev;
        }
    }
}
