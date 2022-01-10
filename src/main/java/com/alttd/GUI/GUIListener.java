package com.alttd.GUI;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;

public class GUIListener implements Listener {

    private static ItemStack air = new ItemStack(Material.AIR);

    /**
     * Handles clicking inside a gui
     * @param event gui click event
     */
    @EventHandler
    public void onClick(InventoryClickEvent event){
        if (!(event.getWhoClicked() instanceof Player player)) return;

        GUI gui = GUI.GUIByUUID.get(player.getUniqueId());
        if (gui == null) return;
        if (gui.getInventory() != null) {
            if (!gui.getInventory().equals(event.getInventory())) return;
        } else if (gui instanceof GUIMerchant) {
            HumanEntity trader = gui.getMerchant().getTrader();
            if (trader == null || !trader.equals(player)) return;
        } else return;

        event.setCancelled(true);

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null || clickedInventory.getType().equals(InventoryType.PLAYER)) return;

        GUIAction action = gui.getGuiAction(event.getSlot());

        if (action != null) action.click(player);
    }

    @EventHandler
    public void onTradeSelect(TradeSelectEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        GUI gui = GUI.GUIByUUID.get(player.getUniqueId());

        if ((!(gui instanceof GUIMerchant guiMerchant))) return;
        if (!gui.getMerchant().equals(event.getMerchant())) return;

        event.setCancelled(true);
        GUIAction action = guiMerchant.getTradeAction(event.getIndex());

        gui.setMerchantInventory(event.getInventory());

        if (action != null) action.click(player);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory() instanceof MerchantInventory merchantInventory)) return;

        HumanEntity player = event.getPlayer();
        GUI gui = GUI.GUIByUUID.get(player.getUniqueId());

        if (!(gui instanceof GUIMerchant)) return;

        HumanEntity trader = gui.getMerchant().getTrader();
        if (trader == null || !trader.equals(player)) return;
        merchantInventory.setItem(0, air);
        merchantInventory.setItem(1, air);
        GUI.GUIByUUID.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        GUI.GUIByUUID.remove(event.getPlayer().getUniqueId());
    }

}
