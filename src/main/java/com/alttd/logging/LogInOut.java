package com.alttd.logging;

import com.alttd.VillagerUI;
import com.alttd.config.Config;
import com.alttd.util.Logger;
import com.alttd.util.Utilities;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Scanner;

public class LogInOut extends BukkitRunnable {

    private final HashMap<String, Double> map;
    private int day;
    private File file;
    private long nextExecution;

    public LogInOut() {
        createLogsDir();
        day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        file = new File(getPath());
        if (file.exists())
            map = loadFile(file);
        else
            map = new HashMap<>();
        this.nextExecution = Utilities.getNextXMinuteTime(Config.LOG_TIME);
    }

    public void log(String material, double cost) {
        if (map.containsKey(material))
            map.put(material, map.get(material) + cost);
        else
            map.put(material, cost);
    }

    private HashMap<String, Double> loadFile(File file) {
        HashMap<String, Double> map = new HashMap<>();
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] split = line.split(": ");
                if (split.length != 2) {
                    Logger.warning("Invalid entry in money-used log: %", line);
                    continue;
                }
                map.put(split[0], Double.parseDouble(split[1]));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public void run() {
        if (this.isCancelled())
            return;
        if (System.currentTimeMillis() < nextExecution)
            return;
        nextExecution = Utilities.getNextXMinuteTime(Config.LOG_TIME);
        int new_day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        if (!file.exists()) {
            boolean success = false;
            try {
                success = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!success) {
                Logger.warning("Unable to log money used because the file couldn't be created");
                return;
            }
        }
        try {
            FileWriter fileWriter = new FileWriter(file, false);
            String text = getText();
            if (text != null)
                fileWriter.write(text);
            else if (Config.DEBUG)
                Logger.info("Didn't write to used money log as map was empty");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (new_day != day) {
            day = new_day;
            map.clear();
            file = new File(getPath());
        }
    }

    private String getText() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : map.keySet())
            stringBuilder.append(key).append(": ").append(Utilities.round(map.get(key), 2)).append("\n");
        return stringBuilder.length() > 2 ? stringBuilder.substring(0, stringBuilder.length() - 1) : null;
    }

    private void createLogsDir() {
        File file = new File(VillagerUI.getInstance().getDataFolder() + File.separator + "logs");
        if (!file.exists()) if (!file.mkdir())
            Logger.warning("Unable to create logs folder");
    }

    private String getPath() {
        return VillagerUI.getInstance().getDataFolder() + File.separator + "logs" + File.separator + getDateStringYYYYMMDD() + "-money-used-log.txt";
    }

    private String getDateStringYYYYMMDD() {

        int day = LocalDate.now().getDayOfMonth();
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();

        String date = "";

        date = date.concat(String.valueOf(year));
        date = date.concat("-");

        if (month < 10) {
            date = date.concat("0");
        }
        date = date.concat(String.valueOf(month));
        date = date.concat("-");

        if (day < 10) {
            date = date.concat("0");
        }
        date = date.concat(String.valueOf(day));

        return date;

    }
}
