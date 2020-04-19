package utm.ptm.mtransport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedTransferQueue;

import utm.ptm.mtransport.data.DatabaseHandler;
import utm.ptm.mtransport.data.models.Route;
import utm.ptm.mtransport.data.models.Transport;
import utm.ptm.mtransport.data.models.Trip;
import utm.ptm.mtransport.helpers.LocationHelper;
import utm.ptm.mtransport.utils.TripAM;
import utm.ptm.mtransport.utils.TripsAdapter;


public class MainActivity extends AppCompatActivity implements MapFragment.Listener, BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private BottomNavigationView mBottomNav;
    private MapFragment mMapFragment;
    private FindRoutesFragment mFindRoutes;
    private BottomSheetBehavior mFindRoutesBehavior;
    private TicketsFragment mTickets;
    private BottomSheetBehavior mTicketsBehavior;
    private TripsAdapter tripsAdapter;
    private ListView foundTripsList;

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

        mFindRoutes = (FindRoutesFragment) getSupportFragmentManager().findFragmentById(R.id.find_routes_fragment);
        mFindRoutesBehavior = BottomSheetBehavior.from(Objects.requireNonNull(mFindRoutes.getView()));
        mFindRoutesBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mFindRoutesBehavior.setFitToContents(false);
        mFindRoutesBehavior.setExpandedOffset(250);

        mTickets = (TicketsFragment) getSupportFragmentManager().findFragmentById(R.id.tickets_fragment);
        mTicketsBehavior = BottomSheetBehavior.from(Objects.requireNonNull(mTickets.getView()));
        mTicketsBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mTicketsBehavior.setFitToContents(false);
        mTicketsBehavior.setExpandedOffset(250);

        tripsAdapter = new TripsAdapter(this);
        foundTripsList = findViewById(R.id.found_trips_list);
        foundTripsList.setAdapter(tripsAdapter);

        mMapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        LocationHelper locationHelper = new LocationHelper(mMapFragment.getView());

        mBottomNav = findViewById(R.id.bottom_navigation);
        mBottomNav.setOnNavigationItemSelectedListener(this);
        mBottomNav.getMenu().findItem(R.id.nav_search).setChecked(true);

        final Context context = this;
        locationHelper.startLocationUpdates(new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                List<Transport> transports = new ArrayList<>();
                Location userLocation = new Location("U");
                userLocation.setLongitude(locationResult.getLastLocation().getLongitude());
                userLocation.setLatitude(locationResult.getLastLocation().getLatitude());
                for (Transport transport : mMapFragment.getObservingTransport()) {
                    float distance = userLocation.distanceTo(transport.getLocation());
                    Log.i(TAG, "onLocationResult: distance = " + distance);
                    if (distance <= Constants.MAX_ALLOWED_TRANSPORT_DISTANCE) {
                        transports.add(transport);
                    }
                }
                if (!transports.isEmpty()) {
                    mTickets.addTransport(transports);
                }
            }
        });
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
                mTicketsBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                switch (mFindRoutesBehavior.getState()) {
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        mFindRoutesBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        item.setChecked(false);
                        return false;
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
                mFindRoutesBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                switch (mTicketsBehavior.getState()) {
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        mTicketsBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        mBottomNav.getMenu().findItem(R.id.nav_search).setChecked(true);
                        return false;
                    }
                    case BottomSheetBehavior.STATE_HIDDEN: {
                        mTicketsBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        break;
                    }
                    case BottomSheetBehavior.STATE_HALF_EXPANDED: {
                        mTicketsBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        break;
                    }
                    default: {
                        mTicketsBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        break;
                    }
                }
                break;
            }
        }
        return true;
    }

    @Override
    public void onFoundTrip(final Trip[] trips) {
        final TextView timeText = findViewById(R.id.timeText);
        LinearLayout foundTripHeader = mFindRoutes.getView().findViewById(R.id.foundTripHeader);
        foundTripHeader.setVisibility(View.GONE);
        timeText.setVisibility(View.GONE);
        foundTripsList.setVisibility(View.VISIBLE);

        TextView tripText = findViewById(R.id.tripText);
        tripText.setText(String.format("%s → %s",
                trips[0].stops.get(0).getName(),
                trips[0].stops.get(trips[0].stops.size() - 1).getName()));

        if (!tripsAdapter.isEmpty()) tripsAdapter.clear();

        for (Trip trip : trips) {
            tripsAdapter.add(new TripAM(trip));
        }

        foundTripsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mMapFragment.getmMapHelper().drawTrip(trips[i]);
            }
        });
        foundTripsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                tripsAdapter.clear();
                Trip trip = trips[i];

                mMapFragment.getmMapHelper().drawTrip(trip);

                timeText.setText((int)(trip.time) + "m");
                timeText.setVisibility(View.VISIBLE);
                foundTripsList.setVisibility(View.GONE);

                LinearLayout tripStopsList = mFindRoutes.getView().findViewById(R.id.tripStopsList);
                LinearLayout foundTripHeader = mFindRoutes.getView().findViewById(R.id.foundTripHeader);
                foundTripHeader.setVisibility(View.VISIBLE);
                tripStopsList.setVisibility(View.VISIBLE);
                View item = LayoutInflater.from(mFindRoutes.getContext()).inflate(R.layout.trip_stops_item, null);
                tripStopsList.addView(item);


                TextView tripStopText = item.findViewById(R.id.tripStopText);
                tripStopText.setText(trip.stops.get(0).getName());
                tripStopText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_location_black_24dp, 0);
                TextView tripRouteText = item.findViewById(R.id.tripRouteText);
                tripRouteText.setText(trip.routes.get(0).getId());
                for (int s = 1; s < trip.stops.size() - 1; s++) {
                    item = LayoutInflater.from(mFindRoutes.getContext()).inflate(R.layout.trip_stops_item, null);
                    tripStopsList.addView(item);
                    tripStopText = item.findViewById(R.id.tripStopText);
                    tripStopText.setText(trip.stops.get(s).getName());
                    String routeString = trip.routes.get(s - 1).getId() + " → " + trip.routes.get(s).getId();
                    tripRouteText = item.findViewById(R.id.tripRouteText);
                    tripRouteText.setText(routeString);
                }
                int lastIndex = trip.stops.size() - 1;
                item = LayoutInflater.from(mFindRoutes.getContext()).inflate(R.layout.trip_stops_item, null);
                tripStopsList.addView(item);
                tripStopText = item.findViewById(R.id.tripStopText);
                tripStopText.setText(trip.stops.get(lastIndex).getName());
                tripStopText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_flag_red_24dp, 0);
                tripRouteText = item.findViewById(R.id.tripRouteText);
                tripRouteText.setText(trip.routes.get(lastIndex - 1).getId());

                return true;
            }
        });
        mFindRoutesBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
}