package hashduck.colored_nether_portals.util;

import hashduck.colored_nether_portals.payload.PortalColorPayload;
import hashduck.colored_nether_portals.blocks.ColoredNetherPortalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Used to set the color of connected portal blocks and sends it to the client
 */
public final class PortalColorManager {

    private static final int MAX_BLOCKS = 1024;

    public static boolean tryDyePortal(Level level, BlockPos pos, BlockState state, Player player, ItemStack stack) {
        if (!isPortalBlock(level, pos)) {
            return false;
        }

        if (!(stack.getItem() instanceof DyeItem dyeItem)) {
            return false;
        }

        DyeColor color = dyeItem.getDyeColor();

        if (level instanceof ServerLevel serverLevel) {

            var portalBlocks = getConnectedPortalBlocks(level, pos);
            PortalColorSavedData data = PortalColorSavedData.get(serverLevel);

            for (BlockPos portalPos : portalBlocks) {
                data.setColor(portalPos, color);

                BlockState currentState = level.getBlockState(portalPos);
                if (currentState.is(Blocks.NETHER_PORTAL)) {
                    var axis = currentState.getValue(NetherPortalBlock.AXIS);
                    BlockState coloredState = ColoredNetherPortalBlock.getInstance().defaultBlockState()
                            .setValue(NetherPortalBlock.AXIS, axis);
                    level.setBlock(portalPos, coloredState, 2 | 16 | 64);
                }
            }
            PortalColorPayload.sendToTrackingPlayers(serverLevel, portalBlocks, color);


            if (!player.isCreative()) {
                stack.shrink(1);
            }

            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);

        }

        return true;
    }

    public static Set<BlockPos> getConnectedPortalBlocks(Level level, BlockPos startPos) {
        Set<BlockPos> portalBlocks = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        Block coloredPortal = ColoredNetherPortalBlock.getInstance();
        queue.add(startPos);
        portalBlocks.add(startPos);

        while (!queue.isEmpty() && portalBlocks.size() < MAX_BLOCKS) {
            BlockPos current = queue.poll();

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);

                if (!portalBlocks.contains(neighbor)) {
                    BlockState neighborState = level.getBlockState(neighbor);

                    if (neighborState.is(Blocks.NETHER_PORTAL) || (coloredPortal != null && neighborState.is(coloredPortal))) {
                        portalBlocks.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        return portalBlocks;
    }


    public static boolean isPortalBlock(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.is(Blocks.NETHER_PORTAL)
                || (ColoredNetherPortalBlock.getInstance() != null
                && state.is(ColoredNetherPortalBlock.getInstance()));
    }
}
