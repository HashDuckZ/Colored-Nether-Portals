package hashduck.colored_nether_portals;

import hashduck.colored_nether_portals.blocks.ColoredNetherPortalBlock;
import hashduck.colored_nether_portals.util.PortalColorManager;
import hashduck.colored_nether_portals.util.PortalColorSavedData;
import hashduck.colored_nether_portals.util.PortalTeleportQueue;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Main entry point for registering portal blocks and handling player interaction and data synchronization
 */
public class ColoredNetherPortals implements ModInitializer {

    private static MinecraftServer serverInstance;

    public static MinecraftServer getServer() {
        return serverInstance;
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            serverInstance = server;
        });

        FabricNetworking.registerCommon();

        ResourceKey<Block> blockKey = ResourceKey.create(
                Registries.BLOCK,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "colored_nether_portal"));

        ColoredNetherPortalBlock block = new ColoredNetherPortalBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_PORTAL).setId(blockKey));

        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
        ColoredNetherPortalBlock.setInstance(block);
        ColoredNetherPortalBlock.registerPortalPoi();

        /**
         * Registers a callback to handle players right-clicking blocks, allowing them to dye portals using items in their hand.
         */
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            var stack = player.getItemInHand(hand);
            var pos = hitResult.getBlockPos();
            var state = world.getBlockState(pos);

            if (PortalColorManager.tryDyePortal(world, pos, state, player, stack)) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });

        /**
         * Sends all existing portal color data to players when they join the server to ensure their client is synchronized
         */
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var level = handler.getPlayer().level();
            var data = PortalColorSavedData.get(level);

            var colors = data.getAllColors();
            if (!colors.isEmpty()) {
                FabricNetworking.sendToPlayer(handler.getPlayer(), colors);
            }
        });

        /**
         * Drops any pending teleport color so a disconnect mid-teleport can't leave a stale entry.
         */
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                PortalTeleportQueue.clear(handler.getPlayer().getUUID()));

        /**
         * Sends updated color portal data when a player changes worlds
         */
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            var data = PortalColorSavedData.get(destination);

            var colors = data.getAllColors();
            if (!colors.isEmpty()) {
                FabricNetworking.sendToPlayer(player, colors);
            }
        });
    }
}