package second_in_command;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;

import java.util.HashSet;
import java.util.Set;

public final class SICUtils {

    public static final Set<String> ACTIVE_SKILLS = new HashSet<>();

    private SICUtils() {
    }

    public static SICData getFleetData() {
        return new SICData();
    }
}
