package com.yahoo.berniak.georeminderr;

import android.content.Context;
import android.database.Cursor;

import java.util.List;

public abstract class DataAccess {

    protected final Context context;

    public static DataAccess create(Context ctx) {
        return new DbBasedDataAccess(ctx);
    }


    protected DataAccess(Context ctx) {
        context = ctx;
    }

    public abstract List<Reminder> getAllReminders();

    public abstract long insert(Reminder e);

    public abstract void update(Reminder e);

    public void delete(Reminder e) {
        delete(e.getId());
    }

    public abstract void delete(long id);

    public abstract Reminder getById(long id);

    public abstract Cursor getCursor();

    public abstract Cursor getbyIdElements(String dbField, String fieldValue);

}



