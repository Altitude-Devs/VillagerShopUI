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
     * Get the points a user has for a villager type
     *
     * @param uuid         Uuid for the user you want to get the points for
     * @param villagerType Type of villager you want to get the points for
     * @return The amount of points a user has for the given villager type
     */
    public static int getUserPoints(UUID uuid, VillagerType villagerType) {
        String sql = "SELECT points FROM user_points " +
                "WHERE UUID = ? " +
                "AND villager_type = ?;";
        int points = 0;

        try {
            PreparedStatement preparedStatement = Database.connection.prepareStatement(sql);
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setString(2, villagerType.getName());

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet != null && resultSet.next()) {
                points = resultSet.getInt("points");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return (points);
    }

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
        } catch (SQLException e) {
            e.printStackTrace();
            return (false);
        }
        setLastUpdated(uuid);
        return (true);
    }

    /**
     * NOTE: run async
     * Create a new user entry
     *
     * @param uuid         Uuid for the user you want to create an entry for
     * @param villagerType Type of villager you want to use in that entry
     * @param points       The amount of points to set the start to
     * @return success
     */
    public static boolean createUserPointsEntry(UUID uuid, String villagerType, int points) {
        String sql = "INSERT INTO user_points " +
                "(UUID, points, villager_type) " +
                "VALUES (?, ?, ?)";

        try {
            PreparedStatement preparedStatement = Database.connection.prepareStatement(sql);
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setInt(2, points);
            preparedStatement.setString(3, villagerType);

            return (preparedStatement.execute());
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.warning("Unable to create point entry for %" +
                    " for villager type %", uuid.toString(), villagerType);
            return (false);
        }
    }

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
     * @param uuid
     */
    private static void setLastUpdated(UUID uuid) {
        String sql = "INSERT INTO user_seen " +
                "(uuid, seen) " +
                "VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE seen = ?";

        try {
            PreparedStatement preparedStatement = Database.connection.prepareStatement(sql);
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setLong(2, new Date().getTime());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.warning("Unable to set last updated time for %.", uuid.toString());
        }
    }

    /**
     * @param   uuid    UUID of user to get last seen for
     *
     * @return  minutes since last seen
     */
    public static int getMinutesSinceUpdated(UUID uuid) {
        String sql = "SELECT seen FROM user_seen WHERE uuid = ?";

        try {
            PreparedStatement preparedStatement = Database.connection.prepareStatement(sql);
            preparedStatement.setString(1, uuid.toString());

            ResultSet resultSet = preparedStatement.executeQuery();
            long time = resultSet.getLong("seen");
            if (time != 0)
                return (int) TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - time);
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.warning("Unable to set last updated time for %.", uuid.toString());
        }

        return (0);
    }
}
