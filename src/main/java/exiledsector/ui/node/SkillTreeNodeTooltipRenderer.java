package exiledsector.ui.node;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exiledsector.persistence.ShipSkillDataManager;
import exiledsector.skills.DescriptionLine;
import exiledsector.skills.ShipSkillData;
import exiledsector.skills.SkillNode;
import exiledsector.skills.SkillType;
import exiledsector.skills.SkillTypeUnlockStatus;
import exiledsector.ui.SkillTreePanelStyle;
import exiledsector.ui.SkillTreeTooltipTable;
import exiledsector.ui.TooltipTable;
import exiledsector.ui.util.CachedText;
import org.lazywizard.lazylib.ui.LazyFont;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static exiledsector.ui.SkillTreePanelStyle.TOOLTIP_BODY_COLOR;
import static exiledsector.ui.SkillTreePanelStyle.TOOLTIP_BODY_FONT_SIZE;
import static exiledsector.ui.SkillTreePanelStyle.TOOLTIP_MAX_TEXT_WIDTH;
import static exiledsector.ui.SkillTreePanelStyle.TOOLTIP_TITLE_COLOR;
import static exiledsector.ui.SkillTreePanelStyle.TOOLTIP_TITLE_FONT_SIZE;

final class SkillTreeNodeTooltipRenderer {

    private static final float TOOLTIP_MAX_TEXT_HEIGHT = 800f;

    private static final String OPTIONAL_NODE_HINT = "Click to choose an option.";
    private static final String FREE_NODE_NOTE = "Granted free by a level-up.";
    private static final String LOCKED_NODE_TITLE = "Unidentified";
    private static final String LOCKED_NODE_BODY = "Unidentified - explore the sector to discover this node";
    private static final String LOCKED_NODE_HINT = "Hullmod blueprints would be a good place to start searching.";

    private final FleetMemberAPI member;
    private final SkillTreePanelStyle style;
    private final CachedText<String, SkillTreePanelStyle.TooltipText> tooltipTitles = new CachedText<>();
    private final CachedText<String, SkillTreePanelStyle.TooltipText> tooltipBodies = new CachedText<>();
    private final CachedText<String, SkillTreePanelStyle.TooltipText> typeTooltipTitles = new CachedText<>();
    private final CachedText<String, SkillTreePanelStyle.TooltipText> typeTooltipBodies = new CachedText<>();
    private final Map<String, List<SkillTreeTooltipTable>> tablesByType = new HashMap<>();

    SkillTreeNodeTooltipRenderer(FleetMemberAPI member, SkillTreePanelStyle style) {
        this.member = member;
        this.style = style;
    }

    void renderTooltip(SkillNode node, float mouseX, float mouseY, float alphaMult) {
        LazyFont font = style.getFont();
        if (font == null) return;

        ShipSkillData data = ShipSkillDataManager.get(member.getId());
        SkillType effectiveType = node.resolveEffectiveType(data);
        boolean showOptionalHint = effectiveType == node.getType() && effectiveType.isOptional()
                && effectiveType.getDescriptionOverride() == null;

        String titleText = titleText(node, effectiveType, data);
        List<DescriptionLine> bodyLines = bodyLines(node, effectiveType, showOptionalHint, data);
        String bodyText = joined(bodyLines);

        SkillTreePanelStyle.TooltipText title = tooltipTitles.get(node.getId(), titleText,
                id -> buildTooltipText(font, titleText, TOOLTIP_TITLE_FONT_SIZE, TOOLTIP_TITLE_COLOR));
        SkillTreePanelStyle.TooltipText body = tooltipBodies.get(node.getId(), bodyText,
                id -> buildBodyText(font, bodyLines));

        boolean showTables = !showOptionalHint && !SkillTypeUnlockStatus.isHidden(node.getType(), data);
        List<SkillTreeTooltipTable> tables = showTables ? tablesFor(font, effectiveType) : List.of();
        style.drawTitleBodyTooltip(title, body, tables, mouseX, mouseY, alphaMult);
    }

    void renderTooltipForType(SkillType type, float mouseX, float mouseY, float alphaMult) {
        LazyFont font = style.getFont();
        if (font == null) return;

        String titleText = type.getDisplayName();
        List<DescriptionLine> bodyLines = SkillNode.describeTypeLines(type, member.getHullSpec().getHullSize());
        String bodyText = joined(bodyLines);

        SkillTreePanelStyle.TooltipText title = typeTooltipTitles.get(type.getId(), titleText,
                id -> buildTooltipText(font, titleText, TOOLTIP_TITLE_FONT_SIZE, TOOLTIP_TITLE_COLOR));
        SkillTreePanelStyle.TooltipText body = typeTooltipBodies.get(type.getId(), bodyText,
                id -> buildBodyText(font, bodyLines));

        style.drawTitleBodyTooltip(title, body, tablesFor(font, type), mouseX, mouseY, alphaMult);
    }

    private List<SkillTreeTooltipTable> tablesFor(LazyFont font, SkillType type) {
        return tablesByType.computeIfAbsent(type.getId(), id -> {
            List<SkillTreeTooltipTable> measured = new ArrayList<>();
            for (TooltipTable table : HullModTooltipTables.forType(type, member.getHullSpec())) {
                measured.add(SkillTreeTooltipTable.measure(font, table, style.getAccentColor()));
            }
            return measured;
        });
    }

    private String titleText(SkillNode node, SkillType effectiveType, ShipSkillData data) {
        return SkillTypeUnlockStatus.isHidden(node.getType(), data) ? LOCKED_NODE_TITLE : effectiveType.getDisplayName();
    }

    private List<DescriptionLine> bodyLines(SkillNode node, SkillType effectiveType, boolean showOptionalHint, ShipSkillData data) {
        if (SkillTypeUnlockStatus.isHidden(node.getType(), data)) {
            return List.of(new DescriptionLine(LOCKED_NODE_BODY, false), new DescriptionLine(LOCKED_NODE_HINT, false));
        }
        List<DescriptionLine> lines = new ArrayList<>();
        if (showOptionalHint) {
            lines.add(new DescriptionLine(OPTIONAL_NODE_HINT, false));
        } else {
            lines.addAll(SkillNode.describeTypeLines(effectiveType, member.getHullSpec().getHullSize()));
        }
        if (data.isFreeNode(node.getId())) {
            lines.add(new DescriptionLine(FREE_NODE_NOTE, false));
        }
        return lines;
    }

    private static String joined(List<DescriptionLine> lines) {
        List<String> texts = new ArrayList<>();
        for (DescriptionLine line : lines) {
            texts.add(line.text());
        }
        return String.join("\n\n", texts);
    }

    private SkillTreePanelStyle.TooltipText buildBodyText(LazyFont font, List<DescriptionLine> lines) {
        return style.buildHighlightedWrappedText(font, lines, TOOLTIP_BODY_FONT_SIZE, TOOLTIP_MAX_TEXT_WIDTH,
                TOOLTIP_MAX_TEXT_HEIGHT, TOOLTIP_BODY_COLOR);
    }

    private SkillTreePanelStyle.TooltipText buildTooltipText(LazyFont font, String rawText, float fontSize, Color color) {
        return SkillTreePanelStyle.buildWrappedText(font, rawText, fontSize, TOOLTIP_MAX_TEXT_WIDTH, TOOLTIP_MAX_TEXT_HEIGHT, color);
    }
}
