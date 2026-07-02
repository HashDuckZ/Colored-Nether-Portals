package hashduck.colored_nether_portals.mixin;

import hashduck.colored_nether_portals.util.PortalTeleportQueue;
import hashduck.colored_nether_portals.payload.PortalColorPayload;
import hashduck.colored_nether_portals.util.PortalColorSavedData;
import hashduck.colored_nether_portals.util.PortalColorManager;
import hashduck.colored_nether_portals.blocks.ColoredNetherPortalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/**
 * Sets the color of new portals created when teleporting if player used a colored portal
 */
@Mixin(ServerPlayer.class)
public abstract class ChangeDimensionMixin {

    @Unique
    private ServerPlayer player() {
        return (ServerPlayer) (Object) this;
    }

    //Gets the color of the portal before changing dimensions and saves it to the tracker
    @Inject(method = "teleport", at = @At("HEAD"))
    private void captureSourceColor(
            TeleportTransition transition,
            CallbackInfoReturnable<ServerPlayer> cir
    ) {

        BlockPos portalPos = findPortalAtPlayer(player().level(), player());
        if (portalPos == null) {
            return;
        }
        PortalColorSavedData data = PortalColorSavedData.get(player().level());
        DyeColor color = data.getColor(portalPos);
        if (color != null) {
            PortalTeleportQueue.prepare(player().getUUID(), color);
        }
    }


    //Checks if the destination portal exist and sets the color if it does not
    @Inject(method = "changeDimension", at = @At("RETURN"))
    private void onChangeDimension(TeleportTransition transition, CallbackInfoReturnable<ServerPlayer> cir) {
        ServerPlayer player = cir.getReturnValue();
        if (player == null) {
            return;
        }

        if (ColoredNetherPortalBlock.getInstance() == null) {
            return;
        }

        DyeColor sourceColor = PortalTeleportQueue.getAndClear(player.getUUID());
        if (sourceColor == null) {
            return;
        }
        ServerLevel destLevel = player.level();
        BlockPos portalPos = findPortalAtPlayer(destLevel, player);
        if (portalPos == null) return;

        PortalColorSavedData data = PortalColorSavedData.get(destLevel);
        Set<BlockPos> destPortalBlocks = PortalColorManager.getConnectedPortalBlocks(destLevel, portalPos);

        for (BlockPos bp : destPortalBlocks) {
            if (data.getColor(bp) != null) return; // Don't overwrite existing colors
        }

        for (BlockPos bp : destPortalBlocks) {
            data.setColor(bp, sourceColor);

            BlockState currentState = destLevel.getBlockState(bp);
            if (currentState.is(Blocks.NETHER_PORTAL)) {
                var axis = currentState.getValue(NetherPortalBlock.AXIS);
                BlockState coloredState = ColoredNetherPortalBlock.getInstance().defaultBlockState()
                        .setValue(NetherPortalBlock.AXIS, axis);

                destLevel.setBlock(bp, coloredState, 18);
            }
        }

        PortalColorPayload.sendToTrackingPlayers(destLevel, destPortalBlocks, sourceColor);
    }

    @Unique
    private static BlockPos findPortalAtPlayer(ServerLevel level, Player player) {
        BlockPos pos = player.blockPosition();

        if (PortalColorManager.isPortalBlock(level, pos)) {
            return pos;
        }

        if (PortalColorManager.isPortalBlock(level, pos.below())) {
            return pos.below();
        }

        if (PortalColorManager.isPortalBlock(level, pos.above())) {
            return pos.above();
        }

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos check = pos.relative(dir);
            if (PortalColorManager.isPortalBlock(level, check)) {
                return check;
            }
        }
        return null;
    }
}