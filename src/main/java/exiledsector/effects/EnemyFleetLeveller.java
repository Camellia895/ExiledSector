package exiledsector.effects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.skills.ShipLevelConfig;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillDataResolver;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillTree;
import exiledsector.skills.enemy.EnemyHullMods;
import exiledsector.skills.enemy.EnemyLayout;
import exiledsector.skills.enemy.EnemyLayouts;
import exiledsector.skills.enemy.EnemyLevelTable;
import exiledsector.skills.enemy.EnemyShipSelector;
import exiledsector.skills.enemy.EnemySkillTreeBuilder;
import exiledsector.skills.enemy.EnemyTreeBuild;
import exiledsector.skills.enemy.EnemyTreeConfig;
import exiledsector.skills.enemy.EnemyTreeRecords;
import exiledsector.skills.enemy.EnemyTreeTag;
import exiledsector.skills.tags.ShipProfile;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class EnemyFleetLeveller {

    private EnemyFleetLeveller() {
    }

    public static void ensure(CampaignFleetAPI fleet) {
        if (!isLevellable(fleet) || !EnemyTreeConfig.isEnabled()) {
            return;
        }
        Map<String, String> records = EnemyTreeRecords.of(fleet.getMemoryWithoutUpdate());
        int playerLevel = Global.getSector().getPlayerStats().getLevel();
        String seedPrefix = Global.getSector().getSeedString() + "|" + fleet.getId() + "|";
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            String record = records.get(member.getId());
            if (record == null) {
                record = decide(member, playerLevel, new Random((seedPrefix + member.getId()).hashCode()));
                records.put(member.getId(), record);
            }
            if (EnemyTreeRecords.isLevelled(record)) {
                apply(member, record);
            }
        }
    }

    static boolean isLevellable(CampaignFleetAPI fleet) {
        return fleet != null && !fleet.isPlayerFleet() && !fleet.isStationMode()
                && fleet.getContainingLocation() != null && fleet.getFleetData() != null
                && (fleet.getFaction() == null || !fleet.getFaction().isPlayerFaction());
    }

    static String decide(FleetMemberAPI member, int playerLevel, Random random) {
        if (!EnemyShipSelector.isCandidate(member) || !EnemyShipSelector.isChosen(member, random)) {
            return EnemyTreeRecords.NOT_LEVELLED;
        }
        ShipProfile profile = ShipProfile.of(member);
        List<EnemyLayout> eligible = EnemyLayouts.eligibleFor(profile);
        if (eligible.isEmpty()) {
            return EnemyTreeRecords.NOT_LEVELLED;
        }
        EnemyLayout layout = eligible.get(random.nextInt(eligible.size()));
        int nodeCount = Math.min(EnemyLevelTable.roll(playerLevel, random), ShipLevelConfig.maxAllocatedNodes());
        EnemyTreeBuild build = EnemySkillTreeBuilder.build(layout, nodeCount, profile, EnemyHullMods.of(member.getVariant()));
        return EnemyTreeTag.encode(layout.id(), build.data());
    }

    static void apply(FleetMemberAPI member, String tag) {
        ShipVariantAPI current = member.getVariant();
        if (current.hasHullMod(SkillTreeHullMod.ID) && tag.equals(EnemyTreeTag.find(current))) {
            return;
        }
        ShipVariantAPI variant = SkillTreeInstaller.ownedVariant(member);
        for (String existing = EnemyTreeTag.find(variant); existing != null; existing = EnemyTreeTag.find(variant)) {
            variant.removeTag(existing);
        }
        variant.addTag(tag);
        stripConflictingHullMods(variant, SkillDataResolver.resolve(member, variant));
        if (!variant.hasHullMod(SkillTreeHullMod.ID)) {
            variant.addMod(SkillTreeHullMod.ID);
        }
        member.setStatUpdateNeeded(true);
    }

    private static void stripConflictingHullMods(ShipVariantAPI variant, ShipSkillData data) {
        Set<String> removable = EnemyHullMods.of(variant).removable();
        for (String nodeId : data.getAllocatedNodeIds()) {
            SkillNode node = SkillTree.get(nodeId);
            if (node == null) {
                continue;
            }
            for (String hullModId : node.resolveEffectiveType(data).getExclusiveHullModIds()) {
                if (removable.contains(hullModId)) {
                    variant.removeMod(hullModId);
                }
            }
        }
    }
}
