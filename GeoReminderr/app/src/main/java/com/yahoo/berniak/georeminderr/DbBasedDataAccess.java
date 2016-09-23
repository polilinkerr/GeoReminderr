package com.yahoo.berniak.georeminderr;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.CursorAdapter;

import java.util.ArrayList;
import java.util.List;


public class DbBasedDataAccess extends DataAccess {
    private final SQLiteOpenHelper dbOpenHelper;
    private SQLiteDatabase database;

    DbBasedDataAccess(Context ctx) {
        super(ctx);
        dbOpenHelper = new ReminderDbOpenHelper(ctx);
        database = dbOpenHelper.getWritableDatabase();
    }


    @Override
    public List<Reminder> getAllReminders() {
        List<Reminder> result = new ArrayList<Reminder>();
        try (Cursor c = database.query("emp", null /* all */,
                null, null, null, null, "_id", null)) {

            while (c.moveToNext()) {
                Reminder e = new Reminder();
                e.setId(c.getLong(0));
                e.setTitle(c.getString(1));
                e.setDescription(c.getString(2));
                e.setLatitude(c.getDouble(3));
                e.setLongitude(c.getDouble(4));
                result.add(e);
            }
        }

        return result;
    }




    @Override
    public long insert(Reminder e) {
        ContentValues cv = new ContentValues();
        cv.put("title", e.getTitle());
        cv.put("description", e.getDescription());
        cv.put("latitude", e.getLatitude());
        cv.put("longitude", e.getLongitude());
        long id = database.insert("emp", null, cv);
        e.setId(id);
        return id;
    }

    @Override
    public void update(Reminder e) {
        ContentValues cv = new ContentValues();
        cv.put("title", e.getTitle());
        cv.put("description", e.getDescription());
        cv.put("latitude", e.getLatitude());
        cv.put("longitude", e.getLongitude());


        database.update("emp", cv, "_id="+e.getId(), null);
    }

    @Override
    public void delete(long id) {
        database.delete("emp", "_id="+id, null);

    }

    @Override
    public Reminder getById(long id) {
        try (Cursor c = database.query("emp", null /* all */,
                "_id=" + id, null, null, null,"_id", null)) {

            if (c.moveToNext()) {
                Reminder e = new Reminder();
                e.setId(c.getLong(0));
                e.setTitle(c.getString(1));
                e.setDescription(c.getString(2));
                e.setLatitude(c.getDouble(3));
                e.setLongitude(c.getDouble(4));


                return e;
            } else {
                return null;
            }
        }

    }

    public Cursor getCursor(){
        Cursor c = database.query("emp", null /* all */,
                null, null, null, null, "_id", null);
        return c;



    }


}
