package com.fablix.model;

public class Movie {
    private String id;
    private String title;
    private String year;
    private String director;
    private String rating;
    private String genres; // Store as comma-separated string for now
    private String stars;  // Store as comma-separated string for now
    private String starIds; // Store IDs to build hyperlinks

    // Constructor, Getters, and Setters
    public Movie(String id, String title, String year, String director,
                 String rating, String genres, String stars, String starIds) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        this.rating = rating;
        this.genres = genres;
        this.stars = stars;
        this.starIds = starIds;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getYear() { return year; }
    public String getDirector() { return director; }
    public String getRating() { return rating; }
    public String getGenres() { return genres; }
    public String getStars() { return stars; }
    public String getStarIds() { return starIds; }
}