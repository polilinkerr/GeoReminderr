package com.yahoo.berniak.georeminderr;

/**
 * Created by krzysztofberniak on 26.09.16.
 */
public class HelpCalculation {

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
