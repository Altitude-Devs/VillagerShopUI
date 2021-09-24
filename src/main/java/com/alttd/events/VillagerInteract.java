package com.alttd.events;

import com.alttd.GUI.windows.OpenGUI;
import com.alttd.objects.LoadedVillagers;
import com.alttd.objects.VillagerType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class VillagerInteract implements Listener {

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager villager))
            return;

        VillagerType loadedVillager = LoadedVillagers.getLoadedVillager(villager.getUniqueId());
        if (loadedVillager == null)
            return;

        new OpenGUI(loadedVillager).open(event.getPlayer());
    }
}
