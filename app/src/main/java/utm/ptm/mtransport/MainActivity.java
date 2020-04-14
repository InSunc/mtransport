package utm.ptm.mtransport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;

import java.util.Objects;

import utm.ptm.mtransport.data.DatabaseHandler;
import utm.ptm.mtransport.data.models.Route;
import utm.ptm.mtransport.data.models.Trip;
import utm.ptm.mtransport.helpers.LocationHelper;


public class MainActivity extends AppCompatActivity implements MapFragment.Listener, BottomNavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private BottomNavigationView mBottomNav;
    private MapFragment mMapFragment;
    private FindRoutesFragment mFindRoutes;
    private BottomSheetBehavior mFindRoutesBehavior;


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

        mBottomNav = findViewById(R.id.bottom_navigation);
        mBottomNav.setOnNavigationItemSelectedListener(this);

        mFindRoutes = (FindRoutesFragment) getSupportFragmentManager().findFragmentById(R.id.findRoutes);
        mFindRoutesBehavior = BottomSheetBehavior.from(Objects.requireNonNull(mFindRoutes.getView()));
        mFindRoutesBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mFindRoutesBehavior.setFitToContents(false);
        mFindRoutesBehavior.setExpandedOffset(250);

        mMapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        LocationHelper locationHelper = new LocationHelper(mMapFragment.getView());
//        locationHelper.startLocationUpdates();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        execute();
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_menu: {

                break;
            }
            case R.id.nav_search: {

                switch (mFindRoutesBehavior.getState()) {
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        mFindRoutesBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        break;
                    }
                    case BottomSheetBehavior.STATE_HIDDEN: {
                        mFindRoutesBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        break;
                    }
                    case BottomSheetBehavior.STATE_HALF_EXPANDED: {
                        mFindRoutesBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        break;
                    }
                    default: {
                        mFindRoutesBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        break;
                    }
                }

                break;
            }
            case R.id.nav_tickets: {

                break;
            }
        }
        return true;
    }

    @Override
    public void onFoundTrip(Trip trip) {
        TextView tripText = findViewById(R.id.tripText);
        tripText.setText(trip.stops.get(0).getName() + " -> " + trip.stops.get(trip.stops.size() - 1).getName());
        TextView timeText = findViewById(R.id.timeText);
        timeText.setText((int)trip.time + "m");
        mFindRoutesBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
}
