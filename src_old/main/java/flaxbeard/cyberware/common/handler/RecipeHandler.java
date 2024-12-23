package flaxbeard.cyberware.common.handler;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

/**
 * This class contains a bunch of helper methods for adding recipes in code in minecraft 1.12
 *
 * Feel free to use this code for your projects.
 *
 * Also please, please point out places where this can be improved.
 */
public class RecipeHandler
{
    /**
     * Adds an IRecipe Object
     *
     * @param recipe The IRecipe that gets added
     */
    public static void addRecipe(IRecipe recipe)
    {
        ForgeRegistries.RECIPES.register(recipe);
    }

    /**
     * Adds a shaped recipe that supports string input paramers corresponding to an oredict entry, can also be used for recipes without ore dict ingredients
     *
     * @param output The stack that should be produced
     */
    public static void addShapedOreRecipe(ItemStack output, Object... params)
    {
        ResourceLocation location = getNameForRecipe(output);
        ShapedOreRecipe recipe = new ShapedOreRecipe(location, output, params);
        recipe.setRegistryName(location);
        ForgeRegistries.RECIPES.register(recipe);
    }

    /**
     * Adds a basic shaped recipe
     *
     * @param output The stack that should be produced
     */
    public static void addShapedRecipe(ItemStack output, Object... params)
    {
        ResourceLocation location = getNameForRecipe(output);
        CraftingHelper.ShapedPrimer primer = CraftingHelper.parseShaped(params);
        ShapedRecipes recipe = new ShapedRecipes(output.getItem().getRegistryName().toString(), primer.width, primer.height, primer.input, output);
        recipe.setRegistryName(location);
        ForgeRegistries.RECIPES.register(recipe);
    }

    /**
     * Adds a shapeless ore recipe
     *
     * @param output The stack that should be produced
     */
    public static void addShapelessOreRecipe(ItemStack output, Object... input)
    {
        ResourceLocation location = getNameForRecipe(output);
        ShapelessOreRecipe recipe = new ShapelessOreRecipe(location, output, input);
        recipe.setRegistryName(location);
        ForgeRegistries.RECIPES.register(recipe);
    }

    /**
     * Adds a basic shapeless recipe
     *
     * @param output The stack that should be produced
     */
    public static void addShapelessRecipe(ItemStack output, Object... input)
    {
        ResourceLocation location = getNameForRecipe(output);
        ShapelessRecipes recipe = new ShapelessRecipes(location.getNamespace(), output, buildInput(input));
        recipe.setRegistryName(location);
        ForgeRegistries.RECIPES.register(recipe);
    }

    /**
     * Genereates a unique name based of the active mod, and the itemstack that the recipe outputs
     *
     * @param output an itemstack, usualy the one the the recipe produces
     * @return a unique ResourceLocation based off the item item
     */
    public static ResourceLocation getNameForRecipe(ItemStack output)
    {
        ModContainer activeContainer = Loader.instance().activeModContainer();
        ResourceLocation baseLoc = new ResourceLocation(activeContainer.getModId(), output.getItem().getRegistryName().getPath());
        ResourceLocation recipeLoc = baseLoc;
        int index = 0;
        while (CraftingManager.REGISTRY.containsKey(recipeLoc)) {
            index++;
            recipeLoc = new ResourceLocation(activeContainer.getModId(), baseLoc.getPath() + "_" + index);
        }
        return recipeLoc;
    }

    /**
     * Converts an object array into a NonNullList of Ingredients
     */
    private static NonNullList<Ingredient> buildInput(Object[] input)
    {
        NonNullList<Ingredient> list = NonNullList.create();
        for (Object obj : input) {
            if (obj instanceof Ingredient) {
                list.add((Ingredient) obj);
            } else {
                Ingredient ingredient = CraftingHelper.getIngredient(obj);
                if (ingredient == null) {
                    ingredient = Ingredient.EMPTY;
                }
                list.add(ingredient);
            }
        }
        return list;
    }

    public static void addSmelting(Block input, ItemStack output, float xp)
    {
        GameRegistry.addSmelting(input, output, xp);
    }

    public static void addSmelting(Item input, ItemStack output, float xp)
    {
        GameRegistry.addSmelting(input, output, xp);
    }

    public static void addSmelting(ItemStack input, ItemStack output, float xp)
    {
        GameRegistry.addSmelting(input, output, xp);
    }

    public static void addSmelting(ItemStack input, ItemStack output)
    {
        addSmelting(input, output, 1.0F);
    }

    public static void addSmelting(Item input, ItemStack output)
    {
        addSmelting(input, output, 1.0F);
    }

    public static void addSmelting(Block input, ItemStack output)
    {
        addSmelting(input, output, 1.0F);
    }
}