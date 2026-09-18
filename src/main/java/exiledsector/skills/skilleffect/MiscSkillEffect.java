package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import static exiledsector.skills.skilleffect.SkillEffectText.flatChange;
import static exiledsector.skills.skilleffect.SkillEffectText.pctChange;

public enum MiscSkillEffect implements SkillEffect {

    ELECTRONIC_WARFARE {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod("electronic_warfare_flat").modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "ECM rating");
        }
    },
    NAV_RATING {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getDynamic().getMod("coord_maneuvers_flat").modifyFlat(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "fleet nav rating");
        }
    },
    PEAK_CR_DURATION {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
            stats.getPeakCRDuration().modifyPercent(modId, magnitude);
        }

        @Override
        public String describe(float magnitude) {
            return pctChange(magnitude, "peak combat readiness duration");
        }
    },
    COMMAND_POINT_RECOVERY_WHILE_FLAGSHIP {
        @Override
        public void apply(MutableShipStatsAPI stats, String modId, float magnitude) {
        }

        @Override
        public boolean isConditional() {
            return true;
        }

        @Override
        public void advanceInCombat(ShipAPI ship, String modId, float magnitude) {
            boolean isFlagship = ship == Global.getCombatEngine().getPlayerShip();
            if (!isFlagship) {
                FleetMemberAPI member = ship.getMutableStats().getFleetMember();
                if (member != null) {
                    PersonAPI commander = member.getFleetCommanderForStats();
                    if (commander == null) commander = member.getFleetCommander();
                    isFlagship = commander != null && commander == ship.getCaptain();
                }
            }

            StatBonus commandPointRate = ship.getMutableStats().getDynamic().getMod("command_point_rate_flat");
            if (isFlagship) {
                commandPointRate.modifyFlat(modId, magnitude);
            } else {
                commandPointRate.unmodify(modId);
            }
        }

        @Override
        public String describe(float magnitude) {
            return flatChange(magnitude, "command point recovery rate while this ship is the flagship");
        }
    };

    @Override
    public abstract void apply(MutableShipStatsAPI stats, String modId, float magnitude);

    @Override
    public abstract String describe(float magnitude);
}
