package utm.ptm.mtransport.data.models;

import android.provider.BaseColumns;

import com.google.android.gms.maps.model.LatLng;

import utm.ptm.mtransport.data.DatabaseContract;

public class Stop {

    private long id;
    private String name;
    private double lat;
    private double lng;

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setName(String name) {
        this.name = name;
    }
}
