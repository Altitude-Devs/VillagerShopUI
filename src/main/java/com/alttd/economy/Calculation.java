package com.alttd.economy;

import com.alttd.config.WorthConfig;
import com.alttd.util.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.*;

import java.util.Collection;
import java.util.List;

public class Calculation {

    /**
     * Calculate the price for an item
     * @param item to calculate price for
     * @return price or int < 0 for error
     */
    public static double price(ItemStack item) {
        if (WorthConfig.prices.containsKey(item.getType()))
            return Utilities.round(WorthConfig.prices.getDouble(item.getType()) * item.getAmount(), 2);

        WorthConfig.prices.put(item.getType(), Utilities.round(getPrice(item, null), 2));

        return WorthConfig.prices.getDouble(item.getType()) * item.getAmount();
    }

    private static double getPrice(ItemStack item, Material blockedMaterial) {
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
                possiblePrice = getPrice(values.stream().toList(), item.getType());
                if (price == -1 || price > possiblePrice)
                    price = possiblePrice;
            } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                if (shapelessRecipe.getIngredientList().stream().anyMatch(itemStack -> itemStack.getType().equals(blockedMaterial)))
                    continue;
                possiblePrice = getPrice(shapelessRecipe.getIngredientList(), item.getType());
                if (price == -1 || price > possiblePrice)
                    price = possiblePrice;
            } else if (recipe instanceof FurnaceRecipe furnaceRecipe) {
                possiblePrice = getPrice(furnaceRecipe.getInput(), item.getType());
                if (price == -1 || price > possiblePrice)
                    price = possiblePrice;
            }
        }
        return price;
    }

    private static double getPrice(List<ItemStack> items, Material blockedMaterial) {
        double price = 0;
        for (ItemStack item : items) {
            double tmp = getPrice(item, blockedMaterial);
            if (tmp == -1)
                return -1;
            price += tmp;
        }
        return price;
    }
}
