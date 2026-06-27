package hashduck.colored_nether_portals.client;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the client-side registry of colored portal blocks and active rendering states.
 * */
public final class PortalColorClientCache {

    private static final Map<ResourceKey<Level>, Map<BlockPos, DyeColor>> DIMENSIONAL_CACHE = new ConcurrentHashMap<>();

    public static void set(ResourceKey<Level> dimension, BlockPos pos, @Nullable DyeColor color) {
        var levelMap = DIMENSIONAL_CACHE.computeIfAbsent(dimension, k -> new ConcurrentHashMap<>());

        if (color == null) {
            levelMap.remove(pos);
        } else {
            levelMap.put(pos, color);
        }
    }


    public static @Nullable DyeColor get(Level level, BlockPos pos) {
        var levelMap = DIMENSIONAL_CACHE.get(level.dimension());
        return (levelMap != null) ? levelMap.get(pos) : null;
    }

    public static void clear() {
        DIMENSIONAL_CACHE.clear();
    }
}