package hashduck.colored_nether_portals.mixin;

import hashduck.colored_nether_portals.util.DyeColorUtil;
import hashduck.colored_nether_portals.client.PortalColorClientCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * Gets the portal color from the cache and sets the particle color
 */
@Mixin(PortalParticle.class)
public abstract class PortalParticleColorMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void colorPortalParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, CallbackInfo ci) {
        BlockPos pos = BlockPos.containing(x, y, z);

        DyeColor color = PortalColorClientCache.get(level, pos);

        if (color != null) {
            float[] rgb = DyeColorUtil.getFireworkRgb(color);

            float twinkleShade = level.random.nextFloat() * 0.6F + 0.4F;

            Particle self = (Particle)(Object) this;
            self.setColor(rgb[0] * twinkleShade, rgb[1] * twinkleShade, rgb[2] * twinkleShade);
        }
    }
}