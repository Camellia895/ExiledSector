package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BoundsAPI;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicFakeBeamPlugin;
import org.magiclib.util.MagicFakeBeam;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static exiledsector.skills.skilleffect.SkillEffectText.pct;

public enum CombatSkillEffect implements SkillEffect {

    BEAM_SPLIT_TARGETS_FLAT {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod(BEAM_SPLIT_TARGETS_KEY).modifyFlat(modId, magnitude);
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
                    + pct(magnitude) + " nearby enem" + (count == 1 ? "y" : "ies") + ".";
        }
    },
    EXPLODE_ON_DEATH {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public void applyAfterShipCreation(ShipAPI ship, String modId, float magnitude) {
            if (!ship.hasListenerOfClass(DeathExplosionListener.class)) {
                ship.addListener(new DeathExplosionListener(ship, magnitude));
            }
        }

        @Override
        public String describe(float magnitude) {
            return "When this ship is destroyed, it detonates violently, dealing explosive damage equal to up to "
                    + pct(magnitude) + "% of its maximum fuel capacity to nearby ships, tapering off with distance "
                    + "from the wreck.";
        }
    },
    DEATH_ON_COLLISION {
        @Override
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
    };

    private static final String BEAM_SPLIT_TARGETS_KEY = "exiledSector_beamSplitTargets";
    private static final float SPLIT_RADIUS_MULT_OF_BEAM_RANGE = 0.5f;
    private static final float SPLIT_BEAM_FULL_DURATION = 0.05f;
    private static final float SPLIT_BEAM_FADE_DURATION = 0.15f;

    @Override
    public abstract void apply(MutableShipStatsAPI stats, String modId, float magnitude);

    @Override
    public abstract String describe(float magnitude);

    private static final class BeamSplitListener implements DamageDealtModifier {

        private final ShipAPI ship;
        private boolean processingSplit;

        private BeamSplitListener(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (processingSplit) return null;
            if (!(param instanceof BeamAPI)) return null;
            if (!(target instanceof ShipAPI)) return null;

            int splitCount = Math.round(ship.getMutableStats().getDynamic().getValue(BEAM_SPLIT_TARGETS_KEY, 0f));
            if (splitCount <= 0) return null;

            BeamAPI beam = (BeamAPI) param;
            float splitRadius = beam.getWeapon().getRange() * SPLIT_RADIUS_MULT_OF_BEAM_RANGE;
            List<ShipAPI> splitTargets = findNearbyEnemies(ship, (ShipAPI) target, point, splitRadius, splitCount);
            if (splitTargets.isEmpty()) return null;

            float perTargetDamage = damage.getDamage() / (1 + splitTargets.size());
            damage.setDamage(perTargetDamage);

            CombatEngineAPI engine = Global.getCombatEngine();
            processingSplit = true;
            try {
                for (ShipAPI splitTarget : splitTargets) {
                    spawnSplitBeam(engine, point, splitTarget, beam, perTargetDamage, damage.getType(), ship);
                }
            } finally {
                processingSplit = false;
            }
            return null;
        }
    }

    private static List<ShipAPI> findNearbyEnemies(ShipAPI source, ShipAPI primaryTarget, Vector2f point, float radius, int count) {
        List<ShipAPI> candidates = new ArrayList<>();
        for (ShipAPI other : Global.getCombatEngine().getShips()) {
            if (other == source || other == primaryTarget) continue;
            if (other.getOwner() == source.getOwner()) continue;
            if (!other.isAlive() || other.isHulk()) continue;
            float distanceSq = Vector2f.sub(other.getLocation(), point, null).lengthSquared();
            if (distanceSq > radius * radius) continue;
            candidates.add(other);
        }
        candidates.sort(Comparator.comparingDouble(other -> Vector2f.sub(other.getLocation(), point, null).lengthSquared()));
        return candidates.size() > count ? candidates.subList(0, count) : candidates;
    }

    private static void spawnSplitBeam(CombatEngineAPI engine, Vector2f from, ShipAPI splitTarget,
                                        BeamAPI sourceBeam, float damageAmount, DamageType damageType, ShipAPI source) {
        float angle = VectorUtils.getAngle(from, splitTarget.getLocation());
        float range = MathUtils.getDistance(from, splitTarget.getLocation()) + 50f;
        Vector2f segEnd = MathUtils.getPoint(from, range, angle);
        Vector2f impactPoint = MagicFakeBeam.getShipCollisionPoint(from, segEnd, splitTarget, angle);
        if (impactPoint == null) {
            impactPoint = splitTarget.getLocation();
        }

        // Pass sourceBeam as the dealer (not just source) so other beam-gated listeners
        // (e.g. BEAM_DAMAGE_HARD_FLUX_PERCENT's DamageDealtModifier, which checks
        // `param instanceof BeamAPI`) correctly recognize and apply to split hits too.
        engine.applyDamage(sourceBeam, splitTarget, impactPoint, damageAmount, damageType, 0f, false, true, source, false);

        float impactSize = sourceBeam.getWidth() * 2f;
        engine.addHitParticle(impactPoint, new Vector2f(), impactSize, 1f,
                SPLIT_BEAM_FULL_DURATION + SPLIT_BEAM_FADE_DURATION, sourceBeam.getFringeColor());
        MagicFakeBeamPlugin.addBeam(SPLIT_BEAM_FULL_DURATION, SPLIT_BEAM_FADE_DURATION, sourceBeam.getWidth(),
                from, angle, MathUtils.getDistance(from, impactPoint) + 10f,
                sourceBeam.getCoreColor(), sourceBeam.getFringeColor());
    }

    private static final class DeathExplosionListener implements HullDamageAboutToBeTakenListener {

        private static final float RADIUS_MULT = 4f;
        private static final float MIN_RADIUS = 300f;
        private static final Color BLAST_COLOR = new Color(255, 200, 120, 40);
        private static final Color CORE_COLOR = new Color(255, 255, 255, 60);

        private final ShipAPI ship;
        private final float fuelDamagePercent;
        private boolean exploded;

        private DeathExplosionListener(ShipAPI ship, float fuelDamagePercent) {
            this.ship = ship;
            this.fuelDamagePercent = fuelDamagePercent;
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

            for (ShipAPI other : engine.getShips()) {
                if (other == ship || other.isHulk() || other.isShuttlePod()) {
                    continue;
                }
                float distance = Vector2f.sub(other.getLocation(), loc, null).length();
                if (distance >= radius) {
                    continue;
                }
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
            CombatEngineAPI engine = Global.getCombatEngine();
            Vector2f loc = ship.getLocation();
            float myRadius = ship.getCollisionRadius();

            for (ShipAPI other : engine.getShips()) {
                if (other == ship || other.isFighter() || other.isHulk() || other.isShuttlePod()
                        || other.getCollisionClass() == CollisionClass.NONE) {
                    continue;
                }
                if (isBroadPhaseNear(loc, myRadius, other) && hullsOverlap(ship, other)) {
                    return true;
                }
            }
            for (CombatEntityAPI asteroid : engine.getAsteroids()) {
                if (isBroadPhaseNear(loc, myRadius, asteroid) && hullsOverlap(ship, asteroid)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isBroadPhaseNear(Vector2f loc, float myRadius, CombatEntityAPI other) {
            float triggerRadius = (myRadius + other.getCollisionRadius()) * BROAD_PHASE_MARGIN;
            return Vector2f.sub(other.getLocation(), loc, null).lengthSquared() <= triggerRadius * triggerRadius;
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
}
