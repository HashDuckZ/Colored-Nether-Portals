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
                BuiltInRegistries.POINT_OF_INTEREST_TYPE.getOrThrow(PoiTypes.NETHER_PORTAL);
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            typeByState.putIfAbsent(state, netherPortalPoi);
        }
    }

    //Clean up the color cache when a portal block is removed (only called when the block actually changes to a different block)
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, boolean movedByPiston) {
        PortalColorSavedData data = PortalColorSavedData.get(level);
        data.removeColor(pos);

        PortalColorPayload.sendRemoveToTrackingPlayers(level, pos);
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }
}