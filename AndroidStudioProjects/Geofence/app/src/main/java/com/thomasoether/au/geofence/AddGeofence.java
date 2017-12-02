package com.thomasoether.au.geofence;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

public class AddGeofence extends AppCompatActivity {
    private int PLACE_PICKER_REQUEST = 2;
    private LatLng latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_geofence);
    }

    protected void addGeofence(View v){
        Intent addGeofenceIntent = getIntent();
        EditText name = findViewById(R.id.editText4);
        EditText notificationText = findViewById(R.id.editText5);
        addGeofenceIntent.putExtra("latitude", latLng.latitude);
        addGeofenceIntent.putExtra("longitude", latLng.longitude);
        addGeofenceIntent.putExtra("name", name.getText().toString());
        addGeofenceIntent.putExtra("notificationText", notificationText.getText().toString());
        setResult(RESULT_OK,addGeofenceIntent);
        finish();
    }

    public void placePicker(View view) {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (Exception e) {
            Log.d("PlacePicker", e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                EditText name = findViewById(R.id.editText4);
                name.setText(String.format("%s", place.getName()));
                latLng = place.getLatLng();
            }
        }
    }
}