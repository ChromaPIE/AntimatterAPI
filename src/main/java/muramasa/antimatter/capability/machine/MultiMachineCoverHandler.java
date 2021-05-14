package muramasa.antimatter.capability.machine;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import muramasa.antimatter.Ref;
import muramasa.antimatter.cover.CoverNone;
import muramasa.antimatter.cover.CoverStack;
import muramasa.antimatter.cover.ICover;
import muramasa.antimatter.structure.StructureResult;
import muramasa.antimatter.tile.TileEntityFakeBlock;
import muramasa.antimatter.tile.multi.TileEntityBasicMultiMachine;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.EnumMap;
import java.util.Map;

public class MultiMachineCoverHandler<T extends TileEntityBasicMultiMachine> extends MachineCoverHandler<T> {

    private final Map<BlockPos, Map<Direction, CoverStack<T>>> fakeTileCovers;

    public MultiMachineCoverHandler(T tile) {
        super(tile);
        this.fakeTileCovers = new Object2ObjectOpenHashMap<>();
    }

    public void onStructureFormed() {
        getTile().getStates("fake").forEach(tuple -> {
            BlockPos pos = tuple.getA();
            TileEntity tile = getTile().getWorld().getTileEntity(pos);
            if (tile instanceof TileEntityFakeBlock) {
                TileEntityFakeBlock fake = (TileEntityFakeBlock) tile;
                if (!fake.ticks()) return;
                for (Direction dir : Ref.DIRS) {
                    ICover cover = fake.getCover(dir);
                    if (cover == null || cover instanceof CoverNone) continue;
                    fakeTileCovers.compute(pos, (k,v) -> {
                        if (v == null) v = new EnumMap<>(Direction.class);
                        v.put(dir, new CoverStack<>(cover, this.getTile(), dir));
                        return v;
                    });
                }
            }
        });
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        BlockPos oldPos = getTile().getPos();
        try {
            fakeTileCovers.forEach((k,v) -> {
                getTile().setPos(k);
                v.forEach((a,b) -> b.onUpdate(a));
            });
        } finally {
            this.getTile().setPos(oldPos);
        }
    }
}
