package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import exiledsector.i18n.StyledText;

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
    VENT_RATE_PERCENT(PERCENT, stat(MutableShipStatsAPI::getVentRateMult), "stat.ventingSpeed", false),
    VENT_RATE_MULT(MULT, stat(MutableShipStatsAPI::getVentRateMult), "stat.ventingSpeed", false),
    ZERO_FLUX_ALWAYS_ON {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getZeroFluxMinimumFluxLevel().modifyFlat(modId, 2f);
        }

        @Override
        public boolean supportsTemporaryGating() {
            return false;
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
        public StyledText description(float magnitude) {
            return StatMode.PERCENT.describeStat(magnitude, "stat.fluxDissipationWhileVenting");
        }
    };

    private final SimpleStatEffect simpleStat;

    FluxSkillEffect() {
        this.simpleStat = null;
    }

    FluxSkillEffect(StatMode mode, StatTarget target, String statKey, boolean lowerIsBetter) {
        this.simpleStat = new SimpleStatEffect(mode, target, statKey, lowerIsBetter);
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        simpleStat.apply(stats, modId, magnitude);
    }

    @Override
    public StyledText description(float magnitude) {
        return simpleStat != null ? simpleStat.description(magnitude) : EffectText.templated(this, magnitude);
    }

    @Override
    public boolean lowerIsBetter() {
        return simpleStat != null && simpleStat.lowerIsBetter();
    }

    private static final class StatNames {
        static final String FLUX_CAPACITY = "stat.fluxCapacity";
        static final String FLUX_DISSIPATION = "stat.fluxDissipation";

        private StatNames() {
        }
    }
}
