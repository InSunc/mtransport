package utm.ptm.mtransport.data.models;

import android.provider.BaseColumns;

import com.google.android.gms.maps.model.LatLng;

import utm.ptm.mtransport.data.DatabaseContract;

public class Stop {
    private String name;
    private LatLng location;



    public String getName() {
        return name;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }
}
