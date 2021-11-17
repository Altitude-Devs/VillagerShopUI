package com.alttd.commands.database;

import com.alttd.VillagerUI;
import com.alttd.config.Config;
import com.alttd.util.Logger;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static Database instance = null;
    public static Connection connection = null;

    private Database() {

    }

    public static Database getDatabase(){
        if (instance == null)
            instance = new Database();
        return (instance);
    }

    public void init() {
        try {
            openConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Tables
        createUserPointsTable();
    }

    /**
     * Opens the connection if it's not already open.
     * @throws SQLException If it can't create the connection.
     */
    private void openConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + Config.IP + ":" + Config.PORT + "/" + Config.DATABASE_NAME +
                            "?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true",
                    Config.USERNAME, Config.PASSWORD);
        }
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
