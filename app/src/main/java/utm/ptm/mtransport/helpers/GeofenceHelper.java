package utm.ptm.mtransport.helpers;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import utm.ptm.mtransport.MapFragment;
import utm.ptm.mtransport.receivers.GeofenceBroadcastReceiver;

public class GeofenceHelper {

    private static final String TAG = GeofenceHelper.class.getSimpleName();

    private Listener mListener;

    private Context context;
    private static List<Geofence> geofences;
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;
    public static final long GEOFENCE_DURATION = 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS = 15.0f; // in meters

    public GeofenceHelper(MapFragment mapFragment) {
        this.context = mapFragment.getContext();
        this.geofencingClient = LocationServices.getGeofencingClient(context);
        mListener = (Listener) mapFragment;
        geofences = new ArrayList<>();
    }

    // Create a Geofence
    private Geofence createGeofence(LatLng latLng, String geofenceId) {
        Log.d(TAG, "createGeofence");
        return new Geofence.Builder()
                .setRequestId(geofenceId)
                .setCircularRegion(latLng.latitude, latLng.longitude, GEOFENCE_RADIUS)
                .setExpirationDuration(GEOFENCE_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    public void addGeofence(final LatLng position, String id) {
        final Geofence geofence = createGeofence(position, id);
        final GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
        geofencingClient.addGeofences(geofenceRequest, getGeofencePendingIntent())
                .addOnSuccessListener((Activity) context, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Added succesfully");
                        mListener.onAddedGeofence(position);
                    }
                })
                .addOnFailureListener((Activity) context, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: geofence not added");
                    }
                });
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        Log.d(TAG, "createGeofenceRequest");
        geofences.add(geofence);
        return new GeofencingRequest.Builder()
                .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
                .addGeofence(geofence)
                .build();
    }

    public void removeGeofences() {
        geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener((Activity) context, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences removed
                        // ...
                    }
                })
                .addOnFailureListener((Activity) context, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to remove geofences
                        // ...
                    }
                });

    }


    public interface Listener {
        void onAddedGeofence(LatLng position);
    }
}
