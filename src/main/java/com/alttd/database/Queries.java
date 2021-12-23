package com.alttd.database;

import com.alttd.objects.EconUser;
import com.alttd.objects.VillagerType;
import com.alttd.util.Logger;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Queries {

    /**
     * NOTE: run async
     * Add a specified amount of points to the user for the given villager type
     *
     * @param   uuid        Uuid for the user you want to add the points to
     * @param   pointsMap   Contains all (villagerType, points) entries for user
     * @return  success
     */
    public static boolean updateUserPoints(UUID uuid, Object2ObjectArrayMap<String, Integer> pointsMap) {
        String sql = "INSERT INTO user_points " +
                "(uuid, villager_type, points) " +
                "VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE points = ?";

        try {
            PreparedStatement preparedStatement = Database.connection.prepareStatement(sql);
            preparedStatement.setString(1, uuid.toString());
            pointsMap.forEach((villagerType, points) -> {
                try {
                    preparedStatement.setString(2, villagerType);
                    preparedStatement.setInt(3, points);
                    preparedStatement.setInt(4, points);
                    preparedStatement.addBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                    Logger.warning("Unable to add % points to %" +
                            " for villager type %", String.valueOf(points), uuid.toString(), villagerType);
                }
            });
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return (false);
        }
        setLastUpdated(uuid);
        return (true);
    }

    /**
     * Get the econ user
     * @param   uuid    UUID of the user to get
     *
     * @return  EconUser
     */
    public static EconUser getEconUser(UUID uuid) {
        String sql = "SELECT * FROM user_points WHERE uuid = ?";

        try {
            PreparedStatement preparedStatement = Database.connection.prepareStatement(sql);
            preparedStatement.setString(1, uuid.toString());

            ResultSet resultSet = preparedStatement.executeQuery();
            Object2ObjectArrayMap<String, Integer> points = new Object2ObjectArrayMap<>();
            while (resultSet.next()) {
                points.put(
                        resultSet.getString("villager_type"),
                        resultSet.getInt("points"));
            }
            return (new EconUser(uuid, points));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return (null);
    }

    /**
     * Set last seen to current time for user
     * @param   uuid    UUID of user to update last seen for
     */
    private static void setLastUpdated(UUID uuid) {
        String sql = "INSERT INTO user_seen " +
                "(uuid, seen) " +
                "VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE seen = ?";
        long time = new Date().getTime();

        try {
            PreparedStatement preparedStatement = Database.connection.prepareStatement(sql);
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setLong(2, time);
            preparedStatement.setLong(3, time);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.warning("Unable to set last updated time for %.", uuid.toString());
        }
    }

    /**
     * Get last seen for user
     * @param   uuid    UUID of user to get last seen for
     *
     * @return  minutes since last seen
     */
    public static int getMinutesSinceUpdated(UUID uuid) {
        String sql = "SELECT seen FROM user_seen WHERE uuid = ?";
        long time;

        try {
            PreparedStatement preparedStatement = Database.connection.prepareStatement(sql);
            preparedStatement.setString(1, uuid.toString());

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                time = resultSet.getLong("seen");
            else
                return (0);
            if (time != 0)
                return (int) TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - time);
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.warning("Unable to set last updated time for %.", uuid.toString());
        }

        return (0);
    }
}
