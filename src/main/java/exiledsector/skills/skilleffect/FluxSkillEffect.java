package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import static exiledsector.skills.skilleffect.SkillEffectText.pctChange;
import static exiledsector.skills.skilleffect.SkillEffectText.pctMore;
import static exiledsector.skills.skilleffect.SkillEffectText.flatChange;

public enum FluxSkillEffect implements SkillEffect {

    FLUX_CAPACITY_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getFluxCapacity().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "flux capacity");
        }
    },
    FLUX_CAPACITY_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getFluxCapacity().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "flux capacity");
        }
    },
    FLUX_DISSIPATION_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getFluxDissipation().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, STAT_FLUX_DISSIPATION);
        }
    },
    FLUX_DISSIPATION_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getFluxDissipation().modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, STAT_FLUX_DISSIPATION);
        }
    },
    FLUX_DISSIPATION_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getFluxDissipation(), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, STAT_FLUX_DISSIPATION);
        }
    },
    VENT_RATE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getVentRateMult().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "venting speed");
        }
    },
    VENT_RATE_MULT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            SkillEffectSupport.applyMult(stats.getVentRateMult(), modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctMore(magnitude, "venting speed");
        }
    },
    ZERO_FLUX_ALWAYS_ON {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getZeroFluxMinimumFluxLevel().modifyFlat(modId, 2f);
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

    private static final String STAT_FLUX_DISSIPATION = "flux dissipation";
}
