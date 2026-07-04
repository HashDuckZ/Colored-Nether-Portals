package hashduck.colored_nether_portals;

import hashduck.colored_nether_portals.blocks.ColoredNetherPortalBlock;
import hashduck.colored_nether_portals.client.PortalColorClientCache;
import hashduck.colored_nether_portals.util.DyeColorUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.item.DyeColor;

/**
 * Initializes client-side rendering, color providers, and networking for the portal blocks.
 */
public class ColoredNetherPortalsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FabricNetworking.registerClient();

        BlockRenderLayerMap.putBlock(ColoredNetherPortalBlock.getInstance(), ChunkSectionLayer.TRANSLUCENT);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> PortalColorClientCache.clear());

        ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
            if (pos == null) return 0xFFFFFF;

            var mc = Minecraft.getInstance();
            if (mc.level != null) {
                DyeColor color = PortalColorClientCache.get(mc.level, pos);
                if (color != null) {
                    return DyeColorUtil.getTintColor(color);
                }
            }
            return 0xFFFFFF;
        }, ColoredNetherPortalBlock.getInstance());
    }
}