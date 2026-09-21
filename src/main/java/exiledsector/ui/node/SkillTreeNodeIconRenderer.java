package exiledsector.ui.node;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import exiledsector.skills.SkillTree;
import exiledsector.skills.SkillType;
import exiledsector.ui.util.SpriteCache;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

final class SkillTreeNodeIconRenderer {

    private static final float PIE_START_ANGLE_DEGREES = -90f;
    private static final float PIE_WEDGE_SEGMENT_DEGREES = 6f;

    private final SpriteCache spriteCache = new SpriteCache(SkillTreeNodeIconRenderer.class);

    void drawIcon(String spritePath, float cx, float cy, float size, float alphaMult, Color tint) {
        if (!spriteCache.ensureLoaded(spritePath)) return;

        SpriteAPI sprite = Global.getSettings().getSprite(spritePath);
        sprite.setSize(size, size);
        sprite.setAlphaMult(alphaMult);
        sprite.setColor(tint);
        sprite.renderAtCenter(cx, cy);
    }

    void drawSplitIcon(SkillType optionalType, float cx, float cy, float size, float alphaMult, Color tint) {
        List<SkillType> options = optionTypesOf(optionalType);
        if (options.isEmpty()) return;
        if (options.size() == 1) {
            drawIcon(options.get(0).getIconPath(), cx, cy, size, alphaMult, tint);
            return;
        }

        float radius = size / 2f;
        float sweep = 360f / options.size();

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        for (int i = 0; i < options.size(); i++) {
            float startAngle = PIE_START_ANGLE_DEGREES + sweep * i;
            float endAngle = startAngle + sweep;

            maskPieWedge(cx, cy, radius, startAngle, endAngle, 1);
            GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            drawIcon(options.get(i).getIconPath(), cx, cy, size, alphaMult, tint);
            maskPieWedge(cx, cy, radius, startAngle, endAngle, 0);
        }
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    private List<SkillType> optionTypesOf(SkillType optionalType) {
        List<SkillType> options = new ArrayList<>();
        for (String optionId : optionalType.getOptionalOptionIds()) {
            SkillType option = SkillTree.getType(optionId);
            if (option != null) options.add(option);
        }
        return options;
    }

    private void maskPieWedge(float cx, float cy, float radius, float startDeg, float endDeg, int stencilValue) {
        GL11.glColorMask(false, false, false, false);
        GL11.glStencilFunc(GL11.GL_ALWAYS, stencilValue, 0xFF);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);

        int segments = Math.max(1, (int) Math.ceil((endDeg - startDeg) / PIE_WEDGE_SEGMENT_DEGREES));
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(cx, cy);
        for (int i = 0; i <= segments; i++) {
            float deg = startDeg + (endDeg - startDeg) * i / segments;
            float rad = (float) Math.toRadians(deg);
            GL11.glVertex2f(cx + (float) Math.cos(rad) * radius, cy + (float) Math.sin(rad) * radius);
        }
        GL11.glEnd();

        GL11.glColorMask(true, true, true, true);
    }
}
