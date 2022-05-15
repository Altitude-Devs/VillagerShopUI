package com.alttd.events;

import com.alttd.objects.EconUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class LoginEvent implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        EconUser.tryLoadUser(event.getPlayer().getUniqueId());
    }
}
