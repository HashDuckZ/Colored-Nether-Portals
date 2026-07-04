package hashduck.colored_nether_portals;

import hashduck.colored_nether_portals.client.PortalColorClientCache;
import hashduck.colored_nether_portals.payload.PortalColorPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The class handles network payload registration, sending portal data to joining players.
 */
public final class FabricNetworking {

    /**
     * Registers the network payload and the server-to-client broadcasting logic
     */
    public static void registerCommon() {
        PayloadTypeRegistry.playS2C().register(PortalColorPayload.TYPE, PortalColorPayload.STREAM_CODEC);
        PortalColorPayload.registerSender((dimKey, positions, color, remove) -> {
            var server = ColoredNetherPortals.getServer();
            if (server == null || positions.isEmpty()) {
                return;
            }

            var serverLevel = server.getLevel(dimKey);
            if (serverLevel == null) {
                return;
            }
            PortalColorPayload payload = new PortalColorPayload(dimKey, new ArrayList<>(positions), color.getId(), remove);

            Set<Long> seenChunks = new HashSet<>();
            Set<ServerPlayer> recipients = new HashSet<>();
            for (BlockPos pos : positions) {
                if (seenChunks.add(ChunkPos.asLong(pos))) {
                    recipients.addAll(PlayerLookup.tracking(serverLevel, new ChunkPos(pos)));
                }
            }

            for (ServerPlayer player : recipients) {
                ServerPlayNetworking.send(player, payload);
            }
        });
    }

    /**
     * Registers the client-side receiver to update the portal color cache and trigger a visual redraw
     */
    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(PortalColorPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                DyeColor color = payload.remove() ? null : DyeColor.byId(payload.colorId());

                for (BlockPos pos : payload.positions()) {
                    PortalColorClientCache.set(payload.dimension(), pos, color);

                    if (!payload.remove() && context.client().level != null && context.client().level.dimension() == payload.dimension()) {
                        var state = context.client().level.getBlockState(pos);
                        context.client().level.sendBlockUpdated(pos, state, state, 3);
                    }
                }
            });
        });
    }

    /**
     * Sends the entire cache of portal colors to a player when they first join.
     */
    public static void sendToPlayer(ServerPlayer player, Map<BlockPos, DyeColor> colors) {
        var byColor = new HashMap<DyeColor, List<BlockPos>>();
        colors.forEach((pos, color) -> byColor.computeIfAbsent(color, k -> new ArrayList<>()).add(pos));

        byColor.forEach((color, positions) -> {
            PortalColorPayload payload = new PortalColorPayload(player.level().dimension(), positions, color.getId(), false);
            ServerPlayNetworking.send(player, payload);
        });
    }
}