package com.fablix.util;

import com.fablix.model.CartItem;

import java.util.LinkedHashMap;
import java.util.Map;

public class CartUtil {
    private CartUtil() {}

    public static Map<String, CartItem> ensureCart(Object cartObj) {
        if (cartObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, CartItem> cart = (Map<String, CartItem>) cartObj;
            return cart;
        }
        return new LinkedHashMap<>();
    }

    public static double computeTotal(Map<String, CartItem> cart) {
        double total = 0.0;
        if (cart == null) {
            return total;
        }
        for (CartItem item : cart.values()) {
            total += item.getPrice() * item.getQuantity();
        }
        return Math.round(total * 100.0) / 100.0;
    }
}
