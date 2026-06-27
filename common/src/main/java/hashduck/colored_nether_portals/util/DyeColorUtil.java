package hashduck.colored_nether_portals.util;

import net.minecraft.world.item.DyeColor;

/**
 * Shared class to get the dye color
 */
public final class DyeColorUtil {

    public static int getTintColor(DyeColor color) {
        int raw = color.getTextureDiffuseColor();
        int r = (raw >> 16) & 0xFF;
        int g = (raw >> 8) & 0xFF;
        int b = raw & 0xFF;

        float[] hsb = java.awt.Color.RGBtoHSB(r, g, b, null);
        if (hsb[1] < 0.05f) return raw;

        // Force saturation to 1.0, but keep vanilla's brightness
        return java.awt.Color.HSBtoRGB(hsb[0], 1.0f, hsb[2]) & 0xFFFFFF;
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