package hashduck.colored_nether_portals.util;

import net.minecraft.world.item.DyeColor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores portal colors when changing dimensions
 */
public final class PortalTeleportQueue {
    private static final Map<UUID, DyeColor> QUEUE = new ConcurrentHashMap<>();

    public static void prepare(UUID uuid, DyeColor color) { QUEUE.put(uuid, color); }
    public static DyeColor getAndClear(UUID uuid) { return QUEUE.remove(uuid); }
    public static void clear(UUID uuid) { QUEUE.remove(uuid); }
}