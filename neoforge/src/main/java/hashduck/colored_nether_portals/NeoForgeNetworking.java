package hashduck.colored_nether_portals;

import hashduck.colored_nether_portals.client.PortalColorTracker;
import hashduck.colored_nether_portals.payload.PortalColorPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The class handles network payload registration, sending portal data to joining players.
 */
public final class NeoForgeNetworking {

    /**
     * Registers the packet on the client side to receive color updates from the server.
     */
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Constants.MOD_ID).optional();

        registrar.playToClient(
                PortalColorPayload.TYPE,
                PortalColorPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    // 1. Update the Client Cache (The data storage)
                    PortalColorTracker.handlePayload(payload);

                    // 2. The Visual Update: Force a redraw of every portal block in the packet
                    var level = context.player().level();
                    for (BlockPos pos : payload.positions()) {
                        var state = level.getBlockState(pos);
                        // Flag 3 tells the renderer to discard the old chunk and redraw the block immediately
                        level.sendBlockUpdated(pos, state, state, 3);
                    }
                })
        );
    }

    /**
     * Registers the server-side logic to broadcast portal color updates to all players tracking the affected chunks.
     */
    public static void registerSender() {
        PortalColorPayload.registerSender((dim, positions, color, remove) -> {
            var server = ServerLifecycleHooks.getCurrentServer();
            var level = server.getLevel(dim);

            if (level != null && !positions.isEmpty()) {
                var payload = new PortalColorPayload(dim, new ArrayList<>(positions), color.getId(), remove);

                Set<Long> seenChunks = new HashSet<>();
                for (BlockPos pos : positions) {
                    if (seenChunks.add(ChunkPos.asLong(pos))) {
                        PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(pos), payload);
                    }
                }
            }
        });
    }

    /**
     * Sends the entire cache of portal colors to a player when they first join.
     */
    public static void sendToPlayer(ServerPlayer player, Map<BlockPos, DyeColor> colors) {
        if (colors.isEmpty()) return;

        // Group blocks by color to minimize the number of packets sent
        var byColor = new HashMap<DyeColor, List<BlockPos>>();
        colors.forEach((pos, color) -> byColor.computeIfAbsent(color, k -> new ArrayList<>()).add(pos));

        byColor.forEach((color, positions) -> {
            PortalColorPayload payload = new PortalColorPayload(
                    player.level().dimension(),
                    positions,
                    color.getId(),
                    false
            );

            PacketDistributor.sendToPlayer(player, payload);
        });
    }
}