package com.yahoo.berniak.georeminderr;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;


/**
 * Created by krzysztofberniak on 23.09.16.
 */
public class CustomArrayAdapter extends CursorAdapter {

    private Geocoder geocoder;
    private List<Address> addresses = new ArrayList<Address>();
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String knownName;

    public CustomArrayAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.activity_main, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {


        TextView apptitle = (TextView) view.findViewById(R.id.appTitle);
        TextView appdescription = (TextView) view.findViewById(R.id.appDescription);
        TextView appcoordinates = (TextView) view.findViewById(R.id.appCoordinates);
        TextView appAdress = (TextView) view.findViewById(R.id.appAdress);
        ImageView IconFirstLetter = (ImageView) view.findViewById(R.id.IconFirstLetter);
        String titlee = cursor.getString(cursor.getColumnIndexOrThrow("title"));
        apptitle.setText(titlee);
        appdescription.setText(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"));
        double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"));
        appcoordinates.setText("X:"+HelpCalculation.round(latitude,4)+" Y:"+HelpCalculation.round(longitude,4));
        appAdress.setText(cursor.getString(cursor.getColumnIndexOrThrow("adress")));
        //appcoordinates.setText("adress:"+address+", city:"+city+", country:"+country);

        String letter = titlee.substring(0,1);
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        TextDrawable drawable = TextDrawable.builder().buildRound(letter, color);
        IconFirstLetter.setImageDrawable(drawable);

    }



}