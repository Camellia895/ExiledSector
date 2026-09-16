package exiledsector.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import lunalib.lunaUI.elements.LunaSpriteElement;

/**
 * Renders a sprite as a circular-vignetted icon (see CircularVignettePlugin)
 * at any size/position within a host panel. Pulled out as a reusable helper
 * since the skill tree will have many node icons, not just the one central
 * ship symbol this was first built for - every caller gets the same masking
 * behavior (including the padding workaround for sprites rendering wider
 * than their nominal box) without repeating the wiring.
 */
public final class VignettedIcon {

    // Kept proportional to icon size (rather than a fixed pixel value) so
    // small node icons and the larger central symbol both get a sensibly
    // sized safety margin - this ratio matches what was tuned for the
    // original 128px symbol (48px padding).
    private static final float PADDING_FRACTION = 0.375f;

    private VignettedIcon() {
    }

    /**
     * @param host    panel the icon is added to
     * @param spritePath asset path of the image to render
     * @param iconSize   width/height of the visible (vignetted) icon
     * @param iconX      x of the icon's own top-left corner within host, not the padded mask
     * @param iconY      y of the icon's own top-left corner within host, not the padded mask
     */
    public static void addTo(CustomPanelAPI host, String spritePath, float iconSize, float iconX, float iconY) {
        float padding = iconSize * PADDING_FRACTION;
        float maskSize = iconSize + padding * 2f;

        TooltipMakerAPI element = host.createUIElement(maskSize, maskSize, false);
        host.addUIElement(element);
        element.getPosition().inTL(iconX - padding, iconY - padding);

        LunaSpriteElement sprite = new LunaSpriteElement(spritePath, LunaSpriteElement.ScalingTypes.STRETCH_SPRITE, element, iconSize, iconSize);
        sprite.getPosition().inTL(padding, padding);

        // Added after the sprite so it renders on top and actually covers
        // the icon's edges, rather than being hidden behind it.
        CustomPanelAPI vignette = Global.getSettings().createCustom(maskSize, maskSize, new CircularVignettePlugin(iconSize));
        element.addCustom(vignette, 0f).getPosition().inTL(0f, 0f);
    }
}
