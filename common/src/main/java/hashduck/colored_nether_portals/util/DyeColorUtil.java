package hashduck.colored_nether_portals.util;

import net.minecraft.world.item.DyeColor;

/**
 * Shared class to get the dye color
 */
public final class DyeColorUtil {

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