package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.combat.WeaponAPI;
import exiledsector.i18n.Translation;

import java.util.Arrays;
import java.util.List;

public enum WeaponScope {

    ALL(null, ""),
    BALLISTIC(ALL, "BALLISTIC_"),
    MISSILE(ALL, "MISSILE_"),
    ENERGY(ALL, "ENERGY_"),
    NON_BEAM_ENERGY(ENERGY, "NON_BEAM_ENERGY_"),
    BEAM(ENERGY, "BEAM_");

    private final WeaponScope parent;
    private final String namePrefix;

    WeaponScope(WeaponScope parent, String namePrefix) {
        this.parent = parent;
        this.namePrefix = namePrefix;
    }

    public WeaponScope parent() {
        return parent;
    }

    public List<WeaponScope> children() {
        return Arrays.stream(values()).filter(scope -> scope.parent == this).toList();
    }

    String namePrefix() {
        return namePrefix;
    }

    String qualify(String statKey) {
        if (this == ALL) {
            return Translation.text(statKey);
        }
        String specific = statKey + "." + name();
        if (Translation.has(specific)) {
            return Translation.text(specific);
        }
        return Translation.msg("weapon.qualified").arg("scope", Translation.text("weapon.scope." + name())).arg("stat", Translation.text(statKey)).text();
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
