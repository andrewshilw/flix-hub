package com.fablix.model;

public class CartItem {
    private final String movieId;
    private final String title;
    private final double price;
    private int quantity;

    public CartItem(String movieId, String title, double price, int quantity) {
        this.movieId = movieId;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
    }

    public String getMovieId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
