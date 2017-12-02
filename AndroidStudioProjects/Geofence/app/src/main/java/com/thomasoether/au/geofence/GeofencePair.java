package com.thomasoether.au.geofence;

/**
 * Created by Ripley on 11/18/2017.
 */

public class GeofencePair {
    private String locationname;
    private long id;
    public GeofencePair(long id, String locationname){
        this.locationname = locationname;
        this.id = id;
    }
    public long getGeofenceId(){
        return id;
    }
    public String getLocationname(){
        return locationname;
    }
    @Override
    public String toString(){
        return locationname;
    }
}
