package com.alttd.GUI;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;

import java.util.HashMap;
import java.util.UUID;

public interface GUI {
    HashMap<UUID, GUI> GUIByUUID = new HashMap<>();

    void open(Player player);

    GUIAction getTradeAction(int slot);

    GUIAction getGuiAction(int slot);

    Inventory getInventory();

    Merchant getMerchant();

    void setMerchantInventory(MerchantInventory merchantInventory);
}
