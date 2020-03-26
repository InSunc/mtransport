package utm.ptm.mtransport.data.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Way {
    private String name;
    private List<LatLng> points;



    public String getName() {
        return name;
    }

    public List<LatLng> getPoints() {
        return points;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPoints(List<LatLng> points) {
        this.points = points;
    }
}
