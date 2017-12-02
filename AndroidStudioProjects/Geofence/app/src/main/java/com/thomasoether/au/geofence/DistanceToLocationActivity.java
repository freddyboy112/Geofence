package com.thomasoether.au.geofence;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;


public class DistanceToLocationActivity extends AppCompatActivity{
    private Location destination = new Location("B");
    private LocationManager locationManager;
    private LocationListener locationListener;


    @Override
    protected void onStart() {
        super.onStart();

        setContentView(R.layout.activity_distance_to_location);

        onNewIntent(getIntent());
        destination = new Location("Destination");

        //has permission or would not get to this step
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if(location != null) {
                        TextView view = findViewById(R.id.textView4);
                        if(destination == null) System.out.println("dest is null");
                        if(view == null) System.out.println("view is null");
                        view.setText(location.distanceTo(destination) + " m");
                    }

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();

        destination.setLatitude(extras.getDouble("lat"));
        destination.setLongitude(extras.getDouble("long"));
    }
}