package exiledsector.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import org.apache.log4j.Logger;

public final class SkillTypeUnlockStatus {

    private static final String NEURAL_INTERFACE_HULLMOD_ID = "neural_interface";
    private static final String HAS_NEURAL_LINK_STAT_ID = "has_neural_link";

    private SkillTypeUnlockStatus() {
    }

    public static boolean isLocked(SkillType type) {
        String hullModId = type.getLockedUntilHullMod();
        if (hullModId == null) return false;

        try {
            CharacterDataAPI playerCharacter = Global.getSector().getCharacterData();
            if (playerCharacter == null) return true;
            if (playerCharacter.knowsHullMod(hullModId)) return false;
            return !(hullModId.equals(NEURAL_INTERFACE_HULLMOD_ID) && hasNeuralLinkStatFlag(playerCharacter));
        } catch (RuntimeException e) {
            Logger.getLogger(SkillTypeUnlockStatus.class).error("Failed to check unlock status for hullmod " + hullModId, e);
            return true;
        }
    }

    private static boolean hasNeuralLinkStatFlag(CharacterDataAPI playerCharacter) {
        PersonAPI person = playerCharacter.getPerson();
        return person != null && person.getStats().getDynamic().getMod(HAS_NEURAL_LINK_STAT_ID).getFlatBonus() > 0f;
    }
}
