package com.alttd.objects;

import org.bukkit.inventory.ItemStack;

import java.util.Comparator;

public class ItemStackComparator implements Comparator<ItemStack> {
    @Override
    public int compare(ItemStack o1, ItemStack o2) {
        return o1.getType().name().compareTo(o2.getType().name());
    }
}
