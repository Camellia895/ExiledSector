package exiledsector.effects;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.VariantSource;
import exiledsector.compat.SecondInCommandCompat;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.npc.NpcTreeTag;
import exiledsector.skills.skilleffect.FleetWideEffects;

public class SkillTreeInstaller implements EveryFrameScript {

    private static final float CHECK_INTERVAL_SECONDS = 1f;

    private float timeSinceLastCheck = CHECK_INTERVAL_SECONDS;
    private boolean syncedSinceLoad;

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        timeSinceLastCheck += amount;
        if (timeSinceLastCheck >= CHECK_INTERVAL_SECONDS) {
            timeSinceLastCheck = 0f;
            checkPlayerShips();
        }
        FleetWideEffects.recomputeExtendedPhaseFieldIfStale();
    }

    private void checkPlayerShips() {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) return;

        boolean changed = !syncedSinceLoad;
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            changed |= adoptNpcTree(member);
            changed |= ensureHullModAppliesLast(member);
        }
        if (changed) {
            syncedSinceLoad = true;
            playerFleet.getFleetData().setSyncNeeded();
        }
    }

    private static boolean ensureHullModAppliesLast(FleetMemberAPI member) {
        ShipVariantAPI variant = member.getVariant();
        if (!variant.hasHullMod(SkillTreeHullMod.ID)) {
            ownedVariant(member).addPermaMod(SkillTreeHullMod.ID);
        } else if (SecondInCommandCompat.isAppliedBeforeController(variant, SkillTreeHullMod.ID)) {
            variant.removePermaMod(SkillTreeHullMod.ID);
            variant.addPermaMod(SkillTreeHullMod.ID);
        } else {
            return false;
        }
        member.setStatUpdateNeeded(true);
        return true;
    }

    static boolean adoptNpcTree(FleetMemberAPI member) {
        String tag = NpcTreeTag.find(member.getVariant());
        if (tag == null) return false;

        ShipSkillData npcTree = NpcTreeTag.decode(tag);
        if (npcTree != null && ShipSkillDataManager.get(member.getId()).isBlank()) {
            npcTree.clearNpcBuild();
            ShipSkillDataManager.put(member.getId(), npcTree);
        }

        ShipVariantAPI variant = ownedVariant(member);
        NpcTreeTag.removeAll(variant);
        if (variant.hasHullMod(SkillTreeHullMod.ID) && !variant.getPermaMods().contains(SkillTreeHullMod.ID)) {
            variant.removeMod(SkillTreeHullMod.ID);
        }
        member.setStatUpdateNeeded(true);
        return true;
    }

    static ShipVariantAPI ownedVariant(FleetMemberAPI member) {
        ShipVariantAPI variant = member.getVariant();
        if (variant.getSource() == VariantSource.REFIT) {
            return variant;
        }
        ShipVariantAPI copy = variant.clone();
        copy.setSource(VariantSource.REFIT);
        member.setVariant(copy, false, true);
        return copy;
    }
}
