package com.alttd.events;

import com.alttd.VillagerUI;
import com.alttd.config.Config;
import com.alttd.database.Queries;
import com.alttd.objects.EconUser;
import com.alttd.util.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class LoginEvent implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        EconUser.dumbfix(player.getUniqueId());
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                UUID uuid = player.getUniqueId();
//                EconUser user = EconUser.getUser(uuid);
//                int minutes = Queries.getMinutesSinceUpdated(uuid);
//
//                user.removePoints(minutes * 2);
//                if (Config.DEBUG)
//                    Logger.info("Loaded EconUser for % and removed % points",
//                            player.getName(), String.valueOf(minutes * 2));
//            }
//        }.runTask(VillagerUI.getInstance());
    }
}
