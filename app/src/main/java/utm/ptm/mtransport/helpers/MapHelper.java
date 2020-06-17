package utm.ptm.mtransport.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import utm.ptm.mtransport.Constants;
import utm.ptm.mtransport.MapFragment;
import utm.ptm.mtransport.R;
import utm.ptm.mtransport.data.models.Stop;
import utm.ptm.mtransport.data.models.Transport;
import utm.ptm.mtransport.data.models.Trip;
import utm.ptm.mtransport.data.models.Way;

public class MapHelper implements GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener
                                , GoogleMap.OnCameraMoveListener {
    private static final String TAG = MapHelper.class.getSimpleName();

    private Listener mListener;
    private final Context context;
    private final GoogleMap mMap;

    @Override
    public void onCameraMove() {
    }

    private enum LongClickState {SELECT_ORIGIN, SELECT_DESTINATION, CLEAR}

    private LongClickState longClickState = LongClickState.SELECT_ORIGIN;
    private Marker originMarker;
    private Marker destinationMarker;
    private List<Marker> tripMarkers;

    private HashMap<Transport, Marker> transportMarkers;
    private List<String> observingRoutes;
    private List<Polyline> path;

    public MapHelper(MapFragment mapFragment, List<String> observingRoutes, HashMap<Transport, Marker> transportMarkerMap) {
        this.mMap = mapFragment.getMap();
        this.context = mapFragment.getContext();
        mListener = (Listener) mapFragment;
        this.observingRoutes = observingRoutes;
        this.transportMarkers = transportMarkerMap;
        tripMarkers = new ArrayList<>();
        path = new ArrayList<>();
    }

    public void mark(LatLng position) {
        CircleOptions circleOptions = new CircleOptions()
                .center(position)
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .radius(GeofenceHelper.GEOFENCE_RADIUS);
        mMap.addCircle(circleOptions);
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

    public MarkerOptions markStop(Stop stop) {
        BitmapDrawable bitmapdraw = (BitmapDrawable) context.getResources().getDrawable(R.drawable.stop_mark);
        Bitmap bmp = bitmapdraw.getBitmap();
        bmp = Bitmap.createScaledBitmap(bmp, 60, 60, false);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(stop.getLocation())
                .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                .title(stop.getName());

        return markerOptions;
    }

    public void drawTrip(Trip trip) {
        for (Stop stop : trip.stops) {
            MarkerOptions mo = markStop(stop);
            tripMarkers.add(mMap.addMarker(mo));
        }
        drawPath(trip.ways);
    }

    private void drawPath(Iterable<Way> ways) {
        if (!path.isEmpty()) {
            for (Polyline polyline : path) {
                polyline.remove();
            }
        }
        for (Way way : ways) {
            final PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.addAll(way.getPoints());
            polylineOptions.color(context.getResources().getColor(R.color.path));
            Polyline polyline = mMap.addPolyline(polylineOptions);
            path.add(polyline);
        }
    }


    @Override
    public void onMapClick(LatLng latLng) {
        Transport transport = new Transport();
        transport.setRouteId("T2");
        transport.setBoard(100);
        transport.setLatitude(latLng.latitude);
        transport.setLongitude(latLng.longitude);
        mark(transport);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        switch (longClickState) {
            case SELECT_ORIGIN: {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                originMarker = mMap.addMarker(markerOptions);
                longClickState = LongClickState.SELECT_DESTINATION;
                break;
            }

            case SELECT_DESTINATION: {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                destinationMarker = mMap.addMarker(markerOptions);

                RequestQueue queue = Volley.newRequestQueue(context);
                LatLng origin = originMarker.getPosition();
                LatLng destination = destinationMarker.getPosition();
                List<LatLng> coords = new ArrayList<>();
                coords.add(origin);
                coords.add(destination);
                Gson gson = new Gson();
                final String jsonString = gson.toJson(coords.toArray(), LatLng[].class);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.TRIP_ENDPOINT,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Gson gson = new Gson();
                                Trip[] trips = gson.fromJson(response, Trip[].class);

                                mListener.onFoundTrip(trips);

//                                for (Stop stop : trip.stops) {
//                                    MarkerOptions mo = markStop(stop);
//                                    tripMarkers.add(mMap.addMarker(mo));
//                                }
//
//                                for (Way way : trip.ways) {
//                                    final PolylineOptions polylineOptions = new PolylineOptions();
//                                    polylineOptions.addAll(way.getPoints());
//                                    polylineOptions.color(context.getResources().getColor(R.color.path));
//                                    Polyline polyline = mMap.addPolyline(polylineOptions);
//                                    path.add(polyline);
//                                }
//                                Trip[] trips = gson.fromJson(response, Trip[].class);
//                                for (Trip trip : trips) {
//                                    Random random = new Random();
//                                    float color = random.nextFloat()*(330.0F - 0.0F + 1);
//                                    Log.i(TAG, "onResponse: " + trip.routes.toString());
//                                    for (Stop stop : trip.stops) {
//                                        MarkerOptions mo = new MarkerOptions();
//                                        mo.position(stop.getLocation())
//                                                .title(stop.getName())
//                                                .icon(BitmapDescriptorFactory
//                                                        .defaultMarker(color));
//                                        tripMarkers.add(mMap.addMarker(mo));
//                                    }
//                                }
//                                Log.d(TAG, trips.toString());
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, "Req Error on gettings routes" + error.networkResponse);
                            }
                        }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<String, String>();
                        headers.put("Content-Type", "application/json");
                        return headers;
                    }

                    @Override
                    public byte[] getBody() {
                        return jsonString.getBytes();
                    }
                };
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(stringRequest);


                longClickState = LongClickState.CLEAR;
                break;
            }

            case CLEAR: {
                for (Marker tripMarker : tripMarkers) {
                    tripMarker.remove();
                }
                originMarker.remove();
                destinationMarker.remove();
                for (Polyline polyline : path) {
                    polyline.remove();
                }
                longClickState = LongClickState.SELECT_ORIGIN;
                break;
            }
        }
    }

    public void mark(Transport transport) {
        Log.i(TAG, "onMessageArrived: MARKED");
        if (observingRoutes.contains(transport.getRouteId())) {
            new MarkingTask().execute(transport);
        }
    }

    private class MarkingTask extends AsyncTask<Transport, Void, Pair<Transport, MarkerOptions>> {
        @Override
        protected Pair<Transport, MarkerOptions> doInBackground(Transport... transports) {
            Log.i(TAG, "doInBackground: ");
            if (transports.length > 1) {
                System.out.println(">>>>>>> 11111111111111111");
            }

            Transport transport = transports[0];

            LatLng location = new LatLng(transport.getLatitude(), transport.getLongitude());

            
            BitmapDrawable iconDrawable = null;
            switch (transport.getLoadLevel()) {
                case 0: {
                    iconDrawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.green_transport_mark);
                    break;
                }

                case 1: {
                    iconDrawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.yellow_transport_mark);
                    break;
                }

                case 2: {
                    iconDrawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.red_transport_mark);
                    break;
                }
            }

            final int INDICATOR_WIDTH = 120;
            final int INDICATOR_HEIGHT = 70;
            final int ICON_SIZE = 35;

            BitmapDrawable cloudDrawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.cloud);
            Bitmap bmp = Bitmap.createBitmap(INDICATOR_WIDTH, INDICATOR_HEIGHT, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bmp);
            Paint paint = new Paint();
            paint.setTextSize(30);
            paint.setColor(Color.BLACK);
            paint.setFakeBoldText(true);
            paint.setElegantTextHeight(true);
            paint.setLinearText(true);

            RectF iconArea = new RectF(0, 0, ICON_SIZE, ICON_SIZE);
            RectF textArea = new RectF(ICON_SIZE, 0, INDICATOR_WIDTH, INDICATOR_HEIGHT);

            canvas.drawBitmap(iconDrawable.getBitmap(), null, iconArea, null);
            canvas.drawBitmap(cloudDrawable.getBitmap(), null, textArea, null);
            String indicatorText = transport.getRouteId();
            // center text in the cloud image
            int textPosX = ICON_SIZE + 15;
            textPosX = indicatorText.length() == 2 ? textPosX + 10 : textPosX;
            canvas.drawText(indicatorText, textPosX, ICON_SIZE + 15, paint);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(location)
                    .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                    .anchor(0.1f, 0.5f);

            return new Pair(transport, (Object) markerOptions);
        }

        @Override
        protected void onPostExecute(Pair<Transport, MarkerOptions> result) {
            Marker marker;
            if (transportMarkers.containsKey(result.first)) {
                marker = transportMarkers.get(result.first);
                marker.setPosition(result.second.getPosition());
                marker.setIcon(result.second.getIcon());
            } else {
                marker = mMap.addMarker(result.second);
                transportMarkers.put(result.first, marker);
            }
        }
    }


    public interface Listener {
        void onFoundTrip(Trip[] trips);
    }
}
