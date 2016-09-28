package edu.uj.android.employees;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbBasedDataAccess extends DataAccess {

    private final SQLiteOpenHelper dbOpenHelper;
    private SQLiteDatabase database;

    DbBasedDataAccess(Context ctx) {
        super(ctx);
        dbOpenHelper = new EmployeeDbOpenHelper(ctx);
        database = dbOpenHelper.getWritableDatabase();
    }

    @Override
    public List<Employee> getAllEmployees() {
        List<Employee> result = new ArrayList<Employee>();
        try (Cursor c = database.query("emp", null /* all */, 
                null, null, null, null, "familyname", null)) {

            while (c.moveToNext()) {
                Employee e = new Employee();
                e.setId(c.getLong(0));
                e.setName(c.getString(1));
                e.setFamilyName(c.getString(2));
                e.setPosition(c.getString(3));
                e.setPhone(c.getString(4));
                e.setWebLink(c.getString(5));

                result.add(e);
            }
        }

        return result;
    }

    @Override
    public long insert(Employee e) {
        ContentValues cv = new ContentValues();
        cv.put("name", e.getName());
        cv.put("familyname", e.getFamilyName());
        cv.put("position", e.getPosition());
        cv.put("phone", e.getPhone());
        cv.put("weblink", e.getWebLink());
        long id = database.insert("emp", null, cv);
        e.setId(id);
        return id;
    }

    @Override
    public void update(Employee e) {
        ContentValues cv = new ContentValues();
        cv.put("name", e.getName());
        cv.put("familyname", e.getFamilyName());
        cv.put("position", e.getPosition());
        cv.put("phone", e.getPhone());
        cv.put("weblink", e.getWebLink());

        database.update("emp", cv, "_id="+e.getId(), null);
    }

    @Override
    public void delete(long id) {
        database.delete("emp", "_id="+id, null);

    }

    @Override
    public Employee getById(long id) {
        try (Cursor c = database.query("emp", null /* all */, 
                "_id=" + id, null, null, null, "familyname", null)) {

            if (c.moveToNext()) {
                Employee e = new Employee();
                e.setId(c.getLong(0));
                e.setName(c.getString(1));
                e.setFamilyName(c.getString(2));
                e.setPosition(c.getString(3));
                e.setPhone(c.getString(4));
                e.setWebLink(c.getString(5));

                return e;
            } else {
                return null;
            }
        }

    }

}
