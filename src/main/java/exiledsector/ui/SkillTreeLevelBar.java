package exiledsector.ui;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.ShipLevelConfig;
import exiledsector.skills.ShipLevelSystem;
import exiledsector.skills.ShipSkillData;

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

        boolean hovered = mouseKnown && position != null && containsPoint(position, mouseX, mouseY);
        bar.advance(amount, xp, xpToNextLevel, hovered);
    }

    void render(PositionAPI position, float alphaMult) {
        refresh();

        float barLeft = barLeft(position);
        float barBottom = barBottom(position);
        bar.render(barLeft, barBottom, xp, xpToNextLevel, alphaMult, label());
    }

    private String label() {
        String label = "Level " + level + "  (" + xp + " / " + xpToNextLevel + " XP)";
        if (bankedFreeAllocations > 0) {
            label += "   +" + bankedFreeAllocations + " Free";
        }
        return label;
    }

    private boolean containsPoint(PositionAPI position, float x, float y) {
        float barLeft = barLeft(position);
        float barBottom = barBottom(position);
        return x >= barLeft && x <= barLeft + SkillTreeReadoutBar.BAR_WIDTH
                && y >= barBottom && y <= barBottom + SkillTreeReadoutBar.BAR_HEIGHT;
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
