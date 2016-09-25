package com.yahoo.berniak.georeminderr;

import android.*;
import android.Manifest;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MainActivity extends ListActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>, LocationListener {

    public static final int DATA_RELOAD_NEEDED = 200;
    public static final int SKIP_DATA_RELOAD = 404;
    private List<Reminder> data;
    private Map<String,LatLng> referenceGeofence = new HashMap<String, LatLng>();
    private DataAccess dataAccess;


    protected static final String TAG = "MainActivity";
    protected GoogleApiClient mGoogleApiClient;
    protected ArrayList<Geofence> mGeofenceList;
    private boolean mGeofencesAdded;
    private PendingIntent mGeofencePendingIntent;
    private SharedPreferences mSharedPreferences;
    public static final String PACKAGE_NAME = "com.google.android.gms.location.Geofence";
    public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES_NAME";
    public static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 1609; // 1 mile, 1.6 km

    private static final long GEO_DURATION = 60 * 60 * 1000;
    //private static final String GEOFENCE_REQ_ID = "My Geofe;nce"
    private static final float GEOFENCE_RADIUS = 100.0f; // in meters


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGoogleApiClient();
        //loadData2();
        mGeofenceList = new ArrayList<Geofence>();
        //loadGeofenceList();
        mGeofencePendingIntent = null;
        mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME,
                MODE_PRIVATE);
        mGeofencesAdded = mSharedPreferences.getBoolean(GEOFENCES_ADDED_KEY, false);

        //addGeofencesHandler();
        buildGoogleApiClient();
        loadReferenceGeofence();
    }

    protected void onStart() {
        super.onStart();
        loadData2();
        loadReferenceGeofence();

        mGoogleApiClient.connect();
        loadGeofenceList();


    }

    protected void onStop() {

        super.onStop();
        mGoogleApiClient.disconnect();
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

        setListAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
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


    protected synchronized void buildGoogleApiClient() {
        Log.d(TAG, "createGoogleApi()");
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

        }
    }

    public void onConnected(@Nullable Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
    }

    @Override
    public void onConnectionFailed(@Nullable ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        Log.i(TAG, "Connection suspended");

        // onConnected() will be called again automatically when the service reconnects
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

     //DO STAREGO POMYSLU
    public void addGeofence(Reminder e) {

        String requestId = e.getTitle();
        String arr[] = requestId.split(" ", 2);
        String firstWord = arr[0];

        mGeofenceList.add(new Geofence.Builder()
                .setRequestId(firstWord)
                .setCircularRegion(
                        e.getLatitude(),
                        e.getLongitude(),
                        GEOFENCE_RADIUS_IN_METERS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                .build());




    }


    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }


    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            // Update state and save in shared preferences.
            mGeofencesAdded = !mGeofencesAdded;
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(GEOFENCES_ADDED_KEY, mGeofencesAdded);
            editor.apply();

            // Update the UI. Adding geofences enables the Remove Geofences button, and removing
            // geofences enables the Add Geofences button.


            Toast.makeText(
                    this,
                    getString(mGeofencesAdded ? R.string.geofences_added :
                            R.string.geofences_removed),
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    public void addGeofencesHandler() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }


        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }

    public void removeGeofencesHandler() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // Remove geofences.
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }

    //DO STAREGO POMYSLU
    private void loadGeofenceList() {

        // DO STAREGO POMYSLU | TERAZ NIE UWZGLENIANY

        DataAccess da = DataAccess.create(this);


        try {

            Cursor todoCursor = da.getCursor();
            try {

                // looping through all rows and adding to list
                if (todoCursor.moveToFirst()) {
                    do {
                        String requestId = todoCursor.getString(todoCursor.getColumnIndexOrThrow("title"));
                        String arr[] = requestId.split(" ", 2);
                        String firstWord = arr[0];
                        double latitude = todoCursor.getDouble(todoCursor.getColumnIndexOrThrow("latitude"));
                        double longitude = todoCursor.getDouble(todoCursor.getColumnIndexOrThrow("longitude"));

                        mGeofenceList.add(new Geofence.Builder()
                                .setRequestId(firstWord)
                                .setCircularRegion(
                                        latitude,
                                        longitude,
                                        GEOFENCE_RADIUS_IN_METERS)
                                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                                .build());
                    } while (todoCursor.moveToNext());
                }

            } finally {
                try {
                    todoCursor.close();
                } catch (Exception ignore){}



            }


        }catch (Exception ignore){}


    }

    @Override
    public void onLocationChanged(Location location) {


    }







    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, String.valueOf(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}))
                == PackageManager.PERMISSION_GRANTED );
    }


    private Geofence createGeofence( String Geofence_req_Id, LatLng latLng, float radius ) {

        //DO PRZEBUDOWY
        Log.d(TAG, "createGeofence");

        return new Geofence.Builder()
                .setRequestId(Geofence_req_Id)
                .setCircularRegion( latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration( GEO_DURATION )
                .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT )
                .build();
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
        Log.d(TAG, "addGeofence");
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    request,
                    getGeofencePendingIntent()
            ).setResultCallback(this);
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest( Geofence geofence ) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
                .addGeofence( geofence )
                .build();
    }



    private void loadReferenceGeofence(){
        DataAccess da = DataAccess.create(this);
        Map<String, LatLng> referenceTmpMap = new HashMap<>();

        if(!referenceGeofence.isEmpty()){referenceTmpMap.putAll(referenceGeofence);}

        try {

            Cursor todoCursor = da.getCursor();
            try {

                // looping through all rows and adding to list
                if (todoCursor.moveToFirst()) {
                    do {
                        String requestId = todoCursor.getString(todoCursor.getColumnIndexOrThrow("_id"));
                        double latitude = todoCursor.getDouble(todoCursor.getColumnIndexOrThrow("latitude"));
                        double longitude = todoCursor.getDouble(todoCursor.getColumnIndexOrThrow("longitude"));
                        if (!referenceTmpMap.containsKey(requestId)){
                            Log.v("loadReferenceGeofence", "Wyslij nowe zawolanie");
                            LatLng latLng = new LatLng(latitude,longitude);
                            referenceTmpMap.put(requestId,latLng);
                            //DODAJ ZAWOLANIE
                            GeofencingRequest geofencingRequest = createGeofenceRequest(createGeofence(requestId,latLng,GEOFENCE_RADIUS));
                            Log.v("loadReferenceGeofence", "Wyslano nowe zawolanie");
                            addGeofence(geofencingRequest);
                        }
                    } while (todoCursor.moveToNext());
                }

            } finally {
                try {
                    todoCursor.close();
                } catch (Exception ignore){}
            }
        }catch (Exception ignore){}

        for (Map.Entry<String,LatLng> cell: referenceTmpMap.entrySet()){
            if(!CheckIsDataAlreadyInDBorNot("_id",cell.getKey())){
                Log.v("loadReferenceGeofence", "Usun zawolanie");
                referenceTmpMap.remove(cell.getKey());
                //USUN ODWOLANIE
            }
        }

        referenceGeofence.clear();
        referenceGeofence.putAll(referenceTmpMap);
    }

    public boolean CheckIsDataAlreadyInDBorNot(String dbfield, String fieldValue) {

        DataAccess da = DataAccess.create(this);
        data = da.getAllReminders();



        Cursor todoCursor = da.getbyIdElements(dbfield,fieldValue);

        if(todoCursor.getCount() <= 0){
            todoCursor.close();
            return false;
        }
        todoCursor.close();
        return true;

    }
}