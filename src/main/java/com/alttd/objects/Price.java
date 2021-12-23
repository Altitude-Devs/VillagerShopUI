package com.alttd.objects;

import com.alttd.util.Utilities;
import org.apache.commons.math3.analysis.function.StepFunction;
import org.apache.commons.math3.analysis.integration.TrapezoidIntegrator;

public final class Price {
    private final double price;
    private final int points;

    private static final double[] xMult = {Integer.MIN_VALUE, -4000, -2000, -500, 500, 2000, 4000};
    private static final double[] yMultSell = {2.5, 1.75, 1.25, 1, 1.5, 2.5, 5};
    private static final double[] yMultBuy = {5, 2.5, 1.5, 1, 1.25, 1.75, 2.5};

    private static final StepFunction multiplierModelBuy = new StepFunction(xMult, yMultBuy);
    private static final StepFunction multiplierModelSell = new StepFunction(xMult, yMultSell);

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

    public double calculatePriceThing(int oldPoints, int transPts, boolean buying) {
        // Compute numerical integration to determine price
        TrapezoidIntegrator trapez = new TrapezoidIntegrator();
        if (buying) {
            return (Utilities.round(price * trapez.integrate(10, multiplierModelBuy, oldPoints, oldPoints + transPts), 2));
        } else {
            return (Utilities.round(price * trapez.integrate(10, multiplierModelSell, oldPoints - transPts, oldPoints), 2));
        }
    }

    public int getPoints() {
        return (points);
    }

}
