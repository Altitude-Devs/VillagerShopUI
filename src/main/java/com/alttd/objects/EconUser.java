package com.alttd.objects;

import com.alttd.VillagerUI;
import com.alttd.database.Queries;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;
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

    private void removePoints(String villagerType, int points, int remove)
    {
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
    }

    public void removePoints(int remove) {
        pointsMap.forEach((villagerType, points) -> removePoints(villagerType, points, remove));
    }

    public void removePoints() {
        pointsMap.forEach((villagerType, points) -> {
            if (points == 0)
                return;
            int remove = points;
            if (remove < 0)
                remove *= -1;
            removePoints(villagerType, points, (int) (0.9 * remove) - 10);
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

    @Unmodifiable
    public static List<EconUser> getEconUsers() {
        return Collections.unmodifiableList(users.values().stream().toList());
    }
}
