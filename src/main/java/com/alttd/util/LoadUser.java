package com.alttd.util;

import com.alttd.config.Config;
import com.alttd.database.Queries;
import com.alttd.objects.EconUser;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.stream.Collectors;

public class LoadUser extends BukkitRunnable {

    UUID uuid;

    public LoadUser(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void run() {
        EconUser user = Queries.getEconUser(uuid);
        if (Config.DEBUG)
            Logger.info("Loaded EconUser % with the following points:\n%",
                    uuid.toString(), user.getPointsMap().object2ObjectEntrySet().stream()
                            .map(entry -> entry.getKey() + " - " + entry.getValue().toString())
                            .collect(Collectors.joining("\n")));
        int minutes = Queries.getMinutesSinceUpdated(uuid);
        user.removePoints(minutes * 2);
    }
}
