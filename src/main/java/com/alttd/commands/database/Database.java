package com.alttd.commands.database;

import com.alttd.VillagerUI;
import com.alttd.config.Config;
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
            VillagerUI.getInstance().getLogger().severe("Connection to database failed!");
            connection = null;
            VillagerUI.getInstance().getLogger().severe("Shutting down VillagerUI");
            Bukkit.getPluginManager().disablePlugin(VillagerUI.getInstance());
            return;
        }

        // Tables
        createUserPointsTable();
    }

    static void createUserPointsTable() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS user_points(" +
                    "UUID varchar 36 NOT NULL, " +
                    "Points int NOT NULL, " +
                    "PRIMARY KEY (UUID)" +
                    ")";
            connection.prepareStatement(sql).executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
