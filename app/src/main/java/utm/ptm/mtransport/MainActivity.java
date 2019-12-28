package utm.ptm.mtransport;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import utm.ptm.mtransport.utils.LocationUtils;

public class MainActivity extends AppCompatActivity implements MapFragment.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void onClick(View view) {
        LocationUtils ls = new LocationUtils(view);
        ls.getLastKnownLocation();
        Toast.makeText(this, "LKL: " + ls.getLastKnownLocation(), Toast.LENGTH_LONG).show();
    }
}
