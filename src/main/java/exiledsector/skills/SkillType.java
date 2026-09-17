package exiledsector.skills;

import java.util.Collections;
import java.util.List;

public class SkillType {

    private final String id;
    private final String displayName;
    private final String iconPath;
    private final int opCost;
    private final float xpCost;
    private final List<SkillTypeEffect> effects;
    private final SkillTier tier;
    private final String vanillaHullModId;
    private final String descriptionOverride;
    private final String todo;

    public SkillType(String id, String displayName, String iconPath, int opCost, float xpCost,
                      List<SkillTypeEffect> effects, SkillTier tier, String vanillaHullModId,
                      String descriptionOverride, String todo) {
        this.id = id;
        this.displayName = displayName;
        this.iconPath = iconPath;
        this.opCost = opCost;
        this.xpCost = xpCost;
        this.effects = effects == null ? Collections.emptyList() : effects;
        this.tier = tier;
        this.vanillaHullModId = vanillaHullModId;
        this.descriptionOverride = descriptionOverride;
        this.todo = todo;
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

    public List<SkillTypeEffect> getEffects() {
        return effects;
    }

    public SkillTier getTier() {
        return tier;
    }

    public String getVanillaHullModId() {
        return vanillaHullModId;
    }

    public String getDescriptionOverride() {
        return descriptionOverride;
    }

    public String getTodo() {
        return todo;
    }
}
