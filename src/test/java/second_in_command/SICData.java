package second_in_command;

public class SICData {

    public boolean isSkillActive(String skillId) {
        return SICUtils.ACTIVE_SKILLS.contains(skillId);
    }
}
