package com.alttd.events;

import com.alttd.objects.EconUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LogoutEvent implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        EconUser.getUser(event.getPlayer().getUniqueId()).syncPoints();
        EconUser.removeUser(event.getPlayer().getUniqueId());
    }
}
