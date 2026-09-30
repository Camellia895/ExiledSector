package exiledsector.skills.tags;

import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;

import java.util.Collection;

public final class NodeRequirements {

    private NodeRequirements() {
    }

    public static String firstUnmet(Collection<String> tags, ShipProfile profile) {
        for (String tag : tags) {
            if (SkillTags.isRequirement(tag) && !isMet(tag, profile)) {
                return tag;
            }
        }
        return null;
    }

    public static boolean isSatisfiedBy(Collection<String> tags, ShipProfile profile) {
        return firstUnmet(tags, profile) == null;
    }

    private static boolean isMet(String requirement, ShipProfile profile) {
        ShieldType shieldType = profile.shieldType();
        return switch (requirement) {
            case "req_shields" -> shieldType == ShieldType.FRONT || shieldType == ShieldType.OMNI;
            case "req_no_shields" -> shieldType == ShieldType.NONE;
            case "req_phase" -> shieldType == ShieldType.PHASE;
            case "req_fighter_bays" -> profile.fighterBays() > 0;
            case "req_no_fighter_bays" -> profile.fighterBays() <= 0;
            case "req_ballistic" -> profile.weaponKinds().contains(WeaponKind.BALLISTIC);
            case "req_missile" -> profile.weaponKinds().contains(WeaponKind.MISSILE);
            case "req_energy" -> profile.weaponKinds().contains(WeaponKind.ENERGY);
            case "req_beam" -> profile.weaponKinds().contains(WeaponKind.BEAM);
            case "req_offensive_beam" -> profile.weaponKinds().contains(WeaponKind.OFFENSIVE_BEAM);
            case "req_non_beam_energy" -> profile.weaponKinds().contains(WeaponKind.NON_BEAM_ENERGY);
            case "req_flagship" -> profile.flagship();
            default -> false;
        };
    }
}
