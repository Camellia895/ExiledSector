package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BoundsAPI;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import exiledsector.skills.CsvIdBlocklist;
import exiledsector.skills.MaxChainCountConfig;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static exiledsector.skills.skilleffect.SkillEffectText.pct;

public enum CombatSkillEffect implements SkillEffect {

    BEAM_SPLIT_TARGETS_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(BeamSplitListener.TARGETS_KEY).modifyFlat(modId, magnitude);
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            if (!ship.hasListenerOfClass(BeamSplitListener.class)) {
                ship.addListener(new BeamSplitListener(ship));
            }
        }

        @Override
        public String describe(float magnitude) {
            int count = Math.round(magnitude);
            return "Beam weapon hits split their damage evenly across the target and up to "
                    + pct(magnitude) + " additional nearby enem" + (count == 1 ? "y" : "ies") + ". " +
                    "The target acquisition range is half the beam weapon's range. " +
                    "Split beams also carry the weapon's special beam effects.";
        }
    },
    EXPLODE_ON_DEATH {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(EXPLODE_ON_DEATH_FUEL_DAMAGE_KEY).modifyFlat(modId, magnitude);
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            if (!ship.hasListenerOfClass(DeathExplosionListener.class)) {
                ship.addListener(new DeathExplosionListener(ship));
            }
        }

        @Override
        public String describe(float magnitude) {
            return "When this ship is destroyed, it detonates violently, dealing high-explosive damage equal to up to "
                    + pct(magnitude) + "% of its maximum fuel capacity to nearby ships, tapering off with distance ";
        }
    },
    DEATH_ON_COLLISION {
        // this effect has no stat to modify - it only acts via the listener added in applyAfterShipCreation
        @Override
        @SuppressWarnings("java:S1186")
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            if (!ship.hasListenerOfClass(CollisionDeathListener.class)) {
                ship.addListener(new CollisionDeathListener(ship));
            }
        }

        @Override
        public String describe(float magnitude) {
            return "Any collision, however slight, is instantly fatal to this ship.";
        }
    },
    NON_BEAM_ENERGY_CHAIN_CHANCE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(NON_BEAM_ENERGY_CHAIN_CHANCE_KEY).modifyFlat(modId, magnitude);
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            if (!ship.hasListenerOfClass(EnergyChainListener.class)) {
                ship.addListener(new EnergyChainListener(ship));
            }
        }

        @Override
        public String describe(float magnitude) {
            return "Non-beam energy weapon hits that land on an enemy shield have a " + pct(magnitude)
                    + "% chance to chain to a nearby enemy ship. The chaining projectile has an equal " +
                    "chance to chain again, as long as it hits a shield.";
        }
    },
    NON_BEAM_ENERGY_CHAIN_FALLOFF_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(NON_BEAM_ENERGY_CHAIN_FALLOFF_KEY).modifyFlat(modId, magnitude);
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            if (!ship.hasListenerOfClass(EnergyChainListener.class)) {
                ship.addListener(new EnergyChainListener(ship));
            }
        }

        @Override
        public String describe(float magnitude) {
            return "Each successive hit in a non-beam energy chain deals " + pct(magnitude) + "% less damage "
                    + "than the one before it.";
        }
    },
    ESCORT_MANEUVER_BONUS_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(ESCORT_MANEUVER_BONUS_KEY).modifyFlat(modId, magnitude);
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            if (!ship.hasListenerOfClass(EscortListener.class)) {
                ship.addListener(new EscortListener(ship));
            }
        }

        @Override
        public String describe(float magnitude) {
            return "While within range of a larger friendly ship, increases maneuverability (acceleration, "
                    + "deceleration, and turn rate) by up to " + pct(magnitude) + "%, fading with distance. "
                    + "Doubled for a destroyer escorting a capital ship.";
        }
    },
    ESCORT_SPEED_BONUS_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(ESCORT_SPEED_BONUS_KEY).modifyFlat(modId, magnitude);
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            if (!ship.hasListenerOfClass(EscortListener.class)) {
                ship.addListener(new EscortListener(ship));
            }
        }

        @Override
        public String describe(float magnitude) {
            return "While within range of a larger friendly ship, increases top speed by up to "
                    + pct(magnitude) + "%, fading with distance. Doubled for a destroyer escorting a "
                    + "capital ship.";
        }
    },
    ESCORT_WEAPON_RANGE_BONUS_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(ESCORT_WEAPON_RANGE_BONUS_KEY).modifyFlat(modId, magnitude);
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            if (!ship.hasListenerOfClass(EscortListener.class)) {
                ship.addListener(new EscortListener(ship));
            }
        }

        @Override
        public String describe(float magnitude) {
            return "While within range of a larger friendly ship, increases ballistic and energy weapon range "
                    + "by up to " + pct(magnitude) + "%, fading with distance. Doubled for a destroyer "
                    + "escorting a capital ship.";
        }
    },
    ESCORT_PROXIMITY_RANGE_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(ESCORT_PROXIMITY_RANGE_KEY).modifyFlat(modId, magnitude);
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            if (!ship.hasListenerOfClass(EscortListener.class)) {
                ship.addListener(new EscortListener(ship));
            }
        }

        @Override
        public String describe(float magnitude) {
            return "The escort bonuses above apply at full strength within " + pct(magnitude)
                    + " su of the larger friendly ship, fading out over an additional 500 su beyond that.";
        }
    },
    NANOFORGE_HULL_REGEN_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(NanoforgeMendingListener.REGEN_PERCENT_KEY).modifyFlat(modId, magnitude);
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            if (!ship.hasListenerOfClass(NanoforgeMendingListener.class)) {
                ship.addListener(new NanoforgeMendingListener(ship));
            }
        }

        @Override
        public String describe(float magnitude) {
            return "After " + pct(NanoforgeMendingListener.UNDAMAGED_SECONDS) + " seconds without taking hull damage, "
                    + "repairs " + pct(magnitude) + "% of maximum hull per second in combat.";
        }
    },
    DISINTEGRATION_ARMOR_DAMAGE_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(DisintegrationListener.ARMOR_DAMAGE_PERCENT_KEY).modifyFlat(modId, magnitude);
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            if (!ship.hasListenerOfClass(DisintegrationListener.class)) {
                ship.addListener(new DisintegrationListener(ship));
            }
        }

        @Override
        public String describe(float magnitude) {
            return "Energy weapon hits on armor strip an additional " + pct(magnitude)
                    + "% of the hit's damage from the armor around the impact. This extra damage never reaches the hull.";
        }
    },
    TERRIFYING_PRESENCE_ACCURACY_PENALTY_PERCENT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(TerrifyingPresenceListener.ACCURACY_PENALTY_PERCENT_KEY).modifyFlat(modId, magnitude);
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            if (!ship.hasListenerOfClass(TerrifyingPresenceListener.class)) {
                ship.addListener(new TerrifyingPresenceListener(ship));
            }
        }

        @Override
        public String describe(float magnitude) {
            return "Enemy ships within " + pct(TerrifyingPresenceListener.RANGE) + " su have the target leading "
                    + "accuracy of their autofiring weapons reduced by " + pct(magnitude) + "%.";
        }
    };

    @Override
    public boolean supportsTemporaryGating() {
        return false;
    }

    private static final String EXPLODE_ON_DEATH_FUEL_DAMAGE_KEY = "exiledSector_explodeOnDeathFuelDamagePercent";

    private static final String NON_BEAM_ENERGY_CHAIN_CHANCE_KEY = "exiledSector_energyChainChance";
    private static final String NON_BEAM_ENERGY_CHAIN_FALLOFF_KEY = "exiledSector_energyChainFalloff";
    private static final String NON_BEAM_ENERGY_CHAIN_HIT_LIST_KEY = "exiledSector_energyChainHitList";
    private static final String NON_BEAM_ENERGY_CHAIN_COUNT_KEY = "exiledSector_energyChainCount";
    private static final String NON_BEAM_ENERGY_CHAIN_DEALT_MULT_KEY = "exiledSector_energyChainDealtMult";

    private static final String ESCORT_MANEUVER_BONUS_KEY = "exiledSector_escortManeuverBonusPercent";
    private static final String ESCORT_SPEED_BONUS_KEY = "exiledSector_escortSpeedBonusPercent";
    private static final String ESCORT_WEAPON_RANGE_BONUS_KEY = "exiledSector_escortWeaponRangeBonusPercent";
    private static final String ESCORT_PROXIMITY_RANGE_KEY = "exiledSector_escortProximityRange";

    private static final class EnergyChainListener implements DamageDealtModifier {

        private final ShipAPI ship;

        private EnergyChainListener(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (!(param instanceof DamagingProjectileAPI proj) || !(target instanceof ShipAPI targetShip)
                    || !canChainFrom(proj.getWeapon())) {
                return null;
            }
            WeaponAPI weapon = proj.getWeapon();

            float fullDamage = damage.getDamage();
            float dealtMult = dealtMultOf(proj);
            if (dealtMult < 1f) {
                ChainHitDamageRestorer.reduceForThisHit(damage, dealtMult, targetShip);
            }
            if (shieldHit) {
                tryChain(proj, weapon, targetShip, point, fullDamage, dealtMult);
            }
            return null;
        }

        private static boolean canChainFrom(WeaponAPI weapon) {
            return weapon != null && !weapon.isBeam() && weapon.getType() == WeaponAPI.WeaponType.ENERGY
                    && !CsvIdBlocklist.ENERGY_CHAIN_WEAPONS.contains(weapon.getId());
        }

        private static float dealtMultOf(DamagingProjectileAPI proj) {
            return proj.getCustomData().get(NON_BEAM_ENERGY_CHAIN_DEALT_MULT_KEY) instanceof Float mult ? mult : 1f;
        }

        private void tryChain(DamagingProjectileAPI proj, WeaponAPI weapon, ShipAPI target, Vector2f point,
                              float fullDamage, float dealtMult) {
            Map<String, Object> customData = proj.getCustomData();
            int chainCount = customData.get(NON_BEAM_ENERGY_CHAIN_COUNT_KEY) instanceof Integer integer ? integer : 0;
            float chancePercent = ship.getMutableStats().getDynamic().getValue(NON_BEAM_ENERGY_CHAIN_CHANCE_KEY, 0f);
            boolean chains = chainCount < MaxChainCountConfig.get() && chancePercent > 0f
                    && Math.random() < chancePercent / 100.0;
            if (!chains) {
                return;
            }

            List<ShipAPI> hitSoFar = hitList(customData);
            hitSoFar.add(target);
            ShipAPI nextTarget = findNearestChainTarget(point, weapon.getRange(), hitSoFar);
            float falloffPercent = ship.getMutableStats().getDynamic().getValue(NON_BEAM_ENERGY_CHAIN_FALLOFF_KEY, 0f);
            float nextDealtMult = dealtMult * (1f - falloffPercent / 100f);
            if (nextTarget == null || nextDealtMult <= 0f) {
                return;
            }

            ChainShot shot = new ChainShot(fullDamage, nextDealtMult, hitSoFar, chainCount + 1);
            spawnChainProjectile(weapon, proj, point, nextTarget, shot);
        }

        // unchecked: the hit list is only ever written by this class as List<ShipAPI>
        @SuppressWarnings("unchecked")
        private List<ShipAPI> hitList(Map<String, Object> customData) {
            List<ShipAPI> hitSoFar = new ArrayList<>();
            if (customData.get(NON_BEAM_ENERGY_CHAIN_HIT_LIST_KEY) instanceof List<?> storedHits) {
                hitSoFar.addAll((List<ShipAPI>) storedHits);
            } else {
                hitSoFar.add(ship);
            }
            return hitSoFar;
        }

        private ShipAPI findNearestChainTarget(Vector2f point, float range, List<ShipAPI> excluded) {
            ShipAPI nearest = null;
            float nearestDistanceSq = Float.MAX_VALUE;
            for (ShipAPI candidate : CombatQueries.shipsMatching(other -> !excluded.contains(other) && other.isAlive() && !other.isHulk()
                    && CombatQueries.isHostile(ship, other) && CombatQueries.withinRadius(other.getLocation(), point, range))) {
                float distanceSq = Vector2f.sub(candidate.getLocation(), point, null).lengthSquared();
                if (distanceSq < nearestDistanceSq) {
                    nearestDistanceSq = distanceSq;
                    nearest = candidate;
                }
            }
            return nearest;
        }

        private void spawnChainProjectile(WeaponAPI weapon, DamagingProjectileAPI source, Vector2f from,
                                          ShipAPI target, ChainShot shot) {
            CombatEngineAPI engine = Global.getCombatEngine();
            float facing = VectorUtils.getAngle(from, target.getLocation());
            CombatEntityAPI spawned = engine.spawnProjectile(ship, weapon, weapon.getId(), source.getProjectileSpecId(),
                    from, facing, new Vector2f());
            if (spawned instanceof DamagingProjectileAPI chainProj) {
                DamageAPI chainDamage = chainProj.getDamage();
                float existingScaling = chainDamage.getModifier().getModifiedValue() * chainDamage.getMultiplier();
                if (existingScaling > 0f) {
                    chainDamage.setDamage(shot.fullDamage() / existingScaling);
                }
                chainProj.setCustomData(NON_BEAM_ENERGY_CHAIN_HIT_LIST_KEY, shot.hitSoFar());
                chainProj.setCustomData(NON_BEAM_ENERGY_CHAIN_COUNT_KEY, shot.chainCount());
                chainProj.setCustomData(NON_BEAM_ENERGY_CHAIN_DEALT_MULT_KEY, shot.dealtMult());
            }
        }

        private record ChainShot(float fullDamage, float dealtMult, List<ShipAPI> hitSoFar, int chainCount) {
        }
    }

    private static final class DeathExplosionListener implements HullDamageAboutToBeTakenListener {

        private static final float RADIUS_MULT = 4f;
        private static final float MIN_RADIUS = 300f;
        private static final Color BLAST_COLOR = new Color(255, 200, 120, 40);
        private static final Color CORE_COLOR = new Color(255, 255, 255, 60);

        private final ShipAPI ship;
        private boolean exploded;

        private DeathExplosionListener(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public boolean notifyAboutToTakeHullDamage(Object param, ShipAPI ship, Vector2f point, float damageAmount) {
            if (exploded || damageAmount < ship.getHitpoints()) {
                return false;
            }
            exploded = true;
            detonate();
            return false;
        }

        private void detonate() {
            CombatEngineAPI engine = Global.getCombatEngine();
            Vector2f loc = ship.getLocation();
            float radius = Math.max(MIN_RADIUS, ship.getCollisionRadius() * RADIUS_MULT);
            float damage = fuelDamage();

            List<ShipAPI> nearby = CombatQueries.shipsMatching(other -> other != ship && !other.isHulk() && !other.isShuttlePod()
                    && CombatQueries.withinRadius(other.getLocation(), loc, radius));
            for (ShipAPI other : nearby) {
                float distance = Vector2f.sub(other.getLocation(), loc, null).length();
                float dealt = damage * (radius - distance) / radius;
                if (dealt <= 0f) {
                    continue;
                }
                engine.applyDamage(other, other.getLocation(), dealt, DamageType.HIGH_EXPLOSIVE, 0f, true, false, ship);
            }

            engine.spawnExplosion(loc, new Vector2f(), BLAST_COLOR, radius, 1.2f);
            engine.spawnExplosion(loc, new Vector2f(), CORE_COLOR, radius * 0.5f, 0.6f);
        }

        private float fuelDamage() {
            FleetMemberAPI member = ship.getMutableStats().getFleetMember();
            if (member == null) {
                return 0f;
            }
            float fuelDamagePercent = ship.getMutableStats().getDynamic().getValue(EXPLODE_ON_DEATH_FUEL_DAMAGE_KEY, 0f);
            return member.getFuelCapacity() * fuelDamagePercent / 100f;
        }
    }

    private static final class CollisionDeathListener implements AdvanceableListener {

        private static final float COLLISION_CHECK_GRACE_PERIOD = 1.5f;
        private static final float BROAD_PHASE_MARGIN = 1.1f;
        private static final float LETHAL_DAMAGE = 999999f;

        private final ShipAPI ship;
        private boolean triggered;
        private float aliveTime;

        private CollisionDeathListener(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public void advance(float amount) {
            if (triggered || !ship.isAlive() || ship.isHulk()) {
                return;
            }
            aliveTime += amount;
            if (aliveTime < COLLISION_CHECK_GRACE_PERIOD || ship.getCollisionClass() == CollisionClass.NONE) {
                return;
            }
            if (isHullTouchingAnything()) {
                triggered = true;
                Global.getCombatEngine().applyDamage(ship, ship.getLocation(), LETHAL_DAMAGE,
                        DamageType.HIGH_EXPLOSIVE, 0f, true, false, ship);
            }
        }

        private boolean isHullTouchingAnything() {
            Vector2f loc = ship.getLocation();
            float myRadius = ship.getCollisionRadius();

            List<ShipAPI> nearby = CombatQueries.shipsMatching(other -> other != ship && !other.isFighter() && !other.isHulk()
                    && !other.isShuttlePod() && other.getCollisionClass() != CollisionClass.NONE
                    && isBroadPhaseNear(loc, myRadius, other));
            for (ShipAPI other : nearby) {
                if (hullsOverlap(ship, other)) {
                    return true;
                }
            }
            for (CombatEntityAPI asteroid : Global.getCombatEngine().getAsteroids()) {
                if (isBroadPhaseNear(loc, myRadius, asteroid) && hullsOverlap(ship, asteroid)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isBroadPhaseNear(Vector2f loc, float myRadius, CombatEntityAPI other) {
            float triggerRadius = (myRadius + other.getCollisionRadius()) * BROAD_PHASE_MARGIN;
            return CombatQueries.withinRadius(other.getLocation(), loc, triggerRadius);
        }

        private boolean hullsOverlap(CombatEntityAPI a, CombatEntityAPI b) {
            BoundsAPI boundsA = a.getExactBounds();
            BoundsAPI boundsB = b.getExactBounds();
            if (boundsA == null || boundsB == null) {
                return false;
            }
            boundsA.update(a.getLocation(), a.getFacing());
            boundsB.update(b.getLocation(), b.getFacing());

            List<BoundsAPI.SegmentAPI> segmentsA = boundsA.getSegments();
            List<BoundsAPI.SegmentAPI> segmentsB = boundsB.getSegments();
            for (BoundsAPI.SegmentAPI segA : segmentsA) {
                for (BoundsAPI.SegmentAPI segB : segmentsB) {
                    if (segmentsIntersect(segA.getP1(), segA.getP2(), segB.getP1(), segB.getP2())) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean segmentsIntersect(Vector2f p1, Vector2f p2, Vector2f p3, Vector2f p4) {
            float d1 = cross(p3, p4, p1);
            float d2 = cross(p3, p4, p2);
            float d3 = cross(p1, p2, p3);
            float d4 = cross(p1, p2, p4);
            return ((d1 > 0f) != (d2 > 0f)) && ((d3 > 0f) != (d4 > 0f));
        }

        private float cross(Vector2f a, Vector2f b, Vector2f c) {
            return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
        }
    }

    private static final class EscortListener implements AdvanceableListener {

        private static final float PROXIMITY_FADE_DISTANCE = 500f;
        private static final float SHIELD_RADIUS_OVERLAP_MULT = 0.75f;
        private static final float DESTROYER_ESCORTING_CAPITAL_MULT = 2f;
        private static final float TURN_ACCELERATION_MULT = 2f;
        private static final String ESCORT_BONUS_MOD_ID = "exiledSector_escortBonus";

        private final ShipAPI ship;
        private final IntervalUtil interval = new IntervalUtil(0.9f, 1.1f);

        private EscortListener(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public void advance(float amount) {
            if (!ship.isAlive() || ship.isHulk()) {
                return;
            }
            interval.advance(amount);
            if (!interval.intervalElapsed()) {
                return;
            }
            applyBonuses(proximityMagnitude());
        }

        private float proximityMagnitude() {
            ShipAPI escorted = findNearestLargerFriendly();
            if (escorted == null) {
                return 0f;
            }

            float range = ship.getMutableStats().getDynamic().getValue(ESCORT_PROXIMITY_RANGE_KEY, 0f);
            float radiusOverlap = (ship.getShieldRadiusEvenIfNoShield() + escorted.getShieldRadiusEvenIfNoShield())
                    * SHIELD_RADIUS_OVERLAP_MULT;
            float distance = Vector2f.sub(ship.getShieldCenterEvenIfNoShield(),
                    escorted.getShieldCenterEvenIfNoShield(), null).length() - radiusOverlap;

            float mag;
            if (distance < range) {
                mag = 1f;
            } else if (distance < range + PROXIMITY_FADE_DISTANCE) {
                mag = 1f - (distance - range) / PROXIMITY_FADE_DISTANCE;
            } else {
                mag = 0f;
            }

            if (ship.isDestroyer() && escorted.isCapital()) {
                mag *= DESTROYER_ESCORTING_CAPITAL_MULT;
            }
            return mag;
        }

        private ShipAPI findNearestLargerFriendly() {
            ShipAPI nearest = null;
            float nearestDistanceSq = Float.MAX_VALUE;
            for (ShipAPI other : CombatQueries.shipsMatching(candidate -> candidate != ship
                    && candidate.getOwner() == ship.getOwner() && candidate.isAlive() && !candidate.isHulk()
                    && candidate.getHullSize().ordinal() > ship.getHullSize().ordinal())) {
                float distanceSq = Vector2f.sub(other.getLocation(), ship.getLocation(), null).lengthSquared();
                if (distanceSq < nearestDistanceSq) {
                    nearestDistanceSq = distanceSq;
                    nearest = other;
                }
            }
            return nearest;
        }

        private void applyBonuses(float mag) {
            MutableShipStatsAPI stats = ship.getMutableStats();
            MutableStat[] maneuverStats = {stats.getAcceleration(), stats.getDeceleration(), stats.getMaxTurnRate()};
            StatBonus[] rangeStats = {stats.getBallisticWeaponRangeBonus(), stats.getEnergyWeaponRangeBonus()};

            if (mag <= 0f) {
                for (MutableStat stat : maneuverStats) {
                    stat.unmodify(ESCORT_BONUS_MOD_ID);
                }
                stats.getTurnAcceleration().unmodify(ESCORT_BONUS_MOD_ID);
                stats.getMaxSpeed().unmodify(ESCORT_BONUS_MOD_ID);
                for (StatBonus stat : rangeStats) {
                    stat.unmodify(ESCORT_BONUS_MOD_ID);
                }
                return;
            }

            float maneuverPercent = stats.getDynamic().getValue(ESCORT_MANEUVER_BONUS_KEY, 0f) * mag;
            for (MutableStat stat : maneuverStats) {
                stat.modifyPercent(ESCORT_BONUS_MOD_ID, maneuverPercent);
            }
            stats.getTurnAcceleration().modifyPercent(ESCORT_BONUS_MOD_ID, maneuverPercent * TURN_ACCELERATION_MULT);
            stats.getMaxSpeed().modifyPercent(ESCORT_BONUS_MOD_ID,
                    stats.getDynamic().getValue(ESCORT_SPEED_BONUS_KEY, 0f) * mag);

            float rangePercent = stats.getDynamic().getValue(ESCORT_WEAPON_RANGE_BONUS_KEY, 0f) * mag;
            for (StatBonus stat : rangeStats) {
                stat.modifyPercent(ESCORT_BONUS_MOD_ID, rangePercent);
            }
        }
    }
}
