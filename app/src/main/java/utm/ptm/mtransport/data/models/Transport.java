package utm.ptm.mtransport.data.models;

import com.google.android.gms.maps.model.LatLng;

public class Transport {
    int board;
    double latitude;
    double longitude;

    public int getBoard() {
        return board;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setBoard(int board) {
        this.board = board;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
