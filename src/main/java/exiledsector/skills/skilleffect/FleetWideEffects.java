package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.PhaseField;
import exiledsector.i18n.Translation;

import java.util.ArrayList;
import java.util.List;

public final class FleetWideEffects {

    static final String POST_BATTLE_SALVAGE_CONTRIBUTION_KEY = "exiledSector_postBattleSalvageContribution";
    static final String PHASE_FIELD_CONTRIBUTION_KEY = "exiledSector_phaseFieldContributionPercent";
    private static final String POST_BATTLE_SALVAGE_FLEET_MOD_ID = "exiledSector_postBattleSalvage";
    private static final String EXTENDED_PHASE_FIELD_MOD_ID = "exiledSector_extendedPhaseField";

    private FleetWideEffects() {
    }

    private static CampaignFleetAPI playerFleet() {
        SectorAPI sector = Global.getSector();
        return sector == null ? null : sector.getPlayerFleet();
    }

    public static void recomputeSalvageBonus() {
        CampaignFleetAPI fleet = playerFleet();
        if (fleet == null) {
            return;
        }
        float totalPercent = 0f;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            totalPercent += member.getStats().getDynamic().getValue(POST_BATTLE_SALVAGE_CONTRIBUTION_KEY, 0f);
        }
        fleet.getStats().getDynamic().getStat(Stats.BATTLE_SALVAGE_MULT_FLEET)
                .modifyFlat(POST_BATTLE_SALVAGE_FLEET_MOD_ID, totalPercent / 100f);
    }

    public static void recomputeExtendedPhaseField() {
        CampaignFleetAPI fleet = playerFleet();
        if (fleet == null) {
            return;
        }

        // This replaces vanilla's own Phase Field fleet-wide calculation (which only ever counts
        // real phase ships) with one that also folds in ships with this node, so both share a
        // single pool and a single floor instead of stacking two independently-clamped
        // multipliers (which could otherwise compound below vanilla's intended floor).
        fleet.getStats().getDetectedRangeMod().unmodifyMult(PhaseField.MOD_KEY);

        if (fleet.isTransponderOn()) {
            fleet.getStats().getDetectedRangeMod().unmodifyMult(EXTENDED_PHASE_FIELD_MOD_ID);
            return;
        }

        List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
        float[] profiles = new float[members.size()];
        List<Float> phaseSensorValues = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            FleetMemberAPI member = members.get(i);
            profiles[i] = member.getStats().getSensorProfile().getModifiedValue();
            Float contribution = phaseSensorContribution(member);
            if (contribution != null) {
                phaseSensorValues.add(contribution);
            }
        }

        if (phaseSensorValues.isEmpty()) {
            fleet.getStats().getDetectedRangeMod().unmodifyMult(EXTENDED_PHASE_FIELD_MOD_ID);
            return;
        }

        float[] phaseSensors = new float[phaseSensorValues.size()];
        for (int i = 0; i < phaseSensorValues.size(); i++) {
            phaseSensors[i] = phaseSensorValues.get(i);
        }

        int topShips = Global.getSettings().getInt("maxSensorShips");
        float totalProfile = PhaseField.getTopKValuesSum(profiles, topShips);
        float totalPhaseSensors = PhaseField.getTopKValuesSum(phaseSensors, topShips);
        float total = Math.max(totalProfile + totalPhaseSensors, 1f);
        float mult = Math.max(PhaseField.MIN_FIELD_MULT, Math.min(1f, totalProfile / total));

        fleet.getStats().getDetectedRangeMod()
                .modifyMult(EXTENDED_PHASE_FIELD_MOD_ID, mult, Translation.gameText("modifier.phaseSensorNetworks"));
    }

    private static Float phaseSensorContribution(FleetMemberAPI member) {
        if (member.isMothballed() || member.getRepairTracker().getCR() < PhaseField.MIN_CR) {
            return null;
        }
        if (member.getVariant().hasHullMod("phasefield")) {
            return member.getStats().getSensorStrength().getModifiedValue();
        }
        float contributionPercent = member.getStats().getDynamic().getValue(PHASE_FIELD_CONTRIBUTION_KEY, 0f);
        if (contributionPercent <= 0f) {
            return null;
        }
        return member.getStats().getSensorStrength().getModifiedValue() * contributionPercent / 100f;
    }
}
