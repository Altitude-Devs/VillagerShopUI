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

    private MerchantInventory merchantInventory;
    protected final Merchant merchant;
    protected final HashMap<Integer, GUIAction> tradeActions;
    protected final HashMap<Integer, GUIAction> guiActions;
    private final VillagerType villagerType;

    public GUIMerchant(Component name, VillagerType villagerType) {
        merchant = Bukkit.createMerchant(name);
        tradeActions = new HashMap<>();
        guiActions = new HashMap<>();
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

        if (action != null)
            tradeActions.put(recipes.size() - 1, action);
    }

    public void setItem(int slot, @NotNull ItemStack itemStack, @Nullable GUIAction action) {
        merchantInventory.setItem(slot, itemStack);
        if (action != null)
            guiActions.put(slot, action);
    }

    public void open(Player player){
        player.openMerchant(merchant, false);
        GUIByUUID.put(player.getUniqueId(), this);
    }

    public GUIAction getTradeAction(int slot) {
        return tradeActions.get(slot);
    }

    public GUIAction getGuiAction(int slot) {
        return guiActions.get(slot);
    }

    public VillagerType getVillagerType() {
        return villagerType;
    }

    @Override
    public void setMerchantInventory(MerchantInventory merchantInventory) {
        this.merchantInventory = merchantInventory;
    }
}
