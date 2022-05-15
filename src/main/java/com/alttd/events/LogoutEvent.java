package com.alttd.events;

import com.alttd.GUI.GUI;
import com.alttd.VillagerUI;
import com.alttd.config.Config;
import com.alttd.objects.EconUser;
import com.alttd.util.Logger;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class LogoutEvent implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        if (Config.DEBUG)
            Logger.info("Syncing %", event.getPlayer().getName());
        GUI.GUIByUUID.remove(uuid);
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("try-unlock");
        out.writeUTF(uuid.toString());
        Bukkit.getServer().sendPluginMessage(VillagerUI.getInstance(),
                "villagerui:player-data",
                out.toByteArray());
        EconUser user = EconUser.getUser(uuid);
        if (user == null)
            return;
        user.syncPoints();
        EconUser.removeUser(uuid);
    }
}
