package com.alttd.objects;

import com.alttd.config.WorthConfig;
import com.alttd.util.Utilities;

public final class Price {
    private final double price;
    private final int points;

    private static final double[] xMult = {Integer.MIN_VALUE, -6000, -4000, -2000, -500, 500, 2000, 4000, 6000, Integer.MAX_VALUE};
    private static final double[] yMultBuy = {0.5, 0.67, 0.8, 0.9, 1, 1.5, 2.5, 5, 10};
    private static final double[] yMultSell = {0.1, 0.2, 0.4, 0.67, 1, 1.1, 1.25, 1.5, 2};

    public Price(double price) {
        this.price = price;
        this.points = (int) (Math.floor(price / WorthConfig.POINT_MOD) + 1);
    }

    public static Price addPrice(Price one, Price two) {
        return (new Price(Utilities.round(one.getPrice(1) + two.getPrice(1), 2)));
    }

    public static double getCurrentMultiplier(int points, boolean buy) {
        for (int i = 1; i < xMult.length; i++) {
            if (points <= xMult[i])
                return buy ? yMultBuy[i - 1] : yMultSell[i - 1];
        }
        return 0;
    }

    public double getPrice(int multiplier) {
        return (Utilities.round(price * multiplier, 2));
    }

    public double calculatePriceThing(int oldPoints, int transPts, boolean buying, int itemPts) {
        double finalPrice = 0; //Initialize final price
        int segment = 1; //Start segment at one
        int high = oldPoints + transPts; //Will be the highest point value

        if (oldPoints > high) //If high is not the highest point value, swap it with oldPoints so it is
        {
            int temp = oldPoints;
            oldPoints = high;
            high = temp;
        }

        while (oldPoints > xMult[segment] && segment < xMult.length - 1) { //Calculate the start segment (first value smaller than lower)
            segment++;
        }

        for (int i = segment; i < xMult.length && high > xMult[i - 1]; i++)
            finalPrice += getPricePerInterval(oldPoints, high, i, buying, itemPts);
        return Utilities.round(finalPrice, 2);
    }

    private double getPricePerInterval(int start_points, int end_points, int segment, boolean buying, int itemPts) {
        double bottom = xMult[segment - 1];
        double top = xMult[segment];
        double priceMult = buying ? yMultBuy[segment - 1] : yMultSell[segment - 1];
        double pricePerPoint = price / itemPts;

        if (start_points <= bottom && end_points <= top)// +_---+---
            return (end_points - bottom) * pricePerPoint * priceMult;
        else if (start_points <= bottom && end_points >= top) // +_---_+
            return (top - bottom) * pricePerPoint * priceMult;
        else if (start_points >= bottom && end_points <= top) // _--+--+--_
            return (end_points - start_points) * pricePerPoint * priceMult;
        else if (start_points >= bottom && end_points >= top) // _--+--_+
            return (top - start_points) * pricePerPoint * priceMult;
        else
            return 0;
    }

    public int getPoints() {
        return (points);
    }

}
