package com.alttd.util;

import com.alttd.objects.Price;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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

//        scale = (int) Math.pow(10, precision + 1);
//        long tmp = (Math.round(num * scale));
//
//        tmp %= 10;
//        if (tmp > 4)
//            total += 0.01;

        return ((int) (num * scale)) / scale;
    }

    /**
     * Calculate the price for an ItemStack (this considers stack size)
     *
     * @param item to calculate price for
     * @return price or int < 0 for error
     */
    public static Price getPrice(ItemStack item, Object2ObjectOpenHashMap<Material, Price> map) {
        if (map.containsKey(item.getType()))
            return (map.get(item.getType()));
        Price price = getWorth(item, null, map);
        if (price == null)
            return (null);
        map.put(item.getType(), price);
        return (map.get(item.getType()));
    }

    /**
     * Get the worth of the material an ItemStack consists of
     *
     * @param item            to get the worth of
     * @param blockedMaterial Material to ignore set to null on initial call
     * @return Worth of the item as a double
     */
    private static Price getWorth(ItemStack item, Material blockedMaterial, Object2ObjectOpenHashMap<Material, Price> map) {
        Price price = null;

        if (item == null)
            return (null);
        if (map.containsKey(item.getType()))
            return (map.get(item.getType()));

        if (map.containsKey(blockedMaterial))
            blockedMaterial = null;

        Material finalBlockedMaterial = blockedMaterial;
        List<Recipe> recipes = Bukkit.getRecipesFor(item);
        for (Recipe recipe : recipes) {
            Price possiblePrice;

            if (recipe instanceof ShapedRecipe shapedRecipe) {
                List<ItemStack> values = shapedRecipe.getIngredientMap().values().stream().toList();
                if (!values.isEmpty() && blockedMaterial != null && values.stream()
                        .anyMatch(itemStack -> itemStack != null && itemStack.getType().equals(finalBlockedMaterial)))
                    continue;
                possiblePrice = getWorth(values, item.getType(), map);
                if (possiblePrice == null)
                    continue;
                if (price == null || price.getPrice(1) > possiblePrice.getPrice(1))
                    price = possiblePrice;
            } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                if (shapelessRecipe.getIngredientList().stream()
                        .anyMatch(itemStack -> itemStack.getType().equals(finalBlockedMaterial)))
                    continue;
                possiblePrice = getWorth(shapelessRecipe.getIngredientList(), item.getType(), map);
                if (possiblePrice == null)
                    continue;
                if (price == null || price.getPrice(1) > possiblePrice.getPrice(1))
                    price = possiblePrice;
            } else if (recipe instanceof CampfireRecipe campfireRecipe) {
                possiblePrice = getWorth(campfireRecipe.getInput(), item.getType(), map);
                if (possiblePrice == null)
                    continue;
                if (price == null || price.getPrice(1) > possiblePrice.getPrice(1))
                    price = possiblePrice;
            } else if (recipe instanceof StonecuttingRecipe stonecuttingRecipe) {
                possiblePrice = getWorth(stonecuttingRecipe.getInput(), item.getType(), map);
                if (possiblePrice == null)
                    continue;
                if (price == null || price.getPrice(1) > possiblePrice.getPrice(1))
                    price = possiblePrice;
            } else if (recipe instanceof CookingRecipe cookingRecipe) {
                if ((recipe instanceof FurnaceRecipe || recipe instanceof BlastingRecipe ) &&
                        !cookingRecipe.getInput().getType().isBlock() &&
                        !cookingRecipe.getInput().getType().equals(Material.CLAY_BALL)) //Needs exception for clay ball idk a better way to do it...
                        continue;
                possiblePrice = getWorth(cookingRecipe.getInput(), item.getType(), map);
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
    private static Price getWorth(List<ItemStack> items, Material blockedMaterial, Object2ObjectOpenHashMap<Material, Price> map) {
        Price price = null;
        for (ItemStack item : items) {
            if (item == null)
                continue;
            Price tmp = getWorth(new ItemStack(item.getType()), blockedMaterial, map);
            if (tmp == null || tmp.getPrice(1) == -1)
                return null;
            map.put(item.getType(), tmp);
            if (price == null)
                price = tmp;
            else
                price = Price.addPrice(price, tmp, item.getType());
        }
        return (price);
    }

    public static void econSyncingMessage(Player player)
    {
        player.sendMiniMessage("<red>Syncing econ data...</red>", null);
    }
}
