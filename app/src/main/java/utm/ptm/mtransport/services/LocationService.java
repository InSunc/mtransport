package utm.ptm.mtransport.services;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;


public class LocationService implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int ACCESS_FINE_LOCATION_CODE = 9000;
    private static final int MY_LOCATION_REQUEST_CODE = 9001;
    private boolean locationPermissionGranted = false;
    private static View view = null;

    public LocationService(View view) {
        this.view = view;
    }

    public boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            locationPermissionGranted = false;
        }

        return locationPermissionGranted;
    }



    public void requestPermission() {
        ActivityCompat.requestPermissions((Activity) view.getContext(),
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                this.ACCESS_FINE_LOCATION_CODE);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(view, "Permission granted!", Snackbar.LENGTH_LONG).show();
                locationPermissionGranted = true;
            } else {
                // Permission was denied. Display an error message.
                Snackbar.make(view, "No location permission!", Snackbar.LENGTH_LONG).show();
                locationPermissionGranted = false;
            }
        }
    }
}
