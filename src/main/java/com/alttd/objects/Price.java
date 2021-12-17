package com.alttd.objects;

import com.alttd.config.Config;
import com.alttd.util.Logger;
import com.alttd.util.Utilities;
import org.apache.commons.math3.analysis.function.StepFunction;
import org.apache.commons.math3.analysis.integration.TrapezoidIntegrator;

public final class Price {
    private final double price;
    private final int points;

    double x_mult[] = {0, 500, 2000, 4000};
    double y_mult[] = {1.0, 1.5, 2.5, 5.0};
    StepFunction multiplierModel = new StepFunction(x_mult, y_mult);

    public Price(double price) {
        this.price = price;
        for (int key : Config.pointsRangeMap.keySet()) {
            if (Config.pointsRangeMap.get(key).contains(price)) {
                points = key;
                return;
            }
        }
        Logger.severe("Points set to -1 for a price: %", String.valueOf(price));
        points = -1; //TODO check for if points is -1
    }

    public static Price addPrice(Price one, Price two) {
        return (new Price(Utilities.round(one.getPrice(1) + two.getPrice(1), 2)));
    }

    public double getPrice(int multiplier) {
        return (Utilities.round(price * multiplier, 2));
    }

    public double calculatePriceThing(int oldPoints, int transPts) {
        // Compute numerical integration to determine price
        TrapezoidIntegrator trapez = new TrapezoidIntegrator();
        return (price * trapez.integrate(10, multiplierModel, oldPoints, oldPoints + transPts));
    }

    public int getPoints() {
        return (points);
    }

}
