package me.shatilov.symlab.sarathi.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.shatilov.symlab.sarathi.R;
import me.shatilov.symlab.sarathi.adapters.AppListArrayAdapter;
import me.shatilov.symlab.sarathi.model.MiddleBoxModel;
import me.shatilov.symlab.sarathi.model.SettingsModel;


public class TabbedActivity extends AppCompatActivity {

    static MiddleBoxModel[] boxes = {
            new MiddleBoxModel("YouTube"),
            new MiddleBoxModel("Skype"),
            new MiddleBoxModel("Facebook")
    };

    static String testMessage;
    static {
        try {
            testMessage = new ObjectMapper().writeValueAsString(new SettingsModel(new ArrayList<>(Arrays.asList(boxes))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BottomNavigationView bottomNavigationView;
    private SettingsModel config;

    private List<View> settingsTab = new ArrayList<>();
    private List<View> appListTab = new ArrayList<>();

    private MqttAndroidClient mqttAndroidClient;
    private String serverUri;
    private String subscriptionTopic;
    private AppListArrayAdapter appListAdapter;

    private void T(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private void handleIconsColor() {
        Menu menu = bottomNavigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            Drawable icon = item.getIcon();
            icon.mutate();
            int colorID = R.color.colorPrimaryDark;
            if (item.getItemId() == bottomNavigationView.getSelectedItemId()) {
                colorID = R.color.colorPrimaryLight;
            }
            icon.setColorFilter(getResources().getColor(colorID), PorterDuff.Mode.SRC_IN);
            item.setIcon(icon);
        }
    }

    private void renderActiveTab(final int actionID) {
        int appListVisibility = actionID == R.id.action_list ? View.VISIBLE : View.GONE;
        int settingsVisibility = actionID == R.id.action_settings ? View.VISIBLE : View.GONE;
        for (View v : settingsTab) {
            v.setVisibility(settingsVisibility);
        }
        for (View v : appListTab) {
            v.setVisibility(appListVisibility);
        }
    }

    private void initMqqtClient() {
        serverUri = ((EditText)findViewById(R.id.serverUriInput)).getText().toString();
        subscriptionTopic = ((EditText)findViewById(R.id.subscriptionTopicInput)).getText().toString();
        String clientId = ((EditText) findViewById(R.id.clientIdInput)).getText().toString();

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    T("Reconnected to: " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic();
                } else {
//                    T("Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                T("The connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                handleIncomingMessage(new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);

        try {
            //addToHistory("Connecting to " + serverUri);
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    T("Failed to connect to: " + serverUri);
                }
            });


        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    public void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    T("Subscribed to: " + serverUri + subscriptionTopic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    T("Failed to subscribe");
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    private void handleIncomingMessage(String message) {
        try {
            SettingsModel newConfig = new ObjectMapper().readValue(message, SettingsModel.class);
            if (config == null || config.getMiddleBoxes().isEmpty() || !config.getMiddleBoxes().containsAll(newConfig.getMiddleBoxes())) {
                config.setMiddleBoxes(newConfig.getMiddleBoxes());
                appListAdapter.notifyDataSetChanged();
                T("New config applied");
            } else {
                T("Received already applied config");
            }
        } catch (IOException e) {
            T("Message ignored. Not a config message");
        }
    }

    public void publishMessage(String publishMessage) {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(publishMessage.getBytes());
            message.setRetained(true);
            mqttAndroidClient.publish(subscriptionTopic, message);
            T("Message Published");
            if (!mqttAndroidClient.isConnected()) {
                T(mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Sarathi settings");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        View settingsView = findViewById(R.id.settings_view);
        ListView appListView = findViewById(R.id.apps_list);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        settingsTab.add(settingsView);
        appListTab.add(appListView);

        config = new SettingsModel(new ArrayList<MiddleBoxModel>());
        appListAdapter = new AppListArrayAdapter(this, R.layout.app_list_item, config.getMiddleBoxes());
        appListAdapter.notifyDataSetChanged();
        appListView.setAdapter(appListAdapter);

        handleIconsColor();
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        item.setChecked(true);
                        handleIconsColor();
                        renderActiveTab(item.getItemId());
                        return false;
                    }
                }
        );

        //Mqqt
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET}, 1);
        }
        initMqqtClient();

        //Settings buttons
        (findViewById(R.id.reconnect_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initMqqtClient();
            }
        });

        (findViewById(R.id.test_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishMessage(testMessage);
            }
        });
    }
}
