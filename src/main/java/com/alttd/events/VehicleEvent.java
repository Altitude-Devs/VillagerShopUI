package com.alttd.events;

import com.alttd.objects.LoadedVillagers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class VehicleEvent implements Listener {

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        Entity entered = event.getEntered();
        if (!(entered instanceof Villager))
            return;
        if (LoadedVillagers.getLoadedVillager(entered.getUniqueId()) != null)
            event.setCancelled(true);
    }
}
