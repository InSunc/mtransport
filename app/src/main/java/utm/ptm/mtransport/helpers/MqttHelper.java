package utm.ptm.mtransport.helpers;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.android.service.MqttService;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import utm.ptm.mtransport.Constants;
import utm.ptm.mtransport.MapFragment;
import utm.ptm.mtransport.data.DatabaseHandler;
import utm.ptm.mtransport.data.models.Route;
import utm.ptm.mtransport.data.models.Transport;

public class MqttHelper {
    private static final String TAG = MqttHelper.class.getSimpleName();

    private Listener mListener;

    private static MqttAsyncClient mqttAndroidClient;
    private boolean connected;
    private IMqttToken token;
    private List<String> topics = new ArrayList<>();

    public MqttHelper(MapFragment mapFragment) {
        final Context context = mapFragment.getContext();
        mListener = (Listener) mapFragment;
        String clientId = generateId(7);
        try {
            mqttAndroidClient = new MqttAsyncClient(Constants.BROKER_URL, clientId, new MemoryPersistence());
            mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean b, String s) {
                    Log.i(TAG, "Successfully connected to " + s);
                    setConnected(true);
                    subscribeToRoutes();
                }

                @Override
                public void connectionLost(Throwable throwable) {
                    Log.w(TAG, "Connection lost!");
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                    Gson gson = new Gson();
                    String data = mqttMessage.toString();
                    data.substring(data.indexOf("{"));
                    Transport transport = gson.fromJson(data, Transport.class);
                    transport.setRouteId(topic);
                    Log.i(TAG, ">>>>>" + transport.getBoard() + " - "
                            + transport.getLatitude() + " - "
                            + transport.getLongitude());
                    mListener.onMessageArrived(transport);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    public void setConnected(boolean value) {
        this.connected = value;
    }

    private String generateId(int idLength) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(idLength);
        for (int i = 0; i < idLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }

        return buffer.toString();
    }

    public void subscribeToRoutes() {
        DatabaseHandler db = DatabaseHandler.getInstance(null);

        List<String> routeIds = db.getRouteIds();
        if (routeIds != null) {
            for (String routeId : routeIds) {
                subscribe(routeId);
            }
        }
    }

    public void connect() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
                mqttConnectOptions.setAutomaticReconnect(true);
                mqttConnectOptions.setCleanSession(false);

                try {
                    IMqttToken token = mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.i(TAG, "Ready to talk!");

                            DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                            disconnectedBufferOptions.setBufferEnabled(true);
                            disconnectedBufferOptions.setBufferSize(100);
                            disconnectedBufferOptions.setPersistBuffer(false);
                            disconnectedBufferOptions.setDeleteOldestMessages(false);
                            mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Log.e(TAG, "Failed to connect to: " + Constants.BROKER_URL + exception.toString());
                        }
                    });

                    token.waitForCompletion(3500);

                } catch (MqttException e) {
                    switch (e.getReasonCode()) {
                        case MqttException.REASON_CODE_BROKER_UNAVAILABLE:
                        case MqttException.REASON_CODE_CLIENT_TIMEOUT:
                        case MqttException.REASON_CODE_CONNECTION_LOST:
                        case MqttException.REASON_CODE_SERVER_CONNECT_ERROR:
                            Log.v(TAG, "CONNECTION " + e.getMessage());
                            e.printStackTrace();
                            break;
                        case MqttException.REASON_CODE_FAILED_AUTHENTICATION:
                            Log.e(TAG, "REASON_CODE_FAILED_AUTHENTICATION" + e.getMessage());
                            break;
                        default:
                            Log.e(TAG, "Emm: " + e.getMessage());
                    }
                }
            }
        });
        thread.start();
    }


    public void subscribe(String topic) {
        if (connected) {
            try {
                mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.i(TAG, " >>>>>> Subscribed <<<<<<<");
                        String[] topicsArray = asyncActionToken.getTopics();
                        for (String topic : topicsArray) {
                            Log.i(TAG, topic);
                            topics.add(topic);
                        }
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.e(TAG, "Subscribe failed!");
                    }
                });

            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }


    public void disconnect() {
        try {
            mqttAndroidClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public interface Listener {
        public void onMessageArrived(Transport transport);
    }
}
