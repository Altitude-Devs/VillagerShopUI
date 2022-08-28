package com.alttd.objects;

import com.alttd.VillagerUI;
import com.alttd.config.Config;
import com.alttd.database.Queries;
import com.alttd.util.Logger;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class EconUser {

    private static Object2ObjectOpenHashMap<UUID, EconUser> users = new Object2ObjectOpenHashMap<>();
    private final static Queue<EconUser> addQueue = new LinkedBlockingQueue<>();
    private final static Queue<EconUser> removeQueue = new LinkedBlockingQueue<>();

    private final UUID uuid;
    private final Object2ObjectOpenHashMap<String, Integer> pointsMap;

    public EconUser(UUID uuid, Object2ObjectOpenHashMap<String, Integer> points) {
        this.uuid = uuid;
        this.pointsMap = points;
        addQueue.offer(this);
        updateUsers();
        if (Config.DEBUG)
            Logger.info("Created EconUser for: %", uuid.toString());
    }

    public static void removeQueriedUser(UUID uuid) {
        queriedUsers.remove(uuid);
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

    //Can return null
    public static EconUser getUser(UUID uuid) {
        return (users.get(uuid));
    }

    private static HashSet<UUID> queriedUsers = new HashSet<>();
    public static void tryLoadUser(UUID uuid) {
        if (queriedUsers.contains(uuid) || users.containsKey(uuid))
            return;
        queriedUsers.add(uuid);
        new BukkitRunnable() {
            @Override
            public void run() {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("try-lock");
                out.writeUTF(uuid.toString());
                Bukkit.getServer().sendPluginMessage(VillagerUI.getInstance(),
                        "villagerui:player-data",
                        out.toByteArray());
            }
        }.runTaskAsynchronously(VillagerUI.getInstance());
    }

    public static void removeUser(UUID uuid) {
        queriedUsers.remove(uuid);
        if (Config.DEBUG)
            Logger.info("Unloading EconUser %", uuid.toString());
        EconUser user = users.get(uuid);
        if (user == null)
            return;
        removeQueue.offer(user);
        updateUsers();
    }

    private static void updateUsers() {
        if (addQueue.isEmpty() && removeQueue.isEmpty())
            return;
        Object2ObjectOpenHashMap<UUID, EconUser> tmp = new Object2ObjectOpenHashMap<>(users);
        while (true) {
            EconUser user = addQueue.poll();
            if (user == null)
                break;
            tmp.put(user.getUuid(), user);
        }
        while (true) {
            EconUser user = addQueue.poll();
            if (user == null)
                break;
            tmp.remove(user.getUuid());
        }
        users = tmp;
    }

    @Unmodifiable
    public static List<EconUser> getEconUsers() {
        return Collections.unmodifiableList(users.values().stream().toList());
    }
}
