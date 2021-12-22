package com.alttd.objects;

import com.alttd.VillagerUI;
import com.alttd.database.Queries;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.Objects;
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
            pointsMap.put(villagerType, Objects.requireNonNullElse(pointsMap.get(villagerType), 0) + points);
    }

    public void syncPoints() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Queries.updateUserPoints(uuid, pointsMap);
            }
        }.runTaskAsynchronously(VillagerUI.getInstance());
    }

    public void removePoints(int remove) {
        pointsMap.forEach((villagerType, points) -> {
            if (points == 0)
                return;
            if (points > 0)
                if (points < remove)
                    points = 0;
                else
                    points -= remove;
            else
                if (-points < remove)
                    points = 0;
                else
                    points += remove;
            pointsMap.put(villagerType, points);
        });
    }

    public static EconUser getUser(UUID uuid) {
        EconUser user = users.get(uuid);
        if (user == null) {
            user = Queries.getEconUser(uuid);
            EconUser.users.put(uuid, user);
        }
        return (user);
    }

    public static void removeUser(UUID uuid) {
        users.remove(uuid);
    }

    public static void syncAllPoints() {
        Collections.unmodifiableMap(users).values().forEach(EconUser::syncPoints);
    }
}
