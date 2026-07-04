package hashduck.colored_nether_portals.util;

import hashduck.colored_nether_portals.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;

public class PortalColorSavedData extends SavedData {
    private static final String DATA_NAME = Constants.MOD_ID;
    private static final String TAG_PORTAL_COLORS = "portal_colors";
    private static final String TAG_POS = "pos";
    private static final String TAG_COLOR = "color";

    private final Map<BlockPos, DyeColor> colors = new HashMap<>();

    public PortalColorSavedData() {
        super();
    }


    public static final Factory<PortalColorSavedData> FACTORY = new Factory<>(PortalColorSavedData::new, PortalColorSavedData::load, null);


    public static PortalColorSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
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

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        ListTag listTag = new ListTag();

        colors.forEach((pos, color) -> {
            CompoundTag entry = new CompoundTag();
            entry.putLong(TAG_POS, pos.asLong());
            entry.putInt(TAG_COLOR, color.getId());
            listTag.add(entry);
        });

        compoundTag.put(TAG_PORTAL_COLORS, listTag);
        return compoundTag;
    }

    public static PortalColorSavedData load(CompoundTag compoundTag, HolderLookup.Provider provider) {
        PortalColorSavedData data = new PortalColorSavedData();
        ListTag listTag = compoundTag.getList(TAG_PORTAL_COLORS, Tag.TAG_COMPOUND);

        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag entry = listTag.getCompound(i);
            BlockPos pos = BlockPos.of(entry.getLong(TAG_POS));
            DyeColor color = DyeColor.byId(entry.getInt(TAG_COLOR));
            data.colors.put(pos, color);
        }

        return data;
    }
}