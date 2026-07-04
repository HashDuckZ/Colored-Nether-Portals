package hashduck.colored_nether_portals;

import hashduck.colored_nether_portals.client.PortalColorClientCache;
import hashduck.colored_nether_portals.util.DyeColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.List;

/**
 * Sets the portal block to translucent and registering block color handlers that apply dimensional data from the client cache.
 */
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ColoredNetherPortalsClient {

    /**
     * Sets the color of the portal from the cache
     */
    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return 0xFFFFFFFF; // opaque white — alpha is required since 26.1
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
                return 0xFFFFFFFF; // opaque white — alpha is required since 26.1
            }
        }), ColoredNetherPortals.COLORED_PORTAL.get());
    }
}
