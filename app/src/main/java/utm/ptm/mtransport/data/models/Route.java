package utm.ptm.mtransport.data.models;

import android.provider.BaseColumns;

import java.util.List;


public class Route {
    private String id;
    private String name;
    private List<Way> ways;
    private List<Stop> stops;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Way> getWays() {
        return ways;
    }

    public List<Stop> getStops() {
        return stops;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWays(List<Way> ways) {
        this.ways = ways;
    }

    public void setStops(List<Stop> stops) {
        this.stops = stops;
    }
}
