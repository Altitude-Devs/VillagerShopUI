package com.alttd.GUI;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    /**
     * Handles clicking inside a gui
     * @param event gui click event
     */
    @EventHandler
    public void onClick(InventoryClickEvent event){
        if (!(event.getWhoClicked() instanceof Player player)){
            return;
        }

        GUI gui = GUI.GUIByUUID.get(player.getUniqueId());
        if (gui == null || gui.getInventory() == null)
            return;
        if (!gui.getInventory().equals(event.getInventory()))
            return;
        event.setCancelled(true);
        GUIAction action = gui.getAction(event.getSlot());

        if (action != null){
            action.click(player);
        }
    }

    @EventHandler
    public void onTradeSelect(TradeSelectEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)){
            return;
        }

        GUI gui = GUI.GUIByUUID.get(player.getUniqueId());
        if ((!(gui instanceof GUIMerchant guiMerchant)))
            return;
        if (!gui.getMerchant().equals(event.getMerchant())) {
            return;
        }
        event.setCancelled(true);
        GUIAction action = guiMerchant.getAction(event.getIndex());

        if (action != null){
            action.click(player);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        GUI.GUIByUUID.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        GUI.GUIByUUID.remove(event.getPlayer().getUniqueId());
    }

}
