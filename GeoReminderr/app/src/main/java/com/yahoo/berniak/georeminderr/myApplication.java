package com.yahoo.berniak.georeminderr;

import android.app.Application;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by krzysztofberniak on 23.09.16.
 */
public class MyApplication extends Application {

    private static double latitude = 0.0;
    private static double longitude = 0.0;
    private  LatLng coordinates = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
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

    public LatLng getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }
}
