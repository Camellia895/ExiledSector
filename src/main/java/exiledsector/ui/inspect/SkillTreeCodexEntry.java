package exiledsector.ui.inspect;

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.codex.CodexDialogAPI;
import com.fs.starfarer.api.impl.codex.CodexEntryV2;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.List;

public class SkillTreeCodexEntry extends CodexEntryV2 implements CustomUIPanelPlugin {

    public static final String ID_PREFIX = "exiledSector_skillTree_";
    static final String TITLE = "Exiled Sector skill tree";
    private static final float RELATED_ENTRIES_WIDTH = 290f;
    private static final float BOX_HORIZONTAL_PAD = 30f;
    private static final float PAD = 10f;

    private final ShipTreeLookup.ShipTree tree;

    public SkillTreeCodexEntry(String id, FleetMemberAPI member, ShipTreeLookup.ShipTree tree) {
        super(id, TITLE, null, member);
        this.tree = tree;
    }

    @Override
    public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
        info.addPara(TITLE, Misc.getBasePlayerColor(), 0f);
        info.addPara("Level " + tree.data().getLevel(), Misc.getGrayColor(), 0f);
    }

    @Override
    public boolean hasCustomDetailPanel() {
        return true;
    }

    @Override
    public CustomUIPanelPlugin getCustomPanelPlugin() {
        return this;
    }

    @Override
    public void createCustomDetail(CustomPanelAPI panel, UIPanelAPI relatedEntries, CodexDialogAPI codex) {
        float width = panel.getPosition().getWidth();
        float textWidth = width - RELATED_ENTRIES_WIDTH - PAD - BOX_HORIZONTAL_PAD + PAD;
        TooltipMakerAPI text = panel.createUIElement(textWidth, 0f, false);
        ShipTreeSummaryRenderer.render(text, (FleetMemberAPI) getParam(), tree, 0f);
        panel.updateUIElementSizeAndMakeItProcessInput(text);

        UIPanelAPI box = panel.wrapTooltipWithBox(text);
        panel.addComponent(box).inTL(0f, 0f);
        float height = box.getPosition().getHeight();
        if (relatedEntries != null) {
            panel.addComponent(relatedEntries).inTR(0f, 0f);
            height = Math.max(height, relatedEntries.getPosition().getHeight());
        }
        panel.getPosition().setSize(width, height);
    }

    @Override
    public void destroyCustomDetail() {
    }

    @Override
    public void positionChanged(PositionAPI position) {
    }

    @Override
    public void renderBelow(float alphaMult) {
    }

    @Override
    public void render(float alphaMult) {
    }

    @Override
    public void advance(float amount) {
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
    }

    @Override
    public void buttonPressed(Object buttonId) {
    }
}
