package hashduck.colored_nether_portals.blocks;

import hashduck.colored_nether_portals.mixin.PoiTypesAccessor;
import hashduck.colored_nether_portals.payload.PortalColorPayload;
import hashduck.colored_nether_portals.util.PortalColorSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Custom portal block
 */
public class ColoredNetherPortalBlock extends NetherPortalBlock {

    private static Block instance;

    public static void setInstance(Block block) {
        instance = block;
    }

    public static Block getInstance() {
        return instance;
    }

    public ColoredNetherPortalBlock(Properties properties) {
        super(properties);
    }

    public static void registerPortalPoi() {
        Block block = getInstance();
        if (block == null) {
            return;
        }
        Map<BlockState, Holder<PoiType>> typeByState = PoiTypesAccessor.getTypeByState();
        Holder<PoiType> netherPortalPoi =
                BuiltInRegistries.POINT_OF_INTEREST_TYPE.getHolderOrThrow(PoiTypes.NETHER_PORTAL);
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            typeByState.putIfAbsent(state, netherPortalPoi);
        }
    }

    //Clean up the color cache when a portal block is removed
    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level instanceof ServerLevel serverLevel) {
                PortalColorSavedData data = PortalColorSavedData.get(serverLevel);
                data.removeColor(pos);

                PortalColorPayload.sendRemoveToTrackingPlayers(serverLevel, pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}