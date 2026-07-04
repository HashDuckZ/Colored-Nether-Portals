package hashduck.colored_nether_portals.mixin;

import hashduck.colored_nether_portals.blocks.ColoredNetherPortalBlock;
import hashduck.colored_nether_portals.client.PortalColorClientCache;
import hashduck.colored_nether_portals.client.PortalColorTracker;
import hashduck.colored_nether_portals.util.DyeColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Changes the color and texture of the nether portal texture that fades in while teleporting
 */
@Mixin(Gui.class)
public class PortalOverlayMixin {

    // Checks if the player is touching a colored portal and caches the color for rendering.
    @Inject(method = "renderPortalOverlay", at = @At("HEAD"))
    private void checkPortalColor(GuiGraphics guiGraphics, float alpha, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        var player = client.player;
        var coloredPortal = ColoredNetherPortalBlock.getInstance();

        if (player == null || client.level == null || coloredPortal == null) {
            return;
        }

        var uuid = player.getUUID();
        if (alpha <= 0.0F) {
            PortalColorTracker.clearActiveColor(uuid);
            return;
        }

        AABB box = player.getBoundingBox();
        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = client.level.getBlockState(pos);

            if (state.is(coloredPortal)) {
                DyeColor color = PortalColorClientCache.get(client.level, pos);
                if (color != null) {
                    PortalColorTracker.setActiveColor(uuid, color);
                    return;
                }
            } else if (state.is(Blocks.NETHER_PORTAL)) {
                PortalColorTracker.clearActiveColor(uuid);
                return;
            }
        }
    }

    //Returns the gray portal texture if the player last entered a colored portal
    @Redirect(
            method = "renderPortalOverlay",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockModelShaper;getParticleIcon(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;")
    )
    private TextureAtlasSprite redirectPortalSprite(BlockModelShaper instance, BlockState originalState) {
        Minecraft client = Minecraft.getInstance();
        var player = client.player; // Create local variable

        // Check it once here
        if (player == null || client.level == null || ColoredNetherPortalBlock.getInstance() == null) {
            return instance.getParticleIcon(originalState);
        }

        DyeColor active = PortalColorTracker.getActiveColor(player.getUUID());
        if (active != null) {
            return instance.getParticleIcon(ColoredNetherPortalBlock.getInstance().defaultBlockState());
        }
        return instance.getParticleIcon(originalState);
    }

    // Sets the color of the overlay to match the last entered portal's color
    @Redirect(
            method = "renderPortalOverlay",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;setColor(FFFF)V", ordinal = 0)
    )
    private void redirectPortalColor(GuiGraphics instance, float r, float g, float b, float a) {
        Minecraft client = Minecraft.getInstance();
        var player = client.player;

        if (player == null || client.level == null || ColoredNetherPortalBlock.getInstance() == null) {
            return;
        }
        DyeColor active = PortalColorTracker.getActiveColor(player.getUUID());

        if (active != null) {
            float[] color = DyeColorUtil.getFireworkRgb(active);
            instance.setColor(color[0], color[1], color[2], a);
        } else {
            instance.setColor(r, g, b, a);
        }
    }
}