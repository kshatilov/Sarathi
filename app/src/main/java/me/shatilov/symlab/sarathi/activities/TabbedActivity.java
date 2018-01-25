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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.shatilov.symlab.sarathi.R;
import me.shatilov.symlab.sarathi.adapters.AppListArrayAdapter;
import me.shatilov.symlab.sarathi.model.MiddleBoxModel;
import me.shatilov.symlab.sarathi.model.SettingsModel;
import me.shatilov.symlab.sarathi.mqqt.MqqtClientWrapper;
import me.shatilov.symlab.sarathi.mqqt.EasyCallable;


public class TabbedActivity extends AppCompatActivity {

    static MiddleBoxModel[] boxes = {
            new MiddleBoxModel("YouTube"),
            new MiddleBoxModel("Skype"),
            new MiddleBoxModel("Facebook")
    };

    static String testMessage;

    static {
        //test message creation
        try {
            testMessage = new ObjectMapper().writeValueAsString(new SettingsModel(new ArrayList<>(Arrays.asList(boxes))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BottomNavigationView bottomNavigationView;
    private SettingsModel config;
    private Map<Integer, EasyCallable<String>> editTextConfig;

    private AppListArrayAdapter appListAdapter;
    private List<View> settingsTab = new ArrayList<>();
    private List<View> appListTab = new ArrayList<>();

    private MqqtClientWrapper settingsReceiver;
    private MqqtClientWrapper metadataPublisher;
    private String rec_clientId, pub_clientId;
    private String rec_serverUri, pub_serverUri;
    private String rec_subscriptionTopic, pub_subscriptionTopic;

    public void setRec_clientId(String rec_clientId) {
        this.rec_clientId = rec_clientId;
    }

    public void setPub_clientId(String pub_clientId) {
        this.pub_clientId = pub_clientId;
    }

    public void setRec_serverUri(String rec_serverUri) {
        this.rec_serverUri = rec_serverUri;
    }

    public void setPub_serverUri(String pub_serverUri) {
        this.pub_serverUri = pub_serverUri;
    }

    public void setRec_subscriptionTopic(String rec_subscriptionTopic) {
        this.rec_subscriptionTopic = rec_subscriptionTopic;
    }

    public void setPub_subscriptionTopic(String pub_subscriptionTopic) {
        this.pub_subscriptionTopic = pub_subscriptionTopic;
    }

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

    private void connectEditText2Field(int viewID, EasyCallable<String> setValue) {
        EditText editText = findViewById(viewID);
        setValue.accept(editText.getText().toString());
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    setValue.accept(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
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

        config = new SettingsModel(new ArrayList<>());
        appListAdapter = new AppListArrayAdapter(this, R.layout.app_list_item, config.getMiddleBoxes());
        appListAdapter.notifyDataSetChanged();
        appListView.setAdapter(appListAdapter);

        handleIconsColor();
        bottomNavigationView.setOnNavigationItemSelectedListener(
                item -> {
                    item.setChecked(true);
                    handleIconsColor();
                    renderActiveTab(item.getItemId());
                    return false;
                }
        );

        //MQQT init
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET}, 1);
        }

        //connection of fields to EditViews
        editTextConfig = new HashMap<>();
        editTextConfig.put(R.id.serverUriInput, this::setRec_serverUri);
        editTextConfig.put(R.id.subscriptionTopicInput, this::setRec_subscriptionTopic);
        editTextConfig.put(R.id.clientIdInput, this::setRec_clientId);
        editTextConfig.put(R.id.pub_serverUriInput, this::setPub_serverUri);
        editTextConfig.put(R.id.pub_subscriptionTopicInput, this::setPub_subscriptionTopic);
        editTextConfig.put(R.id.pub_clientIdInput, this::setPub_clientId);

        for (Map.Entry e : editTextConfig.entrySet()){
            connectEditText2Field((int)e.getKey(), (EasyCallable) e.getValue());
        }

        settingsReceiver = new MqqtClientWrapper(this, rec_serverUri, rec_subscriptionTopic, rec_clientId, this::T, this::handleIncomingMessage);
        settingsReceiver.connect();

        metadataPublisher = new MqqtClientWrapper(this, rec_serverUri, pub_subscriptionTopic, pub_clientId, this::T, (s) -> {/*incoming messages on this topic are ignored*/});
        metadataPublisher.connect();

        //Receiver settings buttons
        (findViewById(R.id.reconnect_button)).setOnClickListener(v -> {
            settingsReceiver.setClientID(rec_clientId)
                    .setServerUri(rec_serverUri)
                    .setSubscriptionTopic(rec_subscriptionTopic);

            settingsReceiver.connect();

            metadataPublisher.setClientID(pub_clientId)
                    .setServerUri(pub_serverUri)
                    .setSubscriptionTopic(pub_subscriptionTopic);

            metadataPublisher.connect();
        });

        (findViewById(R.id.test_button)).setOnClickListener(v -> settingsReceiver.publishMessage(testMessage));
    }
}
