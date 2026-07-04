package hashduck.colored_nether_portals;

import hashduck.colored_nether_portals.blocks.ColoredNetherPortalBlock;
import hashduck.colored_nether_portals.client.PortalColorClientCache;
import hashduck.colored_nether_portals.util.DyeColorUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Initializes client-side rendering, color providers, and networking for the portal blocks.
 */
public class ColoredNetherPortalsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FabricNetworking.registerClient();

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> PortalColorClientCache.clear());

        BlockColorRegistry.register(List.of(new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return 0xFFFFFFFF; 
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                var mc = Minecraft.getInstance();
                if (mc.level != null) {
                    DyeColor color = PortalColorClientCache.get(mc.level, pos);
                    if (color != null) {
                        return DyeColorUtil.getTintColor(color);
                    }
                }
                return 0xFFFFFFFF; 
            }
        }), ColoredNetherPortalBlock.getInstance());
    }
}