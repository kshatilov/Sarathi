package me.shatilov.symlab.sarathi.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import android.view.ViewGroup;
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
import me.shatilov.utility.EasyCallable;
import me.shatilov.utility.android.TabsWrapper;


public class SarathiMainActivity extends AppCompatActivity {

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

    private SettingsModel config;
    private Map<Integer, EasyCallable<String>> editTextConfig;

    private AppListArrayAdapter appListAdapter;
    private List<View> settingsTab = new ArrayList<>();
    private List<View> appListTab = new ArrayList<>();

    private MqqtClientWrapper settingsReceiver;
    private MqqtClientWrapper metadataPublisher;

    private void T(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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
        View observerView = findViewById(R.id.observer);
        settingsTab.add(settingsView);
        appListTab.add(appListView);

        List<TabsWrapper.Tab> tabs = new ArrayList<>();
        Resources resources = getResources();
        tabs.add(new TabsWrapper.Tab(appListView, resources.getDrawable(R.mipmap.list, null), resources.getString(R.string.app_list)));
        tabs.add(new TabsWrapper.Tab(observerView, resources.getDrawable(R.mipmap.observe, null), resources.getString(R.string.observer)));
        tabs.add(new TabsWrapper.Tab(settingsView, resources.getDrawable(R.mipmap.settings, null), resources.getString(R.string.settings)));

        ViewGroup parent = findViewById(R.id.MainActivityLayout);
        TabsWrapper tabsWrapper = new TabsWrapper(tabs, this);
        parent.addView(tabsWrapper.getMenu());

        config = new SettingsModel(new ArrayList<>());
        appListAdapter = new AppListArrayAdapter(this, R.layout.app_list_item, config.getMiddleBoxes());
        appListAdapter.notifyDataSetChanged();
        appListView.setAdapter(appListAdapter);

        //MQQT init
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET}, 1);
        }
        settingsReceiver = new MqqtClientWrapper(this, null, null, null, this::T, this::handleIncomingMessage);
        metadataPublisher = new MqqtClientWrapper(this, null, null, null, this::T, (s) -> {/*incoming messages on this topic are ignored*/});

        //connection of fields to EditViews
        editTextConfig = new HashMap<>();
        editTextConfig.put(R.id.serverUriInput, settingsReceiver::setServerUri);
        editTextConfig.put(R.id.subscriptionTopicInput, settingsReceiver::setSubscriptionTopic);
        editTextConfig.put(R.id.clientIdInput, settingsReceiver::setClientID);
        editTextConfig.put(R.id.pub_serverUriInput, metadataPublisher::setServerUri);
        editTextConfig.put(R.id.pub_subscriptionTopicInput, metadataPublisher::setSubscriptionTopic);
        editTextConfig.put(R.id.pub_clientIdInput, metadataPublisher::setClientID);

        for (Map.Entry e : editTextConfig.entrySet()){
            connectEditText2Field((int)e.getKey(), (EasyCallable<String>) e.getValue());
        }

        settingsReceiver.connect();
        metadataPublisher.connect();

        //Receiver settings buttons
        (findViewById(R.id.reconnect_button)).setOnClickListener(v -> {
            settingsReceiver.connect();
            metadataPublisher.connect();
        });

        (findViewById(R.id.test_button)).setOnClickListener(v -> settingsReceiver.publishMessage(testMessage));
    }
}
