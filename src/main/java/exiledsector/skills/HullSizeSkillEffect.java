package exiledsector.skills;

import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import exiledsector.skills.skilleffect.SkillEffect;

public record HullSizeSkillEffect(SkillEffect effect, float frigate, float destroyer, float cruiser, float capitalShip) {

    public float valueFor(HullSize hullSize) {
        if (hullSize == null) return capitalShip;
        switch (hullSize) {
            case FRIGATE:
                return frigate;
            case DESTROYER:
                return destroyer;
            case CRUISER:
                return cruiser;
            default:
                return capitalShip;
        }
    }
}
