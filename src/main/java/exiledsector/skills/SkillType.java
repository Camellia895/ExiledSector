package exiledsector.skills;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SkillType {

    private final String id;
    private final String displayName;
    private final String iconPath;
    private final List<SkillTypeEffect> effects;
    private final List<HullSizeSkillEffect> hullSizeEffects;
    private final SkillTier tier;
    private final String vanillaHullModId;
    private final String descriptionOverride;
    private final String todo;
    private final List<String> optionalOptionIds;
    private final List<String> exclusiveHullModIds;
    private final List<String> exclusiveSkillTypeIds;
    private final boolean locked;

    public SkillType(String id, String displayName, String iconPath,
                      List<SkillTypeEffect> effects, SkillTier tier, String vanillaHullModId,
                      String descriptionOverride, String todo) {
        this(id, displayName, iconPath, effects, Collections.emptyList(), tier,
                vanillaHullModId, descriptionOverride, todo, Collections.emptyList());
    }

    public SkillType(String id, String displayName, String iconPath,
                      List<SkillTypeEffect> effects, List<HullSizeSkillEffect> hullSizeEffects, SkillTier tier,
                      String vanillaHullModId, String descriptionOverride, String todo) {
        this(id, displayName, iconPath, effects, hullSizeEffects, tier,
                vanillaHullModId, descriptionOverride, todo, Collections.emptyList());
    }

    public SkillType(String id, String displayName, String iconPath,
                      List<SkillTypeEffect> effects, List<HullSizeSkillEffect> hullSizeEffects, SkillTier tier,
                      String vanillaHullModId, String descriptionOverride, String todo, List<String> optionalOptionIds) {
        this(id, displayName, iconPath, effects, hullSizeEffects, tier,
                vanillaHullModId, descriptionOverride, todo, optionalOptionIds, Collections.emptyList());
    }

    public SkillType(String id, String displayName, String iconPath,
                      List<SkillTypeEffect> effects, List<HullSizeSkillEffect> hullSizeEffects, SkillTier tier,
                      String vanillaHullModId, String descriptionOverride, String todo, List<String> optionalOptionIds,
                      List<String> exclusiveHullModIds) {
        this(id, displayName, iconPath, effects, hullSizeEffects, tier,
                vanillaHullModId, descriptionOverride, todo, optionalOptionIds, exclusiveHullModIds, Collections.emptyList());
    }

    public SkillType(String id, String displayName, String iconPath,
                      List<SkillTypeEffect> effects, List<HullSizeSkillEffect> hullSizeEffects, SkillTier tier,
                      String vanillaHullModId, String descriptionOverride, String todo, List<String> optionalOptionIds,
                      List<String> exclusiveHullModIds, List<String> exclusiveSkillTypeIds) {
        this(id, displayName, iconPath, effects, hullSizeEffects, tier,
                vanillaHullModId, descriptionOverride, todo, optionalOptionIds, exclusiveHullModIds, exclusiveSkillTypeIds, false);
    }

    public SkillType(String id, String displayName, String iconPath,
                      List<SkillTypeEffect> effects, List<HullSizeSkillEffect> hullSizeEffects, SkillTier tier,
                      String vanillaHullModId, String descriptionOverride, String todo, List<String> optionalOptionIds,
                      List<String> exclusiveHullModIds, List<String> exclusiveSkillTypeIds, boolean locked) {
        this.id = id;
        this.displayName = displayName;
        this.iconPath = iconPath;
        this.effects = effects == null ? Collections.emptyList() : effects;
        this.hullSizeEffects = hullSizeEffects == null ? Collections.emptyList() : hullSizeEffects;
        this.tier = tier;
        this.vanillaHullModId = vanillaHullModId;
        this.descriptionOverride = descriptionOverride;
        this.todo = todo;
        this.optionalOptionIds = optionalOptionIds == null ? Collections.emptyList() : optionalOptionIds;
        this.exclusiveHullModIds = exclusiveHullModIds == null ? Collections.emptyList() : exclusiveHullModIds;
        this.exclusiveSkillTypeIds = exclusiveSkillTypeIds == null ? Collections.emptyList() : exclusiveSkillTypeIds;
        this.locked = locked;
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

    public List<String> getExclusiveHullModIds() {
        if (vanillaHullModId == null || exclusiveHullModIds.contains(vanillaHullModId)) {
            return exclusiveHullModIds;
        }
        List<String> combined = new ArrayList<>(exclusiveHullModIds);
        combined.add(vanillaHullModId);
        return combined;
    }

    public List<String> getExclusiveSkillTypeIds() {
        return exclusiveSkillTypeIds;
    }

    public boolean isLocked() {
        return locked;
    }
}
