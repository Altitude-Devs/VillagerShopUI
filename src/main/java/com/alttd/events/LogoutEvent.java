package com.alttd.events;

import com.alttd.GUI.GUI;
import com.alttd.config.Config;
import com.alttd.objects.EconUser;
import com.alttd.util.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class LogoutEvent implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        if (Config.DEBUG)
            Logger.info("Syncing %", event.getPlayer().getName());
        EconUser user = EconUser.getUser(uuid);
        if (user != null)
        {
            user.syncPoints();
            EconUser.removeUser(uuid);
        }
        GUI.GUIByUUID.remove(uuid);
    }
}
