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
import exiledsector.i18n.StyledText;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.impl.campaign.ids.Stats;

import java.util.List;
import java.util.function.Function;

import static exiledsector.skills.skilleffect.StatMode.FLAT;
import static exiledsector.skills.skilleffect.StatMode.MULT;
import static exiledsector.skills.skilleffect.StatMode.PERCENT;
import static exiledsector.skills.skilleffect.StatTarget.bonus;
import static exiledsector.skills.skilleffect.StatTarget.dynamicStat;
import static exiledsector.skills.skilleffect.StatTarget.stat;

public enum ShieldSkillEffect implements SkillEffect {

    BEAM_WEAPON_HARD_FLUX_PERCENT(BeamHardFluxListener.HARD_FLUX_PERCENT_KEY, BeamHardFluxListener.class, BeamHardFluxListener::new) {
        @Override
        public boolean supportsTemporaryGating() {
            return false;
        }

        @Override
        public StyledText description(float magnitude) {
            return EffectText.msg(this).arg("value", magnitude).styled();
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
        public StyledText description(float magnitude) {
            return EffectText.of(this);
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
        public StyledText description(float magnitude) {
            return EffectText.of(this);
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
        public StyledText description(float magnitude) {
            return EffectText.of(this);
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
        public StyledText description(float magnitude) {
            return EffectText.of(this);
        }
    },
    SHIELD_ARC_PERCENT(PERCENT, bonus(MutableShipStatsAPI::getShieldArcBonus), StatNames.SHIELD_ARC, false),
    SHIELD_ARC_FLAT(FLAT, bonus(MutableShipStatsAPI::getShieldArcBonus), StatNames.SHIELD_ARC, false),
    SHIELD_ARC_MULT(MULT, bonus(MutableShipStatsAPI::getShieldArcBonus), StatNames.SHIELD_ARC, false),
    SHIELD_PIERCE_CHANCE_PERCENT(PERCENT, dynamicStat(Stats.SHIELD_PIERCED_MULT), "stat.chanceForShieldsToBePiercedByEmpArcs", false),
    SHIELD_PIERCE_CHANCE_MULT(MULT, dynamicStat(Stats.SHIELD_PIERCED_MULT), "stat.chanceForShieldsToBePiercedByEmpArcs", false),
    SHIELD_UPKEEP_PERCENT(PERCENT, stat(MutableShipStatsAPI::getShieldUpkeepMult), "stat.shieldFluxUpkeep", true),
    SHIELD_UPKEEP_MULT(MULT, stat(MutableShipStatsAPI::getShieldUpkeepMult), "stat.shieldFluxUpkeep", true),
    SHIELD_TURN_RATE_PERCENT(PERCENT, stat(MutableShipStatsAPI::getShieldTurnRateMult), "stat.shieldTurnRate", false),
    SHIELD_RAISE_RATE_PERCENT(PERCENT, stat(MutableShipStatsAPI::getShieldUnfoldRateMult), "stat.shieldRaiseRate", false),
    SHIELD_DAMAGE_SHARED_PERCENT(SharedShieldDamageListener.SHARED_PERCENT_KEY,
            SharedShieldDamageListener.class, SharedShieldDamageListener::new) {
        @Override
        public StyledText description(float magnitude) {
            return EffectText.msg(this).arg("value", magnitude).arg("range", Math.round(SHARED_SHIELD_DAMAGE_RANGE)).styled();
        }
    };

    private static final float SHARED_SHIELD_DAMAGE_RANGE = 1000f;

    public static final float MAKESHIFT_SHIELD_EFFICIENCY = 0.5f;
    public static final float MAKESHIFT_SHIELD_TURN_RATE_MULT = 1.2f;
    public static final float MAKESHIFT_SHIELD_ARC = 90f;

    private final SimpleStatEffect simpleStat;
    private final ListenerEffect listener;

    ShieldSkillEffect() {
        this(null, null);
    }

    ShieldSkillEffect(StatMode mode, StatTarget target, String statKey, boolean lowerIsBetter) {
        this(new SimpleStatEffect(mode, target, statKey, lowerIsBetter), null);
    }

    <T> ShieldSkillEffect(String magnitudeKey, Class<T> listenerType, Function<ShipAPI, ? extends T> listenerFactory) {
        this(null, new ListenerEffect(magnitudeKey, listenerType, listenerFactory));
    }

    ShieldSkillEffect(SimpleStatEffect simpleStat, ListenerEffect listener) {
        this.simpleStat = simpleStat;
        this.listener = listener;
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        if (listener != null) {
            listener.storeMagnitude(stats, modId, magnitude);
        } else {
            simpleStat.apply(stats, modId, magnitude);
        }
    }

    @Override
    public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
        if (listener != null) {
            listener.attach(ship);
        }
    }

    @Override
    public StyledText description(float magnitude) {
        return simpleStat.description(magnitude);
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

        private static final String HARD_FLUX_PERCENT_KEY = "exiledSector_beamDamageHardFluxPercent";

        private final ShipAPI ship;

        private BeamHardFluxListener(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (!shieldHit) return null;
            if (!(param instanceof BeamAPI)) return null;
            if (!(target instanceof ShipAPI)) return null;

            float percent = ship.getMutableStats().getDynamic().getValue(HARD_FLUX_PERCENT_KEY, 0f);
            float hardPortion = damage.getDamage() * (percent / 100f);
            if (hardPortion <= 0f) return null;

            damage.setDamage(damage.getDamage() - hardPortion);
            float hardFlux = damage.computeFluxDealt(hardPortion);
            ((ShipAPI) target).getFluxTracker().increaseFlux(hardFlux, true);
            return null;
        }
    }

    private static final class SharedShieldDamageListener implements DamageTakenModifier {

        private static final String SHARED_PERCENT_KEY = "exiledSector_shieldDamageSharedPercent";

        private final ShipAPI ship;

        private SharedShieldDamageListener(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (!shieldHit) return null;

            float percent = ship.getMutableStats().getDynamic().getValue(SHARED_PERCENT_KEY, 0f);
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
        static final String SHIELD_ARC = "stat.shieldArc";

        private StatNames() {
        }
    }
}
