package muramasa.antimatter.capability;

import muramasa.antimatter.capability.machine.MachineRecipeHandler;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class AntimatterCaps {

    @CapabilityInject(ICoverHandler.class)
    public static final Capability<ICoverHandler> COVERABLE_HANDLER_CAPABILITY;

    @CapabilityInject(IComponentHandler.class)
    public static final Capability<IComponentHandler> COMPONENT_HANDLER_CAPABILITY;

    /**
     * Dummy cap
     **/
    public static final Capability<MachineRecipeHandler> RECIPE_HANDLER_CAPABILITY;

    static {
        COVERABLE_HANDLER_CAPABILITY = null;
        COMPONENT_HANDLER_CAPABILITY = null; // Fixme: Optimize
        RECIPE_HANDLER_CAPABILITY = null; // Fixme: REMOVE
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(ICoverHandler.class, new Capability.IStorage<ICoverHandler>() {
            @Nullable
            @Override
            public INBT writeNBT(Capability<ICoverHandler> capability, ICoverHandler instance, Direction side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<ICoverHandler> capability, ICoverHandler instance, Direction side, INBT nbt) {
                instance.deserializeNBT(nbt);
            }
        }, () -> new CoverHandler<>(null));

        CapabilityManager.INSTANCE.register(IComponentHandler.class, new Capability.IStorage<IComponentHandler>() {
            @Nullable
            @Override
            public INBT writeNBT(Capability<IComponentHandler> capability, IComponentHandler instance, Direction side) {
                return null;
            }

            @Override
            public void readNBT(Capability<IComponentHandler> capability, IComponentHandler instance, Direction side, INBT nbt) {

            }
        }, () -> new ComponentHandler("null", null));

        CapabilityManager.INSTANCE.register(MachineRecipeHandler.class, new Capability.IStorage<MachineRecipeHandler>() {
            @Nullable
            @Override
            public INBT writeNBT(Capability<MachineRecipeHandler> capability, MachineRecipeHandler instance, Direction side) {
                return null;
            }

            @Override
            public void readNBT(Capability<MachineRecipeHandler> capability, MachineRecipeHandler instance, Direction side, INBT nbt) {

            }
        }, () -> new MachineRecipeHandler(null));
    }
}
