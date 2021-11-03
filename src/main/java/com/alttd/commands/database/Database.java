package com.alttd.commands.database;

import com.alttd.VillagerUI;
import com.alttd.config.Config;
import com.alttd.util.Logger;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    public static Connection connection = null;

    public static void init() { //Not static so we know for sure it loads on time
        String url = "jdbc:" + Config.DRIVER +
                "://" + Config.IP +
                ":" + Config.PORT +
                "/" + Config.DATABASE_NAME +
                "?autoReconnect=true&useSSL=false";
        try {
            connection = DriverManager.getConnection(url, Config.USERNAME, Config.PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.severe("Connection to database failed!");
            connection = null;
            Logger.severe("Shutting down VillagerUI");
            Bukkit.getPluginManager().disablePlugin(VillagerUI.getInstance());
            return;
        }

        // Tables
        createUserPointsTable();
    }

    private static void createUserPointsTable() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS user_points(" +
                    "UUID varchar(36) NOT NULL, " +
                    "points int NOT NULL, " +
                    "villager_type varchar(128) NOT NULL, " +
                    "PRIMARY KEY (UUID), " +
                    "UNIQUE KEY (villager_type)" +
                    ")";
            connection.prepareStatement(sql).executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.severe("Error while trying to create user point table");
            Logger.severe("Shutting down VillagerUI");
            Bukkit.getPluginManager().disablePlugin(VillagerUI.getInstance());
        }
    }

}
