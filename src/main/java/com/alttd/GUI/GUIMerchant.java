package com.alttd.GUI;

import com.alttd.objects.VillagerType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class GUIMerchant implements GUI{

    protected final Merchant merchant;
    protected final HashMap<Integer, GUIAction> actions;
    private final VillagerType villagerType;

    public GUIMerchant(Component name, VillagerType villagerType) {
        merchant = Bukkit.createMerchant(name);
        actions = new HashMap<>();
        this.villagerType = villagerType;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public Inventory getInventory() {
        return null;
    }

    public void addItem(@NotNull ItemStack result, @NotNull ItemStack one, @Nullable ItemStack two, @Nullable GUIAction action){
        MerchantRecipe merchantRecipe = new MerchantRecipe(result, 0, 10000, false, 0, 0);
        merchantRecipe.addIngredient(one);
        if (two != null)
            merchantRecipe.addIngredient(two);
        merchantRecipe.setPriceMultiplier(0);
        ArrayList<MerchantRecipe> recipes = new ArrayList<>(merchant.getRecipes());
        recipes.add(merchantRecipe);
        merchant.setRecipes(recipes);

        if (action != null){
            actions.put(recipes.size() - 1, action);
        }
    }

    public void open(Player player){
        player.openMerchant(merchant, false);
        GUIByUUID.put(player.getUniqueId(), this);
    }

    public GUIAction getAction(int slot) {
        return actions.get(slot);
    }

    public VillagerType getVillagerType() {
        return villagerType;
    }
}
