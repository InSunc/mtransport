package utm.ptm.mtransport.helpers;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class MapHelper {
    private final GoogleMap mMap;

    public MapHelper(GoogleMap map) {
        this.mMap = map;
    }

    public void mark(LatLng latLng) {
        Marker marker = new Marker();
        marker.setPosition();

    }
}
