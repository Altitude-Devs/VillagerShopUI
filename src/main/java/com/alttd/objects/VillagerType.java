package com.alttd.objects;

import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class VillagerType {
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
    private final String name;
    private final String displayName;
    private final Set<ItemStack> buying;
    private final Set<ItemStack> selling;
    private final Villager.Profession profession;

    public VillagerType(String name, String displayName, TreeSet<ItemStack> buying, TreeSet<ItemStack> selling, String profession) {
        this.name = name;
        this.displayName = displayName;
        this.buying = buying;
        this.selling = selling;
        this.profession = Villager.Profession.valueOf(profession.toUpperCase());
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Set<ItemStack> getBuying() {
        return buying;
    }

    public Set<ItemStack> getSelling() {
        return selling;
    }

    public Villager.Profession getProfession() {
        return profession;
    }

    public String getPermission() {
        return "villagerui.villager." + getName();
    }
}
