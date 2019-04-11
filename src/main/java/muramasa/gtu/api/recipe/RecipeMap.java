package muramasa.gtu.api.recipe;

import muramasa.gtu.api.util.GTLoc;
import muramasa.gtu.api.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

public class RecipeMap {

    public static RecipeMap ORE_BY_PRODUCTS = new RecipeMap("ore_byproducts", 100);
    public static RecipeMap PLASMA_FUELS = new RecipeMap("plasma_fuels", "Fuel Value: ", " EU", 100);

    private LinkedHashMap<String, ArrayList<Recipe>> recipeLookup;
    private String categoryId, categoryName;
    private String specialPre = "", specialPost = "";

    public RecipeMap(String jeiCategoryId, int initialSize) {
        this.categoryId = "gt.recipe_map." + jeiCategoryId;
        this.categoryName = GTLoc.get("jei.category." + jeiCategoryId + ".name");
        recipeLookup = new LinkedHashMap<>(initialSize);
    }

    public RecipeMap(String jeiCategoryId, String specialPre, String specialPost, int initialSize) {
        this(jeiCategoryId, initialSize);
        this.specialPre = specialPre;
        this.specialPost = specialPost;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Collection<Recipe> getRecipes(boolean filterHidden) {
        ArrayList<Recipe> recipes = new ArrayList<>();
        for (ArrayList<Recipe> subList : recipeLookup.values()) {
            for (Recipe recipe : subList) {
                if (!recipes.contains(recipe) && !(recipe.isHidden() && filterHidden)) {
                    recipes.add(recipe);
                }
            }
        }
        return recipes;
    }

    void add(Recipe recipe) {
        if (recipe.hasInputItems() && recipe.hasInputFluids()) {
            String inputString;
            for (int i = 0; i < recipe.getInputItems().length; i++) {
                inputString = Utils.getString(recipe.getInputItems()[i], recipe.getInputFluids()[0]);
                ArrayList<Recipe> existing = recipeLookup.get(inputString);
                if (existing != null) {
                    existing.add(recipe);
                } else {
                    ArrayList<Recipe> list = new ArrayList<>(1);
                    list.add(recipe);
                    recipeLookup.put(inputString, list);
                }
            }
        } else {
            if (recipe.hasInputItems()) {
                String inputString;
                for (int i = 0; i < recipe.getInputItems().length; i++) {
                    inputString = Utils.getString(recipe.getInputItems()[i]);
                    ArrayList<Recipe> existing = recipeLookup.get(inputString);
                    if (existing != null) {
                        existing.add(recipe);
                    } else {
                        ArrayList<Recipe> list = new ArrayList<>(1);
                        list.add(recipe);
                        recipeLookup.put(inputString, list);
                    }
                }
            } else if (recipe.hasInputFluids()) {
                for (int i = 0; i < recipe.getInputFluids().length; i++) {
                    String inputString = Utils.getString(recipe.getInputFluids()[i]);
                    ArrayList<Recipe> existing = recipeLookup.get(inputString);
                    if (existing != null) {
                        existing.add(recipe);
                    } else {
                        ArrayList<Recipe> list = new ArrayList<>(1);
                        list.add(recipe);
                        recipeLookup.put(inputString, list);
                    }
                }
            }
        }
    }

    public static Recipe findRecipeItem(RecipeMap map, ItemStack[] items) {
        if (map == null || items == null || items.length == 0) return null;
        if (Utils.areItemsValid(items)) {
            ArrayList<Recipe> matches = map.recipeLookup.get(Utils.getString(items[0]));
            if (matches == null) return null;
            int size = matches.size();
            for (int i = 0; i < size; i++) {
                if (items.length == matches.get(i).getInputItems().length && Utils.doItemsMatchAndSizeValid(matches.get(i).getInputItems(), items)) {
                    return matches.get(i);
                }
            }
        }
        return null;
    }

    public static Recipe findRecipeFluid(RecipeMap map, FluidStack[] fluids) {
        if (map == null || fluids == null || fluids.length == 0) return null;
        if (Utils.areFluidsValid(fluids)) {
            ArrayList<Recipe> matches = map.recipeLookup.get(Utils.getString(fluids[0]));
            if (matches == null) return null;
            int size = matches.size();
            for (int i = 0; i < size; i++) {
                if (fluids.length == matches.get(i).getInputFluids().length && Utils.doFluidsMatchAndSizeValid(matches.get(i).getInputFluids(), fluids)) {
                    return matches.get(i);
                }
            }
        }
        return null;
    }

    //TODO
    public static Recipe findRecipeItemFluid(RecipeMap map, ItemStack[] items, FluidStack[] fluids) {
        if (map == null) return null;
        if (Utils.areItemsValid(items) && Utils.areFluidsValid(fluids)) {
            if (items.length == 0 || fluids.length == 0) return null;
            ArrayList<Recipe> matches = map.recipeLookup.get(Utils.getString(items[0], fluids[0]));
            if (matches == null) return null;
            System.out.println("PASS MATCHES");
            int size = matches.size();
            for (int i = 0; i < size; i++) {
                if (items.length == matches.get(i).getInputItems().length && Utils.doItemsMatchAndSizeValid(matches.get(i).getInputItems(), items) && Utils.doFluidsMatchAndSizeValid(matches.get(i).getInputFluids(), fluids)) {
                    return matches.get(i);
                }
            }
        }
        return null;
    }
}
