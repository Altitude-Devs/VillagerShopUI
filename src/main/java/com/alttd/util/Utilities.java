package com.alttd.util;

public class Utilities {
    /**
     * Rounds num down to precision (rounds up if last cut off decimal is bigger than 4)
     *
     * @param num       value to be rounded
     * @param precision length to round to
     * @return num rounded
     */
    public static double round(double num, int precision) {
        double scale = Math.pow(10, precision);
        double total = (double) (Math.round(num * scale)) / scale;

        scale = (int) Math.pow(10, precision + 1);
        long tmp = (Math.round(num * scale));

        while (tmp > 10)
            tmp /= 10;
        if (tmp > 4)
            total += 0.01;

        return total;
    }
}
