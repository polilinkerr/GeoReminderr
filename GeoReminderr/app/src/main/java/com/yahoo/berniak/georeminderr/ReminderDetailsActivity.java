package com.yahoo.berniak.georeminderr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

public class ReminderDetailsActivity extends Activity {


    public static final String EXTRA_EMPLOYEE_ID = "emp_id";
    public static final String EXTRA_MODE = "mode";

    public static final String MODE_NEW = "new";
    public static final String MODE_VIEW = "view";
    public static final String MODE_EDIT = "edit";

    private TextView titleField = null;
    private TextView descriptionField = null;
    private DataAccess dataAccess;
    public  static LatLng coordinate = null;
    private TextView textCooridantes;

    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_details);

        titleField = (TextView) findViewById(R.id.fieldTitle);
        descriptionField = (TextView) findViewById(R.id.fieldDescription);

        textCooridantes = (TextView) findViewById(R.id.fieldCooridnates);


        dataAccess = DataAccess.create(this);

        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) {
            mode = MODE_VIEW;
        }

        prepareMode();
        if (MODE_VIEW.equals(mode)) {
            loadData();
        }
    }
    protected void onStart(){
        super.onStart();
        if (!(coordinate ==null)){
            textCooridantes.setText("Location: "+coordinate.latitude+" "+ coordinate.longitude);
        }



    }



    private void loadData() {
        Reminder e = dataAccess.getById(getIntent().getLongExtra(EXTRA_EMPLOYEE_ID, -1));
        if (e != null) {
            titleField.setText(e.getTitle());
            descriptionField.setText(e.getDescription());

            getActionBar().setTitle(e.getTitle() );
        }
    }

    private void prepareMode() {
        boolean enabled = !(MODE_VIEW.equals(mode));

        titleField.setEnabled(enabled);
        descriptionField.setEnabled(enabled);


        if (MODE_NEW.equals(mode)) {
            getActionBar().setTitle(R.string.new_employee);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.remind_details, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_edit).setVisible(MODE_VIEW.equals(mode));
        menu.findItem(R.id.action_delete).setVisible(MODE_VIEW.equals(mode));
        menu.findItem(R.id.action_commit).setVisible(!MODE_VIEW.equals(mode));
        menu.findItem(R.id.action_cancel).setVisible(!MODE_VIEW.equals(mode));

        return true;
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_edit:
                editClicked();
                break;
            case R.id.action_cancel:
                cancelClicked();
                break;
            case R.id.action_commit:
                commitClicked();
                break;
            case R.id.action_delete:
                deleteClicked();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void commitClicked() {
        Reminder e = new Reminder();
        e.setId(getIntent().getLongExtra(EXTRA_EMPLOYEE_ID, -1));
        e.setTitle(titleField.getText().toString());
        e.setDescription(descriptionField.getText().toString());
        e.setLongitude(404);
        e.setLatitude(202);

        if (MODE_NEW.equals(mode)) {
            dataAccess.insert(e);
            setResult(MainActivity.DATA_RELOAD_NEEDED);
            finish();
        }else if (MODE_EDIT.equals(mode) ) {
            dataAccess.update(e);
            setResult(MainActivity.DATA_RELOAD_NEEDED);
            mode = MODE_VIEW;

            loadData();
            invalidateOptionsMenu();
            prepareMode();
        }
    }

    private void cancelClicked() {
        if (MODE_NEW.equals(mode)) {
            setResult(MainActivity.SKIP_DATA_RELOAD);
            finish();
        } else if (MODE_EDIT.equals(mode) ) {
            mode = MODE_VIEW;

            loadData();
            invalidateOptionsMenu();
            prepareMode();
        }
    }

    private void editClicked() {
        mode = MODE_EDIT;
        invalidateOptionsMenu();
        prepareMode();
    }

    private void deleteClicked() {
        //TODO: Implemente employee removal
        Reminder e = new Reminder();
        e.setId(getIntent().getLongExtra(EXTRA_EMPLOYEE_ID, -1));
        e.setTitle(titleField.getText().toString());
        e.setDescription(descriptionField.getText().toString());
        e.setLongitude(404);
        e.setLatitude(202);

        dataAccess.delete(e.getId());
        setResult(MainActivity.DATA_RELOAD_NEEDED);
        loadData();
        invalidateOptionsMenu();
        prepareMode();
        finish();
    }

    public void goToMap (View view){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public LatLng getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(LatLng coordinate) {
        this.coordinate = coordinate;
    }
}
