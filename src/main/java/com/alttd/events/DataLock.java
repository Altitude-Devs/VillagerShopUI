package com.alttd.events;

import com.alttd.VillagerUI;
import com.alttd.config.Config;
import com.alttd.datalock.LockResponseEvent;
import com.alttd.util.LoadUser;
import com.alttd.util.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class DataLock implements Listener {

    @EventHandler
    public void onLockResponseEvent(LockResponseEvent event) {
        UUID uuid;
        try {
            uuid = UUID.fromString(event.getData());
        } catch (Exception e) {
            Logger.warning("Invalid data received from lock response event [%]", event.getData());
            return;
        }
        if (Config.DEBUG) {
            Logger.info("Received lock response: channel: [%], response type: [%], data: [%]", event.getChannel(), event.getResponseType().toString(), event.getData());
        }
        switch (event.getResponseType()) {
            case TRY_LOCK_RESULT -> {
                if (!event.getResult())
                    return;
                new LoadUser(uuid).runTaskAsynchronously(VillagerUI.getInstance());
            }
            case QUEUE_LOCK_FAILED -> {
                Logger.warning("Unable to queue lock");
            }
            case TRY_UNLOCK_RESULT -> {
                if (event.getResult())
                    return;
                Logger.warning("Unable to unlock user [%]", uuid.toString());
            }
            default -> Logger.warning("Received unimplemented response type [%]", event.getResponseType().toString());
        }
    }
}
