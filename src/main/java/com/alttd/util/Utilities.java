package com.alttd.util;

import com.alttd.config.WorthConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.*;

import java.util.Collection;
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
    public static double getWorth(ItemStack item) {
        if (WorthConfig.prices.containsKey(item.getType()))
            return Utilities.round(WorthConfig.prices.getDouble(item.getType()) * item.getAmount(), 2);

        WorthConfig.prices.put(item.getType(), Utilities.round(getWorth(item, null), 2));

        return WorthConfig.prices.getDouble(item.getType()) * item.getAmount();
    }

    /**
     * Get the worth of the material an ItemStack consists of
     *
     * @param item to get the worth of
     * @param blockedMaterial Material to ignore set to null on initial call
     * @return Worth of the item as a double
     */
    private static double getWorth(ItemStack item, Material blockedMaterial) {
        if (WorthConfig.prices.containsKey(item.getType()))
            return WorthConfig.prices.getDouble(item.getType());
        double price = -1;
        List<Recipe> recipes = Bukkit.getRecipesFor(item);
        for (Recipe recipe : recipes) {
            double possiblePrice;
            if (recipe instanceof ShapedRecipe shapedRecipe) {
                Collection<ItemStack> values = shapedRecipe.getIngredientMap().values();
                if (values.stream().anyMatch(itemStack -> itemStack.getType().equals(blockedMaterial)))
                    continue;
                possiblePrice = getWorth(values.stream().toList(), item.getType());
                if (price == -1 || price > possiblePrice)
                    price = possiblePrice;
            } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                if (shapelessRecipe.getIngredientList().stream().anyMatch(itemStack -> itemStack.getType().equals(blockedMaterial)))
                    continue;
                possiblePrice = getWorth(shapelessRecipe.getIngredientList(), item.getType());
                if (price == -1 || price > possiblePrice)
                    price = possiblePrice;
            } else if (recipe instanceof FurnaceRecipe furnaceRecipe) {
                possiblePrice = getWorth(furnaceRecipe.getInput(), item.getType());
                if (price == -1 || price > possiblePrice)
                    price = possiblePrice;
            }
        }
        return price;
    }

    /**
     * Get the total worth of a list of ItemStack's (amount of items in ItemStack is ignored)
     *
     * @param items Items to get the worth of
     * @param blockedMaterial Material to ignore set to null on initial call
     * @return Worth of ItemStack as a double
     */
    private static double getWorth(List<ItemStack> items, Material blockedMaterial) {
        double price = 0;
        for (ItemStack item : items) {
            double tmp = getWorth(item, blockedMaterial);
            if (tmp == -1)
                return -1;
            price += tmp;
        }
        return price;
    }
}
