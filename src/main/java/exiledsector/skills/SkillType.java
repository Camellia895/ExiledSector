package exiledsector.skills;

/**
 * Definition of a kind of skill (its display name, icon, OP/XP cost, and the
 * stat bonus it grants), shared by every SkillNode placement that uses it.
 * Content is authored in data/skilltrees/skill_types.json (see
 * SkillTypeLoader) and looked up by id while parsing
 * data/skilltrees/ship_skill_tree.json, so a tree with many copies of the
 * same skill (e.g. four "Capacitors" nodes) only has to define that skill's
 * name/icon/cost/effect once.
 *
 * effect may be null for a skill that doesn't apply a stat bonus yet (it's
 * still just a visual placeholder in the tree) - SkillTreeHullMod skips
 * those when applying unlocked nodes.
 */
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
