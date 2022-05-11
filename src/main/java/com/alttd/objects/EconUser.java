package com.alttd.objects;

import com.alttd.VillagerUI;
import com.alttd.config.Config;
import com.alttd.database.Queries;
import com.alttd.util.Logger;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.ByteArrayOutputStream;
import java.util.*;

public class EconUser {

    private final static Object2ObjectArrayMap<UUID, EconUser> users = new Object2ObjectArrayMap<>();

    private final UUID uuid;
    private final Object2ObjectArrayMap<String, Integer> pointsMap;

    public EconUser(UUID uuid, Object2ObjectArrayMap<String, Integer> points) {
        this.uuid = uuid;
        this.pointsMap = points;
        users.put(this.uuid, this);
        if (Config.DEBUG)
            Logger.info("Created EconUser for: %", uuid.toString());
    }

    public UUID getUuid() {
        return uuid;
    }

    public Object2ObjectArrayMap<String, Integer> getPointsMap() {
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
        if (pointsMap.get(villagerType) < 0)
            points *= -1;
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
        if (queriedUsers.contains(uuid) && !users.containsKey(uuid))
            return;
        queriedUsers.add(uuid);
        new BukkitRunnable() {
            @Override
            public void run() {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("try-lock");
                out.writeUTF(uuid.toString());
                Bukkit.getServer().sendPluginMessage(VillagerUI.getInstance(),
                        "VillagerUI:player-data",
                        out.toByteArray());
            }
        }.runTaskAsynchronously(VillagerUI.getInstance());
    }

    //Might need to be locked down better?
    public static void loadUser(UUID uuid) {
        new BukkitRunnable() {
            @Override
            public void run() {
                EconUser user = Queries.getEconUser(uuid);

                int minutes = Queries.getMinutesSinceUpdated(uuid);
                user.removePoints(minutes * 2);
                if (Config.DEBUG)
                    Logger.info("Loaded EconUser for % and removed % points",
                            uuid.toString(), String.valueOf(minutes * 2));

                EconUser.users.put(uuid, user);
                queriedUsers.remove(uuid);
            }
        }.runTaskAsynchronously(VillagerUI.getInstance());
    }

    public static void removeUser(UUID uuid) {
        users.remove(uuid);
    }

    @Unmodifiable
    public static List<EconUser> getEconUsers() {
        return Collections.unmodifiableList(users.values().stream().toList());
    }
}
