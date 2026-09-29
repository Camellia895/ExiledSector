package exiledsector.ui.inspect;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import exiledsector.i18n.I18n;
import exiledsector.i18n.Style;
import exiledsector.i18n.StyledText;
import exiledsector.i18n.Translation;
import exiledsector.skills.DescriptionLine;
import exiledsector.skills.SkillTreeBonusSummary;
import exiledsector.skills.SkillTreeBonusSummary.Summary;
import exiledsector.skills.SkillType;
import exiledsector.ui.VanillaText;

import java.util.ArrayList;
import java.util.List;

public final class ShipTreeSummaryRenderer {

    private static final float LINE_PAD = 3f;
    private static final float SECTION_PAD = 10f;
    private static final String BULLET = "    - ";

    private ShipTreeSummaryRenderer() {
    }

    public static void render(TooltipMakerAPI info, FleetMemberAPI member, ShipTreeLookup.ShipTree tree, float pad) {
        I18n.forGameText(() -> renderSummary(info, member, tree, pad));
    }

    private static void renderSummary(TooltipMakerAPI info, FleetMemberAPI member, ShipTreeLookup.ShipTree tree, float pad) {
        Summary summary = SkillTreeBonusSummary.of(tree.data(), member.getHullSpec().getHullSize());

        List<String> parts = new ArrayList<>();
        parts.add(Translation.msg("summary.level").arg("level", summary.level()).text());
        if (tree.layoutName() != null) {
            parts.add(Translation.msg("summary.build").arg("layout", tree.layoutName()).text());
        }
        if (summary.root() != null) {
            parts.add(Translation.msg("summary.start").arg("root", summary.root().getDisplayName()).text());
        }
        parts.add(Translation.msg("summary.nodes").count(summary.nodeCount()).text());
        info.addPara("%s", pad, Misc.getHighlightColor(), String.join(Translation.text("summary.separator"), parts));

        if (summary.notables().isEmpty()) {
            VanillaText.addPara(info, Translation.styled("summary.noNotables"), LINE_PAD, Misc.getGrayColor());
        } else {
            List<StyledText> names = new ArrayList<>();
            for (SkillType notable : summary.notables()) {
                names.add(StyledText.styled(notable.getDisplayName(), Style.HIGHLIGHT));
            }
            VanillaText.addPara(info, Translation.msg("summary.notables").arg("names", Translation.list(names)).styled(), LINE_PAD,
                    Misc.getTextColor());
        }

        if (!summary.bonuses().isEmpty()) {
            VanillaText.addPara(info, Translation.styled("summary.bonuses"), SECTION_PAD, Misc.getTextColor());
            for (DescriptionLine line : summary.bonuses()) {
                VanillaText.addPara(info, StyledText.of(BULLET).append(line.display()), LINE_PAD, Misc.getTextColor());
            }
        }
    }
}
