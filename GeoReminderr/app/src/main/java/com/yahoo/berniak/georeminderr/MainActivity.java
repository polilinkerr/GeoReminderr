package com.yahoo.berniak.georeminderr;

import android.Manifest;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends ListActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>, LocationListener {

    public static final int DATA_RELOAD_NEEDED = 200;
    public static final int SKIP_DATA_RELOAD = 404;
    private static final int REQ_PERMISSION = 1;
    private List<Reminder> data;
    private Map<String, LatLng> referenceGeofenceList = new HashMap<String, LatLng>();
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
    private static final float GEOFENCE_RADIUS = 500.0f; // in meters
    private Location lastLocation;

    private LocationRequest locationRequest;
    // Defined in mili seconds.
    // This number in extremely low, and should be used only for debug
    private final int UPDATE_INTERVAL = 1000;
    private final int FASTEST_INTERVAL = 900;
    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;
    public  static Locale local;


    private Geocoder geocoder;
    private List<Address> addresses = new ArrayList<Address>();
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String knownName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        local = Locale.getDefault();


        // Get the UI widgets.
       //mAddGeofencesButton = (Button) findViewById(R.id.add_geofences_button);
        //mRemoveGeofencesButton = (Button) findViewById(R.id.remove_geofences_button);

        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<Geofence>();

        mGeofencePendingIntent = null;

        mSharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME,
                MODE_PRIVATE);
        mGeofencesAdded = mSharedPreferences.getBoolean(Constants.GEOFENCES_ADDED_KEY, false);


        buildGoogleApiClient();
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        loadData2();
        loadReferenceGeofence();
        populateGeofenceList();



        //loadReferenceGeofence();


    }


    protected void onStop() {

        super.onStop();
        mGoogleApiClient.disconnect();
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
            case R.id.turnOnReminder:
                addGeofencesButtonHandler();
                return true;
            case R.id.turnOffReminder:
                removeGeofencesButtonHandler();
                return  true;

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
        Log.i(TAG, "H createGoogleApi()");
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            Log.i(TAG, "H created GoogleApi()");

        } else {
            Log.i(TAG, "H NO createGoogleApi()");
        }
    }

    public void onConnected(@Nullable Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
        getLastKnownLocation();
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

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");
        if (checkPermission()) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (lastLocation != null) {
                Log.i(TAG, "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                writeLastLocation();
                startLocationUpdates();
            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        } else askPermission();
    }



    // Start location Updates
    private void startLocationUpdates() {
        Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if (checkPermission())
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged [" + location + "]");

        if(location.distanceTo(lastLocation)>10){
            lastLocation = location;
            writeActualLocation(location);
        }

    }

    // Write location coordinates on UI
    private void writeActualLocation(Location location) {
        locationToAdress(location.getLatitude(),location.getLongitude());
        Toast.makeText(getApplicationContext(), "adress:"+address+", city:"+city, Toast.LENGTH_LONG).show();
    }

    private void writeLastLocation() {writeActualLocation(lastLocation);}

    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    // Asks for permission
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                REQ_PERMISSION
        );
    }

    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    getLastKnownLocation();

                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
        }
    }

    // App cannot work without the permissions
    private void permissionsDenied() {
        Log.w(TAG, "permissionsDenied()");
    }

    private Geofence createGeofence(String Geofence_req_Id, LatLng latLng, float radius) {

        //DO PRZEBUDOWY
        Log.d(TAG, "createGeofence");

        return new Geofence.Builder()
                .setRequestId(Geofence_req_Id)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(GEO_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
        mGoogleApiClient.connect();
        Log.d(TAG, "addGeofence");

        Log.d(TAG, "addGeofence");
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(this);

    }
    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if ( geoFencePendingIntent != null )
            return geoFencePendingIntent;

        Intent intent = new Intent( this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT );
    }

    private void removeGeofence(){
        Log.d(TAG, "remove Geofence");
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
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

    private void loadReferenceGeofenceTEST() {
        Map<String, LatLng> referenceTmpMap = new HashMap<>();
        referenceTmpMap.put("Pierwsze_Miejsce",new LatLng(50.0,22.0) );
        referenceTmpMap.put("Drugie_Miejsce", new LatLng(50.0331035,19.9721977));
        //for (Map.Entry<String,LatLng> cell: referenceTmpMap.entrySet()){
         //   GeofencingRequest geofencingRequest = createGeofenceRequest(createGeofence(cell.getKey(),cell.getValue(),GEOFENCE_RADIUS));
          //  Log.v("loadReferenceGeofence", "Wyslano nowe zawolanie");
           // addGeofence(geofencingRequest);

        //}
        referenceGeofenceList.clear();
        referenceGeofenceList.putAll(referenceTmpMap);
    }

    private void loadReferenceGeofence(){
        DataAccess da = DataAccess.create(this);
        Map<String, LatLng> referenceTmpMap = new HashMap<>();

        if(!referenceGeofenceList.isEmpty()){referenceTmpMap.putAll(referenceGeofenceList);}


        try {

            Cursor todoCursor = da.getCursor();
            try {

                // looping through all rows and adding to list
                if (todoCursor.moveToFirst()) {
                    do {
                        String requestId = todoCursor.getString(todoCursor.getColumnIndexOrThrow("title"));
                        double latitude = todoCursor.getDouble(todoCursor.getColumnIndexOrThrow("latitude"));
                        double longitude = todoCursor.getDouble(todoCursor.getColumnIndexOrThrow("longitude"));
                        if (!referenceTmpMap.containsKey(requestId)){
                            Log.v("loadReferenceGeofence", "Wyslij nowe zawolanie");
                            LatLng latLng = new LatLng(latitude,longitude);
                            referenceTmpMap.put(requestId,latLng);
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
            if(!CheckIsDataAlreadyInDBorNot("title",cell.getKey())){
                Log.v("loadReferenceGeofence", "Usun zawolanie");
                referenceTmpMap.remove(cell);
                //USUN ODWOLANIE
            }
        }

        referenceGeofenceList.clear();
        referenceGeofenceList.putAll(referenceTmpMap);
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

    public void onResult(Status status) {
        if (status.isSuccess()) {
            // Update state and save in shared preferences.
            mGeofencesAdded = !mGeofencesAdded;
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(Constants.GEOFENCES_ADDED_KEY, mGeofencesAdded);
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

    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }

    public void removeGeofencesButtonHandler() {
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


    public void addGeofencesButtonHandler() {
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

    public void populateGeofenceList() {

        for (Map.Entry<String, LatLng> entry : referenceGeofenceList.entrySet()) {

            //PYTANIE - CZY TO TYLKO DODAJE CZY WCZESNIEJ MAPA JEST CZYSZCZONA
            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(entry.getKey())

                    // Set the circular region of this geofence.
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )

                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time.
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                    // Create the geofence.
                    .build());
        }

}

    public void locationToAdress(double latitude, double longitude ){

        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        city = addresses.get(0).getLocality();
        state = addresses.get(0).getAdminArea();
        country = addresses.get(0).getCountryName();
        postalCode = addresses.get(0).getPostalCode();
        knownName = addresses.get(0).getFeatureName();

    }





}