package com.alttd.GUI;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public abstract class GUIInventory implements GUI {

    protected final Inventory inventory;
    protected final HashMap<Integer, GUIAction> tradeActions;
    protected final HashMap<Integer, GUIAction> guiActions;

    public GUIInventory(InventoryType type, Component name) {
        inventory = Bukkit.createInventory(null, type, name);
        tradeActions = new HashMap<>();
        guiActions = new HashMap<>();
    }

    public Merchant getMerchant() {
        return null;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setItem(int slot, ItemStack stack, GUIAction action){
        this.inventory.setItem(slot, stack);
        if (action != null){
            guiActions.put(slot, action);
        }
    }

    public void setItem(int slot, ItemStack stack){
        setItem(slot, stack, null);
    }

    public void open(Player player){
        player.openInventory(inventory);
        GUIByUUID.put(player.getUniqueId(), this);
    }

    public GUIAction getTradeAction(int slot) {
        return tradeActions.get(slot);
    }

    public GUIAction getGuiAction(int slot) {
        return guiActions.get(slot);
    }

    @Override
    public void setMerchantInventory(MerchantInventory merchantInventory) {

    }
}
