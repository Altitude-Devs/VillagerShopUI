package com.alttd.objects;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoadedVillagers {
    private final static Map<UUID, VillagerType> loadedVillagers = new HashMap<>();

    public static VillagerType getLoadedVillager(UUID uuid) {
        return loadedVillagers.get(uuid);
    }

    public static void addLoadedVillager(UUID uuid, VillagerType villagerType) {
        loadedVillagers.put(uuid, villagerType);
    }

    public static void removeLoadedVillager(UUID uuid) {
        loadedVillagers.remove(uuid);
    }

    public static void clearLoadedVillagers() {
        loadedVillagers.clear();
    }
}
