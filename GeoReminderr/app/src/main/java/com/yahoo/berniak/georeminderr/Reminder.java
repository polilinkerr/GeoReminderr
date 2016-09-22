package com.yahoo.berniak.georeminderr;

/**
 * Created by krzysztofberniak on 22.09.16.
 */
public class Reminder {
    private long id;
    private String title;
    private String description;
    private double latitude;
    private double longitude;



    Reminder(long id, String title, String description, double latitude, double longitude) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;



    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    public Reminder() {

    }
    @Override
    public String toString() {
        return title + " " + description + " ("  + ")";
    }
}
