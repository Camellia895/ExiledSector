package exiledsector.skills;

public enum SkillTier {

    SMALL(1f),
    NOTABLE(1.5f),
    KEYSTONE(2f),
    ROOT(2.5f);

    private final float sizeMultiplier;

    SkillTier(float sizeMultiplier) {
        this.sizeMultiplier = sizeMultiplier;
    }

    public float getSizeMultiplier() {
        return sizeMultiplier;
    }
}
