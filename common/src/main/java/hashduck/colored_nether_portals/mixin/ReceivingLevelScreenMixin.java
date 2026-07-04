package hashduck.colored_nether_portals.mixin;

import hashduck.colored_nether_portals.blocks.ColoredNetherPortalBlock;
import hashduck.colored_nether_portals.client.PortalColorClientCache;
import hashduck.colored_nether_portals.client.PortalColorTracker;
import hashduck.colored_nether_portals.util.DyeColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


/**
 * Changes the nether loading screen portal texture
 */
@Mixin(ReceivingLevelScreen.class)
public class ReceivingLevelScreenMixin {

    // Swape the texture returned in getNetherPortalSprite with the gray texture if the player last entered a colored portal
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

    // Before showing the background image, change the color to the last entered portal color
    @Inject(method = "renderBackground", at = @At("HEAD"))
    private void applyLoadingColor(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        DyeColor color = PortalColorTracker.getActiveColor(player.getUUID());

        if (color != null) {
            float[] rgb = DyeColorUtil.getFireworkRgb(color);
            guiGraphics.setColor(rgb[0], rgb[1], rgb[2], 1.0F);
        }
    }

    // Reset the gui color to white after showing the background image
    @Inject(method = "renderBackground", at = @At("RETURN"))
    private void clearLoadingColor(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}