package com.yahoo.berniak.georeminderr;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnCameraIdleListener, GoogleMap.OnMyLocationButtonClickListener, ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnMarkerClickListener {


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private Marker geoFenceMarker;
    private LatLng coordinates = null;
    private TextView mTapTextView;
    private GoogleMap mMap;

    private Geocoder geocoder;
    private List<Address> addresses = new ArrayList<Address>();
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String knownName;

    private LatLng CurrentLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mTapTextView = (TextView) findViewById(R.id.frameViewLocation);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        if (savedInstanceState != null) {

            Double tmpLatitude = savedInstanceState.getDouble("latitude");
            double tmpLongitude = savedInstanceState.getDouble("longtutide");
            if (tmpLatitude != null){
                coordinates = new LatLng(tmpLatitude.doubleValue(), tmpLongitude);
                locationToAdress(coordinates.latitude,coordinates.longitude);
                mTapTextView.setText("adress:"+address+", city:"+city+", country:"+country);
            }
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnCameraIdleListener(this);

        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));

        if (location != null){
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),5);
            mMap.animateCamera(yourLocation);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target( new LatLng(location.getLatitude(), location.getLongitude()))  //ustaw widok na moja lokalizacje
                    .zoom(14)                                                                  // zoom
                    .bearing(0)                                                               // ustawienie kamery
                    .tilt(40)                                                                  // nachylenie
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        if (coordinates!=null){
            markerForGeofence(coordinates);

        }
    }


    public  boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.maps_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    public  boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_back:
                //saveData();
                ReminderDetailsActivity.coordinate = this.coordinates;
                ReminderDetailsActivity.adress = "adress:"+address+", city:"+city+", country:"+country;
                //setResult(MainActivity.SKIP_DATA_RELOAD);
                finish();
                return true;
            default:
                return true;
        }

    }


    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);

        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }



    @Override
    public void onCameraIdle() {
        //mCameraTextView.setText(mMap.getCameraPosition().toString());
    }

    @Override
    public void onMapClick(LatLng point) {
        {
            mTapTextView.setText("X:" + HelpCalculation.round(point.latitude,4)+" Y:"+ HelpCalculation.round(point.longitude,4));
            coordinates = point;
            float zoom =14f;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(point, zoom);
            mMap.animateCamera(cameraUpdate);

    }


}

    @Override
    public void onMapLongClick(LatLng point) {
        locationToAdress(point.latitude,point.longitude);
        mTapTextView.setText("adress:"+address+", city:"+city+", country:"+country);
        coordinates = point;
        markerForGeofence(point);
    }

    private void markerForGeofence(LatLng latLng) {

            //Log.i(TAG, "markerForGeofence("+latLng+")");

            String title = latLng.latitude + ", " + latLng.longitude;
            // Define marker options
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    .title(title);
            if ( mMap!=null ) {
                // Remove last geoFenceMarker
                if (geoFenceMarker != null)
                    geoFenceMarker.remove();

                geoFenceMarker = mMap.addMarker(markerOptions);
            }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        return false;
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putDouble("latitude", coordinates.latitude);
        outState.putDouble("longtutide", coordinates.longitude);


    }
}
