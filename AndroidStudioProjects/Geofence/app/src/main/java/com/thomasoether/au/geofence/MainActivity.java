package com.thomasoether.au.geofence;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.PendingIntent;
import android.content.Intent;
import android.text.TextUtils;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.Manifest;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public static final String TAG = "BanegaardFence";
    public static final int LOCATION_REQUEST_CODE = 1;
    private boolean needPermission = true;
    private boolean removeItem = false;
    private AlertDialog dialog;
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
    private List<GeofencePair> geofencelist;
    private ArrayAdapter<GeofencePair> adapter;
    // used for dialogue button onclick
    private int positionOfObjectToBeDeleted;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionSetup();
        //ListVie
        dialog = createDialogue();
        listOfGeofences = (ListView) findViewById(R.id.listView);
        geofencelist = new ArrayList<GeofencePair>();
        adapter = new ArrayAdapter<GeofencePair>(this,android.R.layout.simple_list_item_1,geofencelist);
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

        // Only used for fusedlocationclient
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                }
            };
        };
        getGeofencesFromSharedPreferences();
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
    //get continous location updates.
    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(lrequest,
                mLocationCallback,
                null /* Looper */);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Google Play Services connected!");

        // Set up mock locations
      /*  addNewGeofence("Aarhus Hovedbanegård", "test aarhus hovedbanegård",56.1503116,  10.2047365);
        addNewGeofence("Test Location 1", "Test text", 56.164618, 10.201885);
        addNewGeofence("Test Location 2", "Home!", 56.1960490, 10.1941410);*/

}

    public void getGeofencesFromSharedPreferences(){
        SharedPreferences  prefs = this.getPreferences(MODE_PRIVATE);
        Map<String,?> keys = prefs.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            Log.d("map values",entry.getKey() + ": " + entry.getValue().toString());
            ArrayList<String> geofenceinarray = new ArrayList<>(Arrays.asList(entry.getValue().toString().split(";")));
            Log.d("Arraylist", geofenceinarray.toString());
            long id = Long.parseLong(entry.getKey());
            addGeofenceToMap(geofenceinarray.get(0),geofenceinarray.get(1), Double.parseDouble(geofenceinarray.get(2)), Double.parseDouble(geofenceinarray.get(3)),id);
            addGeofenceToListView(id, geofenceinarray.get(0));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if(requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                finish();
            } else {
                needPermission = false;
            }
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

    private void addGeofenceToMap(String locationName, String notificationText, double latitude, double longitude, long id) {

        // Create Geofence
        Geofence mGeofence = new Geofence.Builder()
                .setRequestId("" + id)
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
        intent.putExtra("lat", latitude);
        intent.putExtra("long", longitude);

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
    }
    // Button click in layout
    public void AddNewGeofenceActivity(View v){

        startActivityForResult(addNewGeofenceIntent,0);
    }
    // get the geofence, that the user has created in another activity
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
            long id = System.currentTimeMillis();
            addGeofenceToMap(name,notificationtext,latitude,longitute, id);
            addGeofenceToListView(id, name);
            addGeofenceToPreferences(name,notificationtext,latitude,longitute,id);
        }


    }
    private void setLocationRequest(){
        lrequest = new LocationRequest();
        lrequest.setInterval(10000);
        lrequest.setFastestInterval(5000);
        lrequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    // give user the option to delete selected geofence from the listview
    private void handleItemClick(){

        listOfGeofences.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                positionOfObjectToBeDeleted = position;
                dialog.show();
                Log.d("dialogue", "dialogue has finished showing. Value is " + removeItem);
            }
        });
    }
    private void addGeofenceToPreferences(String locationName, String notificationText, double latitude, double longitude , long id){
        SharedPreferences sharedPref = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        // using System.currentTimeMillis() to make sure each geofence don't overwrite each other.
        editor.putString("" + id,locationName + ";" + notificationText + ";" + latitude + ";" + longitude + ";");
        editor.apply();
    }
    private void addGeofenceToListView(long id, String locationName){
        geofencelist.add(new GeofencePair(id,locationName));
        adapter.notifyDataSetChanged();
    }
    // TODO make delete occur right after pressing yes in the dialogue button
    private void deleteGeofence(int position){
        GeofencePair item = (GeofencePair) listOfGeofences.getItemAtPosition(position);
        geofencelist.remove(item);
        adapter.notifyDataSetChanged();
        //delete from preferences
        SharedPreferences sharedPref = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        long id = item.getGeofenceId();
        editor.remove("" + id);
        editor.apply();
        deleteGeofenceFromMap(id);
    }
    private void deleteGeofenceFromMap(long id){
        ArrayList<String> list = new ArrayList<String>();
        list.add("" + id);
        mGeofencingClient.removeGeofences(list)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.w("geofence", "geofence removed");
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("geofence", "geofence not removed");
                    }
                });
    }
    protected void permissionSetup() {
        boolean needFineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED;
        boolean needCoarseLocation =ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED;

        if(needCoarseLocation || needFineLocation) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST_CODE);
        }

        try {
            //2 seconds to give permissions until it crashes
            //TODO: refactor all code that needs permission into new activity and ask for permission in seperate (first) activity
            if (needPermission) Thread.sleep(2000);
        } catch (Exception e) {
            Log.d("Permissions", "permission interrupted");
        }
    }
    // Dialogue popup to confirm deleting the geofence
        private AlertDialog createDialogue() {
            return new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Delete geofence")
                    .setMessage("do you want to delete this geofence ?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // delete geofence when user accepts.
                            deleteGeofence(getPositionOfObjectClicked());
                            Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .create();
        }
        private int getPositionOfObjectClicked(){
            return positionOfObjectToBeDeleted;
        }


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
