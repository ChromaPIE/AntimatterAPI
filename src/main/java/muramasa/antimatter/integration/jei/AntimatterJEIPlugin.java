package muramasa.antimatter.integration.jei;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import muramasa.antimatter.Antimatter;
import muramasa.antimatter.AntimatterAPI;
import muramasa.antimatter.Ref;
import muramasa.antimatter.fluid.AntimatterFluid;
import muramasa.antimatter.gui.GuiData;
import muramasa.antimatter.integration.jei.category.RecipeMapCategory;
import muramasa.antimatter.machine.Tier;
import muramasa.antimatter.machine.types.Machine;
import muramasa.antimatter.recipe.RecipeMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static muramasa.antimatter.machine.MachineFlag.RECIPE;


@JeiPlugin
public class AntimatterJEIPlugin implements IModPlugin {

    protected static class RegistryValue {
        RecipeMap map;
        GuiData gui;
        Tier tier;
        String machine;

        public RegistryValue(RecipeMap map, GuiData gui, Tier tier, String machine) {
            this.map = map;
            this.gui = gui;
            this.tier = tier;
            this.machine = machine;
        }
    }

    private static IJeiRuntime runtime;
    private static Object2ObjectMap<String, RegistryValue> REGISTRY = new Object2ObjectLinkedOpenHashMap<>();

    public AntimatterJEIPlugin() {
        Antimatter.LOGGER.debug("AntimatterJEIPlugin created");
    }

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Ref.ID, "jei");
    }

    public static void registerCategory(RecipeMap<?> map, GuiData gui, Tier tier, String itemModel) {
        REGISTRY.put(map.getId(), new RegistryValue(map,map.getGui() == null ? gui : map.getGui(),tier,itemModel));//new Tuple<>(map, new Tuple<>(gui, tier)));
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM, AntimatterAPI.all(AntimatterFluid.class).stream().map(t -> new ItemStack(Item.BLOCK_TO_ITEM.get(t.getFluidBlock()))).collect(Collectors.toList()));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        RecipeMapCategory.setGuiHelper(registry.getJeiHelpers().getGuiHelper());

        Set<String> registeredMachineCats = new ObjectOpenHashSet<>();

        REGISTRY.forEach((id, tuple) -> {
            if (!registeredMachineCats.contains(tuple.map.getId())) registry.addRecipeCategories(new RecipeMapCategory(tuple.map,tuple.gui,tuple.tier,tuple.machine));
        });
    }
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        REGISTRY.forEach((id, tuple) -> {
            registration.addRecipes(tuple.map.getRecipes(true), new ResourceLocation(Ref.ID, id));
        });
    }

    public static void showCategory(Machine... types) {
        if (runtime != null) {
            List<ResourceLocation> list = new LinkedList<>();
            for (int i = 0; i < types.length; i++) {
                if (!types[i].has(RECIPE)) continue;
                list.add(new ResourceLocation(Ref.ID, types[i].getRecipeMap().getId()));
            }
            runtime.getRecipesGui().showCategories(list);
        }
    }
}
