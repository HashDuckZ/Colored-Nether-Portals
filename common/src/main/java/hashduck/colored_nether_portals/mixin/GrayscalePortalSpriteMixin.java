package hashduck.colored_nether_portals.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import hashduck.colored_nether_portals.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Replaces the nether portal texture in the sprite list with a grayed version of the current resource pack's texture.
 *
 */

@Mixin(SpriteLoader.class)
public class GrayscalePortalSpriteMixin {

    @Unique
    private static final Identifier GRAYSCALE_PORTAL_TEXTURE =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "block/grayscale_portal");

    @Unique
    private static final Identifier VANILLA_PORTAL_TEXTURE =
            Identifier.withDefaultNamespace("textures/block/nether_portal.png");


    //Creates a gray texture of the nether portal texture from the current resource pack and applies it to our texture
    @ModifyVariable(method = "stitch", at = @At("HEAD"), argsOnly = true)
    private List<SpriteContents> applyGrayscaleTransformation(List<SpriteContents> originalContents) {
        List<SpriteContents> mutableContents = new java.util.ArrayList<>(originalContents);

        for (int i = 0; i < mutableContents.size(); i++) {
            SpriteContents sprite = mutableContents.get(i);

            if (sprite.name().equals(GRAYSCALE_PORTAL_TEXTURE)) {
                SpriteContents replacement = generateGrayPortalTexture();

                if (replacement != null) {
                    sprite.close();

                    mutableContents.set(i, replacement);
                }
                break;
            }
        }

        return mutableContents;
    }


    // Reads the current resource pack's nether portal texture and applies a grayscale transformation
    @Unique
    private static SpriteContents generateGrayPortalTexture() {
        try {
            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            Resource vanillaResource = resourceManager.getResourceOrThrow(VANILLA_PORTAL_TEXTURE);

            try (InputStream stream = vanillaResource.open();
                 NativeImage originalImage = NativeImage.read(stream)) {

                NativeImage grayscaleImage = desaturate(originalImage);

                ResourceMetadata metadata = vanillaResource.metadata();
                Optional<AnimationMetadataSection> animMeta = metadata.getSection(AnimationMetadataSection.TYPE);

                FrameSize frameSize = animMeta
                        .map(meta -> meta.calculateFrameSize(grayscaleImage.getWidth(), grayscaleImage.getHeight()))
                        .orElseGet(() -> new FrameSize(grayscaleImage.getWidth(), grayscaleImage.getHeight()));

                return new SpriteContents(
                        GRAYSCALE_PORTAL_TEXTURE,
                        frameSize,
                        grayscaleImage,
                        animMeta,
                        List.of(),
                        Optional.empty()
                );
            }
        } catch (IOException | IllegalStateException e) {
            Constants.LOGGER.error("Failed to generate grayscale portal sprite", e);
            return null;
        }
    }



    //Applies the gray transformation to the image
    @Unique
    private static NativeImage desaturate(NativeImage original) {
        int width = original.getWidth();
        int height = original.getHeight();
        NativeImage grayscale = new NativeImage(NativeImage.Format.RGBA, width, height, false);

        // Pass 1: find min/max luminance across the texture (skip transparent pixels)
        float minLum = 1.0f;
        float maxLum = 0.0f;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = original.getPixel(x, y); // ARGB
                int a = (pixel >> 24) & 0xFF;
                if (a == 0) continue;
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;
                float lum = (0.299f * r + 0.587f * g + 0.114f * b) / 255.0f;
                if (lum < minLum) minLum = lum;
                if (lum > maxLum) maxLum = lum;
            }
        }
        float range = Math.max(0.001f, maxLum - minLum);

        // Pass 2: stretch to 0..1, then apply curve
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = original.getPixel(x, y); // ARGB
                int a = (pixel >> 24) & 0xFF;
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;

                float luminance = (0.299f * r + 0.587f * g + 0.114f * b) / 255.0f;
                // Normalize to full 0..1 range
                float normalized = (luminance - minLum) / range;

                // Curve with glow floor — now actually reaches 255 at the top
                float adjusted = (float) Math.sqrt(0.20f + normalized * 0.80f);
                int grayValue = Math.min(255, (int) (adjusted * 255));
                int grayscalePixel = (a << 24) | (grayValue << 16) | (grayValue << 8) | grayValue;
                grayscale.setPixel(x, y, grayscalePixel);
            }
        }

        return grayscale;
    }
}