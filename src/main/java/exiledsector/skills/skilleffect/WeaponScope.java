package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.WeaponAPI;

import java.util.List;

public enum WeaponScope {

    ALL(null, "", ""),
    BALLISTIC(ALL, "BALLISTIC_", "ballistic"),
    MISSILE(ALL, "MISSILE_", "missile"),
    ENERGY(ALL, "ENERGY_", "energy"),
    NON_BEAM_ENERGY(ENERGY, "NON_BEAM_ENERGY_", "non-beam energy"),
    BEAM(ENERGY, "BEAM_", "beam");

    private final WeaponScope parent;
    private final String namePrefix;
    private final String adjective;

    WeaponScope(WeaponScope parent, String namePrefix, String adjective) {
        this.parent = parent;
        this.namePrefix = namePrefix;
        this.adjective = adjective;
    }

    public WeaponScope parent() {
        return parent;
    }

    public List<WeaponScope> children() {
        return switch (this) {
            case ALL -> List.of(BALLISTIC, MISSILE, ENERGY);
            case ENERGY -> List.of(NON_BEAM_ENERGY, BEAM);
            default -> List.of();
        };
    }

    String namePrefix() {
        return namePrefix;
    }

    String qualify(String statName) {
        return adjective.isEmpty() ? statName : adjective + " " + statName;
    }

    boolean matches(WeaponAPI weapon) {
        return switch (this) {
            case ALL -> true;
            case BALLISTIC -> weapon.getType() == WeaponAPI.WeaponType.BALLISTIC;
            case MISSILE -> weapon.getType() == WeaponAPI.WeaponType.MISSILE;
            case ENERGY -> weapon.getType() == WeaponAPI.WeaponType.ENERGY;
            case NON_BEAM_ENERGY -> weapon.getType() == WeaponAPI.WeaponType.ENERGY && !weapon.isBeam();
            case BEAM -> weapon.isBeam();
        };
    }
}
