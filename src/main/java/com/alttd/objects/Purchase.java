package com.alttd.objects;

import org.bukkit.Material;

public record Purchase(Material material, double price, int singlePointCost, int totalPointCost, int amount) {
}
