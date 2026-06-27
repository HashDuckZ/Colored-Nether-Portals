package hashduck.colored_nether_portals;

import hashduck.colored_nether_portals.blocks.ColoredNetherPortalBlock;
import hashduck.colored_nether_portals.client.PortalColorClientCache;
import hashduck.colored_nether_portals.util.DyeColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/**
 * Sets the portal block to translucent and registering block color handlers that apply dimensional data from the client cache.
 */
@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ColoredNetherPortalsClient {

    /**
     * Sets the portal block to be translucent so the grayscale texture correctly blends with the background colors.
     */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemBlockRenderTypes.setRenderLayer(
                ColoredNetherPortalBlock.getInstance(),
                RenderType.translucent()
        ));
    }

    /**
     * Sets the color of the portal from the cache
     */
    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, view, pos, tintIndex) -> {
            if (pos == null) {
                return 0xFFFFFF;
            }
            var mc = Minecraft.getInstance();

            if (mc.level != null) {
                DyeColor color = PortalColorClientCache.get(mc.level, pos);

                if (color != null) {
                    return DyeColorUtil.getTintColor(color);
                }
            }

            return 0xFFFFFF;
        }, ColoredNetherPortals.COLORED_PORTAL.get());
    }
}