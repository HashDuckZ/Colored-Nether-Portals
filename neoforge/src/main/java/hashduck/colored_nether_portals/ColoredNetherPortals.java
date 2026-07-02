package hashduck.colored_nether_portals;

import hashduck.colored_nether_portals.blocks.ColoredNetherPortalBlock;
import hashduck.colored_nether_portals.util.PortalColorManager;
import hashduck.colored_nether_portals.util.PortalColorSavedData;
import hashduck.colored_nether_portals.util.PortalTeleportQueue;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;

/**
 * Main mod class handles block registration, player interactions for dyeing portals, and sending data when players join the server.
 */
@Mod(Constants.MOD_ID)
public class ColoredNetherPortals {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Constants.MOD_ID);

    public static final DeferredBlock<Block> COLORED_PORTAL = BLOCKS.registerBlock("colored_nether_portal",
            ColoredNetherPortalBlock::new,
            BlockBehaviour.Properties.of()
                    .noCollision()
                    .randomTicks()
                    .strength(-1.0F)
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 11)
                    .pushReaction(PushReaction.BLOCK));

    public ColoredNetherPortals(IEventBus modBus) {
        BLOCKS.register(modBus);

        modBus.addListener(this::commonSetup);
        modBus.addListener(NeoForgeNetworking::onRegisterPayloads);

        NeoForgeNetworking.registerSender();

        NeoForge.EVENT_BUS.addListener(ColoredNetherPortals::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(ColoredNetherPortals::onPlayerJoin);
        NeoForge.EVENT_BUS.addListener(ColoredNetherPortals::onPlayerChangeDimension);
        NeoForge.EVENT_BUS.addListener(ColoredNetherPortals::onPlayerLogout);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ColoredNetherPortalBlock.setInstance(COLORED_PORTAL.get());
        event.enqueueWork(ColoredNetherPortalBlock::registerPortalPoi);
    }

    /**
     * Handles the interaction when a player uses dye on a portal block.
     */
private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
    if (PortalColorManager.tryDyePortal(
            event.getLevel(),
            event.getPos(),
            event.getLevel().getBlockState(event.getPos()),
            event.getEntity(),
            event.getItemStack())) {

        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}

    /**
     * Syncs existing portal colors to the client when they join the server.
     */
    private static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PortalColorSavedData data = PortalColorSavedData.get(player.level());

            var colors = data.getAllColors();
            if (!colors.isEmpty()) {
                NeoForgeNetworking.sendToPlayer(player, colors);
            }
        }
    }

    /**
     * Drops any pending teleport color so a disconnect mid-teleport can't leave a stale entry.
     */
    private static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PortalTeleportQueue.clear(event.getEntity().getUUID());
    }

    /**
     * Sends updated color portal data when a player changes worlds
     */
    private static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PortalColorSavedData data = PortalColorSavedData.get(player.level());

            var colors = data.getAllColors();
            if (!colors.isEmpty()) {
                NeoForgeNetworking.sendToPlayer(player, colors);
            }
        }
    }
}