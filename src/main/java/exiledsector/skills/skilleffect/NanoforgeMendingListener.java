package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;

final class NanoforgeMendingListener implements AdvanceableListener {

    static final String REGEN_PERCENT_KEY = "exiledSector_nanoforgeMendingRegenPercent";
    static final float UNDAMAGED_SECONDS = 5f;
    static final float MAX_TOTAL_REGEN_PERCENT_OF_HULL = 100f;

    private final ShipAPI ship;
    private float lastHitpoints = -1f;
    private float secondsSinceHullDamage;
    private float remainingRegen = -1f;

    NanoforgeMendingListener(ShipAPI ship) {
        this.ship = ship;
    }

    @Override
    public void advance(float amount) {
        if (amount <= 0f || !ship.isAlive() || ship.isHulk()) {
            return;
        }
        if (remainingRegen < 0f) {
            remainingRegen = ship.getMaxHitpoints() * MAX_TOTAL_REGEN_PERCENT_OF_HULL / 100f;
        }
        float hitpoints = ship.getHitpoints();
        if (hitpoints < lastHitpoints) {
            secondsSinceHullDamage = 0f;
        } else {
            secondsSinceHullDamage += amount;
        }
        if (secondsSinceHullDamage >= UNDAMAGED_SECONDS && remainingRegen > 0f) {
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
        float healed = Math.min(maxHitpoints * regenPercent / 100f * amount, Math.min(maxHitpoints - hitpoints, remainingRegen));
        remainingRegen -= healed;
        float mended = hitpoints + healed;
        ship.setHitpoints(mended);
        return mended;
    }
}
