package utm.ptm.mtransport;

public interface Constants {
    static String SERVER_URL = "http://178.168.113.196:8080/";
    static String BROKER_URL = "tcp://178.168.113.196";
    static String ROUTES_ENDPOINT = SERVER_URL + "routes/";
    static String TRIP_ENDPOINT = SERVER_URL + "trip/";
    static String TICKETS_ENDPOINT = SERVER_URL + "tickets/";
    static String STOPS_ENDPOINT = SERVER_URL + "stops/";
    static float MAX_ALLOWED_TRANSPORT_DISTANCE = 100f; // meters
}
