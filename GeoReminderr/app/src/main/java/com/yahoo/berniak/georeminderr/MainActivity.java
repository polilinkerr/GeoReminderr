package com.yahoo.berniak.georeminderr;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class MainActivity extends ListActivity {

    public static final int DATA_RELOAD_NEEDED = 200;
    public static final int SKIP_DATA_RELOAD = 404;
    private List<Reminder> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadData();
    }



    private void loadData() {
        DataAccess da = DataAccess.create(this);
        data = da.getAllReminders();

        ArrayAdapter<Reminder> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);

        setListAdapter(adapter);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()){
            case R.id.action_add:
                Intent empDetailsIntent = new Intent(this, ReminderDetailsActivity.class);
                empDetailsIntent.putExtra(ReminderDetailsActivity.EXTRA_MODE, ReminderDetailsActivity.MODE_NEW);

                startActivityForResult(empDetailsIntent, 101);
                return true;
            case R.id.action_setting:
                return true;
            default:
                return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == DATA_RELOAD_NEEDED) {
            loadData();
        }

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Reminder emp = data.get(position);

        Intent empDetailsIntent = new Intent(this, ReminderDetailsActivity.class);
        empDetailsIntent.putExtra(ReminderDetailsActivity.EXTRA_EMPLOYEE_ID, emp.getId());
        empDetailsIntent.putExtra(ReminderDetailsActivity.EXTRA_MODE, ReminderDetailsActivity.MODE_VIEW);

        startActivity(empDetailsIntent);
    }
}
