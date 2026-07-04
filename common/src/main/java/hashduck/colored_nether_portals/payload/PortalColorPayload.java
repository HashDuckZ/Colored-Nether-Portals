package hashduck.colored_nether_portals.payload;

import hashduck.colored_nether_portals.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.List;

/**
 * Coordinates the transmission of portal color data across specific dimensions.
 */
public record PortalColorPayload(ResourceKey<Level> dimension, List<BlockPos> positions, int colorId, boolean remove) implements CustomPacketPayload {

    public static final Type<PortalColorPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "sync_portal_color"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PortalColorPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), PortalColorPayload::dimension,
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), PortalColorPayload::positions,
            ByteBufCodecs.VAR_INT, PortalColorPayload::colorId,
            ByteBufCodecs.BOOL, PortalColorPayload::remove,
            PortalColorPayload::new
    );
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public DyeColor color() {
        return DyeColor.byId(colorId);
    }

    private static PacketSender SENDER;

    public static void registerSender(PacketSender sender) {
        SENDER = sender;
    }

    public static void sendToTrackingPlayers(ServerLevel level, Collection<BlockPos> positions, DyeColor color) {
        if (SENDER != null) {
            SENDER.send(level.dimension(), positions, color, false);
        }
    }

    public static void sendRemoveToTrackingPlayers(ServerLevel level, BlockPos pos) {
        if (SENDER != null) {
            SENDER.send(level.dimension(), List.of(pos), DyeColor.WHITE, true);
        }
    }

    @FunctionalInterface
    public interface PacketSender {
        void send(ResourceKey<Level> dimension, Collection<BlockPos> positions, DyeColor color, boolean remove);
    }
}