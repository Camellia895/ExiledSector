package exiledsector.ui.inspect;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import exiledsector.skills.DescriptionLine;
import exiledsector.skills.SkillTreeBonusSummary;
import exiledsector.skills.SkillTreeBonusSummary.Summary;
import exiledsector.skills.SkillType;
import exiledsector.ui.SkillTreePanelStyle;
import exiledsector.ui.TooltipHighlighter;
import exiledsector.ui.TooltipHighlighter.Span;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public final class ShipTreeSummaryRenderer {

    private static final float LINE_PAD = 3f;
    private static final float SECTION_PAD = 10f;
    private static final String BULLET = "    - ";

    private ShipTreeSummaryRenderer() {
    }

    public static void render(TooltipMakerAPI info, FleetMemberAPI member, ShipTreeLookup.ShipTree tree, float pad) {
        Summary summary = SkillTreeBonusSummary.of(tree.data(), member.getHullSpec().getHullSize());
        Color highlight = Misc.getHighlightColor();

        List<String> parts = new ArrayList<>();
        parts.add("Level " + summary.level());
        if (tree.layoutName() != null) {
            parts.add(tree.layoutName() + " build");
        }
        if (summary.root() != null) {
            parts.add(summary.root().getDisplayName() + " start");
        }
        parts.add(summary.nodeCount() + (summary.nodeCount() == 1 ? " node" : " nodes"));
        String header = String.join("  |  ", parts);
        info.addPara("%s", pad, highlight, header);

        if (summary.notables().isEmpty()) {
            info.addPara("No notables or keystones.", Misc.getGrayColor(), LINE_PAD);
        } else {
            List<String> names = new ArrayList<>();
            for (SkillType notable : summary.notables()) {
                names.add(notable.getDisplayName());
            }
            LabelAPI label = info.addPara("%s", LINE_PAD, Misc.getTextColor(), "Notables and keystones: " + String.join(", ", names));
            label.setHighlight(names.toArray(new String[0]));
            label.setHighlightColor(highlight);
        }

        if (!summary.bonuses().isEmpty()) {
            info.addPara("Bonuses:", SECTION_PAD);
            for (DescriptionLine line : summary.bonuses()) {
                addColouredLine(info, BULLET + line.text(), line.lowerIsBetter());
            }
        }
    }

    private static void addColouredLine(TooltipMakerAPI info, String text, boolean lowerIsBetter) {
        LabelAPI label = info.addPara("%s", LINE_PAD, Misc.getTextColor(), text);
        List<Span> spans = TooltipHighlighter.find(text, lowerIsBetter);
        if (spans.isEmpty()) {
            return;
        }
        String[] substrings = new String[spans.size()];
        Color[] colors = new Color[spans.size()];
        for (int i = 0; i < spans.size(); i++) {
            Span span = spans.get(i);
            substrings[i] = text.substring(span.start(), span.end());
            colors[i] = colorFor(span.highlight());
        }
        label.setHighlight(substrings);
        label.setHighlightColors(colors);
    }

    private static Color colorFor(TooltipHighlighter.Highlight highlight) {
        return switch (highlight) {
            case POSITIVE -> SkillTreePanelStyle.POSITIVE_STAT_COLOR;
            case NEGATIVE -> SkillTreePanelStyle.NEGATIVE_STAT_COLOR;
            default -> Misc.getHighlightColor();
        };
    }
}
