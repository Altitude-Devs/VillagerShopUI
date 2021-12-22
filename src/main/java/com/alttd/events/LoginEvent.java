package com.alttd.events;

import com.alttd.VillagerUI;
import com.alttd.database.Queries;
import com.alttd.objects.EconUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class LoginEvent implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                UUID uuid = event.getPlayer().getUniqueId();
                EconUser user = EconUser.getUser(uuid);
                int minutes = Queries.getMinutesSinceUpdated(uuid);

                user.removePoints(minutes * 2);
            }
        }.runTask(VillagerUI.getInstance());
    }
}
