package edu.uj.android.employees;

import java.util.List;

import android.content.Context;

public abstract class DataAccess {
    
    protected final Context context;
    
    public static DataAccess create(Context ctx) {
        return new DbBasedDataAccess(ctx);
    }
    

    protected DataAccess(Context ctx) {
        context = ctx;
    }
    
    public abstract List<Employee> getAllEmployees();
    
    public abstract long insert(Employee e);
    
    public abstract void update(Employee e);
    
    public void delete(Employee e) {
        delete(e.getId());
    }
    
    public abstract void delete(long id);
    
    public abstract Employee getById(long id);

}
