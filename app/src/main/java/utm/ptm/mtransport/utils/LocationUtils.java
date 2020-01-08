package utm.ptm.mtransport.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;


public class LocationUtils implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = LocationUtils.class.getSimpleName();

    public static final LatLng CHISINAU_COORD = new LatLng(47.0105, 28.8638);
    private static final int ACCESS_FINE_LOCATION_CODE = 9000;
    private static final int MY_LOCATION_REQUEST_CODE = 9001;

    private View mView;
    private static LatLng mLastKnownLocation;
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;



    public LocationUtils(View view) {
        mView = view;
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mView.getContext());
        if (mLastKnownLocation == null) {
            mLastKnownLocation = CHISINAU_COORD;
        }
        getLastKnownLocation();
    }



    public boolean isPermissionGranted() {
        mLocationPermissionGranted = ContextCompat.checkSelfPermission(mView.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        return mLocationPermissionGranted;
    }



    public void requestPermission() {
        ActivityCompat.requestPermissions((Activity) mView.getContext(),
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                ACCESS_FINE_LOCATION_CODE);
    }


    public LatLng getLastKnownLocation() {
        if (isPermissionGranted()) {
            mFusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener((Activity) mView.getContext(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                double lat = location.getLatitude();
                                double lng = location.getLongitude();
                                mLastKnownLocation = new LatLng(lat, lng);
                                Snackbar.make(mView, "Last known: " + mLastKnownLocation, Snackbar.LENGTH_LONG).show();
                            } else {
                                Snackbar.make(mView, "Problems", Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }).addOnFailureListener((Activity) mView.getContext(), new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    String msg = "NULL ptr returned. Location not found. Returning last known location";
                    Log.e(TAG, msg);
                    Snackbar.make(mView, msg, Snackbar.LENGTH_LONG).show();
                }
            });
        } else {
            String msg = "No location permission";
            Log.e(TAG, msg);
            Snackbar.make(mView, msg, Snackbar.LENGTH_LONG).show();
            requestPermission();
        }
        return mLastKnownLocation;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(mView, "Permission granted!", Snackbar.LENGTH_LONG).show();
                mLocationPermissionGranted = true;
            } else {
                // Permission was denied. Display an error message.
                Snackbar.make(mView, "No location permission!", Snackbar.LENGTH_LONG).show();
                mLocationPermissionGranted = false;
            }
        }
    }
}
