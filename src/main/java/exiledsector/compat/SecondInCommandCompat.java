package exiledsector.compat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;

public final class SecondInCommandCompat {

    public static final String MOD_ID = "second_in_command";
    public static final String CONTROLLER_HULLMOD_ID = "sc_skill_controller";
    public static final String REDISTRIBUTION_SKILL_ID = "sc_improvisation_redistribution";
    public static final String ENHANCED_OVERRIDES_SKILL_ID = "sc_improvisation_enhanced_overrides";
    public static final String RECONFIGURATION_SKILL_ID = "sc_strikecraft_reconfiguration";
    private static final String INACTIVE_SMOD_TAG_PREFIX = "sc_inactive_smods_";

    private static Method getFleetData;
    private static Method isSkillActive;
    private static boolean unavailable;

    private SecondInCommandCompat() {
    }

    public static boolean isModEnabled() {
        SettingsAPI settings = Global.getSettings();
        return settings != null && settings.getModManager().isModEnabled(MOD_ID);
    }

    public static boolean isSkillActive(FleetMemberAPI member, String skillId) {
        if (member == null || unavailable || !isModEnabled()) {
            return false;
        }
        CampaignFleetAPI fleet = fleetFor(member);
        if (fleet == null) {
            return false;
        }
        try {
            resolveMethods();
            Object data = getFleetData.invoke(null, fleet);
            return data != null && Boolean.TRUE.equals(isSkillActive.invoke(data, skillId));
        } catch (ReflectiveOperationException | LinkageError | ClassCastException e) {
            unavailable = true;
            Logger.getLogger(SecondInCommandCompat.class).error("Second-in-Command skill lookup failed; disabling compatibility checks", e);
            return false;
        }
    }

    public static boolean hasDeactivatedSMod(ShipVariantAPI variant, String hullModId) {
        return variant != null && variant.hasTag(INACTIVE_SMOD_TAG_PREFIX + hullModId);
    }

    public static boolean isAppliedBeforeController(ShipVariantAPI variant, String hullModId) {
        if (variant == null || !variant.hasHullMod(CONTROLLER_HULLMOD_ID)) {
            return false;
        }
        for (String id : variant.getHullMods()) {
            if (id.equals(CONTROLLER_HULLMOD_ID)) {
                return false;
            }
            if (id.equals(hullModId)) {
                return true;
            }
        }
        return false;
    }

    private static void resolveMethods() throws ReflectiveOperationException {
        if (getFleetData != null) {
            return;
        }
        Class<?> utils = Class.forName("second_in_command.SCUtils", true, Global.getSettings().getScriptClassLoader());
        Method fleetDataMethod = utils.getMethod("getFleetData", CampaignFleetAPI.class);
        isSkillActive = fleetDataMethod.getReturnType().getMethod("isSkillActive", String.class);
        getFleetData = fleetDataMethod;
    }

    private static CampaignFleetAPI fleetFor(FleetMemberAPI member) {
        FleetDataAPI fleetData = member.getFleetData();
        CampaignFleetAPI fleet = fleetData == null ? null : fleetData.getFleet();
        if (fleet == null) {
            return null;
        }
        SectorAPI sector = Global.getSector();
        CampaignFleetAPI playerFleet = sector == null ? null : sector.getPlayerFleet();
        boolean joinedAllyFleet = playerFleet != null && fleet != playerFleet
                && playerFleet.getFleetData() != null
                && playerFleet.getFleetData().getMembersListCopy().contains(member);
        return joinedAllyFleet ? playerFleet : fleet;
    }
}
