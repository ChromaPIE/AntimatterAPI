package muramasa.antimatter.tile;

import muramasa.antimatter.capability.fluid.FluidTanks;
import muramasa.antimatter.capability.machine.MachineFluidHandler;
import muramasa.antimatter.machine.types.Machine;
import muramasa.antimatter.util.LazyHolder;

import javax.annotation.Nullable;

public class TileEntityTank extends TileEntityMachine {

    public TileEntityTank(Machine<?> type) {
        super(type);
        this.fluidHandler = LazyHolder.of(() -> new MachineFluidHandler<TileEntityTank>(this) {
            @Nullable
            @Override
            public FluidTanks getOutputTanks() {
                return super.getInputTanks();
            }
        });
    }
}
