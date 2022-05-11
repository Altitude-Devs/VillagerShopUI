package com.alttd.events;

import com.alttd.GUI.windows.OpenGUI;
import com.alttd.VillagerUI;
import com.alttd.config.Config;
import com.alttd.config.VillagerConfig;
import com.alttd.objects.EconUser;
import com.alttd.objects.LoadedVillagers;
import com.alttd.objects.VillagerType;
import com.alttd.util.Utilities;
import jdk.jshell.execution.Util;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
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

        Player player = event.getPlayer();
        event.setCancelled(true);
        if (!player.hasPermission(loadedVillager.getPermission())) {
            player.sendMiniMessage(Config.NO_PERMISSION, null); //TODO more specific message?
            return;
        }
        EconUser user = EconUser.getUser(player.getUniqueId());
        if (user == null) {
            Utilities.econSyncingMessage(player);
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                OpenGUI openGUI = new OpenGUI(loadedVillager, user);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        openGUI.open(player);
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

    @EventHandler
    public void onVillagerPotioned(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Villager villager))
            return;

        VillagerType loadedVillager = LoadedVillagers.getLoadedVillager(villager.getUniqueId());
        if (loadedVillager == null)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onVillagerEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Villager villager))
            return;

        VillagerType loadedVillager = LoadedVillagers.getLoadedVillager(villager.getUniqueId());
        if (loadedVillager == null)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onVillagerBlockDamage(EntityDamageByBlockEvent event) {
        if (!(event.getEntity() instanceof Villager villager))
            return;

        VillagerType loadedVillager = LoadedVillagers.getLoadedVillager(villager.getUniqueId());
        if (loadedVillager == null)
            return;

        event.setCancelled(true);
    }
}
