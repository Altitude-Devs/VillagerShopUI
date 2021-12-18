package com.alttd.objects;

import com.alttd.VillagerUI;
import com.alttd.database.Queries;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class EconUser {

    private final static Object2ObjectArrayMap<UUID, EconUser> users = new Object2ObjectArrayMap<>();

    private final UUID uuid;
    private final Object2ObjectArrayMap<String, Integer> pointsMap;

    public EconUser(UUID uuid, Object2ObjectArrayMap<String, Integer> points) {
        this.uuid = uuid;
        this.pointsMap = points;
        users.put(this.uuid, this);
    }

    public UUID getUuid() {
        return uuid;
    }

    public Object2ObjectArrayMap<String, Integer> getPointsMap() {
        return pointsMap;
    }

    public void addPoints(String villagerType, int points) {
        if (pointsMap.containsKey(villagerType))
            pointsMap.put(villagerType, points);
        else
            pointsMap.put(villagerType, pointsMap.get(villagerType) + points);
        new BukkitRunnable() {
            @Override
            public void run() {
                Queries.updateUserPoints(uuid, villagerType, pointsMap.get(villagerType));
            }
        }.runTaskAsynchronously(VillagerUI.getInstance());
    }

    public static EconUser getUser(UUID uuid) {
        EconUser user = users.get(uuid);
        return (user == null ? Queries.getEconUser(uuid) : user);
    }
}
