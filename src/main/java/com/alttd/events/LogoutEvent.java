package com.alttd.events;

import com.alttd.GUI.GUI;
import com.alttd.objects.EconUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class LogoutEvent implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        EconUser.getUser(uuid).syncPoints();
        EconUser.removeUser(uuid);
        GUI.GUIByUUID.remove(uuid);
    }
}
