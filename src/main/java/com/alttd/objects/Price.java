package com.alttd.objects;

import com.alttd.config.Config;
import com.alttd.util.Utilities;

public final class Price {
    private final double price;
    private final int points;

    public Price(double price) {
        this.price = price;
        for (int key : Config.pointsRangeMap.keySet()) {
            if (Config.pointsRangeMap.get(key).contains(price)) {
                points = key;
                return;
            }
        }
        points = -1; //TODO check for if points is -1
    }

    public static Price addPrice(Price one, Price two) {
        return (new Price(Utilities.round(one.getPrice(1) + two.getPrice(1), 2)));
    }

    public double getPrice(int multiplier) {
        return (Utilities.round(price * multiplier, 2));
    }

    public int getPoints() {
        return (points);
    }

}
