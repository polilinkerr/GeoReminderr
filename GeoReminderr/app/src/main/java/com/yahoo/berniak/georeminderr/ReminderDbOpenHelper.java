package com.yahoo.berniak.georeminderr;

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
        String sql = "CREATE TABLE emp (_id INTEGER PRIMARY KEY, " + "name TEXT NOT NULL, "
                + "familyname TEXT NOT NULL, "
                + "position TEXT NOT NULL, "
                + "phone TEXT, "
                + "weblink TEXT); ";
        db.execSQL(sql);

        sql="INSERT INTO emp VALUES (null, 'Przemek', 'Kadela', 'agile coach', '997', 'sabre.pl'); ";
        db.execSQL(sql);
        sql="INSERT INTO emp VALUES (null, 'Beata', 'Szydlo', 'premier', '112', 'premier.gov.pl'); ";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }
}
