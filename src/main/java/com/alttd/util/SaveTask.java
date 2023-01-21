package com.alttd.util;

import com.alttd.config.Config;
import com.alttd.objects.EconUser;
import org.bukkit.scheduler.BukkitRunnable;

public class SaveTask extends BukkitRunnable {
    @Override
    public void run() {
        if (Config.DEBUG)
            Logger.info("Syncing users.");
        EconUser.getEconUsers().forEach(econUser -> {
            if (econUser == null)
                return;
            if (Config.DEBUG)
                Logger.info("Syncing %", econUser.getUuid().toString());
            econUser.removePoints();
            econUser.syncPoints();
        });
    }
}
