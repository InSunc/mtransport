package utm.ptm.mtransport.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

import utm.ptm.mtransport.MapFragment;
import utm.ptm.mtransport.R;
import utm.ptm.mtransport.data.models.Transport;

public class MapHelper {
    private static final String TAG = MapHelper.class.getSimpleName();

    private final Context context;
    private final GoogleMap mMap;
    private Map<Integer, Marker> transportMarkerMap = new HashMap<>();

    public MapHelper(MapFragment mapFragment) {
        this.mMap = mapFragment.getMap();
        this.context = mapFragment.getContext();
    }

    public void mark(Transport transport) {
        Log.i(TAG, "onMessageArrived: MARKED");
        new MarkingTask().execute(transport);
    }

    private class MarkingTask extends AsyncTask<Transport, Void, Pair<Integer, Object>> {
        @Override
        protected Pair<Integer, Object> doInBackground(Transport... transports) {

            if (transports.length > 1) {
                System.out.println(">>>>>>> 11111111111111111");
            }
            Transport transport = transports[0];
            LatLng location = new LatLng(transport.getLatitude(), transport.getLongitude());

            Marker foundMarker = transportMarkerMap.get(transport.getBoard());

            if(foundMarker == null) {
                BitmapDrawable bitmapdraw = (BitmapDrawable)context.getResources().getDrawable(R.drawable.green_transport_mark);
                Bitmap bmp = bitmapdraw.getBitmap();
                bmp = Bitmap.createScaledBitmap(bmp, 35, 35, false);

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(location)
                             .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                             .anchor(0.5f,0.5f);

                return new Pair<>(transport.getBoard(), (Object) markerOptions);
            } else {
                return new Pair<>(transport.getBoard(), (Object) location);
            }
        }

        @Override
        protected void onPostExecute(Pair<Integer, Object> result) {
            if (result.second instanceof LatLng) {
                Marker marker = transportMarkerMap.get(result.first);
                marker.setPosition((LatLng) result.second);
            } else if (result.second instanceof MarkerOptions){
                Marker marker = mMap.addMarker((MarkerOptions) result.second);
                transportMarkerMap.put(result.first, marker);
            }
        }
    }

}
