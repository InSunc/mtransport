package utm.ptm.mtransport;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import utm.ptm.mtransport.helpers.MqttHelper;


public class MainActivity extends AppCompatActivity implements MapFragment.OnFragmentInteractionListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private MapFragment mapFragment;
    private MqttHelper mqttHelper;

    private void execute() {
        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        execute();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void onClick(View view) {
        mapFragment.createRoute();
//        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
//        String url ="http://192.168.100.8:8080/routes";
//
//// Request a string response from the provided URL.
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//                new Response.Listener<String>() {2
//                    @Override
//                    public void onResponse(String response) {
//                        // Display the first 500 characters of the response string.
//                        Gson gson = new Gson();
//                        RouteNode[] route = gson.fromJson(response, RouteNode[].class);
//                        for (RouteNode routeNode : route) {
//                            Node node = routeNode.getNode();
//                            LatLng coords = new LatLng(node.getLat(), node.getLng());
////                            mMap.addMarker(new MarkerOptions().position(coords));
//
//                            Log.i(TAG, ">> " + coords);
//                        }                   }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(getApplicationContext(), "Req error", Toast.LENGTH_LONG).show();
//                Log.e(TAG, error.toString());
//            }
//        });
//
//// Add the request to the RequestQueue.
//        queue.add(stringRequest);
    }

    public void onClick2(View view) {

    }
}
