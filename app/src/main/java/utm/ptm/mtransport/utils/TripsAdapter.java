package utm.ptm.mtransport.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import utm.ptm.mtransport.R;

public class TripsAdapter extends ArrayAdapter<TripAM> {
    private Context mContext;

    public TripsAdapter(@NonNull Context context) {
        super(context, 0);
        mContext = context;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View item = convertView;
        if (item == null) {
            item = LayoutInflater.from(mContext).inflate(R.layout.found_trips_item_layout, parent, false);
        }

        TripAM trip = super.getItem(position);

        TextView routesText = item.findViewById(R.id.found_trip_routes);
        routesText.setText(trip.routes);

        TextView timeText = item.findViewById(R.id.trip_time);
        timeText.setText(String.valueOf(trip.time).concat("m"));

        TextView costText = item.findViewById(R.id.trip_cost);
        costText.setText(String.valueOf(trip.cost).concat(" MDL"));

        return item;
    }
}
