package com.alttd.events;

import com.alttd.GUI.windows.OpenGUI;
import com.alttd.GUI.windows.TradeGUI;
import com.alttd.VillagerUI;
import com.alttd.config.Config;
import com.alttd.config.VillagerConfig;
import com.alttd.galaxy.event.player.PlayerInteractOnEntityEvent;
import com.alttd.objects.*;
import org.bukkit.Sound;
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
            player.sendMiniMessage(Config.LOADING_ECON_DATA, null);
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                if (loadedVillager instanceof BlackMarketVillagerType blackMarketVillagerType) {
                    TradeGUI tradeGUI = new TradeGUI(blackMarketVillagerType, user);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            tradeGUI.open(player);
                        }
                    }.runTask(VillagerUI.getInstance());
                } else if (loadedVillager instanceof ShopVillagerType shopVillagerType) {
                    OpenGUI openGUI = new OpenGUI(shopVillagerType, user);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            openGUI.open(player);
                        }
                    }.runTask(VillagerUI.getInstance());
                }
            }
        }.runTaskAsynchronously(VillagerUI.getInstance());
    }

    @EventHandler
    public void onPlayerInteractOnEntity(PlayerInteractOnEntityEvent event) {
        Player player = event.getPlayer();

        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }

        VillagerType loadedVillager = LoadedVillagers.getLoadedVillager(villager.getUniqueId());
        if (loadedVillager == null) {
            return;
        }

        loadedVillager.getRandomMessage().ifPresent(player::sendMessage);
        if (loadedVillager instanceof BlackMarketVillagerType) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0F, 1.0F);
        } else if (loadedVillager instanceof ShopVillagerType) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0F, 1.0F);
        }
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
