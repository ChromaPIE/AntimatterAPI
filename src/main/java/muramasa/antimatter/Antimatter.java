package muramasa.antimatter;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import muramasa.antimatter.capability.AntimatterCaps;
import muramasa.antimatter.client.AntimatterModelManager;
import muramasa.antimatter.datagen.providers.AntimatterItemModelProvider;
import muramasa.antimatter.datagen.providers.AntimatterItemTagProvider;
import muramasa.antimatter.datagen.resources.ResourceMethod;
import muramasa.antimatter.fluid.AntimatterFluid;
import muramasa.antimatter.gui.MenuHandler;
import muramasa.antimatter.network.AntimatterNetwork;
import muramasa.antimatter.proxy.ClientHandler;
import muramasa.antimatter.proxy.IProxyHandler;
import muramasa.antimatter.proxy.ServerHandler;
import muramasa.antimatter.recipe.condition.ConfigCondition;
import muramasa.antimatter.registration.IAntimatterRegistrar;
import muramasa.antimatter.registration.RegistrationEvent;
import muramasa.antimatter.util.Utils;
import muramasa.antimatter.worldgen.AntimatterWorldGenerator;
import muramasa.antimatter.worldgen.feature.*;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.data.DataGenerator;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tesseract.Tesseract;
import tesseract.api.electric.IElectricEvent;
import tesseract.api.fluid.FluidData;
import tesseract.api.fluid.IFluidEvent;

import javax.annotation.Nonnull;

@Mod(Ref.ID)
@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class Antimatter implements IAntimatterRegistrar {

    public static Antimatter INSTANCE;
    public static AntimatterNetwork NETWORK = new AntimatterNetwork();
    public static Logger LOGGER = LogManager.getLogger(Ref.ID);
    public static IProxyHandler PROXY;
    public static Int2ObjectMap<World> WORLDS = new Int2ObjectOpenHashMap<>();

    public Antimatter() {
        INSTANCE = this;
        PROXY = DistExecutor.runForDist(() -> ClientHandler::new, () -> ServerHandler::new);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            if (AntimatterModelManager.RESOURCE_METHOD == ResourceMethod.DYNAMIC_PACK && Minecraft.getInstance() != null) {
                //Minecraft.getInstance().getResourcePackList().addPackFinder(new DynamicPackFinder("antimatter_pack", "Antimatter Resources", "desc", false));
            }
        });
        AntimatterAPI.addRegistrar(INSTANCE);
        AntimatterModelManager.addProvider(Ref.ID, g -> new AntimatterItemModelProvider(Ref.ID, Ref.NAME.concat(" Item Models"), g));
        // TODO: Make explosions depend on voltage, amp
        Tesseract.GLOBAL_ELECTRIC_EVENT = new IElectricEvent() {
            @Override
            public void onNodeOverVoltage(int dim, long pos, int voltage) {
                Utils.createExplosion(WORLDS.get(dim), BlockPos.fromLong(pos), 4.0F, Explosion.Mode.BREAK);
            }

            @Override
            public void onCableOverAmperage(int dim, long pos, int amperage) {
                Utils.createFireAround(WORLDS.get(dim), BlockPos.fromLong(pos));
            }

            @Override
            public void onCableOverVoltage(int dim, long pos, int voltage) {
                Utils.createFireAround(WORLDS.get(dim), BlockPos.fromLong(pos));
            }
        };
        // TODO: Make explosions depend on pressure, capacity, temperature
        Tesseract.GLOBAL_FLUID_EVENT = new IFluidEvent() {
            @Override
            public void onPipeOverPressure(int dim, long pos, int pressure) {
                Utils.createExplosion(WORLDS.get(dim), BlockPos.fromLong(pos), 4.0F, Explosion.Mode.BREAK);
            }

            @Override
            public void onPipeOverCapacity(int dim, long pos, int capacity) {
                Utils.createExplosion(WORLDS.get(dim), BlockPos.fromLong(pos), 1.0F, Explosion.Mode.NONE);
            }

            @Override
            public void onPipeOverTemp(int dim, long pos, int temperature) {
                World world = WORLDS.get(dim);
                if (world != null) world.setBlockState(BlockPos.fromLong(pos), temperature >= Fluids.LAVA.getAttributes().getTemperature() ? Blocks.LAVA.getDefaultState() : Blocks.FIRE.getDefaultState());
            }

            @Override
            public void onPipeGasLeak(int dim, long pos, @Nonnull FluidData fluid) {
                FluidStack resource = (FluidStack) fluid.getFluid();
                resource.setAmount((int)(resource.getAmount() * Configs.GAMEPLAY.PIPE_LEAKING));
            }
        };
    }

    private void setup(final FMLCommonSetupEvent e) {
        AntimatterAPI.onRegistration(RegistrationEvent.READY);

        AntimatterWorldGenerator.init();
        AntimatterAPI.onRegistration(RegistrationEvent.RECIPE);
        AntimatterCaps.register(); //TODO broken
        //if (ModList.get().isLoaded(Ref.MOD_CT)) GregTechAPI.addRegistrar(new GregTechTweaker());
        //if (ModList.get().isLoaded(Ref.MOD_TOP)) TheOneProbePlugin.init();
      
        //if (AntimatterModelManager.RESOURCE_METHOD == ResourceMethod.DYNAMIC_PACK) AntimatterModelManager.runProvidersDynamically();

        AntimatterAPI.getWorkQueue().forEach(DeferredWorkQueue::runLater);
    }
  
    @SubscribeEvent
    public static void onItemRegistry(final RegistryEvent.Register<Item> e) {
        AntimatterAPI.all(AntimatterFluid.class).forEach(f -> e.getRegistry().register(f.getContainerItem()));  // TODO: Convert to revamped system when PR'd
    }

    @SubscribeEvent
    public static void onBlockRegistry(final RegistryEvent.Register<Block> e) {
        AntimatterAPI.all(AntimatterFluid.class).forEach(f -> e.getRegistry().register(f.getFluidBlock()));  // TODO: Convert to revamped system when PR'd
    }

    @SubscribeEvent
    public static void onFluidRegistry(final RegistryEvent.Register<Fluid> e) {
        AntimatterAPI.all(AntimatterFluid.class).forEach(f -> {  // TODO: Convert to revamped system when PR'd
            e.getRegistry().register(f.getFluid());
            e.getRegistry().register(f.getFlowingFluid());
        });
    }

    @SubscribeEvent
    public static void onTileRegistry(final RegistryEvent.Register<TileEntityType<?>> e) {
        AntimatterAPI.all(TileEntityType.class, t -> e.getRegistry().register(t));
    }

    @SubscribeEvent
    public static void onContainerRegistry(final RegistryEvent.Register<ContainerType<?>> e) {
        AntimatterAPI.all(MenuHandler.class, h -> e.getRegistry().register(h.getContainerType()));
    }

    @SubscribeEvent
    public static void onSoundEventRegistry(final RegistryEvent.Register<SoundEvent> e) {
        e.getRegistry().registerAll(Ref.DRILL, Ref.WRENCH);
    }

    @SubscribeEvent
    public static void onRecipeSerializerRegistry(final RegistryEvent.Register<IRecipeSerializer<?>> e) {
        CraftingHelper.register(ConfigCondition.Serializer.INSTANCE);
    }

    @SubscribeEvent
    public static void onDataGather(GatherDataEvent e) {
        DataGenerator gen = e.getGenerator();
        if (e.includeClient()) {
            AntimatterModelManager.onProviderInit(Ref.ID, gen);
        }
        if (e.includeServer()) {
            gen.addProvider(new AntimatterItemTagProvider(Ref.ID, Ref.NAME.concat(" Item Tags"), false, gen));
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load e) {
        WORLDS.put(e.getWorld().getDimension().getType().getId(), e.getWorld().getDimension().getWorld());
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload e) {
        WORLDS.remove(e.getWorld().getDimension().getType().getId());
    }

    @Override
    public String getId() {
        return Ref.ID;
    }

    @Override
    public void onRegistrationEvent(RegistrationEvent event) {
        switch (event) {
            case DATA_INIT:
                Data.init();
                break;
//            case DATA_READY:
//                AntimatterAPI.registerCover(Data.COVER_NONE);
//                AntimatterAPI.registerCover(Data.COVER_OUTPUT);
//                break;
            case WORLDGEN_INIT:
                new FeatureStoneLayer();
                new FeatureVeinLayer();
                new FeatureOreSmall();
                new FeatureOre();
                new FeatureSurfaceRock();
                break;
        }
    }
}
