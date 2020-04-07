package utm.ptm.mtransport.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


import utm.ptm.mtransport.MapFragment;
import utm.ptm.mtransport.R;
import utm.ptm.mtransport.data.models.Transport;
import utm.ptm.mtransport.data.models.TransportMarker;

public class MapHelper {
    private static final String TAG = MapHelper.class.getSimpleName();

    private final Context context;
    private final GoogleMap mMap;
    private HashMap<Transport, Marker> transportMarkers;
    private List<String> observingRoutes;

    public MapHelper(MapFragment mapFragment, List<String> observingRoutes, HashMap<Transport, Marker> transportMarkerMap) {
        this.mMap = mapFragment.getMap();
        this.context = mapFragment.getContext();
        this.observingRoutes = observingRoutes;
        this.transportMarkers = new HashMap<>();
    }

    public void mark(LatLng position) {
        CircleOptions circleOptions = new CircleOptions()
                .center(position)
                .strokeColor(Color.argb(50, 70,70,70))
                .fillColor( Color.argb(100, 150,150,150) )
                .radius(GeofenceHelper.GEOFENCE_RADIUS);
        mMap.addCircle( circleOptions );
    }

    public void startTracking(String routeId) {
        observingRoutes.add(routeId);
        for (Map.Entry<Transport, Marker> entry : transportMarkers.entrySet()) {
            if (entry.getKey().getRouteId().equals(routeId)) {
                entry.getValue().setVisible(true);
            }
        }
    }

    public void stopTracking(String routeId) {
        observingRoutes.remove(routeId);
        for (Map.Entry<Transport, Marker> entry : transportMarkers.entrySet()) {
            if (entry.getKey().getRouteId().equals(routeId)) {
                entry.getValue().setVisible(false);
            }
        }
    }


    public void mark(Transport transport) {
        Log.i(TAG, "onMessageArrived: MARKED");
        if (observingRoutes.contains(transport.getRouteId())) {
            new MarkingTask().execute(transport);
        }
    }


    private class MarkingTask extends AsyncTask<Transport, Void, Pair<Transport, Object>> {
        @Override
        protected Pair<Transport, Object> doInBackground(Transport... transports) {
            Log.i(TAG, "doInBackground: ");
            if (transports.length > 1) {
                System.out.println(">>>>>>> 11111111111111111");
            }

            Transport transport = transports[0];

            LatLng location = new LatLng(transport.getLatitude(), transport.getLongitude());

            if (!transportMarkers.containsKey(transport)) {
                BitmapDrawable bitmapdraw = (BitmapDrawable) context.getResources().getDrawable(R.drawable.green_transport_mark);
                Bitmap bmp = bitmapdraw.getBitmap();
                bmp = Bitmap.createScaledBitmap(bmp, 35, 35, false);

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(location)
                        .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                        .anchor(0.5f, 0.5f);

                return new Pair(transport, (Object) markerOptions);

            } else {
                return new Pair(transport, (Object) location);
            }
        }

        @Override
        protected void onPostExecute(Pair<Transport, Object> result) {
            Marker marker;
            if (result.second instanceof LatLng) {
                marker = transportMarkers.get(result.first);
                marker.setPosition((LatLng) result.second);
            } else if (result.second instanceof MarkerOptions){
                marker = mMap.addMarker((MarkerOptions) result.second);
                transportMarkers.put(result.first, marker);
            }
        }
    }

}
