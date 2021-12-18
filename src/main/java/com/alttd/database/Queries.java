package com.alttd.database;

import com.alttd.objects.EconUser;
import com.alttd.objects.VillagerType;
import com.alttd.util.Logger;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

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
     * @param uuid         Uuid for the user you want to add the points to
     * @param villagerType Type of villager you want to add the points to
     * @param points       The amount of points to add
     * @return success
     */
    public static boolean updateUserPoints(UUID uuid, String villagerType, int points) {
        String sql = "UPDATE Points = SET user_points = user_points + ? " +
                "WHERE UUID = ? " +
                "AND villager_type = ?;";

        try {
            PreparedStatement preparedStatement = Database.connection.prepareStatement(sql);
            preparedStatement.setInt(1, points);
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.setString(3, villagerType);

            if (preparedStatement.executeUpdate() == 0)
                return createUserPointsEntry(uuid, villagerType, points);
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.warning("Unable to add % points to %" +
                    " for villager type %", String.valueOf(points), uuid.toString(), villagerType);
            return (false);
        }
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
                "(uuid, villager_type, points) " +
                "(?, ?, ?)";

        try {
            PreparedStatement preparedStatement = Database.connection.prepareStatement(sql);
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setString(2, villagerType);
            preparedStatement.setInt(3, points);

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
}
