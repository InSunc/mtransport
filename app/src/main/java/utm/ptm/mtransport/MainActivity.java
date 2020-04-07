package utm.ptm.mtransport;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.PointerIcon;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.List;

import utm.ptm.mtransport.data.DatabaseHandler;
import utm.ptm.mtransport.data.models.Route;
import utm.ptm.mtransport.helpers.LocationHelper;
import utm.ptm.mtransport.helpers.MapHelper;
import utm.ptm.mtransport.helpers.MqttHelper;


public class MainActivity extends AppCompatActivity implements MapFragment.OnFragmentInteractionListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private MapFragment mapFragment;

    private void execute() {
        final DatabaseHandler db = DatabaseHandler.getInstance(this);
        if (db.getRouteIds().isEmpty()) {
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, Constants.ROUTES_ENDPOINT,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Gson gson = new Gson();
                            Route[] routes = gson.fromJson(response, Route[].class);
                            db.insert(routes);
                            Log.d(TAG, routes.toString());
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Req Error on gettings routes");
                }
            });
            queue.add(stringRequest);
        }

        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        LocationHelper locationHelper = new LocationHelper(mapFragment.getView());
//        locationHelper.startLocationUpdates();
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

}
