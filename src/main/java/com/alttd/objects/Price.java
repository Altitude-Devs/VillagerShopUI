package com.alttd.objects;

import com.alttd.util.Utilities;
import org.apache.commons.math3.analysis.function.StepFunction;
import org.apache.commons.math3.analysis.integration.TrapezoidIntegrator;

public final class Price {
    private final double price;
    private final int points;

    private static final double x_mult[] = {0, 500, 2000, 4000};
    private static final double y_mult[] = {1.0, 1.5, 2.5, 5.0};
    private static final StepFunction multiplierModel = new StepFunction(x_mult, y_mult);

    public Price(double price) {
        this.price = price;
        this.points = (int) price;
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
        return (Utilities.round(price * trapez.integrate(10, multiplierModel, oldPoints, oldPoints + transPts), 2));
    }

    public int getPoints() {
        return (points);
    }

}
