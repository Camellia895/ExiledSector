package exiledsector.skills;

import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import exiledsector.skills.skilleffect.SkillEffect;

public record HullSizeSkillEffect(SkillEffect effect, float frigate, float destroyer, float cruiser, float capitalShip) {

    public float valueFor(HullSize hullSize) {
        if (hullSize == HullSize.FRIGATE) return frigate;
        if (hullSize == HullSize.DESTROYER) return destroyer;
        if (hullSize == HullSize.CRUISER) return cruiser;
        return capitalShip;
    }
}
