package com.alttd.objects;

import com.alttd.util.Utilities;

public record Price(double price, int points) {
    public static Price addPrice(Price one, Price two) {
        return (new Price(Utilities.round(one.price() + two.price(), 2), one.points() + two.points()));
    }

    public double getPrice(int multiplier) {
        return (Utilities.round(price * multiplier, 2));
    }

    public int getPoints() {
        return (points);
    }
}
