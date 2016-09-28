package edu.uj.android.employees;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;

public class ListBasedDataAccess extends DataAccess {
    
    private static final List<Employee> data = new ArrayList<Employee>();
    private static long lastId;
    static {
        Employee[] emps = { 
                new Employee(0, "Przemek", "Kadela", "agile coach", "+48 111 222 333"),
                new Employee(1, "Bartek", "Jakistam", "engineer", "+48 222 333 444"),
                new Employee(2, "Andrzej", "Duda", "prezydent", "+48 999 888 777"),
                new Employee(3, "Adam", "Malysz", "skoczek/kierowca", "+48 123 456 987")
                };
        
        data.addAll( Arrays.asList(emps) );   
        lastId = emps.length - 1;
    }

    public ListBasedDataAccess(Context ctx) {
        super(ctx);
    }
    
    public List<Employee> getAllEmployees() {
       
        return data;
    }
    
    public long insert(Employee e) {
        lastId++;
        long id = lastId;
        
        e.setId(id);
        data.add(e);
        
        return id;
    }
    
    public void update(Employee e) {
        int position;
        for (position = 0; position < data.size(); position++) {
            if (data.get(position).getId() == e.getId()) {
                data.remove(position);
                data.add(position, e);
                break;
            }
        }
    }

    public void delete(long id) {
        int position;
        for (position = 0; position < data.size(); position++) {
            if (data.get(position).getId() == id) {
                data.remove(position);
                break;
            }
        }
    }
    
    public Employee getById(long id) {
        for (Employee e : data) {
            if (e.getId() == id) {
                return e;
            }
        }
        return null;
    }

}
