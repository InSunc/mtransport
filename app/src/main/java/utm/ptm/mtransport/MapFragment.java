package utm.ptm.mtransport;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import utm.ptm.mtransport.data.DatabaseHandler;
import utm.ptm.mtransport.data.models.Route;
import utm.ptm.mtransport.data.models.Transport;
import utm.ptm.mtransport.data.models.TransportMarker;
import utm.ptm.mtransport.data.models.Way;
import utm.ptm.mtransport.helpers.GeofenceHelper;
import utm.ptm.mtransport.helpers.LocationHelper;
import utm.ptm.mtransport.helpers.MapHelper;
import utm.ptm.mtransport.helpers.MqttHelper;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, MqttHelper.Listener, GeofenceHelper.Listener {

    private static final String TAG = MapFragment.class.getSimpleName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private GoogleMap mMap;
    private MapView mMapView;
    private View mView;

    private LocationHelper mLocationHelper;
    private MqttHelper mMqttHelper;
    private MapHelper mMapHelper;
    private GeofenceHelper mGeofenceHelper;

    private HashMap<Transport, Marker> transportMarkers;
    private List<String> observingRoutes;

    private OnFragmentInteractionListener mListener;

    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mMqttHelper.disconnect();
        Log.d(TAG, "Destroyed");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView =  inflater.inflate(R.layout.fragment_map, container, false);
        mLocationHelper = new LocationHelper(mView);
        mMqttHelper = new MqttHelper(this);
        mGeofenceHelper = new GeofenceHelper(this);

        return mView;
    }



    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    public List<String> getObservingRoutes() {
        return observingRoutes;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMapView = (MapView) mView.findViewById(R.id.mapView);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    public void setMapStyle(int style) {
        try {
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            mView.getContext(), style));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMqttHelper.connect();
        MapsInitializer.initialize(mView.getContext());
        mMap = googleMap;
        observingRoutes = new ArrayList<>();
        transportMarkers = new HashMap<>();
        mMapHelper = new MapHelper(this, observingRoutes, transportMarkers);

        LinearLayout routesList = mView.findViewById(R.id.routes_list);
        List<String> routeIds = DatabaseHandler.getInstance(mView.getContext()).getRouteIds();

        for (String routeId : routeIds) {
            ToggleButton button = new ToggleButton(mView.getContext());
            button.setTextOff(routeId);
            button.setTextOn(routeId);
            button.setText(routeId);
            button.setHighlightColor(getResources().getColor(R.color.colorPrimary));
            button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.trolleybus_icon, 0, 0,0);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToggleButton button = (ToggleButton) v;
                    if (button.isChecked()) {
                        mMapHelper.startTracking(button.getText().toString());
                    } else {
                        mMapHelper.stopTracking(button.getText().toString());
                    }
                }
            });
            routesList.addView(button);
        }


//        setMapStyle(R.raw.map_style);

        UiSettings conf = mMap.getUiSettings();
        conf.setMapToolbarEnabled(false);
        conf.setMyLocationButtonEnabled(true);
        conf.setZoomControlsEnabled(false);

        mMap.setMyLocationEnabled(true);
        LatLng currentLocation = mLocationHelper.getLastKnownLocation();

        mMap.setOnMapClickListener(this);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 10.f));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }


    public GoogleMap getMap() {
        return mMap;
    }


    List<LatLng> simulatioonPoints;
    public void createRoute() {
        RequestQueue queue = Volley.newRequestQueue(mView.getContext());
        String url ="http://192.168.100.7:8080/ways";

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Gson gson = new Gson();
                        Route route = gson.fromJson(response, Route.class);
                        List<Way> ways = route.getWays();
                        for (Way way : ways) {
//                            MarkerOptions mo = new MarkerOptions();
//                            mo.position(way.getPoints().get(0));
//                            mMap.addMarker(mo);
//                            mo.position(way.getPoints().get(way.getPoints().size() - 1));
//                            mMap.addMarker(mo);

                            List<LatLng> points = way.getPoints();
                            final PolylineOptions polylineOptions = new PolylineOptions();
                            polylineOptions.addAll(points);
                            polylineOptions.color(new Random().nextInt());
                            mMap.addPolyline(polylineOptions);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mView.getContext(), "Req error", Toast.LENGTH_LONG).show();
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public MapHelper getmMapHelper() {
        return mMapHelper;
    }

    @Override
    public void onMapClick(LatLng latLng) {
//        mMap.addMarker(new MarkerOptions().position(latLng).title("Tapped here"));
        Random nr = new Random();
        String id = String.valueOf(nr.nextInt());
        mGeofenceHelper.removeGeofences();
        mGeofenceHelper.addGeofence(latLng, id);

    }

    @Override
    public void onMessageArrived(Transport transport) {
        mMapHelper.mark(transport);
    }

    @Override
    public void onAddedGeofence(LatLng position) {
        mMapHelper.mark(position);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
