package hashduck.colored_nether_portals.util;

import net.minecraft.world.item.DyeColor;

/**
 * Shared class to get the dye color
 */
public final class DyeColorUtil {

    // Uses the firework palette: vivid for colorful dyes and truly neutral for gray/black,
    // unlike the texture colors whose grays have a slight teal/blue cast.
    // Must be opaque ARGB: since 26.1 the alpha channel is multiplied into the quad color.
    public static int getTintColor(DyeColor color) {
        return 0xFF000000 | (color.getFireworkColor() & 0xFFFFFF);
    }

    public static float[] getFireworkRgb(DyeColor color) {
        int rgb = color.getFireworkColor();
        return new float[] {
                ((rgb >> 16) & 0xFF) / 255.0f,
                ((rgb >> 8) & 0xFF) / 255.0f,
                (rgb & 0xFF) / 255.0f
        };
    }
}