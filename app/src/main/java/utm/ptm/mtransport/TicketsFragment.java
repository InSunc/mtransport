package utm.ptm.mtransport;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.zxing.WriterException;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import utm.ptm.mtransport.data.models.Ticket;
import utm.ptm.mtransport.data.models.Transport;
import utm.ptm.mtransport.utils.TicketAM;
import utm.ptm.mtransport.utils.TicketsAdapter;


/**
 * attr
 * A simple {@link Fragment} subclass.
 */
public class TicketsFragment extends Fragment {
    private static final String TAG = TicketsFragment.class.getSimpleName();

    private RadioGroup toBuyRoutes;
    private Context context;
    private Transport selectedTransport;
    private HashMap<Transport, RadioButton> radioButtons;

    private TicketsAdapter ticketsAdapter;
    private ListView ticketList;

    public TicketsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_tickets, container, false);
        context = view.getContext();

        ticketsAdapter = new TicketsAdapter(context);
        ticketList = view.findViewById(R.id.ticketList);
        ticketList.setAdapter(ticketsAdapter);

//        ticketList.inflate(R.layout.ticket_preview, null);
        ticketList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TicketAM ticket = ticketsAdapter.getItem(i);
                String inputString = String.valueOf(ticket.id);
                QRGEncoder qrgEncoder = new QRGEncoder(inputString, null, QRGContents.Type.TEXT, 500);
                Bitmap bitmap = null;
                try {
                    bitmap = qrgEncoder.encodeAsBitmap();
                } catch (WriterException e) {
                    e.printStackTrace();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                View qrView = LayoutInflater.from(context).inflate(R.layout.ticket_preview1, null);
                ImageView qrImage = qrView.findViewById(R.id.qrImage);
                qrImage.setImageBitmap(bitmap);
                Button backButton = qrView.findViewById(R.id.back_button);
                TextView qrTitle = qrView.findViewById(R.id.qrTitle);
                qrTitle.setText("Tichet pentru ruta " + ticket.routeId);
                builder.setView(qrView);
                final AlertDialog qrPreviewDialog = builder.create();
                qrPreviewDialog.show();
                qrImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        qrPreviewDialog.dismiss();
                    }
                });
                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        qrPreviewDialog.dismiss();
                    }
                });
            }
        });

        ticketList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                TicketAM ticket = ticketsAdapter.getItem(i);

                TextView expirationTimeText = view.findViewById(R.id.expirationTimeText);
                ticket.expirationTime = LocalDateTime.now();
                String expirationTimeString = ticket.expirationTime.format(DateTimeFormatter.ofPattern("dd MMM HH:mm"));
                expirationTimeText.setText(expirationTimeString);

                return true;
            }
        });

        radioButtons = new HashMap<>();

        toBuyRoutes = view.findViewById(R.id.ticketRoutesList);
        Button button = view.findViewById(R.id.buyTicket);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, selectedTransport.getRouteId(), Toast.LENGTH_SHORT).show();
                RequestQueue queue = Volley.newRequestQueue(context);
                JsonObjectRequest jsonRequest = new JsonObjectRequest
                        (Request.Method.GET, Constants.TICKETS_ENDPOINT + selectedTransport.getBoard(), null, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                                    @Override
                                    public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                                        String dateTimeString = json.getAsJsonPrimitive().getAsString();
                                        dateTimeString = dateTimeString.substring(0, dateTimeString.indexOf('.'));
                                        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, dateTimeFormatter);
                                        Log.w(TAG, "deserialize: " + localDateTime.toString());
                                        return localDateTime;
                                    }
                                }).create();

                                Log.w(TAG, "onResponse: " + response.toString());
                                Ticket ticket = gson.fromJson(response.toString(), Ticket.class);
                                Log.w(TAG, "onResponse: " + ticket.creationTime.toString());
                                ticketsAdapter.add(new TicketAM(selectedTransport, ticket));
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, "Req Error on gettings ticket");
                            }
                        });

                queue.add(jsonRequest);
            }
        });
        return view;
    }

    public void addTransport(List<Transport> transports) {
        for (Transport transport : radioButtons.keySet()) {
            if (!transports.contains(transport)) {
                radioButtons.remove(transport);
            }
        }

        transports.removeAll(radioButtons.keySet());

        for (final Transport transport : transports) {
            RadioButton radioButton = new RadioButton(context);
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RadioButton r = (RadioButton) view;
                    selectedTransport = transport;
                }
            });
            radioButton.setText(transport.getRouteId());
            radioButtons.put(transport, radioButton);
            toBuyRoutes.addView(radioButton);
        }
    }

}
