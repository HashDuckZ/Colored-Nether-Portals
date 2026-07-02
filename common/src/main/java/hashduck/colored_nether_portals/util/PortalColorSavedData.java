package hashduck.colored_nether_portals.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hashduck.colored_nether_portals.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortalColorSavedData extends SavedData {
    private static final String DATA_NAME = Constants.MOD_ID;
    private static final String TAG_PORTAL_COLORS = "portal_colors";

    private final Map<BlockPos, DyeColor> colors = new HashMap<>();

    public PortalColorSavedData() {
        super();
    }

=    private record Entry(long pos, int color) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.LONG.fieldOf("pos").forGetter(Entry::pos),
                Codec.INT.fieldOf("color").forGetter(Entry::color)
        ).apply(instance, Entry::new));
    }

    public static final Codec<PortalColorSavedData> CODEC = Entry.CODEC.listOf()
            .optionalFieldOf(TAG_PORTAL_COLORS, List.of())
            .xmap(PortalColorSavedData::fromEntries, PortalColorSavedData::toEntries)
            .codec();

    public static final SavedDataType<PortalColorSavedData> TYPE = new SavedDataType<>(
            DATA_NAME, PortalColorSavedData::new, CODEC, DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES);

    private static PortalColorSavedData fromEntries(List<Entry> entries) {
        PortalColorSavedData data = new PortalColorSavedData();
        for (Entry entry : entries) {
            data.colors.put(BlockPos.of(entry.pos()), DyeColor.byId(entry.color()));
        }
        return data;
    }

    private List<Entry> toEntries() {
        return colors.entrySet().stream()
                .map(e -> new Entry(e.getKey().asLong(), e.getValue().getId()))
                .toList();
    }

    public static PortalColorSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void setColor(BlockPos pos, DyeColor color) {
        colors.put(pos.immutable(), color);
        setDirty();
    }

    public void removeColor(BlockPos pos) {
        if (colors.remove(pos) != null) {
            setDirty();
        }
    }

    @Nullable
    public DyeColor getColor(BlockPos pos) {
        return colors.get(pos);
    }

    public Map<BlockPos, DyeColor> getAllColors() {
        return Map.copyOf(colors);
    }
}
