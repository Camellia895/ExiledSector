package exiledsector.ui.inspect;

import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import exiledsector.effects.EnemyFleetLeveller;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class EnemyFleetInspectDialog implements CustomVisualDialogDelegate {

    public static final float WIDTH = 720f;
    public static final float HEIGHT = 620f;
    private static final float PAD = 10f;
    private static final float BUTTON_HEIGHT = 25f;
    private static final float BUTTON_WIDTH = 160f;
    private static final float ICON_SIZE = 64f;
    private static final String CLOSE = "exiledSector_inspectClose";

    private final List<CampaignFleetAPI> fleets;
    private final Runnable onDismissed;
    private DialogCallbacks callbacks;

    public EnemyFleetInspectDialog(List<CampaignFleetAPI> fleets, Runnable onDismissed) {
        this.fleets = List.copyOf(fleets);
        this.onDismissed = onDismissed;
    }

    @Override
    public void init(CustomPanelAPI panel, DialogCallbacks callbacks) {
        this.callbacks = callbacks;
        float width = panel.getPosition().getWidth();
        float height = panel.getPosition().getHeight();

        TooltipMakerAPI content = panel.createUIElement(width, height - BUTTON_HEIGHT - PAD * 2f, true);
        for (CampaignFleetAPI fleet : fleets) {
            addFleet(content, fleet);
        }
        panel.addUIElement(content).inTL(0f, 0f);

        TooltipMakerAPI buttons = panel.createUIElement(width, BUTTON_HEIGHT, false);
        buttons.addButton("Close", CLOSE, BUTTON_WIDTH, BUTTON_HEIGHT, 0f);
        panel.addUIElement(buttons).inBL((width - BUTTON_WIDTH) / 2f, PAD);
    }

    static List<FleetMemberAPI> levelledMembers(CampaignFleetAPI fleet) {
        List<FleetMemberAPI> levelled = new ArrayList<>();
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (ShipTreeLookup.isLevelledEnemy(member)) {
                levelled.add(member);
            }
        }
        return levelled;
    }

    private static void addFleet(TooltipMakerAPI content, CampaignFleetAPI fleet) {
        EnemyFleetLeveller.ensure(fleet);
        List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
        List<FleetMemberAPI> levelled = levelledMembers(fleet);
        content.addSectionHeading(fleet.getFullName(), Alignment.MID, PAD);
        content.addPara("%s of %s ships have Exiled Sector skill trees.", PAD, Misc.getHighlightColor(),
                String.valueOf(levelled.size()), String.valueOf(members.size()));
        for (FleetMemberAPI member : levelled) {
            ShipTreeLookup.ShipTree tree = ShipTreeLookup.find(member);
            if (tree == null) {
                continue;
            }
            content.addPara("%s", PAD * 2f, Misc.getBasePlayerColor(), shipTitle(member));
            content.addShipList(1, 1, ICON_SIZE, fleet.getFaction().getBaseUIColor(), List.of(member), PAD / 2f);
            ShipTreeSummaryRenderer.render(content, member, tree, PAD / 2f);
        }
    }

    private static String shipTitle(FleetMemberAPI member) {
        String hull = member.getHullSpec().getHullNameWithDashClass();
        String name = member.getShipName();
        return name == null || name.isBlank() ? hull : name + ", " + hull;
    }

    @Override
    public CustomUIPanelPlugin getCustomPanelPlugin() {
        return new BaseCustomUIPanelPlugin() {
            @Override
            public void buttonPressed(Object buttonId) {
                if (CLOSE.equals(buttonId)) {
                    close();
                }
            }

            @Override
            public void processInput(List<InputEventAPI> events) {
                for (InputEventAPI event : events) {
                    if (!event.isConsumed() && event.isKeyDownEvent() && event.getEventValue() == Keyboard.KEY_ESCAPE) {
                        event.consume();
                        close();
                    }
                }
            }
        };
    }

    private void close() {
        if (callbacks != null) {
            callbacks.dismissDialog();
        }
    }

    @Override
    public float getNoiseAlpha() {
        return 0f;
    }

    @Override
    public void advance(float amount) {
    }

    @Override
    public void reportDismissed(int option) {
        if (onDismissed != null) {
            onDismissed.run();
        }
    }
}
