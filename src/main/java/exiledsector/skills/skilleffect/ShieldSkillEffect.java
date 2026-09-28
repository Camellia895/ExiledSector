package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.impl.campaign.ids.Stats;

import java.util.List;

import static exiledsector.skills.skilleffect.SkillEffectText.pct;
import static exiledsector.skills.skilleffect.StatMode.FLAT;
import static exiledsector.skills.skilleffect.StatMode.MULT;
import static exiledsector.skills.skilleffect.StatMode.PERCENT;
import static exiledsector.skills.skilleffect.StatTarget.bonus;
import static exiledsector.skills.skilleffect.StatTarget.dynamicStat;
import static exiledsector.skills.skilleffect.StatTarget.stat;

public enum ShieldSkillEffect implements SkillEffect {

    BEAM_WEAPON_HARD_FLUX_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(BEAM_DAMAGE_HARD_FLUX_KEY).modifyFlat(modId, magnitude);
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            if (!ship.hasListenerOfClass(BeamHardFluxListener.class)) {
                ship.addListener(new BeamHardFluxListener(ship));
            }
        }

        @Override
        public boolean supportsTemporaryGating() {
            return false;
        }

        @Override
        public String describe(float magnitude) {
            return "Causes " + pct(magnitude) + "% of beam weapon damage dealt to shields to be hard flux.";
        }
    },
    REMOVE_SHIELD {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            ship.setShield(ShieldAPI.ShieldType.NONE, 0f, 1f, 1f);
        }

        @Override
        public boolean supportsTemporaryGating() {
            return false;
        }

        @Override
        public String blockAllocationReason(FleetMemberAPI member, float magnitude, List<SkillEffect> currentlyAllocatedEffects) {
            return shieldTypeBlockReason(resolveDisplayShieldType(member.getHullSpec().getShieldType(), currentlyAllocatedEffects));
        }

        @Override
        public String shieldTypeBlockReason(ShieldAPI.ShieldType resolvedShieldType) {
            return resolvedShieldType == ShieldAPI.ShieldType.NONE ? "Ship has no shields." : null;
        }

        @Override
        public String describe(float magnitude) {
            return "Removes this ship's shield entirely.";
        }
    },
    CREATE_FRONT_SHIELD_IF_NONE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            if (ship.getShield() == null) {
                ship.setShield(ShieldAPI.ShieldType.FRONT, MAKESHIFT_SHIELD_EFFICIENCY, MAKESHIFT_SHIELD_TURN_RATE_MULT, MAKESHIFT_SHIELD_ARC);
            }
        }

        @Override
        public boolean supportsTemporaryGating() {
            return false;
        }

        @Override
        public String describe(float magnitude) {
            return "Installs a makeshift, front-facing shield if this ship has none.";
        }
    },
    CONVERT_SHIELD_TO_FRONT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            ShieldAPI shield = ship.getShield();
            if (shield != null) {
                shield.setType(ShieldAPI.ShieldType.FRONT);
            }
        }

        @Override
        public boolean supportsTemporaryGating() {
            return false;
        }

        @Override
        public String blockAllocationReason(FleetMemberAPI member, float magnitude, List<SkillEffect> currentlyAllocatedEffects) {
            return shieldTypeBlockReason(resolveDisplayShieldType(member.getHullSpec().getShieldType(), currentlyAllocatedEffects));
        }

        @Override
        public String shieldTypeBlockReason(ShieldAPI.ShieldType resolvedShieldType) {
            return resolvedShieldType == ShieldAPI.ShieldType.FRONT ? "Ship already has front shields." : null;
        }

        @Override
        public String describe(float magnitude) {
            return "Converts this ship's shield to front-facing.";
        }
    },
    CONVERT_SHIELD_TO_OMNI {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            ShieldAPI shield = ship.getShield();
            if (shield != null) {
                shield.setType(ShieldAPI.ShieldType.OMNI);
            }
        }

        @Override
        public boolean supportsTemporaryGating() {
            return false;
        }

        @Override
        public String blockAllocationReason(FleetMemberAPI member, float magnitude, List<SkillEffect> currentlyAllocatedEffects) {
            return shieldTypeBlockReason(resolveDisplayShieldType(member.getHullSpec().getShieldType(), currentlyAllocatedEffects));
        }

        @Override
        public String shieldTypeBlockReason(ShieldAPI.ShieldType resolvedShieldType) {
            return resolvedShieldType == ShieldAPI.ShieldType.OMNI ? "Ship already has omni-directional shields." : null;
        }

        @Override
        public String describe(float magnitude) {
            return "Converts this ship's shield to omni-directional.";
        }
    },
    SHIELD_ARC_PERCENT(PERCENT, bonus(MutableShipStatsAPI::getShieldArcBonus), StatNames.SHIELD_ARC, false),
    SHIELD_ARC_FLAT(FLAT, bonus(MutableShipStatsAPI::getShieldArcBonus), StatNames.SHIELD_ARC, false),
    SHIELD_ARC_MULT(MULT, bonus(MutableShipStatsAPI::getShieldArcBonus), StatNames.SHIELD_ARC, false),
    SHIELD_PIERCE_CHANCE_PERCENT(PERCENT, dynamicStat(Stats.SHIELD_PIERCED_MULT), "chance for shields to be pierced by EMP arcs", false),
    SHIELD_PIERCE_CHANCE_MULT(MULT, dynamicStat(Stats.SHIELD_PIERCED_MULT), "chance for shields to be pierced by EMP arcs", false),
    SHIELD_UPKEEP_PERCENT(PERCENT, stat(MutableShipStatsAPI::getShieldUpkeepMult), "shield flux upkeep", true),
    SHIELD_UPKEEP_MULT(MULT, stat(MutableShipStatsAPI::getShieldUpkeepMult), "shield flux upkeep", true),
    SHIELD_TURN_RATE_PERCENT(PERCENT, stat(MutableShipStatsAPI::getShieldTurnRateMult), "shield turn rate", false),
    SHIELD_RAISE_RATE_PERCENT(PERCENT, stat(MutableShipStatsAPI::getShieldUnfoldRateMult), "shield raise rate", false),
    SHIELD_DAMAGE_SHARED_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(SHIELD_DAMAGE_SHARED_KEY).modifyFlat(modId, magnitude);
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            if (!ship.hasListenerOfClass(SharedShieldDamageListener.class)) {
                ship.addListener(new SharedShieldDamageListener(ship));
            }
        }

        @Override
        public String describe(float magnitude) {
            return "Disperses " + pct(magnitude) + "% of shield damage taken to nearby allied ships within "
                    + Math.round(SHARED_SHIELD_DAMAGE_RANGE) + " su, split evenly between them as hard flux.";
        }
    };

    private static final String BEAM_DAMAGE_HARD_FLUX_KEY = "exiledSector_beamDamageHardFluxPercent";

    private static final String SHIELD_DAMAGE_SHARED_KEY = "exiledSector_shieldDamageSharedPercent";
    private static final float SHARED_SHIELD_DAMAGE_RANGE = 1000f;

    public static final float MAKESHIFT_SHIELD_EFFICIENCY = 0.5f;
    public static final float MAKESHIFT_SHIELD_TURN_RATE_MULT = 1.2f;
    public static final float MAKESHIFT_SHIELD_ARC = 90f;

    private final SimpleStatEffect simpleStat;

    ShieldSkillEffect() {
        this.simpleStat = null;
    }

    ShieldSkillEffect(StatMode mode, StatTarget target, String statName, boolean lowerIsBetter) {
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

    public static ShieldAPI.ShieldType resolveDisplayShieldType(ShieldAPI.ShieldType baseType, List<SkillEffect> effectsInAllocationOrder) {
        ShieldAPI.ShieldType type = baseType;
        for (SkillEffect effect : effectsInAllocationOrder) {
            if (effect == REMOVE_SHIELD) {
                type = ShieldAPI.ShieldType.NONE;
            } else if (effect == CREATE_FRONT_SHIELD_IF_NONE && type == ShieldAPI.ShieldType.NONE) {
                type = ShieldAPI.ShieldType.FRONT;
            } else if (effect == CONVERT_SHIELD_TO_FRONT && type != ShieldAPI.ShieldType.NONE) {
                type = ShieldAPI.ShieldType.FRONT;
            } else if (effect == CONVERT_SHIELD_TO_OMNI && type != ShieldAPI.ShieldType.NONE) {
                type = ShieldAPI.ShieldType.OMNI;
            }
        }
        return type;
    }

    private static final class BeamHardFluxListener implements DamageDealtModifier {

        private final ShipAPI ship;

        private BeamHardFluxListener(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (!shieldHit) return null;
            if (!(param instanceof BeamAPI)) return null;
            if (!(target instanceof ShipAPI)) return null;

            float percent = ship.getMutableStats().getDynamic().getValue(BEAM_DAMAGE_HARD_FLUX_KEY, 0f);
            float hardPortion = damage.getDamage() * (percent / 100f);
            if (hardPortion <= 0f) return null;

            damage.setDamage(damage.getDamage() - hardPortion);
            float hardFlux = damage.computeFluxDealt(hardPortion);
            ((ShipAPI) target).getFluxTracker().increaseFlux(hardFlux, true);
            return null;
        }
    }

    private static final class SharedShieldDamageListener implements DamageTakenModifier {

        private final ShipAPI ship;

        private SharedShieldDamageListener(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (!shieldHit) return null;

            float percent = ship.getMutableStats().getDynamic().getValue(SHIELD_DAMAGE_SHARED_KEY, 0f);
            if (percent <= 0f) return null;

            List<ShipAPI> allies = CombatQueries.shipsMatching(other -> other != ship && other.getOwner() == ship.getOwner()
                    && other.isAlive() && !other.isHulk()
                    && CombatQueries.withinRadius(other.getLocation(), ship.getLocation(), SHARED_SHIELD_DAMAGE_RANGE));
            if (allies.isEmpty()) return null;

            float rawDamage = damage.getDamage();
            float sharePerAlly = rawDamage * (percent / 100f) / (allies.size() + 1);
            if (sharePerAlly <= 0f) return null;

            damage.setDamage(rawDamage - sharePerAlly * allies.size());
            float hardFluxPerAlly = damage.computeFluxDealt(sharePerAlly);
            for (ShipAPI ally : allies) {
                ally.getFluxTracker().increaseFlux(hardFluxPerAlly, true);
            }
            return null;
        }
    }

    private static final class StatNames {
        static final String SHIELD_ARC = "shield arc";

        private StatNames() {
        }
    }
}
