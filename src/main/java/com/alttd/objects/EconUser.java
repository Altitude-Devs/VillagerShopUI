package com.alttd.objects;

import com.alttd.VillagerUI;
import com.alttd.config.Config;
import com.alttd.database.Queries;
import com.alttd.datalock.DataLockAPI;
import com.alttd.util.Logger;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class EconUser {

    private static Object2ObjectOpenHashMap<UUID, EconUser> users = new Object2ObjectOpenHashMap<>();

    private final UUID uuid;
    private final Object2ObjectOpenHashMap<String, Integer> pointsMap;

    public EconUser(UUID uuid, Object2ObjectOpenHashMap<String, Integer> points) {
        this.uuid = uuid;
        this.pointsMap = points;
        addUser(uuid, this);
        if (Config.DEBUG)
            Logger.info("Created EconUser for: %", uuid.toString());
    }

    private synchronized static void addUser(UUID uuid, EconUser econUser) {
        users.put(uuid, econUser);
    }

    public synchronized static EconUser getUser(UUID uuid) {
        return users.getOrDefault(uuid, null);
    }

    private synchronized static boolean containsUser(UUID uuid) {
        return users.containsKey(uuid);
    }

    private synchronized static void removeUserFromMap(UUID uuid) {
        users.remove(uuid);
    }

    public UUID getUuid() {
        return uuid;
    }

    public Object2ObjectOpenHashMap<String, Integer> getPointsMap() {
        return pointsMap;
    }

    public void addPoints(String villagerType, int points) {
        if (Config.DEBUG)
            Logger.info("Adding % points to % for %", String.valueOf(points), villagerType, uuid.toString());
        if (pointsMap.containsKey(villagerType))
            pointsMap.put(villagerType,pointsMap.get(villagerType) + points);
        else
            pointsMap.put(villagerType, points);
    }

    public void syncPoints() {
        if (Config.DEBUG)
            Logger.info("Saving points EconUser % currently has the following points:\n%",
                    uuid.toString(), getPointsMap().object2ObjectEntrySet().stream()
                            .map(entry -> entry.getKey() + " - " + entry.getValue().toString())
                            .collect(Collectors.joining("\n")));

        Queries.updateUserPoints(uuid, pointsMap);
    }

    private void removePoints(String villagerType, int points, int remove)
    {
        if (points == 0)
            return;
        if (points > 0) {
            if (points < remove)
                points = 0;
            else
                points -= remove;
        } else {
            if (-points < remove)
                points = 0;
            else
                points += remove;
        }
        pointsMap.put(villagerType, points);
        if (Config.DEBUG)
            Logger.info("Removed % points from villagerType: % for %",
                    String.valueOf(remove), villagerType, uuid.toString());
    }

    private void setPoints(String villagerType, int points) {
        if (pointsMap.getOrDefault(villagerType, 0) < 0)
            points *= -1;
        if (Config.DEBUG) {
            Logger.info("Set villagerType: % to % (was %) for &",
                    villagerType, String.valueOf(points),
                    String.valueOf(pointsMap.getOrDefault(villagerType, 0)), uuid.toString());
        }
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
            int i = (int) (0.93 * remove) - 30;
            setPoints(villagerType, i < 10 && i > -10 ? 0 : i);
        });
    }

    public static void tryLoadUser(UUID uuid) {
        new BukkitRunnable() {
            @Override
            public void run() {
                DataLockAPI.get().tryLock("villagerui:player-data", uuid.toString());
            }
        }.runTaskAsynchronously(VillagerUI.getInstance());
    }

    public static void removeUser(UUID uuid) {
        if (Config.DEBUG)
            Logger.info("Unloading EconUser %", uuid.toString());
//        EconUser user = getUser(uuid);
//        if (user == null)
//            return;
        removeUserFromMap(uuid);
    }

    @Unmodifiable
    public synchronized static List<EconUser> getEconUsers() {
        return Collections.unmodifiableList(users.values().stream().toList());
    }
}
