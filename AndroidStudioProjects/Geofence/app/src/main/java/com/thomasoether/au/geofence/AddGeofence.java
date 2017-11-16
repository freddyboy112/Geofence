package com.thomasoether.au.geofence;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class AddGeofence extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_geofence);
    }
    protected void AddGeofence(View v){
        Intent addGeofenceIntent = getIntent();
        EditText latitude = findViewById(R.id.editText6);
        EditText longitute = findViewById(R.id.editText7);
        EditText name = findViewById(R.id.editText4);
        EditText notificationtext = findViewById(R.id.editText5);
        addGeofenceIntent.putExtra("latitude", Double.parseDouble((latitude.getText().toString())));
        addGeofenceIntent.putExtra("longitude", Double.parseDouble((longitute.getText().toString())));
        addGeofenceIntent.putExtra("name", name.getText().toString());
        addGeofenceIntent.putExtra("notificationtext", notificationtext.getText().toString());
        setResult(RESULT_OK,addGeofenceIntent);
        finish();

    }
}
