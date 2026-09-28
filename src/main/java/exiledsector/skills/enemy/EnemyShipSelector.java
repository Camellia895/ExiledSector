package exiledsector.skills.enemy;

import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import java.util.Random;

public final class EnemyShipSelector {

    private EnemyShipSelector() {
    }

    public static boolean isCandidate(FleetMemberAPI member) {
        return member != null && !member.isFighterWing() && !member.isStation() && !member.isCivilian()
                && !member.isAlly() && !member.isMothballed() && member.getVariant() != null;
    }

    public static boolean isChosen(FleetMemberAPI member, Random random) {
        boolean chanceRoll = random.nextFloat() < EnemyTreeConfig.otherShipChance();
        if (EnemyTreeConfig.levelsOfficeredShips() && hasOfficer(member)) {
            return true;
        }
        if (EnemyTreeConfig.levelsFlagship() && member.isFlagship()) {
            return true;
        }
        return chanceRoll;
    }

    private static boolean hasOfficer(FleetMemberAPI member) {
        PersonAPI captain = member.getCaptain();
        return captain != null && !captain.isDefault();
    }
}
