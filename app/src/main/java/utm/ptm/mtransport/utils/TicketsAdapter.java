package utm.ptm.mtransport.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.WriterException;

import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import utm.ptm.mtransport.R;
import utm.ptm.mtransport.data.models.Ticket;

public class TicketsAdapter extends ArrayAdapter<TicketAM> {
    private Context mContext;

    public TicketsAdapter(@NonNull Context context) {
        super(context, 0);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View item = convertView;
        if (item == null) {
            item = LayoutInflater.from(mContext).inflate(R.layout.ticket_list_item, parent, false);
        }

        final TicketAM ticket = super.getItem(position);

        TextView routeIdText = item.findViewById(R.id.routeIdText);
        routeIdText.setText(ticket.routeId);

        TextView creationTimeText = item.findViewById(R.id.creationTimeText);
        String creationTimeString = ticket.creationTime.format(DateTimeFormatter.ofPattern("dd MMM HH:mm"));
        creationTimeText.setText(creationTimeString);

        TextView expirationTimeText = item.findViewById(R.id.expirationTimeText);
        expirationTimeText.setText("---");



        return item;
    }
}
