package hashduck.colored_nether_portals.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import hashduck.colored_nether_portals.blocks.ColoredNetherPortalBlock;
import hashduck.colored_nether_portals.client.PortalColorTracker;
import hashduck.colored_nether_portals.util.DyeColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Changes the nether loading screen portal texture
 */
@Mixin(LevelLoadingScreen.class)
public class LevelLoadingScreenMixin {

    // Swap the texture returned in getNetherPortalSprite with the gray texture if the player last entered a colored portal
    @Inject(method = "getNetherPortalSprite", at = @At("HEAD"), cancellable = true)
    private void changeLoadingTexture(CallbackInfoReturnable<TextureAtlasSprite> cir) {
        var player = Minecraft.getInstance().player;
        if (player == null || ColoredNetherPortalBlock.getInstance() == null) {
            return;
        }

        DyeColor color = PortalColorTracker.getActiveColor(player.getUUID());

        if (color != null) {
            TextureAtlasSprite customSprite = Minecraft.getInstance().getBlockRenderer()
                    .getBlockModelShaper().getParticleIcon(ColoredNetherPortalBlock.getInstance().defaultBlockState());
            cir.setReturnValue(customSprite);
        }
    }

    // Tint the background portal texture with the last entered portal's color
    @Redirect(
            method = "renderBackground",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;IIII)V")
    )
    private void applyLoadingColor(GuiGraphics guiGraphics, RenderPipeline pipeline, TextureAtlasSprite sprite, int x, int y, int width, int height) {
        var player = Minecraft.getInstance().player;
        DyeColor color = player != null ? PortalColorTracker.getActiveColor(player.getUUID()) : null;

        if (color != null) {
            float[] rgb = DyeColorUtil.getFireworkRgb(color);
            guiGraphics.blitSprite(pipeline, sprite, x, y, width, height, ARGB.colorFromFloat(1.0F, rgb[0], rgb[1], rgb[2]));
        } else {
            guiGraphics.blitSprite(pipeline, sprite, x, y, width, height);
        }
    }
}
