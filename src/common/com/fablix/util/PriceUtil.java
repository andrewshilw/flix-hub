package com.fablix.util;

public class PriceUtil {
    private PriceUtil() {}

    public static double priceForMovie(String movieId) {
        if (movieId == null) {
            return 9.99;
        }
        int hash = Math.abs(movieId.hashCode());
        double price = 5.0 + (hash % 2000) / 100.0; // 5.00 - 24.99
        return Math.round(price * 100.0) / 100.0;
    }
}
