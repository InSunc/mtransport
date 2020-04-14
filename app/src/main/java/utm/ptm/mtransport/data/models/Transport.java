package utm.ptm.mtransport.data.models;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.Objects;

public class Transport {
    int board;
    double latitude;
    double longitude;
    String routeId;
    int loadLevel;

    public String getRouteId() {
        return routeId;
    }

    public int getBoard() {
        return board;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getLoadLevel() {
        return loadLevel;
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

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public void setLoadLevel(int loadLevel) {
        this.loadLevel = loadLevel;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transport)) return false;
        Transport transport = (Transport) o;
        return board == transport.board &&
                Objects.equals(routeId, transport.routeId);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(board);
    }
}
