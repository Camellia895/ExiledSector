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
import exiledsector.skills.skilleffect.LogisticsSkillEffect;

public class SkillTreeInstaller implements EveryFrameScript {

    private static final float CHECK_INTERVAL_SECONDS = 1f;

    private float timeSinceLastCheck = CHECK_INTERVAL_SECONDS;

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
        if (timeSinceLastCheck < CHECK_INTERVAL_SECONDS) return;
        timeSinceLastCheck = 0f;

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) return;

        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            adoptNpcTree(member);
            ShipVariantAPI variant = member.getVariant();
            if (!variant.hasHullMod(SkillTreeHullMod.ID)) {
                variant = ownedVariant(member);
                variant.addPermaMod(SkillTreeHullMod.ID);
            } else if (SecondInCommandCompat.isAppliedBeforeController(variant, SkillTreeHullMod.ID)) {
                variant.removePermaMod(SkillTreeHullMod.ID);
                variant.addPermaMod(SkillTreeHullMod.ID);
                member.setStatUpdateNeeded(true);
            }
            new SkillTreeHullMod().applyEffectsBeforeShipCreation(member.getHullSpec().getHullSize(), member.getStats(), SkillTreeHullMod.ID);
        }

        LogisticsSkillEffect.recomputeExtendedPhaseField();
    }

    static void adoptNpcTree(FleetMemberAPI member) {
        String tag = NpcTreeTag.find(member.getVariant());
        if (tag == null) return;

        ShipSkillData npcTree = NpcTreeTag.decode(tag);
        if (npcTree != null && ShipSkillDataManager.get(member.getId()).isBlank()) {
            npcTree.clearNpcBuild();
            ShipSkillDataManager.put(member.getId(), npcTree);
        }

        ShipVariantAPI variant = ownedVariant(member);
        for (String npcTag = NpcTreeTag.find(variant); npcTag != null; npcTag = NpcTreeTag.find(variant)) {
            variant.removeTag(npcTag);
        }
        if (variant.hasHullMod(SkillTreeHullMod.ID) && !variant.getPermaMods().contains(SkillTreeHullMod.ID)) {
            variant.removeMod(SkillTreeHullMod.ID);
        }
        member.setStatUpdateNeeded(true);
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
