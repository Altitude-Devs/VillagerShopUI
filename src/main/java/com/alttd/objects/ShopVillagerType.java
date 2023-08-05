package com.alttd.objects;

import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.TreeSet;

public class ShopVillagerType implements VillagerType{
    private final String name;
    private final String displayName;
    private final Set<ItemStack> buying;
    private final Set<ItemStack> selling;
    private final Villager.Profession profession;

    public ShopVillagerType(String name, String displayName, TreeSet<ItemStack> buying, TreeSet<ItemStack> selling, String profession) {
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
