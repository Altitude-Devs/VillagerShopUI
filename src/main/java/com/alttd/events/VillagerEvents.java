package com.alttd.events;

import com.alttd.GUI.windows.OpenGUI;
import com.alttd.VillagerUI;
import com.alttd.config.Config;
import com.alttd.config.VillagerConfig;
import com.alttd.objects.EconUser;
import com.alttd.objects.LoadedVillagers;
import com.alttd.objects.VillagerType;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class VillagerEvents implements Listener {

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager villager))
            return;

        VillagerType loadedVillager = LoadedVillagers.getLoadedVillager(villager.getUniqueId());
        if (loadedVillager == null)
            return;

        if (!event.getPlayer().hasPermission(loadedVillager.getPermission())) {
            event.getPlayer().sendMessage(MiniMessage.get().parse(Config.NO_PERMISSION)); //TODO more specific message?
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                OpenGUI openGUI = new OpenGUI(loadedVillager, EconUser.getUser(event.getPlayer().getUniqueId()));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        openGUI.open(event.getPlayer());
                    }
                }.runTask(VillagerUI.getInstance());
            }
        }.runTaskAsynchronously(VillagerUI.getInstance());
    }

    @EventHandler
    public void onVillagerDeath(EntityDeathEvent event) {
        if (!event.getEntityType().equals(EntityType.VILLAGER))
            return;
        UUID uuid = event.getEntity().getUniqueId();

        LoadedVillagers.removeLoadedVillager(uuid);
        VillagerConfig.removeVillager(uuid);
    }
}
