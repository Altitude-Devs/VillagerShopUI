package com.alttd.events;

import com.alttd.GUI.GUI;
import com.alttd.VillagerUI;
import com.alttd.config.Config;
import com.alttd.datalock.DataLockAPI;
import com.alttd.objects.EconUser;
import com.alttd.util.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class LogoutEvent implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (Config.DEBUG)
                    Logger.info("Syncing %", event.getPlayer().getName());
                GUI.GUIByUUID.remove(uuid);
                EconUser user = EconUser.getUser(uuid);
                if (user != null) {
                    user.syncPoints();
                    EconUser.removeUser(uuid);
                }
                DataLockAPI.get().tryUnlock("villagerui:player-data", uuid.toString());
            }
        }.runTaskAsynchronously(VillagerUI.getInstance());
    }
}
