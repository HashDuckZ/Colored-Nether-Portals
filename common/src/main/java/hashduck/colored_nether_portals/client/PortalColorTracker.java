package hashduck.colored_nether_portals.client;

import hashduck.colored_nether_portals.payload.PortalColorPayload;
import net.minecraft.world.item.DyeColor;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the last colored portal the player entered for the screen overlay
 */
public final class PortalColorTracker {
    private static final Map<UUID, DyeColor> ACTIVE_PLAYER_COLORS = new ConcurrentHashMap<>();

    public static void handlePayload(PortalColorPayload payload) {
        payload.positions().forEach(pos ->
                PortalColorClientCache.set(payload.dimension(), pos, payload.color())
        );
    }

    public static void setActiveColor(UUID playerUuid, DyeColor color) { ACTIVE_PLAYER_COLORS.put(playerUuid, color); }
    public static void clearActiveColor(UUID playerUuid) { ACTIVE_PLAYER_COLORS.remove(playerUuid); }
    public static DyeColor getActiveColor(UUID playerUuid) { return ACTIVE_PLAYER_COLORS.get(playerUuid); }
}