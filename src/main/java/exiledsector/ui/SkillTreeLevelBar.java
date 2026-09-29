package exiledsector.ui;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import exiledsector.i18n.Translation;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.progression.ShipLevelConfig;
import exiledsector.skills.progression.ShipLevelSystem;

final class SkillTreeLevelBar {

    private static final float BAR_GAP = 8f;

    private final FleetMemberAPI member;
    private final SkillTreeReadoutBar bar = new SkillTreeReadoutBar(SkillTreeLevelBar.class);

    private int level;
    private int xp;
    private int xpToNextLevel;
    private int bankedFreeAllocations;

    SkillTreeLevelBar(FleetMemberAPI member) {
        this.member = member;
    }

    void advance(float amount, PositionAPI position, float mouseX, float mouseY, boolean mouseKnown) {
        refresh();

        boolean hovered = mouseKnown && isHovered(position, mouseX, mouseY);
        bar.advance(amount, xp, xpToNextLevel, hovered);
    }

    void render(PositionAPI position, float alphaMult) {
        refresh();

        float barLeft = barLeft(position);
        float barBottom = barBottom(position);
        bar.render(barLeft, barBottom, xp, xpToNextLevel, alphaMult, label());
    }

    private String label() {
        if (bankedFreeAllocations > 0) {
            return Translation.msg("ui.levelBar.levelWithFree").arg("level", level).arg("free", bankedFreeAllocations).text();
        }
        return Translation.msg("ui.levelBar.level").arg("level", level).text();
    }

    boolean isHovered(PositionAPI position, float x, float y) {
        return position != null && SkillTreeReadoutBar.containsPoint(barLeft(position), barBottom(position), x, y);
    }

    private float barLeft(PositionAPI position) {
        return position.getX() + SkillTreeRefitButton.SHIP_CARD_MARGIN;
    }

    private float barBottom(PositionAPI position) {
        float opBarTop = position.getY() + position.getHeight() - SkillTreeRefitButton.SHIP_CARD_MARGIN;
        float opBarBottom = opBarTop - SkillTreeReadoutBar.BAR_HEIGHT;
        return opBarBottom - BAR_GAP - SkillTreeReadoutBar.BAR_HEIGHT;
    }

    private void refresh() {
        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        level = data.getLevel();
        xp = Math.round(data.getXp());
        bankedFreeAllocations = data.getBankedFreeAllocations();

        int maxLevel = ShipLevelConfig.maxLevel();
        xpToNextLevel = level >= maxLevel ? Math.max(xp, 1)
                : Math.round(ShipLevelSystem.xpToReachNextLevel(level, ShipLevelConfig.xpBase(), ShipLevelConfig.xpGrowth(),
                        ShipLevelConfig.xpGrowthCutoffLevel()));
    }
}
