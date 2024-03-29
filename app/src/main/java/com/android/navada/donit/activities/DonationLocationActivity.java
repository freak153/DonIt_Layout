package com.android.navada.donit.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.navada.donit.R;
import com.android.navada.donit.fragments.DonateFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DonationLocationActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOACTION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean mLocationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST = 1234;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 20;
    private boolean mMarkerAdded = false;
    private Button mLocationSubmitButton;
    private String state,city,donorAddress,pinCode;
    private LatLng mChoosenLatLng;


    public DonationLocationActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_location);

        getLocationPermission();
        mLocationSubmitButton = findViewById(R.id.submitLocationBotton);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             onSubmitLocation();
            }
        });
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOACTION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST);
            }
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.donorLocationMap);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mLocationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
        mMap.setOnMapLongClickListener(this);

    }
    @Override
    public void onMapLongClick(LatLng latLng) {
        mMarkerAdded = true;
        mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,19f));
        mMap.addMarker(markerOptions);
        mChoosenLatLng = latLng;
        Geocoder mgeocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> mListAddress = mgeocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(mListAddress != null && mListAddress.size() > 0){
                donorAddress = "";
                city = "";
                state = "";
                pinCode = "";
                if(mListAddress.get(0).getThoroughfare() != null){
                    donorAddress += mListAddress.get(0).getThoroughfare().toString() + " ";
                }
                if(mListAddress.get(0).getSubAdminArea() != null){
                    donorAddress += mListAddress.get(0).getSubAdminArea().toString() + " ";
                }
                if(mListAddress.get(0).getLocality() != null){
                    city += mListAddress.get(0).getLocality().toString();
                }
                if(mListAddress.get(0).getAdminArea() != null) {
                    state += mListAddress.get(0).getAdminArea().toString();
                }
                if(mListAddress.get(0).getPostalCode() != null){
                    pinCode += mListAddress.get(0).getPostalCode().toString();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void onSubmitLocation(){
        if(mMarkerAdded){
            DonateFragment.addressText = donorAddress;
            DonateFragment.city = city;
            DonateFragment.pinCode = pinCode;
            DonateFragment.state = state;
            DonateFragment.latitude = mChoosenLatLng.latitude;
            DonateFragment.longitude = mChoosenLatLng.longitude;
            onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    private void makeToast(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                        } else {
                            makeToast("unable to get current Location");
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            makeToast("Error in finding Location");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    initMap();
                }
        }
    }
    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }
}
