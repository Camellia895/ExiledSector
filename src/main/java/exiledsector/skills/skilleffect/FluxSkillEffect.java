package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import static exiledsector.skills.skilleffect.SkillEffectText.pctChange;
import static exiledsector.skills.skilleffect.StatMode.FLAT;
import static exiledsector.skills.skilleffect.StatMode.MULT;
import static exiledsector.skills.skilleffect.StatMode.PERCENT;
import static exiledsector.skills.skilleffect.StatTarget.stat;

public enum FluxSkillEffect implements SkillEffect {

    FLUX_CAPACITY_PERCENT(PERCENT, stat(MutableShipStatsAPI::getFluxCapacity), StatNames.FLUX_CAPACITY, false),
    FLUX_CAPACITY_FLAT(FLAT, stat(MutableShipStatsAPI::getFluxCapacity), StatNames.FLUX_CAPACITY, false),
    FLUX_CAPACITY_MULT(MULT, stat(MutableShipStatsAPI::getFluxCapacity), StatNames.FLUX_CAPACITY, false),
    FLUX_DISSIPATION_PERCENT(PERCENT, stat(MutableShipStatsAPI::getFluxDissipation), StatNames.FLUX_DISSIPATION, false),
    FLUX_DISSIPATION_FLAT(FLAT, stat(MutableShipStatsAPI::getFluxDissipation), StatNames.FLUX_DISSIPATION, false),
    FLUX_DISSIPATION_MULT(MULT, stat(MutableShipStatsAPI::getFluxDissipation), StatNames.FLUX_DISSIPATION, false),
    VENT_RATE_PERCENT(PERCENT, stat(MutableShipStatsAPI::getVentRateMult), "venting speed", false),
    VENT_RATE_MULT(MULT, stat(MutableShipStatsAPI::getVentRateMult), "venting speed", false),
    ZERO_FLUX_ALWAYS_ON {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getZeroFluxMinimumFluxLevel().modifyFlat(modId, 2f);
        }

        @Override
        public boolean supportsTemporaryGating() {
            return false;
        }

        @Override
        public String describe(float magnitude) {
            return "Allows the zero-flux speed boost to take effect regardless of flux level.";
        }
    },
    FLUX_DISSIPATION_WHILE_VENTING_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public boolean isConditional() {
            return true;
        }

        @Override
        public boolean supportsTemporaryGating() {
            return false;
        }

        @Override
        public void advanceInCombat(ShipAPI ship, String modId, float magnitude) {
            if (ship.getFluxTracker().isVenting()) {
                ship.getMutableStats().getFluxDissipation().modifyPercent(modId, magnitude);
            } else {
                ship.getMutableStats().getFluxDissipation().unmodify(modId);
            }
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "flux dissipation rate while venting");
        }
    };

    private final SimpleStatEffect simpleStat;

    FluxSkillEffect() {
        this.simpleStat = null;
    }

    FluxSkillEffect(StatMode mode, StatTarget target, String statName, boolean lowerIsBetter) {
        this.simpleStat = new SimpleStatEffect(mode, target, statName, lowerIsBetter);
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        simpleStat.apply(stats, modId, magnitude);
    }

    @Override
    public String describe(float magnitude) {
        return simpleStat.describe(magnitude);
    }

    @Override
    public boolean lowerIsBetter() {
        return simpleStat != null && simpleStat.lowerIsBetter();
    }

    private static final class StatNames {
        static final String FLUX_CAPACITY = "flux capacity";
        static final String FLUX_DISSIPATION = "flux dissipation";

        private StatNames() {
        }
    }
}
