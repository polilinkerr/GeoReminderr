package com.yahoo.berniak.georeminderr;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;

import java.util.Random;


/**
 * Created by krzysztofberniak on 23.09.16.
 */
public class CustomArrayAdapter extends CursorAdapter {

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
        ImageView IconFirstLetter = (ImageView) view.findViewById(R.id.IconFirstLetter);
        String titlee = cursor.getString(cursor.getColumnIndexOrThrow("title"));
        apptitle.setText(titlee);
        appdescription.setText(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"));
        double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"));
        appcoordinates.setText("X:"+latitude+" Y:"+longitude);

        String letter = titlee.substring(0,1);
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        TextDrawable drawable = TextDrawable.builder().buildRound(letter, color);


        IconFirstLetter.setImageDrawable(drawable);




    }


}