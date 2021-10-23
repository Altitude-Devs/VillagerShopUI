package com.alttd.events;

import com.alttd.GUI.windows.OpenGUI;
import com.alttd.VillagerUI;
import com.alttd.objects.LoadedVillagers;
import com.alttd.objects.VillagerType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class VillagerInteract implements Listener {

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager villager))
            return;

        VillagerType loadedVillager = LoadedVillagers.getLoadedVillager(villager.getUniqueId());
        if (loadedVillager == null)
            return;

        new BukkitRunnable() {
            @Override
            public void run() {
                OpenGUI openGUI = new OpenGUI(loadedVillager);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        openGUI.open(event.getPlayer());
                    }
                }.runTask(VillagerUI.getInstance());
            }
        }.runTaskAsynchronously(VillagerUI.getInstance());
    }
}
