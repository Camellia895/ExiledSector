package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.CombatListenerManagerAPI;
import com.fs.starfarer.api.combat.listeners.DamageListener;

import java.util.ArrayList;
import java.util.List;

final class ChainHitDamageRestorer implements DamageListener {

    private final List<PendingRestore> pending = new ArrayList<>();

    static void reduceForThisHit(DamageAPI damage, float dealtMult, CombatEntityAPI target) {
        float baseDamage = damage.getBaseDamage();
        instance().pending.add(new PendingRestore(damage, baseDamage, target));
        damage.setDamage(baseDamage * dealtMult);
    }

    private static ChainHitDamageRestorer instance() {
        CombatListenerManagerAPI listeners = Global.getCombatEngine().getListenerManager();
        List<ChainHitDamageRestorer> existing = listeners.getListeners(ChainHitDamageRestorer.class);
        if (!existing.isEmpty()) {
            return existing.get(0);
        }
        ChainHitDamageRestorer restorer = new ChainHitDamageRestorer();
        listeners.addListener(restorer);
        return restorer;
    }

    @Override
    public void reportDamageApplied(Object source, CombatEntityAPI target, ApplyDamageResultAPI result) {
        pending.removeIf(restore -> restore.restoreIfFor(target));
    }

    private record PendingRestore(DamageAPI damage, float baseDamage, CombatEntityAPI target) {
        private boolean restoreIfFor(CombatEntityAPI hitTarget) {
            if (hitTarget != target) {
                return false;
            }
            damage.setDamage(baseDamage);
            return true;
        }
    }
}
