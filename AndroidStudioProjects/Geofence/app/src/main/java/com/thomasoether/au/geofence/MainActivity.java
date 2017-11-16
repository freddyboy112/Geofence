package com.thomasoether.au.geofence;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.tasks.OnSuccessListener;

import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.Manifest;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.security.Permission;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public static final String TAG = "BanegaardFence";
    public static final int LOCATION_REQUEST_CODE = 1;

    private boolean location_updates_activated = true;
    private LocationRequest lrequest;
    private GoogleApiClient mGoogleApiClient;
    private GeofencingClient mGeofencingClient;
    private GeofencingRequest mGeofencingRequest;
    private LocationCallback mLocationCallback;
    private Intent addNewGeofenceIntent;
    private PendingIntent mPi;
    private FusedLocationProviderClient mFusedLocationClient;
    private ListView listOfGeofences;
    private List<String> geofencelist;
    private ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ListVie

        listOfGeofences = (ListView) findViewById(R.id.listView);
        geofencelist = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,geofencelist);
        listOfGeofences.setAdapter(adapter);
        handleItemClick();
        //location
        setLocationRequest();
        addNewGeofenceIntent = new Intent(this, AddGeofence.class);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Set up Google API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    EditText edittext1 = findViewById(R.id.editText2);
                    EditText edittext2 = findViewById(R.id.editText3);
                    edittext1.setText(Double.toString(location.getLatitude()));
                    edittext2.setText(Double.toString(location.getLongitude()));
                }
            };
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        //getLastLocation(mFusedLocationClient);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (location_updates_activated) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }
    @SuppressWarnings("MissingPermission")
    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(lrequest,
                mLocationCallback,
                null /* Looper */);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Google Play Services connected!");

        // Set up mock locations
        addNewGeofence("Aarhus Hovedbanegård", "test aarhus hovedbanegård",56.1503116,  10.2047365);
        addNewGeofence("Test Location 1", "Test text", 56.164618, 10.201885);
        addNewGeofence("Test Location 2", "Home!", 56.1960490, 10.1941410);
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    String message = "Location permission accepted. Geofence will be created.";
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                    // OK, request it now
                    mGeofencingClient.addGeofences(mGeofencingRequest, mPi);
                    Log.d(TAG, "We added the geofence!");

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    String message = "Location permission denied. Geofence will not work.";
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Google Play Services connection suspended!");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Google Play Services connection failed!");
    }

    private void addNewGeofence(String locationName, String notificationText, double latitude, double longitude) {

        // Create Geofence
        Geofence mGeofence = new Geofence.Builder()
                .setRequestId(locationName)
                .setCircularRegion(latitude, longitude, 500)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();

        // Create Geofence request - saved outside method in case permissions have not been granted.
        mGeofencingRequest = new GeofencingRequest.Builder()
                .addGeofence(mGeofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();

        // Create an Intent pointing to the IntentService
        Intent intent = new Intent(this,
                ReceiveGeoFenceTransitionService.class);

        //add additional data for notification use
        intent.putExtra("location", locationName);
        intent.putExtra("message", notificationText);

        mPi = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // If permissions are in order, add geofence. Else wait for onRequestPermissionsResult
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mGeofencingClient.addGeofences(mGeofencingRequest, mPi);
            Log.d(TAG, "We added the geofence!");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST_CODE);
        }
        adapter.add(locationName);
    }

    public void AddNewGeofenceActivity(View v){
        startActivityForResult(addNewGeofenceIntent,0);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            //Retrieve data in the intent
            double latitude = data.getDoubleExtra("latitude",0);
            double longitute = data.getDoubleExtra("longitude",0);
            String name = data.getStringExtra("name");
            String notificationtext = data.getStringExtra("notificationtext");
            Log.d("parameters : ", " long " + longitute +  "lat " + latitude + "name " + name + "   " + notificationtext);
            addNewGeofence(name,notificationtext,latitude,longitute);
        }


    }

    private void setLocationRequest(){
        lrequest = new LocationRequest();
        lrequest.setInterval(10000);
        lrequest.setFastestInterval(5000);
        lrequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    // give user the option to delete selected item
    private void handleItemClick(){

        listOfGeofences.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String item = (String) listOfGeofences.getItemAtPosition(position);
            }
        });
    }
/*
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createDialogue(){
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Title")
                .setMessage("Do you really want to whatever?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                        // adapter.remove(item);
                    }})
                .setNegativeButton(android.R.string.no, null).show()
                .create();
    }
    */

    /*
    @SuppressWarnings("MissingPermission")
    private void getLastLocation(FusedLocationProviderClient locationclient){
        locationclient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known lsocation. In some rare situations this can be null.
                        if (location != null) {

                        }
                    }
                });
    }*/
}
