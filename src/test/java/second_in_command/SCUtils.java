package second_in_command;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;

import java.util.HashSet;
import java.util.Set;

public final class SCUtils {

    public static final Set<String> ACTIVE_SKILLS = new HashSet<>();

    private SCUtils() {
    }

    // S1172: signature must match the real SCUtils.getFleetData, which SecondInCommandCompat resolves by reflection
    @SuppressWarnings("java:S1172")
    public static SCData getFleetData(CampaignFleetAPI fleet) {
        return new SCData();
    }
}
