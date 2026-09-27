package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

final class SplitBeamDrones {

    private static final float SPLIT_TIMEOUT_SECONDS = 0.3f;
    private static final float EXIT_MARGIN = 10f;
    private static final String SHARE_MOD_ID = "exiledSector_splitBeamDroneShare";

    private final ShipAPI firingShip;
    private final Map<SplitKey, SplitDrone> drones = new HashMap<>();

    SplitBeamDrones(ShipAPI firingShip) {
        this.firingShip = firingShip;
    }

    void refresh(WeaponAPI weapon, ShipAPI primaryTarget, ShipAPI splitTarget, Vector2f impactPoint, float share) {
        SplitDrone split = drones.computeIfAbsent(new SplitKey(weapon, splitTarget),
                key -> new SplitDrone(SplitBeamDroneFactory.create(firingShip, weapon), splitTarget));
        split.retarget(refractionOrigin(primaryTarget, impactPoint, splitTarget.getLocation()), share);
    }

    void advance(float amount) {
        if (drones.isEmpty()) {
            return;
        }
        boolean firingShipGone = !firingShip.isAlive();
        Iterator<SplitDrone> iterator = drones.values().iterator();
        while (iterator.hasNext()) {
            SplitDrone split = iterator.next();
            if (split.advance(amount, firingShipGone)) {
                Global.getCombatEngine().removeEntity(split.drone);
                iterator.remove();
            }
        }
    }

    static Vector2f refractionOrigin(ShipAPI primaryTarget, Vector2f impactPoint, Vector2f towards) {
        Vector2f direction = Vector2f.sub(towards, impactPoint, null);
        if (direction.lengthSquared() <= 0f) {
            return new Vector2f(impactPoint);
        }
        direction.normalise();
        Vector2f offset = Vector2f.sub(impactPoint, primaryTarget.getShieldCenterEvenIfNoShield(), null);
        float radius = blockingRadius(primaryTarget);
        float along = Vector2f.dot(offset, direction);
        float discriminant = along * along - (offset.lengthSquared() - radius * radius);
        float exitDistance = discriminant < 0f ? 0f : -along + (float) Math.sqrt(discriminant);
        if (exitDistance <= 0f) {
            return new Vector2f(impactPoint);
        }
        float travel = exitDistance + EXIT_MARGIN;
        return new Vector2f(impactPoint.x + direction.x * travel, impactPoint.y + direction.y * travel);
    }

    private static float blockingRadius(ShipAPI ship) {
        ShieldAPI shield = ship.getShield();
        return shield != null && shield.isOn() ? shield.getRadius() : ship.getCollisionRadius();
    }

    private record SplitKey(WeaponAPI weapon, ShipAPI splitTarget) {
    }

    private final class SplitDrone {

        private final ShipAPI drone;
        private final WeaponAPI droneWeapon;
        private final ShipAPI splitTarget;
        private final ShareListener shareListener = new ShareListener();
        private final Vector2f origin = new Vector2f();
        private float secondsSinceRefresh;

        private SplitDrone(ShipAPI drone, ShipAPI splitTarget) {
            this.drone = drone;
            this.droneWeapon = drone.getAllWeapons().get(0);
            this.splitTarget = splitTarget;
            drone.addListener(shareListener);
        }

        private void retarget(Vector2f newOrigin, float share) {
            origin.set(newOrigin);
            shareListener.share = share;
            secondsSinceRefresh = 0f;
        }

        private boolean advance(float amount, boolean firingShipGone) {
            secondsSinceRefresh += amount;
            boolean firing = !firingShipGone && secondsSinceRefresh <= SPLIT_TIMEOUT_SECONDS && splitTarget.isAlive();
            if (!firing && !droneWeapon.isFiring()) {
                return true;
            }
            if (!firingShipGone) {
                SplitBeamDroneStats.mirror(firingShip.getMutableStats(), drone.getMutableStats());
            }
            float angle = VectorUtils.getAngle(origin, splitTarget.getLocation());
            drone.getLocation().set(origin);
            drone.setFacing(angle);
            droneWeapon.setForceFireOneFrame(firing);
            droneWeapon.setFacing(angle);
            droneWeapon.updateBeamFromPoints();
            return false;
        }
    }

    static final class ShareListener implements DamageDealtModifier {

        private float share = 1f;

        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (!(param instanceof BeamAPI)) {
                return null;
            }
            damage.getModifier().modifyMult(SHARE_MOD_ID, share);
            return SHARE_MOD_ID;
        }
    }
}
