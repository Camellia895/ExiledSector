package exiledsector.ui;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import exiledsector.i18n.Translation;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.progression.ShipLevelConfig;
import exiledsector.skills.progression.ShipLevelSystem;

final class SkillTreeLevelBar {

    private final FleetMemberAPI member;
    private final SkillTreeReadoutBar bar;

    private int level;
    private int bankedFreeAllocations;

    SkillTreeLevelBar(FleetMemberAPI member, int row) {
        this.member = member;
        this.bar = new SkillTreeReadoutBar(SkillTreeLevelBar.class, row);
    }

    void advance(float amount, PositionAPI position, float mouseX, float mouseY, boolean mouseKnown) {
        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        level = data.getLevel();
        bankedFreeAllocations = data.getBankedFreeAllocations();
        int xp = Math.round(data.getXp());

        int maxLevel = ShipLevelConfig.maxLevel();
        int xpToNextLevel = level >= maxLevel ? Math.max(xp, 1)
                : Math.round(ShipLevelSystem.xpToReachNextLevel(level, ShipLevelConfig.xpBase(), ShipLevelConfig.xpGrowth(),
                        ShipLevelConfig.xpGrowthCutoffLevel()));
        bar.advance(amount, position, xp, xpToNextLevel, mouseX, mouseY, mouseKnown);
    }

    void render(PositionAPI position, float alphaMult) {
        bar.render(position, alphaMult, label());
    }

    private String label() {
        if (bankedFreeAllocations > 0) {
            return Translation.msg("ui.levelBar.levelWithFree").arg("level", level).arg("free", bankedFreeAllocations).text();
        }
        return Translation.msg("ui.levelBar.level").arg("level", level).text();
    }

    boolean isHovered(PositionAPI position, float x, float y) {
        return bar.isHovered(position, x, y);
    }
}
