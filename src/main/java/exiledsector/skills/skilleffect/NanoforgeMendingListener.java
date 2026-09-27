package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;

final class NanoforgeMendingListener implements AdvanceableListener {

    static final String REGEN_PERCENT_KEY = "exiledSector_nanoforgeMendingRegenPercent";
    static final float UNDAMAGED_SECONDS = 5f;

    private final ShipAPI ship;
    private float lastHitpoints = -1f;
    private float secondsSinceHullDamage;

    NanoforgeMendingListener(ShipAPI ship) {
        this.ship = ship;
    }

    @Override
    public void advance(float amount) {
        if (amount <= 0f || !ship.isAlive() || ship.isHulk()) {
            return;
        }
        float hitpoints = ship.getHitpoints();
        if (hitpoints < lastHitpoints) {
            secondsSinceHullDamage = 0f;
        } else {
            secondsSinceHullDamage += amount;
        }
        if (secondsSinceHullDamage >= UNDAMAGED_SECONDS) {
            hitpoints = mend(hitpoints, amount);
        }
        lastHitpoints = hitpoints;
    }

    private float mend(float hitpoints, float amount) {
        float maxHitpoints = ship.getMaxHitpoints();
        float regenPercent = ship.getMutableStats().getDynamic().getValue(REGEN_PERCENT_KEY, 0f);
        if (hitpoints >= maxHitpoints || regenPercent <= 0f) {
            return hitpoints;
        }
        float mended = Math.min(maxHitpoints, hitpoints + maxHitpoints * regenPercent / 100f * amount);
        ship.setHitpoints(mended);
        return mended;
    }
}
