package muramasa.antimatter.structure;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import muramasa.antimatter.capability.IComponentHandler;
import net.minecraft.block.BlockState;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;

import java.util.List;

@FunctionalInterface
public interface IRequirement {

    boolean test(Object2ObjectMap<String, List<IComponentHandler>> components, Object2ObjectMap<String, List<Tuple<BlockPos, BlockState>>> states);
}
