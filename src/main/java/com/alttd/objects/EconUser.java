package com.alttd.objects;

import com.alttd.commands.database.Queries;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.util.UUID;

public class EconUser {

    public static Object2ObjectArrayMap<UUID, EconUser> users = new Object2ObjectArrayMap<>();

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
        Queries.updateUserPoints(uuid, villagerType, pointsMap.get(villagerType));
    }
}
