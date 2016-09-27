package com.yahoo.berniak.georeminderr;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by krzysztofberniak on 22.09.16.
 */
public class ReminderDbOpenHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "employees";

    public ReminderDbOpenHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE emp (_id INTEGER PRIMARY KEY,title TEXT, description TEXT,latitude REAL, longitude REAL, adress TEXT); ";
        db.execSQL(sql);
        insertReminder(db, "Uczelnia", "Rozmowa z Profesorem", 23.4, 321.32,"Akacjowa");
    }
    private static void insertReminder(SQLiteDatabase db, String name, String description, double latitude, double longitude, String adress) {
        ContentValues ReminderValue = new ContentValues();
        ReminderValue.put("title", name);
        ReminderValue.put("description", description);
        ReminderValue.put("latitude", latitude);
        ReminderValue.put("longitude", longitude);
        ReminderValue.put("adress", adress);
        db.insert("emp", null, ReminderValue);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }
}
