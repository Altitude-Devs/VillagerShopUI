package com.alttd.objects;

import java.util.Random;

public class PriceRange {
    private final double lowerBound;
    private final double upperBound;
    private final Random random;

    public PriceRange(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.random = new Random();
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public double getRandomPrice() {
        return lowerBound + (random.nextDouble() * (upperBound - lowerBound));
    }
}
