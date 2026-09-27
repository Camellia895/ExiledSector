package exiledsector.ui;

import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.impl.items.ModSpecItemPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import exiledsector.skills.BlueprintNodeReveals;

public class BlueprintRevealModSpecItemPlugin extends ModSpecItemPlugin {

    static final String REVEAL_TEXT = "Learning this blueprint reveals the equivalent node on the ExiledSector ship skill tree.";
    private static final float PARAGRAPH_PAD = 10f;

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource) {
        super.createTooltip(tooltip, expanded, transferHandler, stackSource);
        if (BlueprintNodeReveals.revealsAnyNode(getModId())) {
            tooltip.addPara(REVEAL_TEXT, Misc.getHighlightColor(), PARAGRAPH_PAD);
        }
    }
}
