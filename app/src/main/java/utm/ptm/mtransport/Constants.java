package utm.ptm.mtransport;

public interface Constants {
    static String SERVER_URL = "http://192.168.100.7:8080/";
    static String BROKER_URL = "tcp://192.168.100.7";
    static String ROUTES_ENDPOINT = SERVER_URL + "routes/";
    static String TRIP_ENDPOINT = SERVER_URL + "trip/";
    static String TICKETS_ENDPOINT = SERVER_URL + "tickets/";
    static float MAX_ALLOWED_TRANSPORT_DISTANCE = 100f; // meters
}
