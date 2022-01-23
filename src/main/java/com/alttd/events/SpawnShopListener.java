package com.alttd.events;

import com.alttd.logging.LogInOut;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SpawnShopListener implements Listener {

    private final LogInOut log;
    public SpawnShopListener(LogInOut log) {
        this.log = log;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawnShopEvent(SpawnShopEvent event) {
        log.log(event.item().name(), event.price());
    }

}
