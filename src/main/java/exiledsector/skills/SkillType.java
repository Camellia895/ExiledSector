package exiledsector.skills;

import java.util.Collections;
import java.util.List;

public class SkillType {

    private final String id;
    private final String displayName;
    private final String iconPath;
    private final int opCost;
    private final List<SkillTypeEffect> effects;
    private final List<HullSizeSkillEffect> hullSizeEffects;
    private final SkillTier tier;
    private final String vanillaHullModId;
    private final String descriptionOverride;
    private final String todo;
    private final List<String> optionalOptionIds;

    public SkillType(String id, String displayName, String iconPath, int opCost,
                      List<SkillTypeEffect> effects, SkillTier tier, String vanillaHullModId,
                      String descriptionOverride, String todo) {
        this(id, displayName, iconPath, opCost, effects, Collections.emptyList(), tier,
                vanillaHullModId, descriptionOverride, todo, Collections.emptyList());
    }

    public SkillType(String id, String displayName, String iconPath, int opCost,
                      List<SkillTypeEffect> effects, List<HullSizeSkillEffect> hullSizeEffects, SkillTier tier,
                      String vanillaHullModId, String descriptionOverride, String todo) {
        this(id, displayName, iconPath, opCost, effects, hullSizeEffects, tier,
                vanillaHullModId, descriptionOverride, todo, Collections.emptyList());
    }

    public SkillType(String id, String displayName, String iconPath, int opCost,
                      List<SkillTypeEffect> effects, List<HullSizeSkillEffect> hullSizeEffects, SkillTier tier,
                      String vanillaHullModId, String descriptionOverride, String todo, List<String> optionalOptionIds) {
        this.id = id;
        this.displayName = displayName;
        this.iconPath = iconPath;
        this.opCost = opCost;
        this.effects = effects == null ? Collections.emptyList() : effects;
        this.hullSizeEffects = hullSizeEffects == null ? Collections.emptyList() : hullSizeEffects;
        this.tier = tier;
        this.vanillaHullModId = vanillaHullModId;
        this.descriptionOverride = descriptionOverride;
        this.todo = todo;
        this.optionalOptionIds = optionalOptionIds == null ? Collections.emptyList() : optionalOptionIds;
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

    public List<SkillTypeEffect> getEffects() {
        return effects;
    }

    public List<HullSizeSkillEffect> getHullSizeEffects() {
        return hullSizeEffects;
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

    public List<String> getOptionalOptionIds() {
        return optionalOptionIds;
    }

    public boolean isOptional() {
        return !optionalOptionIds.isEmpty();
    }
}
