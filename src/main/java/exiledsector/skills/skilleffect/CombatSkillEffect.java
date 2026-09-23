package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;

import static exiledsector.skills.skilleffect.SkillEffectText.pct;

public enum CombatSkillEffect implements SkillEffect {

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
                    + "from the wreck. Any collision damage, however slight, is instantly fatal to this ship.";
        }
    };

    @Override
    public abstract void apply(MutableShipStatsAPI stats, String modId, float magnitude);

    @Override
    public abstract String describe(float magnitude);

    private static final class DeathExplosionListener implements HullDamageAboutToBeTakenListener, AdvanceableListener {

        private static final float RADIUS_MULT = 4f;
        private static final float MIN_RADIUS = 300f;
        private static final float COLLISION_CHECK_GRACE_PERIOD = 1.5f;
        private static final Color BLAST_COLOR = new Color(255, 200, 120, 40);
        private static final Color CORE_COLOR = new Color(255, 255, 255, 60);

        private final ShipAPI ship;
        private final float fuelDamagePercent;
        private boolean exploded;
        private float aliveTime;

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

        @Override
        public void advance(float amount) {
            if (exploded || !ship.isAlive() || ship.isHulk()) {
                return;
            }
            aliveTime += amount;
            if (aliveTime < COLLISION_CHECK_GRACE_PERIOD) {
                return;
            }
            if (ship.getCollisionClass() == CollisionClass.NONE) {
                return;
            }
            if (isCollidingWithAnything()) {
                exploded = true;
                detonate();
                ship.setHitpoints(0f);
            }
        }

        private boolean isCollidingWithAnything() {
            CombatEngineAPI engine = Global.getCombatEngine();
            Vector2f loc = ship.getLocation();
            float myRadius = ship.getCollisionRadius();

            for (ShipAPI other : engine.getShips()) {
                if (other == ship || other.isFighter() || other.isHulk() || other.isShuttlePod()
                        || other.getCollisionClass() == CollisionClass.NONE) {
                    continue;
                }
                if (isOverlapping(loc, myRadius, other)) {
                    return true;
                }
            }
            for (CombatEntityAPI asteroid : engine.getAsteroids()) {
                if (isOverlapping(loc, myRadius, asteroid)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isOverlapping(Vector2f loc, float myRadius, CombatEntityAPI other) {
            float combinedRadius = myRadius + other.getCollisionRadius();
            return Vector2f.sub(other.getLocation(), loc, null).lengthSquared() <= combinedRadius * combinedRadius;
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
}
