package com.alttd.util;

import com.alttd.config.WorthConfig;
import com.alttd.objects.Price;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.*;

import java.util.List;

public class Utilities {
    /**
     * Rounds num down to precision (rounds up if last cut off decimal is bigger than 4)
     *
     * @param num       value to be rounded
     * @param precision length to round to
     * @return num rounded
     */
    public static double round(double num, int precision) {
        double scale = Math.pow(10, precision);
        double total = (double) (Math.round(num * scale)) / scale;

        scale = (int) Math.pow(10, precision + 1);
        long tmp = (Math.round(num * scale));

        tmp %= 10;
        if (tmp > 4)
            total += 0.01;

        return total;
    }

    /**
     * Calculate the price for an ItemStack (this considers stack size)
     *
     * @param item to calculate price for
     * @return price or int < 0 for error
     */
    public static Price getPrice(ItemStack item) {
        if (WorthConfig.prices.containsKey(item.getType()))
            return (WorthConfig.prices.get(item.getType()));
        Price price = getWorth(item, null);
        if (price == null)
            return (null);
        WorthConfig.prices.put(item.getType(), price);
        return (WorthConfig.prices.get(item.getType()));
    }

    /**
     * Get the worth of the material an ItemStack consists of
     *
     * @param item            to get the worth of
     * @param blockedMaterial Material to ignore set to null on initial call
     * @return Worth of the item as a double
     */
    private static Price getWorth(ItemStack item, Material blockedMaterial) {
        Price price = null;

        if (item == null)
            return (null);
        if (WorthConfig.prices.containsKey(item.getType()))
            return (WorthConfig.prices.get(item.getType()));

        List<Recipe> recipes = Bukkit.getRecipesFor(item);
        for (Recipe recipe : recipes) {
            Price possiblePrice;

            if (recipe instanceof ShapedRecipe shapedRecipe) {
                List<ItemStack> values = shapedRecipe.getIngredientMap().values().stream().toList();
                if (!values.isEmpty() && blockedMaterial != null && values.stream()
                        .anyMatch(itemStack -> itemStack != null && itemStack.getType().equals(blockedMaterial)))
                    continue;
                possiblePrice = getWorth(values, item.getType());
                if (possiblePrice == null)
                    continue;
                if (price == null || price.getPrice(1) > possiblePrice.getPrice(1))
                    price = possiblePrice;
            } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                if (shapelessRecipe.getIngredientList().stream()
                        .anyMatch(itemStack -> itemStack.getType().equals(blockedMaterial)))
                    continue;
                possiblePrice = getWorth(shapelessRecipe.getIngredientList(), item.getType());
                if (possiblePrice == null)
                    continue;
                if (price == null || price.getPrice(1) > possiblePrice.getPrice(1))
                    price = possiblePrice;
            } else if (recipe instanceof CampfireRecipe campfireRecipe) {
                possiblePrice = getWorth(campfireRecipe.getInput(), item.getType());
                if (possiblePrice == null)
                    continue;
                if (price == null || price.getPrice(1) > possiblePrice.getPrice(1))
                    price = possiblePrice;
            } else if (recipe instanceof StonecuttingRecipe stonecuttingRecipe) {
                possiblePrice = getWorth(stonecuttingRecipe.getInput(), item.getType());
                if (possiblePrice == null)
                    continue;
                if (price == null || price.getPrice(1) > possiblePrice.getPrice(1))
                    price = possiblePrice;
            } else if (recipe instanceof CookingRecipe cookingRecipe) {
                if ((recipe instanceof FurnaceRecipe || recipe instanceof BlastingRecipe ) &&
                        !cookingRecipe.getInput().getType().isBlock() &&
                        !cookingRecipe.getInput().getType().equals(Material.CLAY_BALL)) //Needs exception for clay ball idk a better way to do it...
                        continue;
                possiblePrice = getWorth(cookingRecipe.getInput(), item.getType());
                if (possiblePrice == null)
                    continue;
                if (price == null || price.getPrice(1) > possiblePrice.getPrice(1))
                    price = possiblePrice;
            }
        }
        return price;
    }

    /**
     * Get the total worth of a list of ItemStack's (amount of items in ItemStack is ignored)
     *
     * @param items           Items to get the worth of
     * @param blockedMaterial Material to ignore set to null on initial call
     * @return Worth of ItemStack as a double
     */
    private static Price getWorth(List<ItemStack> items, Material blockedMaterial) {
        Price price = null;
        for (ItemStack item : items) {
            if (item == null)
                continue;
            Price tmp = getWorth(new ItemStack(item.getType()), blockedMaterial);
            if (tmp == null || tmp.getPrice(1) == -1)
                return null;
            WorthConfig.prices.put(item.getType(), tmp);
            if (price == null)
                price = tmp;
            else
                price = Price.addPrice(price, tmp);
        }
        return (price);
    }
}
