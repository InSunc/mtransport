package utm.ptm.mtransport.data.models;

public class Node {
    private long id;
    private double lat;
    private double lng;

    public void setId(long id) {
        this.id = id;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public long getId() {
        return id;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
