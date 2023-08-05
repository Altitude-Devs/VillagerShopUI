package com.alttd.objects;

import java.util.HashSet;
import java.util.Set;

public class VillagerTypeManager {
    private static final Set<VillagerType> villagerTypes = new HashSet<>();

    public static Set<VillagerType> getVillagerTypes() {
        return villagerTypes;
    }

    public static VillagerType getVillagerType(String name) {
        return villagerTypes.stream().filter(villagerType -> villagerType.getName().equals(name)).findFirst().orElse(null);
    }

    public static void addVillagerType(VillagerType villagerType) {
        villagerTypes.add(villagerType);
    }

    public static void clearVillagerTypes() {
        villagerTypes.clear();
    }
}
