package utm.ptm.mtransport.utils;

import utm.ptm.mtransport.data.models.Route;
import utm.ptm.mtransport.data.models.Trip;

public class TripAM {
    public String routes;
    public int time;
    public float cost;

    public TripAM(Trip trip) {
        StringBuilder routeString = new StringBuilder();
        routeString.append(trip.routes.get(0).getId());
        for (int i = 1; i < trip.routes.size(); i++) {
            routeString.append(" → ").append(trip.routes.get(i).getId());
        }

        this.routes = routeString.toString();
        time = (int)trip.time;
        cost = trip.cost;
    }
}
