package hashduck.colored_nether_portals.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Exposes the private {@code TYPE_BY_STATE} map so the colored portal block's
 * states can be registered under the vanilla nether portal POI. Without this,
 * vanilla's portal search ({@link net.minecraft.world.level.portal.PortalForcer})
 * can't see existing colored portals and builds a duplicate gray portal next to them.
 */
@Mixin(PoiTypes.class)
public interface PoiTypesAccessor {
    @Accessor("TYPE_BY_STATE")
    static Map<BlockState, Holder<PoiType>> getTypeByState() {
        throw new AssertionError();
    }
}
