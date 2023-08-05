package com.alttd.objects;

import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public interface VillagerType {

    String getName();

    String getDisplayName();

    Set<ItemStack> getBuying();

    Set<ItemStack> getSelling();

    Villager.Profession getProfession();

    String getPermission();
}
