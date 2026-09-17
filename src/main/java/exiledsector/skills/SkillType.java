package exiledsector.skills;

public class SkillType {

    private final String id;
    private final String displayName;
    private final String iconPath;
    private final int opCost;
    private final float xpCost;
    private final SkillEffect effect;
    private final float magnitude;

    public SkillType(String id, String displayName, String iconPath, int opCost, float xpCost,
                      SkillEffect effect, float magnitude) {
        this.id = id;
        this.displayName = displayName;
        this.iconPath = iconPath;
        this.opCost = opCost;
        this.xpCost = xpCost;
        this.effect = effect;
        this.magnitude = magnitude;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconPath() {
        return iconPath;
    }

    public int getOpCost() {
        return opCost;
    }

    public float getXpCost() {
        return xpCost;
    }

    public SkillEffect getEffect() {
        return effect;
    }

    public float getMagnitude() {
        return magnitude;
    }
}
