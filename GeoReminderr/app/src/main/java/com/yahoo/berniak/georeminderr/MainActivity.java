package com.yahoo.berniak.georeminderr;

import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    public static final int DATA_RELOAD_NEEDED = 200;
    public static final int SKIP_DATA_RELOAD = 404;
    private List<Reminder> data;
    private DataAccess dataAccess;


    protected static final String TAG = "MainActivity";
    protected GoogleApiClient mGoogleApiClient;
    protected ArrayList<Geofence> mGeofenceList;
    private boolean mGeofencesAdded;
    private PendingIntent mGeofencePendingIntent;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //loadData();
        loadData2();
        buildGoogleApiClient();


    }

    protected void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }


    protected void onStart(){
        super.onStart();
        loadData2();
        mGoogleApiClient.connect();

    }
    protected void onStop(){
        mGoogleApiClient.disconnect();
        super.onStop();
    }



    private void loadData() {
        DataAccess da = DataAccess.create(this);
        data = da.getAllReminders();

        ArrayAdapter<Reminder> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);

        setListAdapter(adapter);
    }

    private void loadData2() {
        DataAccess da = DataAccess.create(this);
        data = da.getAllReminders();


        Cursor todoCursor = da.getCursor();
        ListAdapter adapter = new CustomArrayAdapter(this, todoCursor);

        //ArrayAdapter<Reminder> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);

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
            loadData2();
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull Status status) {

    }
}
